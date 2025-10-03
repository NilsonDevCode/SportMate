package com.nilson.appsportmate.features.townhall.ui.gestiondeportes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * ViewModel para crear eventos deportivos del ayuntamiento.
 * Guarda en la ruta: deportes_ayuntamiento/{ayuntamientoUid}/lista/{autoId}
 */
public class GestionDeportesAyuntamientoViewModel extends ViewModel {

    private final MutableLiveData<GestionDeportesAyuntamientoUiState> _ui =
            new MutableLiveData<>(GestionDeportesAyuntamientoUiState.loading(null));
    public LiveData<GestionDeportesAyuntamientoUiState> ui = _ui;

    // Señales simples de navegación (sin crear nuevas clases).
    private final MutableLiveData<Boolean> _navigateToGestionEventos = new MutableLiveData<>(false);
    public LiveData<Boolean> navigateToGestionEventos = _navigateToGestionEventos;

    private final MutableLiveData<Boolean> _navigateAfterLogout = new MutableLiveData<>(false);
    public LiveData<Boolean> navigateAfterLogout = _navigateAfterLogout;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Llamar en onViewCreated() del Fragment */
    public void init() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        _ui.setValue(GestionDeportesAyuntamientoUiState.idle(uid));
    }

    /** Validación mínima */
    private String validate(String nombreDeporte,
                            String plazasStr,
                            String fecha,
                            String hora,
                            String descripcion,
                            String reglas,
                            String materiales,
                            String url) {

        if (isEmpty(nombreDeporte)) return "Nombre del deporte requerido";
        if (isEmpty(plazasStr)) return "Cantidad de jugadores requerida";
        try {
            int p = Integer.parseInt(plazasStr.trim());
            if (p <= 0) return "Plazas debe ser > 0";
        } catch (NumberFormatException e) {
            return "Plazas debe ser un número";
        }
        if (isEmpty(fecha)) return "Fecha requerida";
        if (isEmpty(hora)) return "Hora requerida";
        // descripción/reglas/materiales/url pueden ser opcionales, adapta si quieres forzarlas
        return null;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Crea un evento en la colección del ayuntamiento autenticado.
     * Campos mapeados a tus inputs.
     */
    public void crearEvento(String nombreDeporte,
                            String plazasStr,
                            String fecha,      // texto (p.ej. 2025-10-03)
                            String hora,       // texto (p.ej. 18:30)
                            String descripcion,
                            String reglas,
                            String materiales,
                            String urlExacta) {

        GestionDeportesAyuntamientoUiState st = _ui.getValue();
        String aytoUid = st != null ? st.ayuntamientoUid : (auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null);

        if (aytoUid == null) {
            _ui.setValue(GestionDeportesAyuntamientoUiState.error(null, "No hay sesión de ayuntamiento"));
            return;
        }

        String v = validate(nombreDeporte, plazasStr, fecha, hora, descripcion, reglas, materiales, urlExacta);
        if (v != null) {
            _ui.setValue(GestionDeportesAyuntamientoUiState.error(aytoUid, v));
            return;
        }

        int plazas = Integer.parseInt(plazasStr.trim());

        _ui.setValue(GestionDeportesAyuntamientoUiState.loading(aytoUid));

        Map<String, Object> evento = new HashMap<>();
        evento.put("nombreDeporte", nombreDeporte.trim());
        evento.put("plazasTotales", plazas);
        evento.put("plazasDisponibles", plazas);
        evento.put("fecha", fecha.trim());
        evento.put("hora", hora.trim());
        evento.put("descripcion", isEmpty(descripcion) ? null : descripcion.trim());
        evento.put("reglas", isEmpty(reglas) ? null : reglas.trim());
        evento.put("materiales", isEmpty(materiales) ? null : materiales.trim());
        evento.put("url", isEmpty(urlExacta) ? null : urlExacta.trim());

        // Metadatos
        evento.put("ayuntamientoId", aytoUid);
        evento.put("createdAt", FieldValue.serverTimestamp());
        evento.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("deportes_ayuntamiento")
                .document(aytoUid)
                .collection("lista")
                .add(evento)
                .addOnSuccessListener(ref ->
                        _ui.setValue(GestionDeportesAyuntamientoUiState.success(aytoUid, "Evento creado")))
                .addOnFailureListener(e ->
                        _ui.setValue(GestionDeportesAyuntamientoUiState.error(aytoUid, "Error al crear: " + e.getMessage())));
    }

    /** Señal para abrir la pantalla/listado de gestión de eventos */
    public void irAGestionEventos() {
        _navigateToGestionEventos.setValue(true);
        // reset (por si se observa varias veces)
        _navigateToGestionEventos.setValue(false);
    }

    /** Cerrar sesión del admin (ayuntamiento) */
    public void logout() {
        auth.signOut();
        _navigateAfterLogout.setValue(true);
        _navigateAfterLogout.setValue(false);
    }
}
