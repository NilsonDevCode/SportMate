package com.nilson.appsportmate.features.user.ui.menuPrincipal;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Carga inscripciones del usuario, completa datos del evento y cuenta inscritos. */
public class InicioViewModel extends ViewModel {

    private final MutableLiveData<InicioUiState> _uiState = new MutableLiveData<>(InicioUiState.loading());
    public LiveData<InicioUiState> uiState = _uiState;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String uid = FirebaseAuth.getInstance().getUid();

    public void cargarDeportesApuntados() {
        if (uid == null) { _uiState.setValue(InicioUiState.error("Usuario no autenticado")); return; }
        _uiState.setValue(InicioUiState.loading());

        db.collection("usuarios")
                .document(uid)
                .collection("inscripciones")
                .get(Source.SERVER)
                .addOnSuccessListener(query -> {
                    List<Map<String, Object>> rows = new ArrayList<>();
                    List<Task<?>> pending = new ArrayList<>();

                    for (DocumentSnapshot d : query.getDocuments()) {
                        Map<String, Object> base = new HashMap<>();
                        base.put("docId", d.getId());
                        base.put("aytoId", str(d.get("ayuntamientoId")));

                        // Valores base desde la inscripción
                        base.put("nombre", firstNonEmpty(
                                d.get("nombre"), d.get("deporteNombre"), d.get("nombreDeporte"),
                                d.get("deporte"), d.get("titulo")));

                        base.put("descripcion", firstNonEmpty(d.get("descripcion"), d.get("desc")));
                        base.put("fecha", firstNonEmpty(d.get("fecha"), d.get("date")));
                        base.put("hora",  firstNonEmpty(d.get("hora"),  d.get("time")));

                        // ⬅️ AÑADIDO urlPueblo como opción para "lugar"
                        base.put("lugar", firstNonEmpty(
                                d.get("lugar"), d.get("ubicacion"), d.get("pistaNombre"), d.get("urlPueblo")));

                        base.put("aytoNombre", firstNonEmpty(d.get("ayuntamientoNombre"), d.get("ayuntamiento")));

                        // ⬅️ AÑADIDO plazasDisponibles como source principal
                        Integer plazas = intOrNull(d.get("plazasDisponibles"), d.get("plazasMax"),
                                d.get("cupoMax"), d.get("plazas"));
                        if (plazas != null) base.put("plazasMax", plazas);

                        rows.add(base);

                        String aytoId = str(d.get("ayuntamientoId"));
                        String docId = d.getId();

                        // Completar con datos del evento si faltan
                        boolean needEvento = (isEmpty((String) base.get("nombre")) ||
                                isEmpty((String) base.get("fecha")) ||
                                isEmpty((String) base.get("hora")) ||
                                isEmpty((String) base.get("lugar")) ||
                                base.get("plazasMax") == null)
                                && !isEmpty(aytoId);

                        if (needEvento) {
                            Task<DocumentSnapshot> tEv = db.collection("deportes_ayuntamiento")
                                    .document(aytoId)
                                    .collection("lista")
                                    .document(docId)
                                    .get(Source.SERVER)
                                    .addOnSuccessListener(ev -> {
                                        if (!ev.exists()) return;

                                        putIfEmpty(base, "nombre", firstNonEmpty(
                                                ev.get("nombre"), ev.get("deporteNombre"),
                                                ev.get("nombreDeporte"), ev.get("deporte"), ev.get("titulo")));

                                        putIfEmpty(base, "descripcion", firstNonEmpty(ev.get("descripcion"), ev.get("desc")));
                                        putIfEmpty(base, "fecha", firstNonEmpty(ev.get("fecha"), ev.get("date")));
                                        putIfEmpty(base, "hora",  firstNonEmpty(ev.get("hora"),  ev.get("time")));

                                        // ⬅️ AÑADIDO urlPueblo también desde el evento
                                        putIfEmpty(base, "lugar", firstNonEmpty(
                                                ev.get("lugar"), ev.get("ubicacion"),
                                                ev.get("pistaNombre"), ev.get("urlPueblo")));

                                        if (base.get("plazasMax") == null) {
                                            Integer p = intOrNull(ev.get("plazasDisponibles"), ev.get("plazasMax"),
                                                    ev.get("cupoMax"), ev.get("plazas"));
                                            if (p != null) base.put("plazasMax", p);
                                        }
                                    });
                            pending.add(tEv);
                        }

                        // Nombre del ayuntamiento si falta
                        if (isEmpty((String) base.get("aytoNombre")) && !isEmpty(aytoId)) {
                            Task<DocumentSnapshot> tAy = db.collection("ayuntamientos")
                                    .document(aytoId)
                                    .get(Source.SERVER)
                                    .addOnSuccessListener(ay -> {
                                        if (!ay.exists()) return;
                                        String nom = str(ay.get("nombre"));
                                        if (isEmpty(nom)) nom = str(ay.get("razonSocial"));
                                        if (!isEmpty(nom)) base.put("aytoNombre", nom);
                                    });
                            pending.add(tAy);
                        }

                        // Contar inscritos
                        if (!isEmpty(aytoId)) {
                            CollectionReference inscritosRef = db.collection("deportes_ayuntamiento")
                                    .document(aytoId)
                                    .collection("lista")
                                    .document(docId)
                                    .collection("inscritos");
                            Task<QuerySnapshot> tCount = inscritosRef.get(Source.SERVER)
                                    .addOnSuccessListener(snap -> base.put("inscritos", snap.size()));
                            pending.add(tCount);
                        }
                    }

                    if (rows.isEmpty()) {
                        _uiState.setValue(InicioUiState.success(new ArrayList<>()));
                        return;
                    }

                    Tasks.whenAllComplete(pending).addOnCompleteListener(done -> {
                        List<InicioUiState.DeporteUi> out = new ArrayList<>();
                        for (Map<String, Object> r : rows) {
                            InicioUiState.DeporteUi ui = new InicioUiState.DeporteUi();
                            ui.docId = str(r.get("docId"));
                            ui.aytoId = str(r.get("aytoId"));
                            ui.nombreDeporte = str(r.get("nombre"));
                            ui.descripcion = str(r.get("descripcion"));
                            ui.fecha = str(r.get("fecha"));
                            ui.hora  = str(r.get("hora"));
                            ui.lugar = str(r.get("lugar"));
                            ui.ayuntamiento = str(r.get("aytoNombre"));

                            Object p = r.get("plazasMax");
                            ui.plazasMax = p instanceof Integer ? (Integer) p :
                                    (p instanceof Long ? ((Long) p).intValue() : 0);

                            Object ins = r.get("inscritos");
                            ui.inscritos = ins instanceof Integer ? (Integer) ins :
                                    (ins instanceof Long ? ((Long) ins).intValue() : 0);

                            out.add(ui);
                        }
                        _uiState.setValue(InicioUiState.success(out));
                    });

                })
                .addOnFailureListener(e ->
                        _uiState.setValue(InicioUiState.error("Error cargando deportes: " + (e != null ? e.getMessage() : "")))
                );
    }

