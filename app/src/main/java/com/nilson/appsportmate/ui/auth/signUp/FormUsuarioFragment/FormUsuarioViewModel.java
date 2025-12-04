package com.nilson.appsportmate.ui.auth.signUp.FormUsuarioFragment;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.nilson.appsportmate.common.utils.AuthAliasHelper;
import com.nilson.appsportmate.common.utils.Preferencias;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FormUsuarioViewModel extends ViewModel {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public FormUsuarioViewModel() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    private final MutableLiveData<String> eAlias = new MutableLiveData<>(null);
    private final MutableLiveData<String> ePassword = new MutableLiveData<>(null);
    private final MutableLiveData<String> eNombre = new MutableLiveData<>(null);
    private final MutableLiveData<String> eApellidos = new MutableLiveData<>(null);
    private final MutableLiveData<String> message = new MutableLiveData<>(null);

    private final MutableLiveData<Boolean> navUser = new MutableLiveData<>(false);

    public LiveData<String> getEAlias() { return eAlias; }
    public LiveData<String> getEPassword() { return ePassword; }
    public LiveData<String> getENombre() { return eNombre; }
    public LiveData<String> getEApellidos() { return eApellidos; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getNavUser() { return navUser; }

    public void onRegisterClicked(
            Context ctx,
            String aliasInput,
            String pass1,
            String pass2,
            String nombre,
            String apellidos,
            String comunidadNom,
            String provinciaNom,
            String ciudadNom,
            String puebloNom,     // nombre visible seleccionado
            String puebloId,      // ⬅️ ANTES "ign" – AHORA ID REAL DEL PUEBLO
            String rol,
            String ayuntamientoId,
            String comunidadId,
            String provinciaId,
            String ciudadId
    ) {
        eAlias.setValue(null);
        ePassword.setValue(null);
        eNombre.setValue(null);
        eApellidos.setValue(null);

        String aliasErr = AuthAliasHelper.getAliasValidationError(aliasInput);
        if (aliasErr != null) { eAlias.setValue(aliasErr); return; }

        if (pass1.isEmpty()) { ePassword.setValue("Contraseña requerida"); return; }
        if (pass1.length() < 6) { ePassword.setValue("Mínimo 6 caracteres"); return; }
        if (!pass1.equals(pass2)) { ePassword.setValue("Las contraseñas no coinciden"); return; }

        if (nombre.isEmpty()) { eNombre.setValue("Nombre requerido"); return; }
        if (apellidos.isEmpty()) { eApellidos.setValue("Apellidos requeridos"); return; }
        if (puebloNom.isEmpty()) { message.setValue("Selecciona un pueblo"); return; }

        // PuebloId real obligatorio para poder filtrar eventos por pueblo
        if (puebloId == null || puebloId.trim().isEmpty()) {
            message.setValue("Error interno: pueblo no válido. Vuelve a seleccionar el pueblo.");
            return;
        }

        if (ayuntamientoId == null || ayuntamientoId.isEmpty()) {
            message.setValue("No se pudo obtener el ayuntamiento del pueblo");
            return;
        }

        String email = AuthAliasHelper.aliasToEmail(aliasInput);

        auth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener((SignInMethodQueryResult r) -> {
                    boolean existe = r.getSignInMethods() != null && !r.getSignInMethods().isEmpty();
                    if (existe) {
                        eAlias.setValue("Alias ya en uso");
                        return;
                    }
                    crear(ctx, email, pass1, aliasInput, nombre, apellidos,
                            comunidadNom, provinciaNom, ciudadNom, puebloNom,
                            puebloId, ayuntamientoId);
                })
                .addOnFailureListener(e -> message.setValue("Error: " + e.getMessage()));
    }

    private void crear(
            Context ctx,
            String email,
            String pass,
            String alias,
            String nombre,
            String apellidos,
            String comunidadNom,
            String provinciaNom,
            String ciudadNom,
            String puebloNom,
            String puebloId,
            String ayuntamientoId
    ) {
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(res -> {
                    String uid = res.getUser().getUid();
                    String id = UUID.randomUUID().toString();

                    Map<String, Object> perfil = new HashMap<>();
                    perfil.put("id", id);
                    perfil.put("uid", uid);
                    perfil.put("alias", alias);
                    perfil.put("rol", "usuario");
                    perfil.put("nombre", nombre);
                    perfil.put("apellidos", apellidos);
                    perfil.put("comunidad", comunidadNom);
                    perfil.put("provincia", provinciaNom);
                    perfil.put("ciudad", ciudadNom);
                    perfil.put("pueblo", puebloNom);
                    perfil.put("puebloId", puebloId);          // ⬅️ ID REAL DEL PUEBLO
                    perfil.put("ayuntamientoId", ayuntamientoId);

                    Map<String, Object> authDoc = new HashMap<>();
                    authDoc.put("alias", alias);
                    authDoc.put("uid", uid);
                    authDoc.put("rol", "usuario");

                    WriteBatch batch = db.batch();

                    batch.set(db.collection("usuarios").document(uid), perfil, SetOptions.merge());
                    batch.set(db.collection("usuariosAuth").document(uid), authDoc, SetOptions.merge());

                    batch.commit().addOnSuccessListener(unused -> {
                        // Guardamos todo lo necesario para eventos privados
                        Preferencias.guardarUid(ctx, uid);
                        Preferencias.guardarAlias(ctx, alias);
                        Preferencias.guardarRol(ctx, "usuario");
                        Preferencias.guardarAyuntamientoId(ctx, ayuntamientoId);

                        // Clave: guardamos el ID REAL del pueblo + nombre
                        Preferencias.guardarPuebloId(ctx, puebloId);
                        Preferencias.guardarPuebloNombre(ctx, puebloNom);

                        navUser.setValue(true);
                        message.setValue("Registro exitoso.");
                    });
                })
                .addOnFailureListener(e -> message.setValue("Error creando usuario: " + e.getMessage()));
    }

    public void consumeMessage() { message.setValue(null); }
    public void consumeNavUser() { navUser.setValue(false); }
}
