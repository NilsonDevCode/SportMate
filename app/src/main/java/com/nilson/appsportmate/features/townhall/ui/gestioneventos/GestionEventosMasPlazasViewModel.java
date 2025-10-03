package com.nilson.appsportmate.features.townhall.ui.gestioneventos;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** ViewModel con CRUD sobre eventos del ayuntamiento. */
public class GestionEventosMasPlazasViewModel extends ViewModel {

    public static class UiState {
        public final boolean loading;
        public final List<Map<String, Object>> eventos;
        public final String error;

        public UiState(boolean loading, List<Map<String, Object>> eventos, String error) {
            this.loading = loading;
            this.eventos = eventos;
            this.error = error;
        }

        public static UiState loading() { return new UiState(true, new ArrayList<>(), null); }
        public static UiState success(List<Map<String, Object>> eventos) { return new UiState(false, eventos, null); }
        public static UiState error(String msg) { return new UiState(false, new ArrayList<>(), msg); }
    }

    private final MutableLiveData<UiState> _ui = new MutableLiveData<>(UiState.loading());
    public LiveData<UiState> uiState = _ui;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Carga todos los eventos y, para cada uno, calcula número de inscritos. */
    public void fetchEventos(@NonNull String ayuntamientoId) {
        if (ayuntamientoId.isEmpty()) {
            _ui.setValue(UiState.error("Falta ayuntamientoId"));
            return;
        }
        _ui.setValue(UiState.loading());

        db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .collection("lista")
                .get()
                .addOnSuccessListener(snap -> {
                    List<Map<String, Object>> base = new ArrayList<>();
                    List<com.google.android.gms.tasks.Task<QuerySnapshot>> pending = new ArrayList<>();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Map<String, Object> ev = d.getData();
                        if (ev == null) continue;
                        ev = new HashMap<>(ev);
                        ev.put("idDoc", d.getId());
                        base.add(ev);

                        // tarea para contar inscritos de este evento
                        com.google.android.gms.tasks.Task<QuerySnapshot> t =
                                d.getReference().collection("inscritos").get();
                        Map<String, Object> finalEv = ev;
                        pending.add(t.continueWith(task -> {
                            int count = 0;
                            if (task.isSuccessful() && task.getResult() != null) {
                                count = task.getResult().size();
                            }
                            finalEv.put("inscritosCount", count);
                            return null;
                        }));
                    }

                    Tasks.whenAllComplete(pending).addOnCompleteListener(done ->
                            _ui.setValue(UiState.success(base))
                    );
                })
                .addOnFailureListener(e -> _ui.setValue(UiState.error("Error cargando: " + e.getMessage())));
    }

    /** +1 / -1 plazas con transacción de servidor. */
    public void updatePlazas(@NonNull String aytoId, @NonNull String docId, int delta) {
        if (aytoId.isEmpty() || docId.isEmpty()) return;
        DocumentReference ref = db.collection("deportes_ayuntamiento")
                .document(aytoId).collection("lista").document(docId);

        db.runTransaction(tx -> {
                    DocumentSnapshot d = tx.get(ref);
                    Long plazas = d.getLong("plazasDisponibles");
                    if (plazas == null) plazas = 0L;
                    long nuevo = plazas + delta;
                    if (nuevo < 0) nuevo = 0;
                    tx.update(ref, "plazasDisponibles", nuevo);
                    return null;
                }).addOnSuccessListener(v -> fetchEventos(aytoId))
                .addOnFailureListener(e -> _ui.setValue(UiState.error("No se pudo actualizar: " + e.getMessage())));
    }

    /** Borra el evento y sus subdocumentos 'inscritos'. */
    public void deleteEvento(@NonNull String aytoId, @NonNull String docId) {
        if (aytoId.isEmpty() || docId.isEmpty()) return;
        DocumentReference ref = db.collection("deportes_ayuntamiento")
                .document(aytoId).collection("lista").document(docId);

        // Borramos subcolección inscritos (si existe) y luego el evento
        ref.collection("inscritos").get().addOnSuccessListener(snap -> {
            List<com.google.android.gms.tasks.Task<Void>> deletions = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                deletions.add(d.getReference().delete());
            }
            Tasks.whenAll(deletions).addOnCompleteListener(x ->
                    ref.delete()
                            .addOnSuccessListener(v -> fetchEventos(aytoId))
                            .addOnFailureListener(e -> _ui.setValue(UiState.error("No se pudo borrar: " + e.getMessage())))
            );
        }).addOnFailureListener(e -> _ui.setValue(UiState.error("No se pudo borrar inscritos: " + e.getMessage())));
    }

    /** Actualiza campos del evento (merge). */
    public void updateEvento(@NonNull String aytoId, @NonNull String docId, @NonNull Map<String, Object> fields) {
        if (aytoId.isEmpty() || docId.isEmpty()) return;
        db.collection("deportes_ayuntamiento")
                .document(aytoId).collection("lista").document(docId)
                .update(fields)
                .addOnSuccessListener(v -> fetchEventos(aytoId))
                .addOnFailureListener(e -> _ui.setValue(UiState.error("No se pudo editar: " + e.getMessage())));
    }

    /** Obtiene alias (o info básica) de inscritos para mostrar en diálogo. */
    public void fetchInscritos(@NonNull String aytoId, @NonNull String docId,
                               @NonNull InscritosCallback cb) {
        if (aytoId.isEmpty() || docId.isEmpty()) {
            cb.onResult(new ArrayList<>(), "Faltan IDs");
            return;
        }
        db.collection("deportes_ayuntamiento")
                .document(aytoId).collection("lista").document(docId)
                .collection("inscritos")
                .get()
                .addOnSuccessListener(snap -> {
                    List<String> lista = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String alias = asString(d.get("alias"));
                        if (alias.isEmpty()) alias = d.getId();
                        lista.add(alias);
                    }
                    cb.onResult(lista, null);
                })
                .addOnFailureListener(e -> cb.onResult(new ArrayList<>(), e.getMessage()));
    }

    public interface InscritosCallback {
        void onResult(List<String> aliases, String error);
    }

    private static String asString(Object o) {
        if (o == null) return "";
        String s = String.valueOf(o);
        return "null".equalsIgnoreCase(s) ? "" : s;
    }
}
