package com.nilson.appsportmate.features.user.ui.deportesdisponibles;

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

public class DeportesDisponiblesViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final MutableLiveData<DeportesDisponiblesUiState> _uiState =
            new MutableLiveData<>(DeportesDisponiblesUiState.loading());
    public final LiveData<DeportesDisponiblesUiState> uiState = _uiState;

    private @Nullable String ayuntamientoId;
    private @Nullable String uid;
    private @Nullable String alias;

    private final List<Map<String, Object>> cacheDisponibles = new ArrayList<>();
    private final List<Map<String, Object>> cacheMis = new ArrayList<>();

    public void init(String ayuntamientoId, String uid, String alias) {
        this.ayuntamientoId = emptyToNull(ayuntamientoId);
        this.uid = emptyToNull(uid);
        this.alias = emptyToNull(alias);
    }

    public void ensureAyuntamientoId(@Nullable String nuevoId) {
        nuevoId = emptyToNull(nuevoId);
        if ((nuevoId == null && ayuntamientoId != null) ||
                (nuevoId != null && !nuevoId.equals(ayuntamientoId))) {
            this.ayuntamientoId = nuevoId;
            loadAll();
        }
    }

    public void loadAll() {
        _uiState.setValue(DeportesDisponiblesUiState.loading());
        Tasks.whenAll(
                Tasks.call(() -> { cargarDisponiblesInternal(); return null; }),
                Tasks.call(() -> { cargarMisInternal(); return null; })
        ).addOnSuccessListener(v -> {
            _uiState.setValue(DeportesDisponiblesUiState.success(
                    new ArrayList<>(cacheDisponibles),
                    new ArrayList<>(cacheMis)
            ));
        }).addOnFailureListener(e -> {
            _uiState.setValue(DeportesDisponiblesUiState.message(
                    DeportesDisponiblesUiState.success(new ArrayList<>(cacheDisponibles), new ArrayList<>(cacheMis)),
                    "Error cargando datos: " + (e != null ? e.getMessage() : "")
            ));
        });
    }

    private void cargarDisponiblesInternal() throws Exception {
        cacheDisponibles.clear();
        if (ayuntamientoId == null) return;

        List<DocumentSnapshot> docs = Tasks.await(
                db.collection("deportes_ayuntamiento")
                        .document(ayuntamientoId)
                        .collection("lista")
                        .get(Source.SERVER)
        ).getDocuments();

        for (DocumentSnapshot d : docs) {
            Map<String, Object> m = d.getData();
            if (m == null) continue;
            m = new HashMap<>(m);
            m.put("idDoc", d.getId());
            cacheDisponibles.add(m);
        }
    }

    private void cargarMisInternal() throws Exception {
        cacheMis.clear();
        if (uid == null) return;

        List<DocumentSnapshot> snaps = Tasks.await(
                db.collection("usuarios")
                        .document(uid)
                        .collection("inscripciones")
                        .get(Source.SERVER)
        ).getDocuments();

        if (snaps.isEmpty()) return;

        List<Map<String, Object>> tmp = new ArrayList<>();
        WriteBatch batchDelete = db.batch();

        for (DocumentSnapshot d : snaps) {
            Map<String, Object> m = d.getData();
            if (m == null) continue;

            String idDoc = d.getId();
            String aytoIdInscripcion = valueOf(m.get("ayuntamientoId"));
            if (aytoIdInscripcion == null || aytoIdInscripcion.isEmpty()) {
                aytoIdInscripcion = ayuntamientoId;
            }

            m = new HashMap<>(m);
            m.put("idDoc", idDoc);

            DocumentSnapshot evSnap = Tasks.await(
                    db.collection("deportes_ayuntamiento")
                            .document(aytoIdInscripcion)
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

    public void apuntarse(Map<String, Object> deporte) {
        if (uid == null || ayuntamientoId == null || alias == null) {
            postMessage("Inicia sesión para inscribirte.");
            return;
        }
        setActionInProgress(true);

        String docId = valueOf(deporte.get("idDoc"));
        DocumentReference refDeporte = db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .collection("lista")
                .document(docId);

        DocumentReference refInscrito = refDeporte.collection("inscritos").document(uid);
        DocumentReference refUser = db.collection("usuarios")
                .document(uid)
                .collection("inscripciones").document(docId);

        db.runTransaction(tx -> {
            DocumentSnapshot snapDeporte = tx.get(refDeporte);
            Long plazas = snapDeporte.getLong("plazasDisponibles");
            if (plazas == null) plazas = 0L;
            if (plazas <= 0) throw new IllegalStateException("NO_PLAZAS");

            DocumentSnapshot snapInscrito = tx.get(refInscrito);
            if (snapInscrito.exists()) throw new IllegalStateException("YA_INSCRITO");

            tx.update(refDeporte, "plazasDisponibles", plazas - 1);

            Map<String, Object> ins = new HashMap<>();
            ins.put("uid", uid);
            ins.put("alias", alias);
            ins.put("ts", System.currentTimeMillis());
            tx.set(refInscrito, ins);

            Map<String, Object> copia = new HashMap<>(deporte);
            copia.put("idDoc", docId);
            copia.put("ayuntamientoId", ayuntamientoId);
            tx.set(refUser, copia);

            return null;
        }).addOnSuccessListener(unused -> {
            postMessage("Inscripción realizada");
            reloadAfterAction();
        }).addOnFailureListener(e -> {
            String code = e != null && e.getMessage() != null ? e.getMessage() : "";
            if (code.contains("YA_INSCRITO")) {
                postMessage("Solo puedes apuntarte una vez a esta actividad.");
            } else if (code.contains("NO_PLAZAS")) {
                postMessage("No hay plazas disponibles.");
            } else {
                postMessage("No se pudo inscribir: " + code);
            }
            setActionInProgress(false);
        });
    }

    public void desapuntarse(Map<String, Object> deporte) {
        if (uid == null || ayuntamientoId == null) {
            postMessage("Inicia sesión para continuar.");
            return;
        }
        setActionInProgress(true);

        String docId = valueOf(deporte.get("idDoc"));
        DocumentReference refDeporte = db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .collection("lista")
                .document(docId);

        DocumentReference refInscrito = refDeporte.collection("inscritos").document(uid);
        DocumentReference refUser = db.collection("usuarios")
                .document(uid)
                .collection("inscripciones").document(docId);

        db.runTransaction(tx -> {
            DocumentSnapshot snapDep = tx.get(refDeporte);
            Long plazas = snapDep.getLong("plazasDisponibles");
            if (plazas == null) plazas = 0L;

            DocumentSnapshot snapIns = tx.get(refInscrito);
            if (!snapIns.exists()) throw new IllegalStateException("NO_ESTABA_INSCRITO");

            tx.update(refDeporte, "plazasDisponibles", plazas + 1);
            tx.delete(refInscrito);
            tx.delete(refUser);

            return null;
        }).addOnSuccessListener(unused -> {
            postMessage("Te has desapuntado");
            reloadAfterAction();
        }).addOnFailureListener(e -> {
            String code = e != null && e.getMessage() != null ? e.getMessage() : "";
            if (code.contains("NO_ESTABA_INSCRITO")) {
                postMessage("No estabas inscrito en esta actividad.");
            } else {
                postMessage("Error al desapuntarte: " + code);
            }
            setActionInProgress(false);
        });
    }

    private void reloadAfterAction() {
        Tasks.whenAll(
                Tasks.call(() -> { cargarDisponiblesInternal(); return null; }),
                Tasks.call(() -> { cargarMisInternal(); return null; })
        ).addOnSuccessListener(v -> {
            _uiState.setValue(DeportesDisponiblesUiState.success(
                    new ArrayList<>(cacheDisponibles),
                    new ArrayList<>(cacheMis)
            ));
            setActionInProgress(false);
        }).addOnFailureListener(e -> {
            _uiState.setValue(DeportesDisponiblesUiState.message(
                    DeportesDisponiblesUiState.success(new ArrayList<>(cacheDisponibles), new ArrayList<>(cacheMis)),
                    "Error recargando: " + (e != null ? e.getMessage() : "")
            ));
            setActionInProgress(false);
        });
    }

    private void setActionInProgress(boolean inProgress) {
        DeportesDisponiblesUiState prev = _uiState.getValue();
        if (prev == null) prev = DeportesDisponiblesUiState.loading();
        _uiState.setValue(DeportesDisponiblesUiState.withAction(prev, inProgress));
    }

    private void postMessage(String msg) {
        DeportesDisponiblesUiState prev = _uiState.getValue();
        if (prev == null) prev = DeportesDisponiblesUiState.loading();
        _uiState.setValue(DeportesDisponiblesUiState.message(prev, msg));
    }

    public void consumeMessage() {
        DeportesDisponiblesUiState prev = _uiState.getValue();
        if (prev != null && prev.message != null) {
            _uiState.setValue(prev.clearMessage());
        }
    }

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
