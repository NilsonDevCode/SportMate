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
    public void CargarEventosApuntados() {

        if (uid == null) {
            _uiState.setValue(VerEventosApuntadoPrivateUiState.error("Debes iniciar sesión"));
            return;
        }

        _uiState.setValue(VerEventosApuntadoPrivateUiState.loading());

        db.collection("usuarios")
                .document(uid)
                .collection("inscripciones_privadas")
                .get(Source.SERVER)
                .addOnSuccessListener(query -> {

                    List<Map<String, Object>> rows = new ArrayList<>();
                    List<Task<?>> pending = new ArrayList<>();

                    for (DocumentSnapshot d : query.getDocuments()) {

                        Map<String, Object> base = new HashMap<>();
                        String eventId = d.getId();
                        String ownerId = str(d.get("ownerId"));

                        base.put("docId", eventId);
                        base.put("ownerId", ownerId);

                        // ----- CAMPOS BASE DESDE LA INSCRIPCIÓN -----
                        base.put("nombre", first(
                                d.get("nombre"), d.get("titulo"), d.get("deporteNombre"),
                                d.get("nombreDeporte"), d.get("deporte")
                        ));

                        base.put("descripcion", first(d.get("descripcion"), d.get("desc")));
                        base.put("fecha", first(d.get("fecha"), d.get("date")));
                        base.put("hora", first(d.get("hora"), d.get("time")));

                        base.put("lugar", first(
                                d.get("lugar"), d.get("ubicacion"), d.get("pistaNombre"), d.get("urlPueblo")
                        ));

                        // plazasDisponibles DESDE INSCRIPCIÓN (si las guardaste)
                        Integer plazas = intOrNull(
                                d.get("plazasDisponibles"),
                                d.get("plazasMax"),
                                d.get("plazas"),
                                d.get("cupoMax")
                        );
                        if (plazas != null) base.put("plazasBase", plazas);

                        String docId = eventId;

                        // ----------- LEER EVENTO ORIGINAL SI FALTAN CAMPOS --------------
                        Task<DocumentSnapshot> tEv = db.collection("eventos_user_private")
                                .document(ownerId)
                                .collection("lista")
                                .document(docId)
                                .get(Source.SERVER)
                                .addOnSuccessListener(ev -> {

                                    if (!ev.exists()) return;

                                    put(base, "nombre", first(
                                            ev.get("nombre"), ev.get("titulo"),
                                            ev.get("deporteNombre"), ev.get("nombreDeporte")
                                    ));
                                    put(base, "descripcion", first(ev.get("descripcion"), ev.get("desc")));
                                    put(base, "fecha", first(ev.get("fecha"), ev.get("date")));
                                    put(base, "hora", first(ev.get("hora"), ev.get("time")));

                                    put(base, "lugar", first(
                                            ev.get("lugar"), ev.get("ubicacion"),
                                            ev.get("pistaNombre"), ev.get("urlPueblo")
                                    ));

                                    // LEER plazasDisponibles REAL
                                    Integer p2 = intOrNull(
                                            ev.get("plazasDisponibles"),   // 🔥 ESTE ES EL CORRECTO
                                            ev.get("plazas"),
                                            ev.get("plazasMax"),
                                            ev.get("cupoMax")
                                    );
                                    if (p2 != null) base.put("plazasBase", p2);
                                });
                        pending.add(tEv);

                        // ---------- CONTAR INSCRITOS REALES ----------
                        Task<QuerySnapshot> tCount = db.collection("eventos_user_private")
                                .document(ownerId)
                                .collection("lista")
                                .document(docId)
                                .collection("inscritos_privados")
                                .get(Source.SERVER)
                                .addOnSuccessListener(snap -> base.put("inscritos", snap.size()));

                        pending.add(tCount);

                        rows.add(base);
                    }

                    if (rows.isEmpty()) {
                        _uiState.setValue(VerEventosApuntadoPrivateUiState.success(new ArrayList<>()));
                        return;
                    }

                    // ----------- FINAL: MAPEAR IGUAL QUE InicioViewModel ----------
                    Tasks.whenAllComplete(pending).addOnCompleteListener(done -> {

                        List<EventoUi> out = new ArrayList<>();

                        for (Map<String, Object> r : rows) {

                            EventoUi ui = new EventoUi();

                            ui.docId = str(r.get("docId"));
                            ui.ownerId = str(r.get("ownerId"));

                            ui.nombre = str(r.get("nombre"));
                            ui.descripcion = str(r.get("descripcion"));
                            ui.fecha = str(r.get("fecha"));
                            ui.hora = str(r.get("hora"));
                            ui.lugar = str(r.get("lugar"));

                            // plazas BASE reales del evento
                            int plazasBase = toInt(r.get("plazasBase"));

                            // inscritos reales
                            int inscritos = toInt(r.get("inscritos"));
                            ui.inscritos = inscritos;

                            // 🔥 SI EXISTE plazasDisponibles → NO RECALCULAR
                            if (r.get("plazasDisponibles") != null || r.get("plazasBase") != null) {
                                ui.plazas = plazasBase;  // este valor ya es real, no se resta
                            } else {
                                // 🔥 fallback (por si no existía en el documento)
                                ui.plazas = Math.max(plazasBase - inscritos, 0);
                            }

                            out.add(ui);
                        }

                        _uiState.setValue(VerEventosApuntadoPrivateUiState.success(out));
                    });

                })
                .addOnFailureListener(e -> {
                    _uiState.setValue(
                            VerEventosApuntadoPrivateUiState.error("Error cargando tus eventos")
                    );
                });
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

                }).addOnSuccessListener(v -> CargarEventosApuntados())
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
