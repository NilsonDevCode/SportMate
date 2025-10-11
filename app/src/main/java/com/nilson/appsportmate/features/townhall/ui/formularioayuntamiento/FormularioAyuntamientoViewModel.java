package com.nilson.appsportmate.features.townhall.ui.formularioayuntamiento;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * ViewModel para crear/actualizar los datos del Ayuntamiento.
 * Graba en la colección "ayuntamientos" con documentId = uid del usuario.
 */
public class FormularioAyuntamientoViewModel extends ViewModel {

    private final MutableLiveData<FormularioAyuntamientoUiState> _ui =
            new MutableLiveData<>(FormularioAyuntamientoUiState.loading(null));
    public LiveData<FormularioAyuntamientoUiState> ui = _ui;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Llama desde el Fragment en onViewCreated() */
    public void init() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        _ui.setValue(FormularioAyuntamientoUiState.idle(uid));
    }

    /** Validación mínima de campos obligatorios */
    private String validate(String nombre, String razonSocial, String comunidad,
                            String provincia, String ciudad, String pueblo, String localidad) {

        if (nombre == null || nombre.trim().isEmpty()) return "Nombre requerido";
        if (razonSocial == null || razonSocial.trim().isEmpty()) return "Razón social requerida";
        if (comunidad == null || comunidad.trim().isEmpty()) return "Comunidad requerida";
        if (provincia == null || provincia.trim().isEmpty()) return "Provincia requerida";
        if (ciudad == null || ciudad.trim().isEmpty()) return "Ciudad requerida";
        if (pueblo == null || pueblo.trim().isEmpty()) return "Pueblo requerido";
        if (localidad == null || localidad.trim().isEmpty()) return "Localidad requerida";
        return null;
    }

    /**
     * Guarda/actualiza el ayuntamiento del usuario autenticado.
     * Campos mapeados a tus IDs de layout.
     */
    public void guardar(String nombre,
                        String razonSocial,            // etNumero
                        String comunidad,              // etDescripcionEvento
                        String provincia,              // etReglasEvento
                        String ciudad,                 // etMateriales
                        String pueblo,                 // etUrlPueblo
                        String localidad               // etLocalidad
    ) {
        FormularioAyuntamientoUiState current = _ui.getValue();
        String uid = current != null ? current.uid : (auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null);

        if (uid == null) {
            _ui.setValue(FormularioAyuntamientoUiState.error(null, "No hay sesión activa"));
            return;
        }

        String v = validate(nombre, razonSocial, comunidad, provincia, ciudad, pueblo, localidad);
        if (v != null) {
            _ui.setValue(FormularioAyuntamientoUiState.error(uid, v));
            return;
        }

        _ui.setValue(FormularioAyuntamientoUiState.loading(uid));

        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("nombre", nombre.trim());
        data.put("razonSocial", razonSocial.trim());
        data.put("comunidad", comunidad.trim());
        data.put("provincia", provincia.trim());
        data.put("ciudad", ciudad.trim());
        data.put("pueblo", pueblo.trim());
        data.put("localidad", localidad.trim());
        data.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("ayuntamientos").document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused ->
                        _ui.setValue(FormularioAyuntamientoUiState.success(uid, "Ayuntamiento guardado")))
                .addOnFailureListener(e ->
                        _ui.setValue(FormularioAyuntamientoUiState.error(uid, "Error al guardar: " + e.getMessage())));
    }
}
