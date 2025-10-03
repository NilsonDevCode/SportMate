package com.nilson.appsportmate.features.user.ui.menuPrincipal;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Carga inscripciones del usuario y completa campos que falten
 * consultando el evento y el ayuntamiento correspondientes.
 */
public class InicioViewModel extends ViewModel {

    private final MutableLiveData<InicioUiState> _uiState =
            new MutableLiveData<>(InicioUiState.loading());
    public LiveData<InicioUiState> uiState = _uiState;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String uid = FirebaseAuth.getInstance().getUid();

    public void cargarDeportesApuntados() {
        if (uid == null) {
            _uiState.setValue(InicioUiState.error("Usuario no autenticado"));
            return;
        }

        _uiState.setValue(InicioUiState.loading());

        db.collection("usuarios")
                .document(uid)
                .collection("inscripciones")
                .get(Source.SERVER)
                .addOnSuccessListener(query -> {
                    // Acumulamos filas editables y tareas de fallback
                    List<Map<String, String>> rows = new ArrayList<>();
                    List<Task<?>> fallbacks = new ArrayList<>();

                    for (DocumentSnapshot d : query.getDocuments()) {
                        Map<String, Object> m = d.getData();
                        if (m == null) continue;

                        String docId  = d.getId();
                        String aytoId = s(m.get("ayuntamientoId"));

                        // Nombre del deporte (probamos varias claves)
                        String nombre = firstNonEmpty(
                                m.get("nombre"),
                                m.get("deporteNombre"),
                                m.get("nombreDeporte"),
                                m.get("deporte"),
                                m.get("titulo")
                        );

                        // Fecha/Hora (por si usaste otras claves)
                        String fecha = firstNonEmpty(m.get("fecha"), m.get("date"));
                        String hora  = firstNonEmpty(m.get("hora"),  m.get("time"));

                        // Nombre del ayuntamiento (preferimos "nombre" frente a "razonSocial")
                        String aytoNombre = firstNonEmpty(
                                m.get("ayuntamientoNombre"),  // si ya lo guardaste
                                m.get("ayuntamiento")         // compat
                        );

                        // Construimos fila base
                        Map<String, String> row = new HashMap<>();
                        row.put("docId", docId);
                        row.put("aytoId", aytoId);
                        row.put("nombre", nombre);
                        row.put("fecha",  fecha);
                        row.put("hora",   hora);
                        row.put("aytoNombre", aytoNombre);
                        rows.add(row);

                        // ===== Fallback a EVENTO si faltan nombre/fecha/hora =====
                        boolean needEvento = (isEmpty(nombre) || isEmpty(fecha) || isEmpty(hora)) && !isEmpty(aytoId);
                        if (needEvento) {
                            Task<DocumentSnapshot> tEv = db.collection("deportes_ayuntamiento")
                                    .document(aytoId)
                                    .collection("lista")
                                    .document(docId)
                                    .get(Source.SERVER)
                                    .addOnSuccessListener(ev -> {
                                        if (!ev.exists()) return;

                                        // nombre del evento (probamos varias)
                                        String evNombre = firstNonEmpty(
                                                ev.get("nombre"),
                                                ev.get("deporteNombre"),
                                                ev.get("nombreDeporte"),
                                                ev.get("deporte"),
                                                ev.get("titulo")
                                        );
                                        String evFecha = firstNonEmpty(ev.get("fecha"), ev.get("date"));
                                        String evHora  = firstNonEmpty(ev.get("hora"),  ev.get("time"));

                                        if (isEmpty(row.get("nombre")) && !isEmpty(evNombre))
                                            row.put("nombre", evNombre);
                                        if (isEmpty(row.get("fecha")) && !isEmpty(evFecha))
                                            row.put("fecha", evFecha);
                                        if (isEmpty(row.get("hora")) && !isEmpty(evHora))
                                            row.put("hora", evHora);
                                    });
                            fallbacks.add(tEv);
                        }

                        // ===== Fallback a AYUNTAMIENTO si falta aytoNombre =====
                        boolean needAyto = isEmpty(aytoNombre) && !isEmpty(aytoId);
                        if (needAyto) {
                            Task<DocumentSnapshot> tAy = db.collection("ayuntamientos")
                                    .document(aytoId)
                                    .get(Source.SERVER)
                                    .addOnSuccessListener(ay -> {
                                        if (!ay.exists()) return;
                                        // preferimos "nombre" y solo si falta usamos "razonSocial"
                                        String nom = s(ay.get("nombre"));
                                        if (isEmpty(nom)) nom = s(ay.get("razonSocial"));
                                        if (!isEmpty(nom)) row.put("aytoNombre", nom);
                                    });
                            fallbacks.add(tAy);
                        }
                    }

                    // Cuando no hay inscripciones
                    if (rows.isEmpty()) {
                        _uiState.setValue(InicioUiState.success(new ArrayList<>()));
                        return;
                    }

                    // Si no hay fallbacks, publicamos directo desde rows
                    if (fallbacks.isEmpty()) {
                        _uiState.setValue(InicioUiState.success(toUi(rows)));
                        return;
                    }

                    // Esperamos a que acaben los fallbacks y publicamos con los datos ya completados
                    Tasks.whenAllComplete(fallbacks).addOnCompleteListener(done ->
                            _uiState.setValue(InicioUiState.success(toUi(rows)))
                    );
                })
                .addOnFailureListener(e ->
                        _uiState.setValue(InicioUiState.error("Error cargando deportes: " + e.getMessage()))
                );
    }

    // ——— Helpers ———

    @NonNull
    private static List<InicioUiState.DeporteUi> toUi(List<Map<String, String>> rows) {
        List<InicioUiState.DeporteUi> out = new ArrayList<>();
        for (Map<String, String> r : rows) {
            out.add(new InicioUiState.DeporteUi(
                    nz(r.get("nombre")),
                    nz(r.get("fecha")),
                    nz(r.get("hora")),
                    nz(r.get("aytoNombre"))
            ));
        }
        return out;
    }

    private static String s(Object o) {
        if (o == null) return "";
        String x = String.valueOf(o).trim();
        return "null".equalsIgnoreCase(x) ? "" : x;
    }

    private static boolean isEmpty(String x) {
        return x == null || x.trim().isEmpty();
    }

    private static String nz(String x) { return x == null ? "" : x; }

    /** Devuelve el primer valor no vacío de la lista de objetos. */
    private static String firstNonEmpty(Object... options) {
        for (Object o : options) {
            String v = s(o);
            if (!isEmpty(v)) return v;
        }
        return "";
    }
}
