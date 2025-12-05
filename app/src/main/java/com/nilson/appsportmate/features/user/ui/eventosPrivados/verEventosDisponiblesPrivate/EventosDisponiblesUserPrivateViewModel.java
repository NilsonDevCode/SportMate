package com.nilson.appsportmate.features.user.ui.eventosPrivados.verEventosDisponiblesPrivate;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventosDisponiblesUserPrivateViewModel extends ViewModel {

    private static final String TAG = "EventosPrivadosVM";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<EventosDisponiblesUserPrivateUiState> _uiState =
            new MutableLiveData<>(EventosDisponiblesUserPrivateUiState.loading());
    public final LiveData<EventosDisponiblesUserPrivateUiState> uiState = _uiState;

    private @Nullable String uid;
    private @Nullable String alias;
    private @Nullable String puebloId;

    // CACHES
    private final List<Map<String, Object>> cacheDisponibles = new ArrayList<>();
    private final List<Map<String, Object>> cacheMis = new ArrayList<>();

    // LISTENERS FIRESTORE
    private ListenerRegistration listenerEventos = null;
    private ListenerRegistration listenerMisInscripciones = null;


    // ==========================================================
    // INIT
    // ==========================================================
    public void init(@Nullable String uid, @Nullable String alias, @Nullable String puebloId) {
        this.uid = emptyToNull(uid);
        this.alias = emptyToNull(alias);
        this.puebloId = emptyToNull(puebloId);

        Log.e(TAG, "INIT → uid=" + this.uid + " alias=" + this.alias + " puebloId=" + this.puebloId);
    }

    // ==========================================================
    // ACTIVAR + DETENER LISTENERS
    // ==========================================================
    public void activarListeners() {
        Log.e(TAG, "🔥 activarListeners()");

        detenerListeners();

        if (puebloId == null) {
            Log.e(TAG, "⛔ No hay puebloId → no escucho nada");
            return;
        }

        // LISTENER DE EVENTOS DISPONIBLES EN ESTE PUEBLO
        listenerEventos =
                db.collection("eventos_privados_por_pueblo")
                        .document(puebloId)
                        .collection("lista")
                        .addSnapshotListener((snap, e) -> {

                            if (e != null) {
                                Log.e(TAG, "❌ Error listener eventos", e);
                                return;
                            }
                            if (snap == null) return;

                            cacheDisponibles.clear();

                            for (DocumentSnapshot d : snap.getDocuments()) {
                                Map<String, Object> m = d.getData();
                                if (m == null) continue;

                                m = new HashMap<>(m);
                                m.put("idDoc", d.getId());
                                m.put("ownerId", m.get("uidCreador"));

                                cacheDisponibles.add(m);
                            }

                            Log.e(TAG, "📡 Listener eventos → " + cacheDisponibles.size());

                            publicarEstado();
                        });

        // LISTENER DE MIS INSCRIPCIONES (FILTRADAS POR ESTE PUEBLO)
        if (uid != null) {

            listenerMisInscripciones =
                    db.collection("usuarios")
                            .document(uid)
                            .collection("inscripciones_privadas")
                            .whereEqualTo("puebloId", puebloId)
                            .addSnapshotListener((snap, e) -> {

                                if (e != null) {
                                    Log.e(TAG, "❌ Error listener mis inscripciones", e);
                                    return;
                                }
                                if (snap == null) return;

                                cacheMis.clear();

                                for (DocumentSnapshot d : snap.getDocuments()) {

                                    Map<String, Object> m = d.getData();
                                    if (m == null) continue;

                                    m = new HashMap<>(m);
                                    m.put("idDoc", d.getId());
                                    m.put("estoyInscrito", true);

                                    cacheMis.add(m);
                                }

                                Log.e(TAG, "📡 Listener MIS → " + cacheMis.size());

                                publicarEstado();
                            });
        }
    }

    public void detenerListeners() {
        if (listenerEventos != null) {
            listenerEventos.remove();
            listenerEventos = null;
        }
        if (listenerMisInscripciones != null) {
            listenerMisInscripciones.remove();
            listenerMisInscripciones = null;
        }
    }

    // ==========================================================
    // PUBLICAR ESTADO
    // ==========================================================
    private void publicarEstado() {

        Log.e(TAG, "📤 publicarEstado() → disponibles=" + cacheDisponibles.size()
                + " mis=" + cacheMis.size());

        _uiState.setValue(
                EventosDisponiblesUserPrivateUiState.success(
                        new ArrayList<>(cacheDisponibles),
                        new ArrayList<>(cacheMis)
                )
        );
    }


    // ==========================================================
    // APUNTARSE
    // ==========================================================
    public void apuntarse(Map<String, Object> evento) {

        Log.e(TAG, "🟢 apuntarse() evento=" + evento);

        if (uid == null || alias == null) {
            postMessage("Inicia sesión para apuntarte.");
            return;
        }

        setActionInProgress(true);

        String eventId = valueOf(evento.get("idDoc"));
        String ownerId = valueOf(evento.get("ownerId"));

        DocumentReference refEvt = db.collection("eventos_user_private")
                .document(ownerId)
                .collection("lista")
                .document(eventId);

        DocumentReference refInscrito = refEvt.collection("inscritos_privados").document(uid);

        DocumentReference refUser = db.collection("usuarios")
                .document(uid)
                .collection("inscripciones_privadas")
                .document(eventId);

        db.runTransaction(tx -> {

            DocumentSnapshot snapEvt = tx.get(refEvt);
            Long plazas = snapEvt.getLong("plazasDisponibles");
            if (plazas == null) plazas = 0L;

            if (plazas <= 0) throw new IllegalStateException("NO_PLAZAS");
            if (tx.get(refInscrito).exists()) throw new IllegalStateException("YA_INSCRITO");

            tx.update(refEvt, "plazasDisponibles", plazas - 1);

            Map<String, Object> ins = new HashMap<>();
            ins.put("uid", uid);
            ins.put("alias", alias);
            ins.put("puebloId", puebloId);
            ins.put("ts", System.currentTimeMillis());

            tx.set(refInscrito, ins);

            Map<String, Object> copia = new HashMap<>(evento);
            copia.put("puebloId", puebloId);
            copia.put("idDoc", eventId);
            copia.put("ownerId", ownerId);

            tx.set(refUser, copia);

            return null;

        }).addOnSuccessListener(v -> {
            postMessage("Inscripción completada");
            setActionInProgress(false);

        }).addOnFailureListener(e -> {

            String msg = (e != null ? e.getMessage() : "");

            if (msg.contains("YA_INSCRITO"))
                postMessage("Ya estás inscrito");
            else if (msg.contains("NO_PLAZAS"))
                postMessage("No quedan plazas disponibles.");
            else
                postMessage("Error: " + msg);

            setActionInProgress(false);
        });
    }


    // ==========================================================
    // HELPERS
    // ==========================================================
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

    private void setActionInProgress(boolean inProgress) {
        EventosDisponiblesUserPrivateUiState prev = _uiState.getValue();
        if (prev == null) prev = EventosDisponiblesUserPrivateUiState.loading();

        _uiState.setValue(EventosDisponiblesUserPrivateUiState.withAction(prev, inProgress));
    }

    private static @Nullable String emptyToNull(@Nullable String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private static @Nullable String valueOf(@Nullable Object o) {
        if (o == null) return null;
        String s = String.valueOf(o);
        return "null".equalsIgnoreCase(s) ? null : s;
    }
}
