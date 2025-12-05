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
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventosDisponiblesUserPrivateViewModel extends ViewModel {

    private static final String TAG = "EventosPrivadosVM";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 🔥 EJECUTOR para evitar errores por await en MAIN
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<EventosDisponiblesUserPrivateUiState> _uiState =
            new MutableLiveData<>(EventosDisponiblesUserPrivateUiState.loading());
    public final LiveData<EventosDisponiblesUserPrivateUiState> uiState = _uiState;

    private @Nullable String uid;
    private @Nullable String alias;
    private @Nullable String puebloId;

    private final List<Map<String, Object>> cacheDisponibles = new ArrayList<>();
    private final List<Map<String, Object>> cacheMis = new ArrayList<>();


    // ==========================================================
    // INIT
    // ==========================================================
    public void init(@Nullable String uid, @Nullable String alias, @Nullable String puebloId) {
        this.uid = emptyToNull(uid);
        this.alias = emptyToNull(alias);
        this.puebloId = emptyToNull(puebloId);

        Log.e(TAG, "INIT → uid=" + this.uid + " alias=" + this.alias + " puebloId=" + this.puebloId);
    }

    public void ensurePuebloId(@Nullable String nuevoId) {
        nuevoId = emptyToNull(nuevoId);
        Log.e(TAG, "ensurePuebloId() nuevo=" + nuevoId + " actual=" + puebloId);

        if ((nuevoId == null && puebloId != null) || (nuevoId != null && !nuevoId.equals(puebloId))) {
            Log.e(TAG, "→ Cambio detectado, recargando...");
            this.puebloId = nuevoId;
            loadAll();
        }
    }


    // ==========================================================
    // LOAD ALL
    // ==========================================================
    public void loadAll() {
        Log.e(TAG, "loadAll() llamado");

        _uiState.setValue(EventosDisponiblesUserPrivateUiState.loading());

        Tasks.whenAll(
                Tasks.call(executor, () -> {
                    Log.e(TAG, "📌 Ejecutando cargarDisponiblesInternal...");
                    cargarDisponiblesInternal();
                    return null;
                }),
                Tasks.call(executor, () -> {
                    Log.e(TAG, "📌 Ejecutando cargarMisInternal...");
                    cargarMisInternal();
                    return null;
                })
        ).addOnSuccessListener(v -> {

            Log.e(TAG, "✔ loadAll COMPLETADO → disponibles=" + cacheDisponibles.size()
                    + " mis=" + cacheMis.size());

            _uiState.setValue(
                    EventosDisponiblesUserPrivateUiState.success(
                            new ArrayList<>(cacheDisponibles),
                            new ArrayList<>(cacheMis)
                    )
            );

        }).addOnFailureListener(e -> {

            Log.e(TAG, "❌ ERROR en loadAll()", e);

            _uiState.setValue(
                    EventosDisponiblesUserPrivateUiState.message(
                            EventosDisponiblesUserPrivateUiState.success(
                                    new ArrayList<>(cacheDisponibles),
                                    new ArrayList<>(cacheMis)
                            ),
                            "Error cargando datos: " + (e != null ? e.getMessage() : "")
                    )
            );
        });
    }


    // ==========================================================
    // CARGA DE DISPONIBLES (POR PUEBLO)
    // ==========================================================
    private void cargarDisponiblesInternal() throws Exception {

        Log.e(TAG, "cargarDisponiblesInternal() puebloId=" + puebloId);

        cacheDisponibles.clear();

        if (puebloId == null) {
            Log.e(TAG, "⛔ puebloId es NULL, no se cargan eventos");
            return;
        }

        Log.e(TAG, "Consultando ruta: eventos_privados_por_pueblo/" + puebloId + "/lista");

        List<DocumentSnapshot> docs = Tasks.await(
                db.collection("eventos_privados_por_pueblo")
                        .document(puebloId)
                        .collection("lista")
                        .get(Source.SERVER)
        ).getDocuments();

        Log.e(TAG, "Documentos encontrados: " + docs.size());

        for (DocumentSnapshot d : docs) {
            Log.e(TAG, "➡ Evento: " + d.getId() + " → " + d.getData());

            Map<String, Object> m = d.getData();
            if (m == null) continue;

            m = new HashMap<>(m);
            m.put("idDoc", d.getId());
            m.put("ownerId", m.get("uidCreador"));

            cacheDisponibles.add(m);
        }

        Log.e(TAG, "✔ Disponibles cargados: " + cacheDisponibles.size());
    }


    // ==========================================================
    // CARGA DE MIS INSCRIPCIONES
    // ==========================================================
    private void cargarMisInternal() throws Exception {

        Log.e(TAG, "cargarMisInternal() uid=" + uid);

        cacheMis.clear();

        if (uid == null) {
            Log.e(TAG, "⛔ uid NULL, no se cargan inscripciones");
            return;
        }

        List<DocumentSnapshot> snaps = Tasks.await(
                db.collection("usuarios")
                        .document(uid)
                        .collection("inscripciones_privadas")
                        .get(Source.SERVER)
        ).getDocuments();

        Log.e(TAG, "Inscripciones encontradas: " + snaps.size());

        if (snaps.isEmpty()) return;

        List<Map<String, Object>> tmp = new ArrayList<>();
        WriteBatch limpieza = db.batch();

        for (DocumentSnapshot d : snaps) {

            Log.e(TAG, "➡ Inscripción: " + d.getId() + " → " + d.getData());

            Map<String, Object> m = d.getData();
            if (m == null) continue;

            String eventId = d.getId();
            String ownerId = valueOf(m.get("ownerId"));

            Log.e(TAG, "Verificando evento real → ownerId=" + ownerId + " eventId=" + eventId);

            DocumentSnapshot snapEvt = Tasks.await(
                    db.collection("eventos_user_private")
                            .document(ownerId)
                            .collection("lista")
                            .document(eventId)
                            .get(Source.SERVER)
            );

            if (!snapEvt.exists()) {
                Log.e(TAG, "⚠ Evento eliminado → limpiando inscripción");
                limpieza.delete(d.getReference());
                continue;
            }

            m = new HashMap<>(m);
            m.put("idDoc", eventId);
            tmp.add(m);
        }

        Tasks.await(limpieza.commit());

        cacheMis.clear();
        cacheMis.addAll(tmp);

        Log.e(TAG, "✔ Inscripciones válidas: " + cacheMis.size());
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

        Log.e(TAG, "→ Apuntarse eventId=" + eventId + " ownerId=" + ownerId);

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

            Log.e(TAG, "🔄 Ejecutando transacción APUNTARSE...");

            DocumentSnapshot snapEvt = tx.get(refEvt);
            Long plazas = snapEvt.getLong("plazasDisponibles");

            Log.e(TAG, "📊 plazasDisponibles=" + plazas);

            if (plazas == null) plazas = 0L;
            if (plazas <= 0) throw new IllegalStateException("NO_PLAZAS");
            if (tx.get(refInscrito).exists()) throw new IllegalStateException("YA_INSCRITO");

            tx.update(refEvt, "plazasDisponibles", plazas - 1);

            Map<String, Object> ins = new HashMap<>();
            ins.put("uid", uid);
            ins.put("alias", alias);
            ins.put("ts", System.currentTimeMillis());

            Log.e(TAG, "Añadiendo inscrito → " + ins);

            tx.set(refInscrito, ins);

            Map<String, Object> copia = new HashMap<>(evento);
            copia.put("idDoc", eventId);
            copia.put("ownerId", ownerId);
            tx.set(refUser, copia);

            return null;

        }).addOnSuccessListener(v -> {
            Log.e(TAG, "✔ APUNTADO CORRECTAMENTE");
            postMessage("Inscripción completada");
            reloadAfterAction();
        }).addOnFailureListener(e -> {

            Log.e(TAG, "❌ Error apuntándose", e);
            String msg = e != null ? e.getMessage() : "";

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
    // DESAPUNTARSE
    // ==========================================================
    public void desapuntarse(Map<String, Object> evento) {

        Log.e(TAG, "🔴 desapuntarse() evento=" + evento);

        if (uid == null) {
            postMessage("Inicia sesión");
            return;
        }

        setActionInProgress(true);

        String eventId = valueOf(evento.get("idDoc"));
        String ownerId = valueOf(evento.get("ownerId"));

        Log.e(TAG, "→ Desapuntarse eventId=" + eventId + " ownerId=" + ownerId);

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

            Log.e(TAG, "🔄 Ejecutando transacción DESAPUNTARSE...");

            DocumentSnapshot snapEvt = tx.get(refEvt);
            Long plazas = snapEvt.getLong("plazasDisponibles");

            Log.e(TAG, "📊 plazasDisponibles=" + plazas);

            if (plazas == null) plazas = 0L;
            if (!tx.get(refInscrito).exists())
                throw new IllegalStateException("NO_ESTABA_INSCRITO");

            tx.update(refEvt, "plazasDisponibles", plazas + 1);
            tx.delete(refInscrito);
            tx.delete(refUser);

            return null;

        }).addOnSuccessListener(v -> {
            Log.e(TAG, "✔ DESAPUNTADO CORRECTAMENTE");
            postMessage("Inscripción eliminada");
            reloadAfterAction();
        }).addOnFailureListener(e -> {

            Log.e(TAG, "❌ Error al desapuntarse", e);
            String msg = e != null ? e.getMessage() : "";

            if (msg.contains("NO_ESTABA_INSCRITO"))
                postMessage("No estabas inscrito");
            else
                postMessage("Error: " + msg);

            setActionInProgress(false);
        });
    }


    // ==========================================================
    // RECARGAR DESPUÉS DE ACCIÓN
    // ==========================================================
    private void reloadAfterAction() {

        Log.e(TAG, "🔄 reloadAfterAction() → recargando...");

        Tasks.whenAll(
                Tasks.call(executor, () -> {
                    Log.e(TAG, "→ Recargando disponibles");
                    cargarDisponiblesInternal();
                    return null;
                }),
                Tasks.call(executor, () -> {
                    Log.e(TAG, "→ Recargando mis inscripciones");
                    cargarMisInternal();
                    return null;
                })
        ).addOnSuccessListener(v -> {

            Log.e(TAG, "✔ Recarga completada OK");

            _uiState.setValue(
                    EventosDisponiblesUserPrivateUiState.success(
                            new ArrayList<>(cacheDisponibles),
                            new ArrayList<>(cacheMis)
                    )
            );

            setActionInProgress(false);

        }).addOnFailureListener(e -> {

            Log.e(TAG, "❌ Error recargando datos", e);

            postMessage("Error recargando");
            setActionInProgress(false);
        });
    }


    // ==========================================================
    // HELPERS
    // ==========================================================
    private void postMessage(String msg) {
        Log.e(TAG, "📢 postMessage() → " + msg);

        EventosDisponiblesUserPrivateUiState prev = _uiState.getValue();
        if (prev == null) prev = EventosDisponiblesUserPrivateUiState.loading();

        _uiState.setValue(EventosDisponiblesUserPrivateUiState.message(prev, msg));
    }

    public void consumeMessage() {
        Log.e(TAG, "🧹 consumeMessage()");

        EventosDisponiblesUserPrivateUiState prev = _uiState.getValue();
        if (prev != null && prev.message != null) {
            _uiState.setValue(prev.clearMessage());
        }
    }

    private void setActionInProgress(boolean inProgress) {
        Log.e(TAG, "⏳ setActionInProgress=" + inProgress);

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
