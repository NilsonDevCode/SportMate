package com.nilson.appsportmate.common.datos.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio Firestore:
 * - Listar/escuchar eventos (tiempo real)
 * - Escuchar inscritos (tiempo real)
 * - Incrementar/decrementar plazas (transacciones)
 * - Actualizar, borrar, migrar (con copia de inscritos)
 * - Expulsar inscrito (transacción)
 */
public class FirestoreTransacciones {

    /* ====== Callbacks ====== */
    public interface SimpleResult {
        void onSuccess();
        void onError(@NonNull Exception e);
    }

    public interface EventsResult {
        void onSuccess(@NonNull List<Map<String, Object>> eventos);
        void onError(@NonNull Exception e);
    }

    public interface EventsListener {
        void onChange(@NonNull List<Map<String, Object>> eventos);
        void onError(@NonNull Exception e);
    }

    public interface InscritosListener {
        void onChange(@NonNull List<String> aliases, @NonNull List<String> uids);
        void onError(@NonNull Exception e);
    }

    /* ====== Campos ====== */
    private final FirebaseFirestore db;
    private final String ayuntamientoId;

    public FirestoreTransacciones(@NonNull FirebaseFirestore db,
                                  @NonNull String ayuntamientoId) {
        this.db = db;
        this.ayuntamientoId = ayuntamientoId;
    }

    public FirebaseFirestore getDb() { return db; }

    /* ====== Refs ====== */
    public CollectionReference eventosRef() {
        return db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .collection("lista");
    }

    public CollectionReference inscritosRef(@NonNull String idDoc) {
        return eventosRef().document(idDoc).collection("inscritos");
    }

    /* ====== Listado / Tiempo real ====== */

