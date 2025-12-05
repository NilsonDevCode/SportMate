package com.nilson.appsportmate.common.datos.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class FirestoreTransaccionesPrivate {

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
        void onChange(@NonNull List<String> aliases,
                      @NonNull List<String> uids);
        void onError(@NonNull Exception e);
    }

    private final FirebaseFirestore db;
    private final String ownerId;

    public FirestoreTransaccionesPrivate(@NonNull FirebaseFirestore db,
                                         @NonNull String ownerId) {
        this.db = db;
        this.ownerId = ownerId;
    }

    public CollectionReference eventosRef() {
        return db.collection("eventos_user_private")
                .document(ownerId)
                .collection("lista");
    }

    public CollectionReference inscritosRef(@NonNull String idDoc) {
        return eventosRef()
                .document(idDoc)
                .collection("inscritos_privados");
    }

    /* NECESARIO PARA EL VIEWMODEL */
    public CollectionReference getInscritosRef(String idDoc) {
        return inscritosRef(idDoc);
    }

    public void listarEventos(@NonNull EventsResult cb) {
        eventosRef().get()
                .addOnSuccessListener(q -> {
                    List<Map<String, Object>> lista = new ArrayList<>();
                    for (DocumentSnapshot d : q.getDocuments()) {
                        Map<String, Object> m = d.getData();
                        if (m == null) continue;
                        m.put("idDoc", d.getId());
                        lista.add(m);
                    }
                    cb.onSuccess(lista);
                })
                .addOnFailureListener(cb::onError);
    }

    public ListenerRegistration escucharEventos(@NonNull EventsListener listener) {
        return eventosRef().addSnapshotListener((snap, e) -> {
            if (e != null) {
                listener.onError(e);
                return;
            }
            if (snap == null) {
                listener.onChange(Collections.emptyList());
                return;
            }

            List<Map<String, Object>> out = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                Map<String, Object> m = d.getData();
                if (m == null) continue;
                m = new HashMap<>(m);
                m.put("idDoc", d.getId());
                out.add(m);
            }

            listener.onChange(out);
        });
    }

    public ListenerRegistration escucharInscritos(
            @NonNull String idDoc,
            @NonNull InscritosListener listener) {

        return inscritosRef(idDoc).addSnapshotListener((snap, e) -> {
            if (e != null) {
                listener.onError(e);
                return;
            }

            if (snap == null) {
                listener.onChange(new ArrayList<>(), new ArrayList<>());
                return;
            }

            List<String> aliases = new ArrayList<>();
            List<String> uids = new ArrayList<>();

            for (DocumentSnapshot d : snap.getDocuments()) {
                String alias = String.valueOf(d.get("alias"));
                if (alias == null || alias.trim().isEmpty()) alias = "(sin alias)";
                aliases.add(alias);
                uids.add(d.getId());
            }

            listener.onChange(aliases, uids);
        });
    }

    public void incrementarPlazas(@NonNull String idDoc,
                                  @NonNull SimpleResult cb) {

        DocumentReference ref = eventosRef().document(idDoc);

        db.runTransaction(tx -> {
                    DocumentSnapshot snap = tx.get(ref);
                    if (!snap.exists()) throw new RuntimeException("No existe el evento");

                    tx.update(ref, "plazasDisponibles", FieldValue.increment(1));
                    return null;
                })
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void decrementarPlazas(@NonNull String idDoc,
                                  @NonNull SimpleResult cb) {

        DocumentReference ref = eventosRef().document(idDoc);

        db.runTransaction(tx -> {
                    DocumentSnapshot snap = tx.get(ref);
                    if (!snap.exists()) throw new RuntimeException("No existe el evento");

                    Long plazas = snap.getLong("plazasDisponibles");
                    long cur = plazas == null ? 0 : plazas;

                    if (cur <= 0) throw new RuntimeException("Plazas ya en 0");

                    tx.update(ref, "plazasDisponibles", cur - 1);
                    return null;
                })
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void borrarEvento(@NonNull String idDoc,
                             @NonNull SimpleResult cb) {

        eventosRef().document(idDoc).delete()
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void actualizarEventoCampos(
            @NonNull String idDoc,
            @NonNull Map<String, Object> nuevos,
            @NonNull SimpleResult cb) {

        eventosRef().document(idDoc).set(nuevos)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void crearEventoConMigracion(
            @NonNull String oldId,
            @NonNull String newId,
            @NonNull Map<String, Object> nuevos,
            @NonNull SimpleResult cb) {

        DocumentReference oldRef = eventosRef().document(oldId);
        DocumentReference newRef = eventosRef().document(newId);

        newRef.set(nuevos)
                .addOnSuccessListener(unused ->
                        copiarInscritos(oldRef, newRef, new SimpleResult() {
                            @Override public void onSuccess() {
                                oldRef.delete()
                                        .addOnSuccessListener(v -> cb.onSuccess())
                                        .addOnFailureListener(cb::onError);
                            }

                            @Override public void onError(@NonNull Exception e) {
                                cb.onError(e);
                            }
                        }))
                .addOnFailureListener(cb::onError);
    }

    public void copiarInscritos(
            @NonNull DocumentReference oldRef,
            @NonNull DocumentReference newRef,
            @NonNull SimpleResult cb) {

        oldRef.collection("inscritos_privados").get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        cb.onSuccess();
                        return;
                    }

                    db.runBatch(batch -> {
                                for (QueryDocumentSnapshot d : snap) {
                                    Map<String, Object> data = d.getData();
                                    batch.set(
                                            newRef.collection("inscritos_privados")
                                                    .document(d.getId()),
                                            data
                                    );
                                }
                            })
                            .addOnSuccessListener(v -> cb.onSuccess())
                            .addOnFailureListener(cb::onError);
                })
                .addOnFailureListener(cb::onError);
    }

    public void expulsarInscrito(
            String idDoc,
            String uidUser,
            SimpleResult cb) {

        DocumentReference refEvento = eventosRef().document(idDoc);
        DocumentReference refInscrito = inscritosRef(idDoc).document(uidUser);
        DocumentReference refEspejo = db.collection("usuarios")
                .document(uidUser)
                .collection("inscripciones_privadas")
                .document(idDoc);

        db.runTransaction(tx -> {

                    DocumentSnapshot snapEvento = tx.get(refEvento);
                    DocumentSnapshot snapInscrito = tx.get(refInscrito);

                    if (!snapInscrito.exists()) {
                        throw new IllegalStateException("El usuario no está inscrito.");
                    }

                    Long plazas = snapEvento.getLong("plazasDisponibles");
                    if (plazas == null) plazas = 0L;

                    tx.update(refEvento, "plazasDisponibles", plazas + 1);
                    tx.delete(refInscrito);
                    tx.delete(refEspejo);

                    return null;
                })
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }
}
