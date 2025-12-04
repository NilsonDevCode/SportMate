package com.nilson.appsportmate.features.user.ui.eventosPrivados.crearEventoPrivate;

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
 * ViewModel para creación de eventos privados del usuario.
 * Mismo estilo profesional que GestionDeportesAyuntamientoViewModel
 * pero adaptado totalmente a eventos de tipo PARTICULAR.
 */
public class CrearEventoUserPrivateViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Estado
    private String uidUsuario;

    // Señales UI
    private final MutableLiveData<String> toast = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToGestionEventos = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> navigateToLogin = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> clearForm = new MutableLiveData<>(false);

    // ---------------------------
    // Setters / Getters
    // ---------------------------

    public void setUidUsuario(String uid) {
        this.uidUsuario = uid;
    }

    public LiveData<String> getToast() { return toast; }
    public void consumeToast() { toast.setValue(null); }

    public LiveData<Boolean> getNavigateToGestionEventos() { return navigateToGestionEventos; }
    public void onNavigatedToGestionEventos() { navigateToGestionEventos.setValue(false); }

    public LiveData<Boolean> getNavigateToLogin() { return navigateToLogin; }
    public void onNavigatedToLogin() { navigateToLogin.setValue(false); }

    public LiveData<Boolean> getClearForm() { return clearForm; }
    public void onFormCleared() { clearForm.setValue(false); }

    // ---------------------------
    // LÓGICA PRINCIPAL
    // ---------------------------

    public void crearEventoParticular(String nombre,
                                      Integer cantidad,
                                      String fecha,
                                      String hora,
                                      String descripcion,
                                      String reglas,
                                      String materiales,
                                      String url) {

        if (isEmpty(nombre) || cantidad == null || cantidad < 1 ||
                isEmpty(fecha) || isEmpty(hora) ||
                isEmpty(descripcion) || isEmpty(reglas) ||
                isEmpty(materials(materiales)) || isEmpty(url)) {

            toast.postValue("Completa todos los campos");
            return;
        }

        if (isEmpty(uidUsuario)) {
            toast.postValue("Error: UID no encontrado.");
            navigateToLogin.postValue(true);
            return;
        }

        // Mapa del evento PARTICULAR
        Map<String, Object> evento = new HashMap<>();
        evento.put("nombre", nombre);
        evento.put("plazasDisponibles", cantidad);
        evento.put("fecha", fecha);
        evento.put("hora", hora);
        evento.put("descripcion", descripcion);
        evento.put("reglas", reglas);
        evento.put("materiales", materiales);
        evento.put("url", url);

        evento.put("uidCreador", uidUsuario);
        evento.put("tipo", "PARTICULAR"); // ← CLAVE IMPORTANTE
        evento.put("idDoc", generarDocId(nombre, fecha, hora));

        // RUTA EN FIRESTORE PARA USUARIOS PARTICULARES
        db.collection("eventos_user_private")
                .document(uidUsuario)
                .collection("lista")
                .document(evento.get("idDoc").toString())
                .set(evento)
                .addOnSuccessListener(unused -> {
                    toast.postValue("Evento creado correctamente.");
                    clearForm.postValue(true);
                    navigateToGestionEventos.postValue(true);
                })
                .addOnFailureListener(e ->
                        toast.postValue("Error al crear el evento: " + e.getMessage()));
    }

    // ---------------------------
    // Logout (si fuese necesario)
    // ---------------------------
    public void logout(Context ctx) {
        FirebaseAuth.getInstance().signOut();
        Preferencias.guardarRol(ctx, null);
        navigateToLogin.postValue(true);
    }

    // ---------------------------
    // Helpers
    // ---------------------------

    public static String generarDocId(String nombre, String fecha, String hora) {
        return nombre.replace(" ", "_")
                + "_" + fecha.replace("/", "_")
                + "_" + hora.replace(":", "_");
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String materials(String m) {
        return m == null ? "" : m.trim();
    }
}