    /** One-shot. */
    public void listarEventos(@NonNull EventsResult cb) {
        eventosRef().get()
                .addOnSuccessListener((QuerySnapshot query) -> {
                    List<Map<String, Object>> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        Map<String, Object> ev = doc.getData();
                        if (ev == null) continue;
                        ev.put("idDoc", doc.getId());
                        lista.add(ev);
                    }
                    cb.onSuccess(lista);
                })
                .addOnFailureListener(cb::onError);
    }

    /** Tiempo real para eventos. */
    public ListenerRegistration escucharEventos(@NonNull EventsListener listener) {
        return eventosRef().addSnapshotListener((snap, e) -> {
            if (e != null) { listener.onError(e); return; }
            if (snap == null) { listener.onChange(Collections.emptyList()); return; }
            List<Map<String, Object>> out = new ArrayList<>();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Map<String, Object> m = doc.getData();
                if (m == null) continue;
                m = new HashMap<>(m);
                m.put("idDoc", doc.getId());
                out.add(m);
            }
            listener.onChange(out);
        });
    }

    /** Tiempo real para inscritos de un evento. */
    public ListenerRegistration escucharInscritos(@NonNull String idDoc,
                                                  @NonNull InscritosListener listener) {
        return inscritosRef(idDoc).addSnapshotListener((snap, e) -> {
            if (e != null) { listener.onError(e); return; }
            if (snap == null) { listener.onChange(new ArrayList<>(), new ArrayList<>()); return; }
            List<DocumentSnapshot> docs = snap.getDocuments();
            List<String> aliases = new ArrayList<>(docs.size());
            List<String> uids    = new ArrayList<>(docs.size());
            for (DocumentSnapshot d : docs) {
                String alias = String.valueOf(d.get("alias"));
                if (alias == null || "null".equals(alias) || alias.trim().isEmpty()) alias = "(sin alias)";
                aliases.add(alias);
                uids.add(d.getId());
            }
            listener.onChange(aliases, uids);
        });
    }

    /* ====== Plazas (+/-) ====== */

    public void incrementarPlazas(@NonNull String idDoc, @NonNull SimpleResult cb) {
        DocumentReference ref = eventosRef().document(idDoc);
        db.runTransaction(trx -> {
                    DocumentSnapshot snap = trx.get(ref);
                    if (!snap.exists()) throw new RuntimeException("No existe el evento");
                    trx.update(ref, "plazasDisponibles", FieldValue.increment(1));
                    return null;
                })
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void decrementarPlazas(@NonNull String idDoc, @NonNull SimpleResult cb) {
        DocumentReference ref = eventosRef().document(idDoc);
        db.runTransaction(trx -> {
                    DocumentSnapshot snap = trx.get(ref);
                    if (!snap.exists()) throw new RuntimeException("No existe el evento");
                    Long plazas = snap.getLong("plazasDisponibles");
                    long cur = plazas == null ? 0 : plazas;
                    if (cur <= 0) throw new RuntimeException("Plazas ya en 0");
                    trx.update(ref, "plazasDisponibles", cur - 1);
                    return null;
                })
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    /* ====== Borrar / Actualizar / Migración ====== */

    public void borrarEvento(@NonNull String idDoc, @NonNull SimpleResult cb) {
        eventosRef().document(idDoc).delete()
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void actualizarEventoCampos(@NonNull String idDoc,
                                       @NonNull Map<String, Object> nuevos,
                                       @NonNull SimpleResult cb) {
        eventosRef().document(idDoc).set(nuevos)
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void crearEventoConMigracion(@NonNull String oldId,
                                        @NonNull String newId,
                                        @NonNull Map<String, Object> nuevos,
                                        @NonNull SimpleResult cb) {
        DocumentReference oldRef = eventosRef().document(oldId);
        DocumentReference newRef = eventosRef().document(newId);

        newRef.set(nuevos)
                .addOnSuccessListener(unused -> copiarInscritos(oldRef, newRef, new SimpleResult() {
                    @Override public void onSuccess() {
                        oldRef.delete()
                                .addOnSuccessListener(u -> cb.onSuccess())
                                .addOnFailureListener(cb::onError);
                    }
                    @Override public void onError(@NonNull Exception e) { cb.onError(e); }
                }))
                .addOnFailureListener(cb::onError);
    }

    public void copiarInscritos(@NonNull DocumentReference oldRef,
                                @NonNull DocumentReference newRef,
                                @NonNull SimpleResult cb) {
        oldRef.collection("inscritos").get()
                .addOnSuccessListener((QuerySnapshot snap) -> {
                    if (snap.isEmpty()) { cb.onSuccess(); return; }
                    db.runBatch(batch -> {
                                for (QueryDocumentSnapshot d : snap) {
                                    Map<String, Object> data = d.getData();
                                    batch.set(newRef.collection("inscritos").document(d.getId()), data);
                                }
                            })
                            .addOnSuccessListener(unused -> cb.onSuccess())
                            .addOnFailureListener(cb::onError);
                })
                .addOnFailureListener(cb::onError);
    }

    /* ====== Expulsar inscrito ====== */

    public void expulsarInscrito(String idDoc, String uid, SimpleResult cb) {
        final DocumentReference refEvento = eventosRef().document(idDoc);
        final DocumentReference refInscrito = refEvento.collection("inscritos").document(uid);
        final DocumentReference refEspejoUsuario = db.collection("usuarios")
                .document(uid)
                .collection("inscripciones")
                .document(idDoc);

        db.runTransaction(tx -> {
                    DocumentSnapshot snapEvento   = tx.get(refEvento);
                    DocumentSnapshot snapInscrito = tx.get(refInscrito);

                    if (!snapInscrito.exists()) {
                        throw new IllegalStateException("El usuario no está inscrito.");
                    }

                    Long plazas = snapEvento.getLong("plazasDisponibles");
                    if (plazas == null) plazas = 0L;

                    tx.update(refEvento, "plazasDisponibles", plazas + 1);
                    tx.delete(refInscrito);
                    tx.delete(refEspejoUsuario);

                    return null;
                })
                .addOnSuccessListener(unused -> { if (cb != null) cb.onSuccess(); })
                .addOnFailureListener(e -> { if (cb != null) cb.onError(e); });
    }
}


