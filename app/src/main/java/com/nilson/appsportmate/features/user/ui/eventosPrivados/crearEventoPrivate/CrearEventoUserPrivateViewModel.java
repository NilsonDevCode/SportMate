package com.nilson.appsportmate.features.user.ui.eventosPrivados.crearEventoPrivate;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.nilson.appsportmate.common.utils.Preferencias;

import java.util.HashMap;
import java.util.Map;

public class CrearEventoUserPrivateViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String uidUsuario;
    private String puebloId;

    private final MutableLiveData<String> toast = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToGestionEventos = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> navigateToLogin = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> clearForm = new MutableLiveData<>(false);

    public void setUidUsuario(String uid) { this.uidUsuario = uid; }
    public void setPuebloId(String id) { this.puebloId = id; }

    public LiveData<String> getToast() { return toast; }
    public void consumeToast() { toast.setValue(null); }

    public LiveData<Boolean> getNavigateToGestionEventos() { return navigateToGestionEventos; }
    public void onNavigatedToGestionEventos() { navigateToGestionEventos.setValue(false); }

    public LiveData<Boolean> getNavigateToLogin() { return navigateToLogin; }
    public void onNavigatedToLogin() { navigateToLogin.setValue(false); }

    public LiveData<Boolean> getClearForm() { return clearForm; }
    public void onFormCleared() { clearForm.setValue(false); }

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
                isEmpty(materiales) || isEmpty(url)) {

            toast.postValue("Completa todos los campos");
            return;
        }

        if (isEmpty(uidUsuario)) {
            toast.postValue("Error: UID no encontrado.");
            navigateToLogin.postValue(true);
            return;
        }

        if (isEmpty(puebloId)) {
            toast.postValue("Error: pueblo no encontrado. Vuelve a iniciar sesión.");
            navigateToLogin.postValue(true);
            return;
        }

        // ============================
        // CREAR EVENTO
        // ============================

        String idDoc = generarDocId(nombre, fecha, hora);

        Map<String, Object> evento = new HashMap<>();
        evento.put("idDoc", idDoc);
        evento.put("nombre", nombre);
        evento.put("plazasDisponibles", cantidad);
        evento.put("fecha", fecha);
        evento.put("hora", hora);
        evento.put("descripcion", descripcion);
        evento.put("reglas", reglas);
        evento.put("materiales", materiales);
        evento.put("urlPueblo", url);

        evento.put("uidCreador", uidUsuario);
        evento.put("tipo", "PARTICULAR");
        evento.put("puebloId", puebloId);

        // ============================
        // GUARDAR EN DOS SITIOS
        // ============================

        WriteBatch batch = db.batch();

        DocumentReference refUser = db.collection("eventos_user_private")
                .document(uidUsuario)
                .collection("lista")
                .document(idDoc);

        DocumentReference refPueblo = db.collection("eventos_privados_por_pueblo")
                .document(puebloId)
                .collection("lista")
                .document(idDoc);

        batch.set(refUser, evento);
        batch.set(refPueblo, evento);

        batch.commit()
                .addOnSuccessListener(unused -> {
                    toast.postValue("Evento creado correctamente.");
                    clearForm.postValue(true);
                    navigateToGestionEventos.postValue(true);
                })
                .addOnFailureListener(e ->
                        toast.postValue("Error al crear el evento: " + e.getMessage()));
    }

    public void logout(Context ctx) {
        FirebaseAuth.getInstance().signOut();
        Preferencias.guardarRol(ctx, null);
        navigateToLogin.postValue(true);
    }

    public static String generarDocId(String nombre, String fecha, String hora) {
        return nombre.replace(" ", "_")
                + "_" + fecha.replace("/", "_")
                + "_" + hora.replace(":", "_");
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