    /** Desapuntarse con transacción: +1 plazaDisponible, borra inscripción y refresca. */
    public void desapuntarse(@NonNull String docId, @NonNull String aytoId) {
        if (uid == null) { _uiState.setValue(InicioUiState.error("Usuario no autenticado")); return; }

        DocumentReference refDeporte = db.collection("deportes_ayuntamiento")
                .document(aytoId).collection("lista").document(docId);
        DocumentReference refInscrito = refDeporte.collection("inscritos").document(uid);
        DocumentReference refUser = db.collection("usuarios").document(uid)
                .collection("inscripciones").document(docId);

        db.runTransaction(tx -> {
            DocumentSnapshot dep = tx.get(refDeporte);
            Long plazasDisp = dep.getLong("plazasDisponibles");
            if (plazasDisp == null) plazasDisp = 0L;

            DocumentSnapshot ins = tx.get(refInscrito);
            if (!ins.exists()) throw new IllegalStateException("NO_ESTABA_INSCRITO");

            tx.update(refDeporte, "plazasDisponibles", plazasDisp + 1);
            tx.delete(refInscrito);
            tx.delete(refUser);
            return null;
        }).addOnSuccessListener(unused -> {
            _uiState.setValue(_uiState.getValue() == null ? InicioUiState.loading()
                    : _uiState.getValue().withMessage("Te has desapuntado"));
            cargarDeportesApuntados();
        }).addOnFailureListener(e -> {
            String msg = e != null && e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("NO_ESTABA_INSCRITO")) {
                _uiState.setValue(InicioUiState.error("No estabas inscrito en esta actividad."));
            } else {
                _uiState.setValue(InicioUiState.error("Error al desapuntarte: " + msg));
            }
        });
    }

    // ===== Helpers =====
    private static void putIfEmpty(Map<String, Object> map, String key, String value) {
        if (map.get(key) == null || str(map.get(key)).isEmpty()) {
            if (!isEmpty(value)) map.put(key, value);
        }
    }

    private static boolean isEmpty(String x) { return x == null || x.trim().isEmpty(); }

    private static String str(Object o) {
        if (o == null) return "";
        String s = String.valueOf(o).trim();
        return "null".equalsIgnoreCase(s) ? "" : s;
    }

    private static String firstNonEmpty(Object... opts) {
        for (Object o : opts) {
            String v = str(o);
            if (!isEmpty(v)) return v;
        }
        return "";
    }

    private static Integer intOrNull(Object... opts) {
        for (Object o : opts) {
            if (o instanceof Integer) return (Integer) o;
            if (o instanceof Long) return ((Long) o).intValue();
            try {
                if (o != null) return Integer.parseInt(String.valueOf(o));
            } catch (Exception ignored) {}
        }
        return null;
    }
}
