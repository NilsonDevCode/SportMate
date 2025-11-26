package com.nilson.appsportmate.ui.auth.signUp.FormAytoFragment;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.nilson.appsportmate.common.utils.AuthAliasHelper;
import com.nilson.appsportmate.common.utils.Preferencias;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FormAytoViewModel extends ViewModel {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    // 🔧 Constructor principal (uso normal en la app)
    public FormAytoViewModel() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    // 🔧 Constructor alternativo para tests (sin Firebase real)
    public FormAytoViewModel(FirebaseAuth auth, FirebaseFirestore db) {
        this.auth = auth;
        this.db = db;
    }

    private final MutableLiveData<String> eAlias = new MutableLiveData<>(null);
    private final MutableLiveData<String> ePassword = new MutableLiveData<>(null);
    private final MutableLiveData<String> eNombre = new MutableLiveData<>(null);
    private final MutableLiveData<String> eApellidos = new MutableLiveData<>(null);
    private final MutableLiveData<String> eRazon = new MutableLiveData<>(null);
    private final MutableLiveData<String> message = new MutableLiveData<>(null);

    private final MutableLiveData<Boolean> navAyto = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> navUser = new MutableLiveData<>(false);

    public LiveData<String> getEAlias() { return eAlias; }
    public LiveData<String> getEPassword() { return ePassword; }
    public LiveData<String> getENombre() { return eNombre; }
    public LiveData<String> getEApellidos() { return eApellidos; }
    public LiveData<String> getERazon() { return eRazon; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getNavAyto() { return navAyto; }
    public LiveData<Boolean> getNavUser() { return navUser; }

    public void onRegisterClicked(
            Context appContext,
            String aliasInput,
            String pass1,
            String pass2,
            String nombre,
            String apellidos,
            String comunidadNombre,
            String provinciaNombre,
            String ciudadNombre,
            String puebloNombre,
            String razonSocial,
            String rol, // "usuario" | "ayuntamiento"
            String ayuntamientoSeleccionadoId,
            String comunidadIdSel,
            String provinciaIdSel,
            String ciudadIdSel
    ) {
        // Reset errores
        eAlias.setValue(null); ePassword.setValue(null); eNombre.setValue(null);
        eApellidos.setValue(null); eRazon.setValue(null);

        String aliasErr = AuthAliasHelper.getAliasValidationError(aliasInput);
        if (aliasErr != null) { eAlias.setValue(aliasErr); return; }
        if (pass1 == null || pass1.isEmpty()) { ePassword.setValue("Contraseña requerida"); return; }
        if (pass1.length() < 6) { ePassword.setValue("Mínimo 6 caracteres"); return; }
        if (!pass1.equals(pass2)) { ePassword.setValue("Las contraseñas no coinciden"); return; }
        if (nombre == null || nombre.trim().isEmpty()) { eNombre.setValue("Nombre requerido"); return; }

        if ("usuario".equals(rol)) {
            if (apellidos == null || apellidos.trim().isEmpty()) { eApellidos.setValue("Apellidos requeridos"); return; }
            if (puebloNombre == null || puebloNombre.trim().isEmpty()) { message.setValue("Selecciona un pueblo"); return; }
            if (ayuntamientoSeleccionadoId == null || ayuntamientoSeleccionadoId.trim().isEmpty()) {
                message.setValue("No se pudo resolver el ayuntamiento del pueblo seleccionado");
                return;
            }
        } else {
            if (razonSocial == null || razonSocial.trim().isEmpty()) { eRazon.setValue("Razón social requerida"); return; }
        }

        String emailSintetico = AuthAliasHelper.aliasToEmail(aliasInput.trim());

        auth.fetchSignInMethodsForEmail(emailSintetico)
                .addOnSuccessListener((SignInMethodQueryResult r) -> {
                    boolean existe = r.getSignInMethods() != null && !r.getSignInMethods().isEmpty();
                    if (existe) { eAlias.setValue("Alias en uso. Inicia sesión o elige otro."); return; }
                    crearUsuario(
                            appContext,
                            emailSintetico, pass1, aliasInput.trim(), rol,
                            nombre, apellidos, comunidadNombre, provinciaNombre, ciudadNombre,
                            puebloNombre, razonSocial,
                            ayuntamientoSeleccionadoId, comunidadIdSel, provinciaIdSel, ciudadIdSel
                    );
                })
                .addOnFailureListener(e -> message.setValue("Error comprobando alias: " + e.getMessage()));
    }

    private void crearUsuario(
            Context appContext,
            String email, String pass1, String aliasInput, String rol,
            String nombre, String apellidos, String comunidadNombre, String provinciaNombre,
            String ciudadNombre, String puebloNombre, String razonSocial,
            String ayuntamientoSeleccionadoId,
            String comunidadIdSel, String provinciaIdSel, String ciudadIdSel
    ) {
        auth.createUserWithEmailAndPassword(email, pass1)
                .addOnSuccessListener(result -> {
                    if (result.getUser() == null) { message.setValue("Sin UID"); return; }
                    String uid = result.getUser().getUid();
                    String id = UUID.randomUUID().toString();

                    Map<String, Object> perfil = new HashMap<>();
                    perfil.put("id", id);
                    perfil.put("uid", uid);
                    perfil.put("alias", aliasInput);
                    perfil.put("rol", rol);
                    perfil.put("nombre", nombre);
                    perfil.put("comunidad", comunidadNombre);
                    perfil.put("provincia", provinciaNombre);
                    perfil.put("ciudad", ciudadNombre);
                    perfil.put("pueblo", puebloNombre);

                    Map<String, Object> authDoc = new HashMap<>();
                    authDoc.put("alias", aliasInput);
                    authDoc.put("uid", uid);
                    authDoc.put("rol", rol);

                    WriteBatch batch1 = db.batch();

                    if ("ayuntamiento".equals(rol)) {
                        Map<String, Object> perfilAyto = new HashMap<>(perfil);
                        perfilAyto.put("razonSocial", razonSocial);
                        perfilAyto.put("ayuntamientoId", uid);
                        batch1.set(db.collection("ayuntamientos").document(uid), perfilAyto, SetOptions.merge());
                    } else {
                        Map<String, Object> perfilUsuario = new HashMap<>(perfil);
                        perfilUsuario.put("apellidos", apellidos);
                        perfilUsuario.put("ayuntamientoId", ayuntamientoSeleccionadoId);
                        batch1.set(db.collection("usuarios").document(uid), perfilUsuario, SetOptions.merge());
                    }

                    batch1.set(db.collection("usuariosAuth").document(uid), authDoc, SetOptions.merge());

                    batch1.commit()
                            .addOnSuccessListener(unused -> {
                                if ("ayuntamiento".equals(rol)
                                        && puebloNombre != null && !puebloNombre.trim().isEmpty()
                                        && comunidadIdSel != null && !comunidadIdSel.isEmpty()
                                        && provinciaIdSel != null && !provinciaIdSel.isEmpty()
                                        && ciudadIdSel != null && !ciudadIdSel.isEmpty()) {

                                    Map<String, Object> puebloDoc = new HashMap<>();
                                    puebloDoc.put("nombre", puebloNombre.trim());
                                    puebloDoc.put("comunidadId", comunidadIdSel);
                                    puebloDoc.put("provinciaId", provinciaIdSel);
                                    puebloDoc.put("ciudadId", ciudadIdSel);
                                    puebloDoc.put("createdByUid", uid);
                                    puebloDoc.put("createdAt", FieldValue.serverTimestamp());
                                    puebloDoc.put("comunidadNombre", comunidadNombre);
                                    puebloDoc.put("provinciaNombre", provinciaNombre);
                                    puebloDoc.put("ciudadNombre", ciudadNombre);
                                    puebloDoc.put("ayuntamientoId", uid);
                                    puebloDoc.put("ayuntamientoNombre", nombre);

                                    db.collection("pueblos").add(puebloDoc)
                                            .addOnFailureListener(e -> message.setValue("Perfil OK pero falló crear pueblo: " + e.getMessage()));
                                }

                                Preferencias.guardarUid(appContext, uid);
                                Preferencias.guardarAlias(appContext, aliasInput);
                                Preferencias.guardarRol(appContext, rol);
                                if ("ayuntamiento".equals(rol)) {
                                    Preferencias.guardarAyuntamientoId(appContext, uid);
                                    navAyto.setValue(true);
                                } else {
                                    Preferencias.guardarAyuntamientoId(appContext, ayuntamientoSeleccionadoId);
                                    navUser.setValue(true);
                                }
                                message.setValue("Registro exitoso.");
                            })
                            .addOnFailureListener(e -> message.setValue("Error guardando perfil: " + e.getMessage()));
                })
                .addOnFailureListener(e -> message.setValue("Registro fallido: " + e.getMessage()));
    }

    public void consumeMessage() { message.setValue(null); }
    public void consumeNavAyto() { navAyto.setValue(false); }
    public void consumeNavUser() { navUser.setValue(false); }
}
