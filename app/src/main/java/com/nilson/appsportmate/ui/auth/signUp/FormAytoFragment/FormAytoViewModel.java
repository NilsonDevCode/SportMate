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

    public FormAytoViewModel() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    private final MutableLiveData<String> eAlias = new MutableLiveData<>(null);
    private final MutableLiveData<String> ePassword = new MutableLiveData<>(null);
    private final MutableLiveData<String> eNombre = new MutableLiveData<>(null);
    private final MutableLiveData<String> eRazon = new MutableLiveData<>(null);
    private final MutableLiveData<String> message = new MutableLiveData<>(null);

    private final MutableLiveData<Boolean> navAyto = new MutableLiveData<>(false);

    public LiveData<String> getEAlias() { return eAlias; }
    public LiveData<String> getEPassword() { return ePassword; }
    public LiveData<String> getENombre() { return eNombre; }
    public LiveData<String> getERazon() { return eRazon; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getNavAyto() { return navAyto; }

    public void onRegisterClicked(
            Context ctx,
            String aliasInput,
            String pass1,
            String pass2,
            String nombre,
            String apellidosIgnorado,
            String comunidadNom,
            String provinciaNom,
            String ciudadNom,
            String puebloCreado,
            String razonSocial,
            String rolFijo,
            String ignorado,
            String comunidadId,
            String provinciaId,
            String ciudadId
    ) {
        eAlias.setValue(null);
        ePassword.setValue(null);
        eNombre.setValue(null);
        eRazon.setValue(null);

        String errAlias = AuthAliasHelper.getAliasValidationError(aliasInput);
        if (errAlias != null) { eAlias.setValue(errAlias); return; }

        if (pass1.isEmpty()) { ePassword.setValue("Contraseña requerida"); return; }
        if (pass1.length() < 6) { ePassword.setValue("Mínimo 6 caracteres"); return; }
        if (!pass1.equals(pass2)) { ePassword.setValue("Las contraseñas no coinciden"); return; }

        if (nombre.isEmpty()) { eNombre.setValue("Nombre requerido"); return; }
        if (razonSocial.isEmpty()) { eRazon.setValue("Razón social requerida"); return; }
        if (puebloCreado.isEmpty()) { message.setValue("Debes crear un pueblo"); return; }

        String email = AuthAliasHelper.aliasToEmail(aliasInput);

        auth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(res -> {
                    boolean existe = res.getSignInMethods() != null && !res.getSignInMethods().isEmpty();
                    if (existe) {
                        eAlias.setValue("Alias ya está en uso");
                        return;
                    }
                    crearAyuntamiento(
                            ctx, email, pass1, aliasInput, nombre,
                            razonSocial, comunidadNom, provinciaNom, ciudadNom,
                            puebloCreado, comunidadId, provinciaId, ciudadId
                    );
                })
                .addOnFailureListener(ex -> message.setValue("Error: " + ex.getMessage()));
    }

    private void crearAyuntamiento(
            Context ctx,
            String email,
            String pass,
            String alias,
            String nombreAyto,
            String razonSocial,
            String comunidadNom,
            String provinciaNom,
            String ciudadNom,
            String puebloCreado,
            String comunidadId,
            String provinciaId,
            String ciudadId
    ) {
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(res -> {
                    String uid = res.getUser().getUid();
                    String id = UUID.randomUUID().toString();

                    Map<String, Object> docAyto = new HashMap<>();
                    docAyto.put("id", id);
                    docAyto.put("uid", uid);
                    docAyto.put("alias", alias);
                    docAyto.put("nombre", nombreAyto);
                    docAyto.put("razonSocial", razonSocial);
                    docAyto.put("rol", "ayuntamiento");
                    docAyto.put("comunidad", comunidadNom);
                    docAyto.put("provincia", provinciaNom);
                    docAyto.put("ciudad", ciudadNom);

                    WriteBatch batch = db.batch();
                    batch.set(db.collection("ayuntamientos").document(uid), docAyto, SetOptions.merge());

                    Map<String, Object> authDoc = new HashMap<>();
                    authDoc.put("alias", alias);
                    authDoc.put("uid", uid);
                    authDoc.put("rol", "ayuntamiento");
                    batch.set(db.collection("usuariosAuth").document(uid), authDoc, SetOptions.merge());

                    batch.commit().addOnSuccessListener(unused -> {
                        crearPueblo(uid, puebloCreado, comunidadId, provinciaId, ciudadId,
                                comunidadNom, provinciaNom, ciudadNom, nombreAyto);

                        Preferencias.guardarUid(ctx, uid);
                        Preferencias.guardarAlias(ctx, alias);
                        Preferencias.guardarRol(ctx, "ayuntamiento");
                        Preferencias.guardarAyuntamientoId(ctx, uid);

                        navAyto.setValue(true);
                        message.setValue("Registro exitoso.");
                    });
                })
                .addOnFailureListener(e -> message.setValue("Error creando usuario: " + e.getMessage()));
    }

    private void crearPueblo(
            String uid,
            String puebloCreado,
            String comunidadId,
            String provinciaId,
            String ciudadId,
            String comunidadNom,
            String provinciaNom,
            String ciudadNom,
            String nombreAyto
    ) {
        Map<String, Object> pueblo = new HashMap<>();
        pueblo.put("nombre", puebloCreado);
        pueblo.put("comunidadId", comunidadId);
        pueblo.put("provinciaId", provinciaId);
        pueblo.put("ciudadId", ciudadId);
        pueblo.put("createdByUid", uid);
        pueblo.put("createdAt", FieldValue.serverTimestamp());
        pueblo.put("comunidadNombre", comunidadNom);
        pueblo.put("provinciaNombre", provinciaNom);
        pueblo.put("ciudadNombre", ciudadNom);
        pueblo.put("ayuntamientoId", uid);
        pueblo.put("ayuntamientoNombre", nombreAyto);

        db.collection("pueblos").add(pueblo);
    }

    public void consumeMessage() { message.setValue(null); }
    public void consumeNavAyto() { navAyto.setValue(false); }
}
