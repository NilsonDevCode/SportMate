package com.nilson.appsportmate.features.user.ui.eventosPrivados.VerEventosApuntadoPrivate;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerEventosApuntadoPrivateViewModel extends ViewModel {

    private static final String TAG = "EventosApuntadosVM";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<VerEventosApuntadoPrivateUiState> _uiState =
            new MutableLiveData<>(VerEventosApuntadoPrivateUiState.loading());
    public LiveData<VerEventosApuntadoPrivateUiState> uiState = _uiState;

    private final String uid;

    public VerEventosApuntadoPrivateViewModel() {
        uid = FirebaseAuth.getInstance().getUid();
    }

    // ============================================================
    // UI CLASS
    // ============================================================
    public static class EventoUi {
        public String docId;
        public String ownerId;

        public String nombre;
        public String descripcion;
        public String fecha;
        public String hora;
        public String lugar;

        public int plazas;
        public int inscritos;

        public EventoUi() {}
    }

    // ============================================================
    // SUBIR FOTO DE PERFIL
    // ============================================================
    public void subirFotoPerfilUsuario(
            @NonNull Uri uri,
            @NonNull Runnable onSuccess,
            @NonNull java.util.function.Consumer<String> onError
    ) {
        if (uid == null) {
            onError.accept("Usuario no autenticado.");
            return;
        }

        FirebaseStorage.getInstance()
                .getReference("logos_usuarios/" + uid + ".jpg")
                .putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        taskSnapshot.getStorage().getDownloadUrl()
                                .addOnSuccessListener(downloadUri ->
                                        db.collection("usuarios")
                                                .document(uid)
                                                .update("fotoUrl", downloadUri.toString())
                                                .addOnSuccessListener(aVoid -> onSuccess.run())
                                                .addOnFailureListener(e -> onError.accept("Error guardando URL: " + e.getMessage()))
                                )
                                .addOnFailureListener(e -> onError.accept("Error obteniendo URL: " + e.getMessage()))
                )
                .addOnFailureListener(e -> onError.accept("Error subiendo imagen: " + e.getMessage()));
    }


    // ============================================================
    // CARGAR EVENTOS PRIVADOS APUNTADOS – VERSIÓN PROFESIONAL
    // ============================================================
    public void loadEventosApuntados() {

        if (uid == null) {
            _uiState.setValue(VerEventosApuntadoPrivateUiState.error("Debes iniciar sesión"));
            return;
        }

        _uiState.setValue(VerEventosApuntadoPrivateUiState.loading());

        new Thread(() -> {
            try {
                // 1️⃣ Obtener inscripciones
                List<DocumentSnapshot> inscripciones = Tasks.await(
                        db.collection("usuarios")
                                .document(uid)
                                .collection("inscripciones_privadas")
                                .get(Source.SERVER)
                ).getDocuments();

                if (inscripciones.isEmpty()) {
                    _uiState.postValue(VerEventosApuntadoPrivateUiState.success(new ArrayList<>()));
                    return;
                }

                List<Map<String, Object>> rows = new ArrayList<>();
                List<Task<?>> pending = new ArrayList<>();
                WriteBatch limpiar = db.batch();

                for (DocumentSnapshot ins : inscripciones) {

                    Map<String, Object> base = new HashMap<>();
                    String eventId = ins.getId();
                    String ownerId = str(ins.get("ownerId"));

                    base.put("docId", eventId);
                    base.put("ownerId", ownerId);

                    // 2️⃣ Leer evento privado
                    Task<DocumentSnapshot> tEvt = db.collection("eventos_user_private")
                            .document(ownerId)
                            .collection("lista")
                            .document(eventId)
                            .get(Source.SERVER)
                            .addOnSuccessListener(evt -> {
                                if (!evt.exists()) {
                                    limpiar.delete(ins.getReference());
                                    return;
                                }

                                put(base, "nombre", first(evt.get("nombre")));
                                put(base, "descripcion", first(evt.get("descripcion")));
                                put(base, "fecha", first(evt.get("fecha")));
                                put(base, "hora", first(evt.get("hora")));
                                put(base, "lugar", first(evt.get("lugar")));
                                put(base, "lugar", first(
                                        evt.get("lugar"),
                                        evt.get("ubicacion"),
                                        evt.get("pistaNombre"),
                                        evt.get("urlPueblo")
                                ));

                                Integer plazas = intOrNull(
                                        evt.get("plazasDisponibles"),
                                        evt.get("plazasMax")
                                );
                                if (plazas != null) base.put("plazas", plazas);
                            });

                    pending.add(tEvt);

                    // 3️⃣ Contar inscritos reales
                    CollectionReference inscritosRef = db.collection("eventos_user_private")
                            .document(ownerId)
                            .collection("lista")
                            .document(eventId)
                            .collection("inscritos_privados");

                    Task<QuerySnapshot> tCount = inscritosRef.get(Source.SERVER)
                            .addOnSuccessListener(snap ->
                                    base.put("inscritos", snap.size())
                            );
                    pending.add(tCount);

                    rows.add(base);
                }

                // 4️⃣ Esperar todo
                Tasks.whenAllComplete(pending).addOnCompleteListener(done -> {

                    List<EventoUi> salida = new ArrayList<>();

                    for (Map<String, Object> r : rows) {
                        EventoUi e = new EventoUi();

                        e.docId = str(r.get("docId"));
                        e.ownerId = str(r.get("ownerId"));

                        e.nombre = str(r.get("nombre"));
                        e.descripcion = str(r.get("descripcion"));
                        e.fecha = str(r.get("fecha"));
                        e.hora = str(r.get("hora"));
                        e.lugar = str(r.get("lugar"));

                        e.plazas = toInt(r.get("plazas"));
                        e.inscritos = toInt(r.get("inscritos"));

                        salida.add(e);
                    }

                    _uiState.postValue(VerEventosApuntadoPrivateUiState.success(salida));
                });

                Tasks.await(limpiar.commit());

            } catch (Exception e) {
                Log.e(TAG, "Error cargando eventos apuntados", e);
                _uiState.postValue(
                        VerEventosApuntadoPrivateUiState.error("Error cargando tus eventos")
                );
            }
        }).start();
    }

    // ============================================================
    // DESAPUNTARSE
    // ============================================================
    public void desapuntarse(EventoUi evento) {
        if (uid == null) return;

        String eventId = evento.docId;
        String ownerId = evento.ownerId;

        var refEvt = db.collection("eventos_user_private")
                .document(ownerId).collection("lista").document(eventId);

        var refInscrito = refEvt.collection("inscritos_privados").document(uid);
        var refUser = db.collection("usuarios").document(uid)
                .collection("inscripciones_privadas").document(eventId);

        db.runTransaction(tx -> {

                    DocumentSnapshot snapEvt = tx.get(refEvt);

                    int plazas = getInt(snapEvt.get("plazasDisponibles"));

                    tx.update(refEvt, "plazasDisponibles", plazas + 1);
                    tx.delete(refInscrito);
                    tx.delete(refUser);

                    return null;

                }).addOnSuccessListener(v -> loadEventosApuntados())
                .addOnFailureListener(e ->
                        _uiState.setValue(
                                VerEventosApuntadoPrivateUiState.error("No se pudo desapuntar"))
                );
    }

    // ============================================================
    // HELPERS
    // ============================================================
    private static void put(Map<String, Object> map, String key, String val) {
        if (val != null && !val.trim().isEmpty() && !"null".equalsIgnoreCase(val)) {
            map.put(key, val);
        }
    }

    private static String str(Object o) {
        if (o == null) return "";
        String s = String.valueOf(o).trim();
        return "null".equalsIgnoreCase(s) ? "" : s;
    }

    private static String first(Object... opts) {
        for (Object o : opts) {
            String v = str(o);
            if (!v.isEmpty()) return v;
        }
        return "";
    }

    private static int getInt(Object o) {
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Long) return ((Long) o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); }
        catch (Exception ignored) {}
        return 0;
    }

    private static Integer intOrNull(Object... opts) {
        for (Object o : opts) {
            if (o instanceof Integer) return (Integer) o;
            if (o instanceof Long) return ((Long) o).intValue();
            try { return Integer.parseInt(String.valueOf(o)); }
            catch (Exception ignored) {}
        }
        return null;
    }

    private static int toInt(Object o) {
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Long) return ((Long) o).intValue();
        return 0;
    }
}
