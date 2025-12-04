package com.nilson.appsportmate.features.user.ui.eventosPrivados.crearEventoPrivate;

public class CrearEventoUserPrivateUiState {

    public final boolean loading;
    public final String message;   // mensaje de éxito / info
    public final String error;     // validaciones o errores de guardado
    public final String usuarioUid; // uid del usuario creador

    public CrearEventoUserPrivateUiState(boolean loading,
                                         String message,
                                         String error,
                                         String usuarioUid) {

        this.loading = loading;
        this.message = message;
        this.error = error;
        this.usuarioUid = usuarioUid;
    }

    public static CrearEventoUserPrivateUiState idle(String uid) {
        return new CrearEventoUserPrivateUiState(false, null, null, uid);
    }

    public static CrearEventoUserPrivateUiState loading(String uid) {
        return new CrearEventoUserPrivateUiState(true, null, null, uid);
    }

    public static CrearEventoUserPrivateUiState success(String uid, String msg) {
        return new CrearEventoUserPrivateUiState(false, msg, null, uid);
    }

    public static CrearEventoUserPrivateUiState error(String uid, String err) {
        return new CrearEventoUserPrivateUiState(false, null, err, uid);
    }
}
