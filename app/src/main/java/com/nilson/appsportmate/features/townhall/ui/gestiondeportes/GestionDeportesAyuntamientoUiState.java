package com.nilson.appsportmate.features.townhall.ui.gestiondeportes;

public class GestionDeportesAyuntamientoUiState {
    public final boolean loading;
    public final String message; // info / éxito
    public final String error;   // error validación o guardado
    public final String ayuntamientoUid; // uid del admin (para info)

    public GestionDeportesAyuntamientoUiState(boolean loading, String message, String error, String ayuntamientoUid) {
        this.loading = loading;
        this.message = message;
        this.error = error;
        this.ayuntamientoUid = ayuntamientoUid;
    }

    public static GestionDeportesAyuntamientoUiState idle(String uid) {
        return new GestionDeportesAyuntamientoUiState(false, null, null, uid);
    }

    public static GestionDeportesAyuntamientoUiState loading(String uid) {
        return new GestionDeportesAyuntamientoUiState(true, null, null, uid);
    }

    public static GestionDeportesAyuntamientoUiState success(String uid, String msg) {
        return new GestionDeportesAyuntamientoUiState(false, msg, null, uid);
    }

    public static GestionDeportesAyuntamientoUiState error(String uid, String err) {
        return new GestionDeportesAyuntamientoUiState(false, null, err, uid);
    }
}
