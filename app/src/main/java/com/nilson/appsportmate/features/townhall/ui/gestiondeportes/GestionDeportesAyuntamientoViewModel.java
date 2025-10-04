package com.nilson.appsportmate.features.townhall.ui.gestiondeportes;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.common.utils.Preferencias;

import java.util.HashMap;
import java.util.Map;

/**
 * Lógica de creación de evento (MVVM).
 * Keys/colecciones iguales a tu implementación previa.
 */
public class GestionDeportesAyuntamientoViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Estado base
    private String ayuntamientoId;

    // Señales UI
    private final MutableLiveData<String>  toast = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToLogin = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> navigateToGestionEventos = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> clearForm = new MutableLiveData<>(false); // NUEVO

    // ---------------------------
    // Exposición
    // ---------------------------

    public void setAyuntamientoId(String id) { this.ayuntamientoId = id; }

    public LiveData<String>  getToast()                 { return toast; }
    public void consumeToast() { toast.setValue(null); }

    public LiveData<Boolean> getNavigateToLogin()       { return navigateToLogin; }
    public LiveData<Boolean> getNavigateToGestionEventos() { return navigateToGestionEventos; }
    public LiveData<Boolean> getClearForm()             { return clearForm; } // NUEVO
    public void onFormCleared()                         { clearForm.setValue(false); } // NUEVO

    // ---------------------------
    // Acciones
    // ---------------------------

    public void crearDeporte(String nombre,
                             Integer cantidad,
                             String fecha,
                             String hora,
                             String descripcion,
                             String reglas,
                             String materiales,
                             String urlPueblo) {

        if (isEmpty(nombre) || cantidad == null || cantidad < 1 ||
                isEmpty(fecha) || isEmpty(hora) || isEmpty(descripcion) ||
                isEmpty(reglas) || isEmpty(materiales) || isEmpty(urlPueblo)) {
            toast.postValue("Completa todos los campos");
            return;
        }

        if (isEmpty(ayuntamientoId)) {
            toast.postValue("Error: ayuntamiento_id no encontrado.");
            navigateToLogin.postValue(true);
            return;
        }

        Map<String, Object> deporte = new HashMap<>();
        deporte.put("nombre", nombre);
        deporte.put("plazasDisponibles", cantidad);
        deporte.put("fecha", fecha);
        deporte.put("hora", hora);
        deporte.put("descripcion", descripcion);
        deporte.put("reglas", reglas);
        // claves reales que consume tu adapter
        deporte.put("materiales", materiales);
        deporte.put("urlPueblo", urlPueblo);
        deporte.put("ayuntamientoId", ayuntamientoId);

        String docId = generarDocId(nombre, fecha, hora);

        db.collection("deportes_ayuntamiento")
                .document(ayuntamientoId)
                .collection("lista")
                .document(docId)
                .set(deporte)
                .addOnSuccessListener(unused -> {
                    toast.postValue("Deporte creado correctamente."); // CAMBIADO
                    clearForm.postValue(true);                         // NUEVO: limpiar
                    navigateToGestionEventos.postValue(true);          // luego navegar
                })
                .addOnFailureListener(e ->
                        toast.postValue("Error al crear el evento: " + e.getMessage()));
    }

    public void logout(Context ctx) {
        FirebaseAuth.getInstance().signOut();
        Preferencias.guardarRol(ctx, null);
        navigateToLogin.postValue(true);
    }

    public void onNavigatedToLogin()            { navigateToLogin.setValue(false); }
    public void onNavigatedToGestionEventos()   { navigateToGestionEventos.setValue(false); }

    // ---------------------------
    // Helpers
    // ---------------------------

    public static String generarDocId(String nombre, String fecha, String hora) {
        return nombre.replace(" ", "_") + "_" + fecha.replace("/", "_") + "_" + hora.replace(":", "_");
    }

    private boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }
}
