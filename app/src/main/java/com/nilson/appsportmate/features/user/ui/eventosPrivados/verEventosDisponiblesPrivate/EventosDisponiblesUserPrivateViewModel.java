package com.nilson.appsportmate.features.user.ui.eventosPrivados.verEventosDisponiblesPrivate;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel para eventos privados disponibles para el usuario particular:
 * - Lista TODOS los eventos privados creados por cualquier usuario
 * - Gestiona inscripciones / desinscripciones
 * - Mantiene caché y UiState
 */
public class EventosDisponiblesUserPrivateViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final MutableLiveData<EventosDisponiblesUserPrivateUiState> _uiState =
            new MutableLiveData<>(EventosDisponiblesUserPrivateUiState.loading());
    public final LiveData<EventosDisponiblesUserPrivateUiState> uiState = _uiState;


    private @Nullable String uid;
    private @Nullable String alias;
    private @Nullable String localidad;

    private final List<Map<String, Object>> cacheDisponibles = new ArrayList<>();
    private final List<Map<String, Object>> cacheMis = new ArrayList<>();

    // -------------------------------------------------------------------------
    // INIT
    // -------------------------------------------------------------------------
    public void init(String uid, String alias, String localidad) {
        this.uid = emptyToNull(uid);
        this.alias = emptyToNull(alias);
        this.localidad = emptyToNull(localidad);
    }

    // -------------------------------------------------------------------------
    // CARGA GENERAL
    // -------------------------------------------------------------------------
    public void loadAll() {
        _uiState.setValue(EventosDisponiblesUserPrivateUiState.loading());


        Tasks.whenAll(
                Tasks.call(() -> { cargarDisponiblesInternal(); return null; }),
                Tasks.call(() -> { cargarMisInternal(); return null; })
        ).addOnSuccessListener(v -> {
            _uiState.setValue(EventosDisponiblesUserPrivateUiState.success(
                    new ArrayList<>(cacheDisponibles),
                    new ArrayList<>(cacheMis)
            ));
        }).addOnFailureListener(e -> {
            _uiState.setValue(EventosDisponiblesUserPrivateUiState.message(
                    EventosDisponiblesUserPrivateUiState.success(new ArrayList<>(cacheDisponibles),
                            new ArrayList<>(cacheMis)),
                    "Error cargando datos: " + (e != null ? e.getMessage() : "")
            ));
        });
    }

    // -------------------------------------------------------------------------
    // CARGAR TODOS LOS EVENTOS PRIVADOS DISPONIBLES
    // -------------------------------------------------------------------------
    private void cargarDisponiblesInternal() throws Exception {
        cacheDisponibles.clear();

        List<DocumentSnapshot> owners =
                Tasks.await(db.collection("eventos_user_private").get(Source.SERVER))
                        .getDocuments();

        for (DocumentSnapshot ownerDoc : owners) {

            String ownerId = ownerDoc.getId();

            List<DocumentSnapshot> docs = Tasks.await(
                    db.collection("eventos_user_private")
                            .document(ownerId)
                            .collection("lista")
                            .get(Source.SERVER)
            ).getDocuments();

            for (DocumentSnapshot d : docs) {
                Map<String, Object> m = d.getData();
                if (m == null) continue;

                m = new HashMap<>(m);
                m.put("idDoc", d.getId());
                m.put("ownerId", ownerId);

                cacheDisponibles.add(m);
            }
        }
    }

    // -------------------------------------------------------------------------
    // CARGAR MIS INSCRIPCIONES PRIVADAS
    // -------------------------------------------------------------------------
    private void cargarMisInternal() throws Exception {
        cacheMis.clear();
        if (uid == null) return;

        List<DocumentSnapshot> snaps = Tasks.await(
                db.collection("usuarios")
                        .document(uid)
                        .collection("inscripciones_privadas")
                        .get(Source.SERVER)
        ).getDocuments();

        if (snaps.isEmpty()) return;

        List<Map<String, Object>> tmp = new ArrayList<>();
        WriteBatch batchDelete = db.batch();

        for (DocumentSnapshot d : snaps) {

            Map<String, Object> m = d.getData();
            if (m == null) continue;

            String idDoc = d.getId();
            String ownerId = valueOf(m.get("ownerId"));

            if (ownerId == null || ownerId.isEmpty()) {
                ownerId = uid;
            }

            m = new HashMap<>(m);
            m.put("idDoc", idDoc);

            DocumentSnapshot evSnap = Tasks.await(
                    db.collection("eventos_user_private")
                            .document(ownerId)
                            .collection("lista")
                            .document(idDoc)
                            .get(Source.SERVER)
            );

            if (evSnap.exists()) {
                tmp.add(m);
            } else {
                batchDelete.delete(d.getReference());
            }
        }

        Tasks.await(batchDelete.commit());

        cacheMis.clear();
        cacheMis.addAll(tmp);
    }

    // -------------------------------------------------------------------------
    // APUNTARSE
    // -------------------------------------------------------------------------
    public void apuntarse(Map<String, Object> evento) {

        if (uid == null || alias == null) {
            postMessage("Inicia sesión para inscribirte.");
            return;
        }
        setAction(true);

        String docId = valueOf(evento.get("idDoc"));
        String ownerId = valueOf(evento.get("ownerId"));

        DocumentReference refEvento = db.collection("eventos_user_private")
                .document(ownerId)
                .collection("lista")
                .document(docId);

        DocumentReference refInscrito = refEvento
                .collection("inscritos_privados")
                .document(uid);

        DocumentReference refUser = db.collection("usuarios")
                .document(uid)
                .collection("inscripciones_privadas")
                .document(docId);

        db.runTransaction(tx -> {

            DocumentSnapshot snap = tx.get(refEvento);
            Long plazas = snap.getLong("plazasDisponibles");
            if (plazas == null) plazas = 0L;
            if (plazas <= 0) throw new IllegalStateException("NO_PLAZAS");

            if (tx.get(refInscrito).exists())
                throw new IllegalStateException("YA_INSCRITO");

            tx.update(refEvento, "plazasDisponibles", plazas - 1);

            Map<String, Object> ins = new HashMap<>();
            ins.put("uid", uid);
            ins.put("alias", alias);
            ins.put("ts", System.currentTimeMillis());
            tx.set(refInscrito, ins);

            Map<String, Object> copia = new HashMap<>(evento);
            copia.put("idDoc", docId);
            copia.put("ownerId", ownerId);
            tx.set(refUser, copia);

            return null;

        }).addOnSuccessListener(unused -> {
            postMessage("Inscripción realizada");
            reload();
        }).addOnFailureListener(e -> {
            String code = e.getMessage() != null ? e.getMessage() : "";
            if (code.contains("YA_INSCRITO"))
                postMessage("Ya estás inscrito.");
            else if (code.contains("NO_PLAZAS"))
                postMessage("No hay plazas disponibles.");
            else
                postMessage("Error: " + code);

            setAction(false);
        });
    }

    // -------------------------------------------------------------------------
    // DESAPUNTARSE
    // -------------------------------------------------------------------------
    public void desapuntarse(Map<String, Object> evento) {

        if (uid == null) {
            postMessage("Inicia sesión para continuar.");
            return;
        }
        setAction(true);

        String docId = valueOf(evento.get("idDoc"));
        String ownerId = valueOf(evento.get("ownerId"));

        DocumentReference refEvento = db.collection("eventos_user_private")
                .document(ownerId)
                .collection("lista")
                .document(docId);

        DocumentReference refInscrito = refEvento
                .collection("inscritos_privados")
                .document(uid);

        DocumentReference refUser = db.collection("usuarios")
                .document(uid)
                .collection("inscripciones_privadas")
                .document(docId);

        db.runTransaction(tx -> {

            DocumentSnapshot ev = tx.get(refEvento);
            Long plazas = ev.getLong("plazasDisponibles");
            if (plazas == null) plazas = 0L;

            if (!tx.get(refInscrito).exists())
                throw new IllegalStateException("NO_ESTABA_INSCRITO");

            tx.update(refEvento, "plazasDisponibles", plazas + 1);
            tx.delete(refInscrito);
            tx.delete(refUser);

            return null;

        }).addOnSuccessListener(unused -> {
            postMessage("Te has desapuntado");
            reload();
        }).addOnFailureListener(e -> {
            String code = e.getMessage() != null ? e.getMessage() : "";
            if (code.contains("NO_ESTABA_INSCRITO"))
                postMessage("No estabas inscrito.");
            else
                postMessage("Error: " + code);

            setAction(false);
        });
    }

    // -------------------------------------------------------------------------
    // RELOAD
    // -------------------------------------------------------------------------
    private void reload() {
        Tasks.whenAll(
                Tasks.call(() -> { cargarDisponiblesInternal(); return null; }),
                Tasks.call(() -> { cargarMisInternal(); return null; })
        ).addOnSuccessListener(v -> {
            _uiState.setValue(EventosDisponiblesUserPrivateUiState.success(
                    new ArrayList<>(cacheDisponibles),
                    new ArrayList<>(cacheMis)
            ));
            setAction(false);
        }).addOnFailureListener(e -> {
            _uiState.setValue(EventosDisponiblesUserPrivateUiState.message(
                    EventosDisponiblesUserPrivateUiState.success(new ArrayList<>(cacheDisponibles),
                            new ArrayList<>(cacheMis)),
                    "Error recargando: " + (e != null ? e.getMessage() : "")
            ));
            setAction(false);
        });
    }

    // -------------------------------------------------------------------------
    // UiState helpers
    // -------------------------------------------------------------------------
    private void setAction(boolean value) {
        EventosDisponiblesUserPrivateUiState prev = _uiState.getValue();
        if (prev == null) prev = EventosDisponiblesUserPrivateUiState.loading();
        _uiState.setValue(EventosDisponiblesUserPrivateUiState.withAction(prev, value));
    }

    private void postMessage(String msg) {
        EventosDisponiblesUserPrivateUiState prev = _uiState.getValue();
        if (prev == null) prev = EventosDisponiblesUserPrivateUiState.loading();
        _uiState.setValue(EventosDisponiblesUserPrivateUiState.message(prev, msg));
    }

    public void consumeMessage() {
        EventosDisponiblesUserPrivateUiState prev = _uiState.getValue();
        if (prev != null && prev.message != null) {
            _uiState.setValue(prev.clearMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Utils
    // -------------------------------------------------------------------------
    private static @Nullable String emptyToNull(@Nullable String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static @Nullable String valueOf(@Nullable Object o) {
        if (o == null) return null;
        String s = String.valueOf(o);
        return "null".equalsIgnoreCase(s) ? null : s;
    }
}
