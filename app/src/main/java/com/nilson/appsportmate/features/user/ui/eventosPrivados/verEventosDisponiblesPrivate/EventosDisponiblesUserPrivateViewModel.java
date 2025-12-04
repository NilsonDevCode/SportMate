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

    // 🔥 EJECUTOR PROPIO PARA EVITAR EL ERROR “Must not be called on main thread”
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<EventosDisponiblesUserPrivateUiState> _uiState =
            new MutableLiveData<>(EventosDisponiblesUserPrivateUiState.loading());
    public final LiveData<EventosDisponiblesUserPrivateUiState> uiState = _uiState;

    private @Nullable String uid;
    private @Nullable String alias;
    private @Nullable String puebloIdFiltro;

    private final List<Map<String, Object>> cacheDisponibles = new ArrayList<>();
    private final List<Map<String, Object>> cacheMis = new ArrayList<>();


    // ==========================================================
    // INIT
    // ==========================================================
    public void init(@Nullable String uid, @Nullable String alias, @Nullable String puebloId) {
        this.uid = emptyToNull(uid);
        this.alias = emptyToNull(alias);
        this.puebloIdFiltro = emptyToNull(puebloId);

        Log.e(TAG, "INIT → uid=" + uid + " alias=" + alias + " puebloIdFiltro=" + puebloId);
    }


    // ==========================================================
    // CARGAR TODO — YA NO BLOQUEA EL MAIN
    // ==========================================================
    public void loadAll() {
        Log.e(TAG, "🔄 loadAll() llamado");
        _uiState.setValue(EventosDisponiblesUserPrivateUiState.loading());

        Tasks.whenAll(
                Tasks.call(executor, () -> {
                    loadDisponiblesSafe();
                    return null;
                }),
                Tasks.call(executor, () -> {
                    loadMisSafe();
                    return null;
                })
        ).addOnSuccessListener(x -> {

            Log.e(TAG, "✔ loadAll completado → disponibles=" + cacheDisponibles.size()
                    + " misInscripciones=" + cacheMis.size());

            _uiState.setValue(EventosDisponiblesUserPrivateUiState.success(
                    new ArrayList<>(cacheDisponibles),
                    new ArrayList<>(cacheMis)
            ));

        }).addOnFailureListener(e -> {
            Log.e(TAG, "❌ loadAll ERROR", e);

            _uiState.setValue(EventosDisponiblesUserPrivateUiState.message(
                    EventosDisponiblesUserPrivateUiState.success(
                            new ArrayList<>(cacheDisponibles),
                            new ArrayList<>(cacheMis)
                    ),
                    "Error cargando datos"
            ));
        });
    }


    // ==========================================================
    // DISPONIBLES — FILTRADOS POR PUEBLO
    // ==========================================================
    private void loadDisponiblesSafe() throws Exception {

        Log.e(TAG, "🔍 loadDisponiblesSafe() puebloIdFiltro=" + puebloIdFiltro);

        cacheDisponibles.clear();
        if (puebloIdFiltro == null) {
            Log.e(TAG, "⛔ ERROR → puebloIdFiltro es NULL");
            return;
        }

        var ref = db.collection("eventos_privados_por_pueblo")
                .document(puebloIdFiltro)
                .collection("lista");

        Log.e(TAG, "📂 Consultando → eventos_privados_por_pueblo/" + puebloIdFiltro + "/lista");

        List<DocumentSnapshot> docs = Tasks.await(
                ref.get(Source.SERVER)
        ).getDocuments();

        Log.e(TAG, "📄 Documentos encontrados: " + docs.size());

        for (DocumentSnapshot d : docs) {

            Log.e(TAG, "➡ Documento → " + d.getId() + " → " + d.getData());

            Map<String, Object> m = d.getData();
            if (m == null) {
                Log.e(TAG, "⚠ Evento sin data → ignorado");
                continue;
            }

            m = new HashMap<>(m);
            m.put("idDoc", d.getId());
            m.put("ownerId", m.get("uidCreador"));

            cacheDisponibles.add(m);
        }

        Log.e(TAG, "✔ Disponibles cargados: " + cacheDisponibles.size());
    }


    // ==========================================================
    // MIS INSCRIPCIONES
    // ==========================================================
    private void loadMisSafe() throws Exception {

        Log.e(TAG, "🔍 loadMisSafe() uid=" + uid);

        cacheMis.clear();
        if (uid == null) {
            Log.e(TAG, "⛔ ERROR → uid es NULL");
            return;
        }

        List<DocumentSnapshot> snaps = Tasks.await(
                db.collection("usuarios")
                        .document(uid)
                        .collection("inscripciones_privadas")
                        .get(Source.SERVER)
        ).getDocuments();

        Log.e(TAG, "📄 Inscripciones encontradas: " + snaps.size());

        if (snaps.isEmpty()) return;

        List<Map<String, Object>> tmp = new ArrayList<>();
        WriteBatch limpieza = db.batch();

        for (DocumentSnapshot d : snaps) {

            Log.e(TAG, "➡ Inscripción → " + d.getId() + " → " + d.getData());

            Map<String, Object> m = d.getData();
            if (m == null) continue;

            String eventId = d.getId();
            String ownerId = valueOf(m.get("ownerId"));

            Log.e(TAG, "🔎 Verificando evento " + eventId + " del owner " + ownerId);

            DocumentSnapshot snapEvt = Tasks.await(
                    db.collection("eventos_user_private")
                            .document(ownerId)
                            .collection("lista")
                            .document(eventId)
                            .get(Source.SERVER)
            );

            if (!snapEvt.exists()) {
                Log.e(TAG, "⚠ Evento YA NO EXISTE → limpiando inscripción");
                limpieza.delete(d.getReference());
                continue;
            }

            m = new HashMap<>(m);
            m.put("idDoc", eventId);

            tmp.add(m);
        }

        Tasks.await(limpieza.commit());

        cacheMis.addAll(tmp);

        Log.e(TAG, "✔ Mis eventos válidos: " + cacheMis.size());
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

        Log.e(TAG, "📌 Apuntarse eventId=" + eventId + " ownerId=" + ownerId);

        DocumentReference refEvento = db.collection("eventos_user_private")
                .document(ownerId)
                .collection("lista")
                .document(eventId);

        DocumentReference refInscrito = refEvento.collection("inscritos_privados").document(uid);
        DocumentReference refUser = db.collection("usuarios")
                .document(uid)
                .collection("inscripciones_privadas")
                .document(eventId);

        db.runTransaction(tx -> {

            DocumentSnapshot snapEvt = tx.get(refEvento);
            Long plazas = snapEvt.getLong("plazasDisponibles");

            Log.e(TAG, "📊 plazasDisponibles=" + plazas);

            if (plazas == null) plazas = 0L;

            if (plazas <= 0) throw new IllegalStateException("NO_PLAZAS");
            if (tx.get(refInscrito).exists()) throw new IllegalStateException("YA_INSCRITO");

            tx.update(refEvento, "plazasDisponibles", plazas - 1);

            Map<String, Object> ins = new HashMap<>();
            ins.put("uid", uid);
            ins.put("alias", alias);
            ins.put("ts", System.currentTimeMillis());
            tx.set(refInscrito, ins);

            Map<String, Object> copia = new HashMap<>(evento);
            copia.put("idDoc", eventId);
            copia.put("ownerId", ownerId);
            tx.set(refUser, copia);

            return null;

        }).addOnSuccessListener(unused -> {

            Log.e(TAG, "✔ Apuntado correctamente");
            postMessage("Inscripción completada");
            reloadAfterAction();

        }).addOnFailureListener(e -> {

            Log.e(TAG, "❌ Error apuntándose", e);

            String msg = (e.getMessage() == null) ? "" : e.getMessage();

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
            postMessage("Debes iniciar sesión");
            return;
        }

        setActionInProgress(true);

        String eventId = valueOf(evento.get("idDoc"));
        String ownerId = valueOf(evento.get("ownerId"));

        Log.e(TAG, "📌 Desapuntarse eventId=" + eventId + " ownerId=" + ownerId);

        DocumentReference refEvento = db.collection("eventos_user_private")
                .document(ownerId)
                .collection("lista")
                .document(eventId);

        DocumentReference refInscrito = refEvento.collection("inscritos_privados").document(uid);
        DocumentReference refUser = db.collection("usuarios")
                .document(uid)
                .collection("inscripciones_privadas")
                .document(eventId);

        db.runTransaction(tx -> {

            DocumentSnapshot snapEvt = tx.get(refEvento);
            Long plazas = snapEvt.getLong("plazasDisponibles");

            Log.e(TAG, "📊 plazasDisponibles antes=" + plazas);

            if (plazas == null) plazas = 0L;

            if (!tx.get(refInscrito).exists())
                throw new IllegalStateException("NO_ESTABA_INSCRITO");

            tx.update(refEvento, "plazasDisponibles", plazas + 1);
            tx.delete(refInscrito);
            tx.delete(refUser);

            return null;

        }).addOnSuccessListener(unused -> {

            Log.e(TAG, "✔ Desapuntado correctamente");
            postMessage("Inscripción cancelada");
            reloadAfterAction();

        }).addOnFailureListener(e -> {

            Log.e(TAG, "❌ Error desapuntarse", e);

            String msg = (e.getMessage() == null) ? "" : e.getMessage();

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

        Log.e(TAG, "🔄 Recargando datos...");

        Tasks.whenAll(
                Tasks.call(executor, () -> {
                    loadDisponiblesSafe();
                    return null;
                }),
                Tasks.call(executor, () -> {
                    loadMisSafe();
                    return null;
                })
        ).addOnSuccessListener(v -> {

            Log.e(TAG, "✔ Recarga completada OK");

            _uiState.setValue(EventosDisponiblesUserPrivateUiState.success(
                    new ArrayList<>(cacheDisponibles),
                    new ArrayList<>(cacheMis)
            ));

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
        Log.e(TAG, "📢 Mensaje UI → " + msg);
        EventosDisponiblesUserPrivateUiState prev = _uiState.getValue();
        if (prev == null) prev = EventosDisponiblesUserPrivateUiState.loading();
        _uiState.setValue(EventosDisponiblesUserPrivateUiState.message(prev, msg));
    }

    public void consumeMessage() {
        EventosDisponiblesUserPrivateUiState prev = _uiState.getValue();
        if (prev != null && prev.message != null) {
            Log.e(TAG, "🧹 Limpiando mensaje UI");
            _uiState.setValue(prev.clearMessage());
        }
    }

    private void setActionInProgress(boolean inProgress) {
        Log.e(TAG, "⏳ Acción en progreso = " + inProgress);
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
