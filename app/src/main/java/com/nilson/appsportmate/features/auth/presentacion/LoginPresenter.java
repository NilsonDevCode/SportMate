package com.nilson.appsportmate.features.auth.presentacion;

import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.common.datos.firebase.FirestoreManager;
import com.nilson.appsportmate.common.utils.AuthAliasHelper;
import com.nilson.appsportmate.common.utils.Preferencias;

public class LoginPresenter {
    private final LoginView view;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final Context context; // Para usar en Preferencias

    public LoginPresenter(LoginView view, Context context) {
        this.view = view;
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public void onLoginClicked() {
        String aliasInput = view.getAliasInput();
        String password   = view.getPasswordInput();

        if (TextUtils.isEmpty(aliasInput)) {
            view.mostrarErrorAlias("Alias requerido");
            return;
        }
        String aliasErr = AuthAliasHelper.getAliasValidationError(aliasInput);
        if (aliasErr != null) {
            view.mostrarErrorAlias(aliasErr);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            view.mostrarErrorPassword("Contraseña requerida");
            return;
        }

        String emailSintetico = AuthAliasHelper.aliasToEmail(aliasInput);

        auth.signInWithEmailAndPassword(emailSintetico, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();

                    FirestoreManager.resolveRolOrRepair(uid, aliasInput, new FirestoreManager.RoleCallback() {
                        @Override public void onResolved(String rol) {
                            if (rol == null) {
                                view.mostrarMensaje("Perfil no encontrado");
                                return;
                            }
                            String r = rol.trim().toLowerCase();

                            // Guardar preferencias base
                            Preferencias.guardarUid(context, uid);
                            Preferencias.guardarAlias(context, aliasInput);
                            Preferencias.guardarRol(context, r);

                            if ("ayuntamiento".equals(r)) {
                                Preferencias.guardarAyuntamientoId(context, uid);
                                view.navegarAyuntamiento(uid);
                            } else {
                                db.collection("usuarios").document(uid).get()
                                        .addOnSuccessListener(doc -> {
                                            String aytoId = doc.getString("ayuntamientoId");
                                            if (aytoId == null || aytoId.isEmpty()) {
                                                view.mostrarMensaje("Tu perfil no tiene ayuntamiento asignado");
                                                return;
                                            }
                                            Preferencias.guardarAyuntamientoId(context, aytoId);
                                            view.navegarUsuario(aytoId);
                                        })
                                        .addOnFailureListener(e ->
                                                view.mostrarMensaje("Error leyendo perfil de usuario"));
                            }
                        }

                        @Override public void onError(Exception e) {
                            view.mostrarMensaje("Error perfil: " + e.getMessage());
                        }
                    });
                })
                .addOnFailureListener(e -> view.mostrarMensaje("Login fallido: " + e.getMessage()));
    }
}
