package com.nilson.appsportmate.features.townhall.ui.formularioayuntamiento;

public class FormularioAyuntamientoUiState {
    public final boolean loading;
    public final String message;   // info/ok/error text
    public final String error;     // validation or save error
    public final String uid;       // UID mostrado en el campo (solo lectura)

    public FormularioAyuntamientoUiState(boolean loading, String message, String error, String uid) {
        this.loading = loading;
        this.message = message;
        this.error = error;
        this.uid = uid;
    }

    public static FormularioAyuntamientoUiState idle(String uid) {
        return new FormularioAyuntamientoUiState(false, null, null, uid);
    }

    public static FormularioAyuntamientoUiState loading(String uid) {
        return new FormularioAyuntamientoUiState(true, null, null, uid);
    }

    public static FormularioAyuntamientoUiState success(String uid, String msg) {
        return new FormularioAyuntamientoUiState(false, msg, null, uid);
    }

    public static FormularioAyuntamientoUiState error(String uid, String err) {
        return new FormularioAyuntamientoUiState(false, null, err, uid);
    }
}
