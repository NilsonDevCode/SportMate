package com.nilson.appsportmate.common.utilidades;

import android.text.TextUtils;

public class Validaciones {

    public static boolean esTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    public static boolean esEmailValido(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean esPasswordValida(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean camposLlenos(String... campos) {
        for (String campo : campos) {
            if (TextUtils.isEmpty(campo)) return false;
        }
        return true;
    }
}
