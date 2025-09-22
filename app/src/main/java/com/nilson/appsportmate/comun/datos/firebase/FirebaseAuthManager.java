package com.nilson.appsportmate.comun.datos.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;


//Gestiona login, registro y logout usando alias como ID.
public class FirebaseAuthManager {

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static Task<AuthResult> registrarUsuario(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    public static Task<AuthResult> iniciarSesion(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public static void cerrarSesion() {
        auth.signOut();
    }

    public static FirebaseUser getUsuarioActual() {
        return auth.getCurrentUser();
    }

    public static String getUidActual() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public static boolean estaAutenticado() {
        return auth.getCurrentUser() != null;
    }
}
