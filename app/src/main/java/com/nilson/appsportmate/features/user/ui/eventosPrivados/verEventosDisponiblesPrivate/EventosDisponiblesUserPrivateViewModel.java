package com.nilson.appsportmate.features.user.ui.eventosPrivados.verEventosDisponiblesPrivate;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventosDisponiblesUserPrivateViewModel extends ViewModel {

    private static final String TAG = "EventosPrivadosVM";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final MutableLiveData<EventosDisponiblesUserPrivateUiState> _uiState =
            new MutableLiveData<>(EventosDisponiblesUserPrivateUiState.loading());
    public final LiveData<EventosDisponiblesUserPrivateUiState> uiState = _uiState;

    private @Nullable String uid;
    private @Nullable String alias;
    private @Nullable String puebloId;

    private final List<Map<String, Object>> cacheDisponibles = new ArrayList<>();
    private final List<Map<String, Object>> cacheMis = new ArrayList<>();

    private ListenerRegistration listenerEventos = null;
    private ListenerRegistration listenerMisInscripciones = null;

    // 👉 NUEVO: listeners por cada evento privado
    private final List<ListenerRegistration> listenerEventosUserPrivate = new ArrayList<>();


    // ============================================================
    // INIT
    // ============================================================
    public void init(@Nullable String uid, @Nullable String alias, @Nullable String puebloId) {
        this.uid = emptyToNull(uid);
        this.alias = emptyToNull(alias);
        this.puebloId = emptyToNull(puebloId);

        Log.e(TAG, "INIT → uid=" + this.uid + " alias=" + this.alias + " puebloId=" + this.puebloId);
    }


    // ============================================================
    // LISTENERS PRINCIPALES
    // ============================================================
    public void activarListeners() {

        detenerListeners();

        if (puebloId == null) {
            Log.e(TAG, "⛔ No hay puebloId → no escucho nada");
            return;
        }

        // ============================================================
        // LISTENER EVENTOS DISPONIBLES (lista pública por pueblo)
        // ============================================================
        listenerEventos = db.collection("eventos_privados_por_pueblo")
                .document(puebloId)
                .collection("lista")
                .addSnapshotListener((snap, e) -> {

                    if (e != null || snap == null) {
                        Log.e(TAG, "Error listener eventos: " + e);
                        return;
                    }

                    cacheDisponibles.clear();
                    listenerEventosUserPrivate.forEach(ListenerRegistration::remove);
                    listenerEventosUserPrivate.clear();

                    for (DocumentSnapshot d : snap.getDocuments()) {

                        Map<String, Object> m = d.getData();
                        if (m == null) continue;

                        m = new HashMap<>(m);
                        String idDoc = d.getId();
                        String ownerId = String.valueOf(m.get("uidCreador"));

                        m.put("idDoc", idDoc);
                        m.put("ownerId", ownerId);

                        cacheDisponibles.add(m);

                        // ============================================================
                        // 🔥 AÑADIMOS LISTENER DINÁMICO AL EVENTO REAL
                        // ============================================================
                        ListenerRegistration lr = db.collection("eventos_user_private")
                                .document(ownerId)
                                .collection("lista")
                                .document(idDoc)
                                .addSnapshotListener((evSnap, exEv) -> {

                                    if (exEv != null || evSnap == null || !evSnap.exists())
                                        return;

                                    Long nuevasPlazas = evSnap.getLong("plazasDisponibles");

                                    actualizarPlazasEnCache(idDoc, nuevasPlazas);

                                    publicarEstado();
                                });

                        listenerEventosUserPrivate.add(lr);
                    }

                    publicarEstado();
                });


        // ============================================================
        // LISTENER MIS INSCRIPCIONES PRIVADAS
        // ============================================================
        if (uid != null) {
            listenerMisInscripciones = db.collection("usuarios")
                    .document(uid)
                    .collection("inscripciones_privadas")
                    .whereEqualTo("puebloId", puebloId)
                    .addSnapshotListener((snap, e) -> {

                        if (e != null || snap == null) return;

                        cacheMis.clear();

                        for (DocumentSnapshot d : snap.getDocuments()) {

                            Map<String, Object> m = d.getData();
                            if (m == null) continue;

                            m = new HashMap<>(m);
                            m.put("idDoc", d.getId());
                            m.put("estoyInscrito", true);

                            cacheMis.add(m);
                        }

                        publicarEstado();
                    });
        }
    }


    // ============================================================
    // METODO PARA ACTUALIZAR CACHE
    // ============================================================
    private void actualizarPlazasEnCache(String eventId, Long nuevasPlazas) {
        if (nuevasPlazas == null) return;

        for (Map<String, Object> m : cacheDisponibles) {
            if (eventId.equals(m.get("idDoc"))) {
                m.put("plazasDisponibles", nuevasPlazas);
                break;
            }
        }
    }


    // ============================================================
    // DETENER LISTENERS
    // ============================================================
    public void detenerListeners() {
        if (listenerEventos != null) listenerEventos.remove();
        if (listenerMisInscripciones != null) listenerMisInscripciones.remove();

        listenerEventosUserPrivate.forEach(ListenerRegistration::remove);
        listenerEventosUserPrivate.clear();
    }


    private void publicarEstado() {
        _uiState.setValue(
                EventosDisponiblesUserPrivateUiState.success(
                        new ArrayList<>(cacheDisponibles),
                        new ArrayList<>(cacheMis)
                )
        );
    }


    // ============================================================
    // APUNTARSE (NO TOCO NADA, YA ESTABA PERFECTO)
    // ============================================================
    public void apuntarse(Map<String, Object> evento) {

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

            DocumentReference refPueblo = db.collection("eventos_privados_por_pueblo")
                    .document(puebloId)
                    .collection("lista")
                    .document(eventId);

            tx.update(refPueblo, "plazasDisponibles", plazas - 1);

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


    // ============================================================
    // HELPERS
    // ============================================================
    private void postMessage(String msg) {
        EventosDisponiblesUserPrivateUiState prev = _uiState.getValue();
        _uiState.setValue(EventosDisponiblesUserPrivateUiState.message(prev, msg));
    }

    private void setActionInProgress(boolean inProgress) {
        EventosDisponiblesUserPrivateUiState prev = _uiState.getValue();
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
