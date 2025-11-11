package com.nilson.appsportmate.ui.auth.login;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.common.datos.firebase.FirestoreManager;
import com.nilson.appsportmate.common.utils.AuthAliasHelper;
import com.nilson.appsportmate.common.utils.Preferencias;

public class LoginViewModel extends ViewModel {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    // 🧠 Constructor testable (para tests unitarios con mocks)
    public LoginViewModel(FirebaseAuth auth, FirebaseFirestore db) {
        this.auth = auth;
        this.db = db;
    }

    // ⚙️ Constructor normal (para uso real en la app)
    public LoginViewModel() {
        this(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance());
    }

    private final MutableLiveData<String> errorAlias = new MutableLiveData<>(null);
    private final MutableLiveData<String> errorPassword = new MutableLiveData<>(null);
    private final MutableLiveData<String> message = new MutableLiveData<>(null);

    private final MutableLiveData<String> navTownhall = new MutableLiveData<>(null);
    private final MutableLiveData<String> navUser = new MutableLiveData<>(null);

    public LiveData<String> getErrorAlias() { return errorAlias; }
    public LiveData<String> getErrorPassword() { return errorPassword; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<String> getNavTownhall() { return navTownhall; }
    public LiveData<String> getNavUser() { return navUser; }

    public void onLoginClicked(String aliasInput, String password, Context appContext) {
        if (aliasInput == null || aliasInput.trim().isEmpty()) {
            errorAlias.setValue("Alias requerido");
            return;
        }
        String aliasErr = AuthAliasHelper.getAliasValidationError(aliasInput);
        if (aliasErr != null) {
            errorAlias.setValue(aliasErr);
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            errorPassword.setValue("Contraseña requerida");
            return;
        }

        errorAlias.setValue(null);
        errorPassword.setValue(null);

        String emailSintetico = AuthAliasHelper.aliasToEmail(aliasInput.trim());

        auth.signInWithEmailAndPassword(emailSintetico, password)
                .addOnSuccessListener(result -> {
                    if (result.getUser() == null) {
                        message.setValue("No se pudo obtener el usuario");
                        return;
                    }
                    String uid = result.getUser().getUid();

                    FirestoreManager.resolveRolOrRepair(uid, aliasInput.trim(), new FirestoreManager.RoleCallback() {
                        @Override
                        public void onResolved(String rol) {
                            if (rol == null) {
                                message.setValue("Perfil no encontrado");
                                return;
                            }
                            String r = rol.trim().toLowerCase();

                            Preferencias.guardarUid(appContext, uid);
                            Preferencias.guardarAlias(appContext, aliasInput.trim());
                            Preferencias.guardarRol(appContext, r);

                            if ("ayuntamiento".equals(r)) {
                                Preferencias.guardarAyuntamientoId(appContext, uid);
                                navTownhall.setValue(uid);
                            } else {
                                db.collection("usuarios").document(uid).get()
                                        .addOnSuccessListener(doc -> {
                                            String aytoId = doc.getString("ayuntamientoId");
                                            if (aytoId == null || aytoId.isEmpty()) {
                                                message.setValue("Tu perfil no tiene ayuntamiento asignado");
                                            } else {
                                                Preferencias.guardarAyuntamientoId(appContext, aytoId);
                                                navUser.setValue(aytoId);
                                            }
                                        })
                                        .addOnFailureListener(e -> message.setValue("Error leyendo perfil de usuario"));
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            message.setValue("Error perfil: " + e.getMessage());
                        }
                    });
                })
                .addOnFailureListener(e -> message.setValue("Login fallido: " + e.getMessage()));
    }

    public void consumeNavTownhall() { navTownhall.setValue(null); }
    public void consumeNavUser() { navUser.setValue(null); }
    public void consumeMessage() { message.setValue(null); }
}
