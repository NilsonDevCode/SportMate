package com.nilson.appsportmate.features.user.ui.menuPrincipal;

import java.util.ArrayList;
import java.util.List;

public class InicioUiState {
    public final boolean loading;
    public final String message;
    public final String error;
    public final List<DeporteUi> deportes;

    public InicioUiState(boolean loading, String message, String error, List<DeporteUi> deportes) {
        this.loading = loading;
        this.message = message;
        this.error = error;
        this.deportes = (deportes == null) ? new ArrayList<>() : deportes;
    }

    /* ---------- Fábricas ---------- */

    public static InicioUiState loading() {
        return new InicioUiState(true, null, null, new ArrayList<>());
    }

    public static InicioUiState success(List<DeporteUi> d) {
        return new InicioUiState(false, null, null, d);
    }

    public static InicioUiState error(String e) {
        return new InicioUiState(false, null, e, new ArrayList<>());
    }

    /* ---------- Helpers de mensaje ---------- */

    /** Devuelve un nuevo estado con mensaje (y sin error). */
    public InicioUiState withMessage(String msg) {
        return new InicioUiState(false, msg, null, this.deportes);
    }

    /** Limpia el mensaje actual manteniendo el resto del estado. */
    public InicioUiState clearMessage() {
        return new InicioUiState(this.loading, null, this.error, this.deportes);
    }

    /* ---------- DTO para cada deporte ---------- */
    public static class DeporteUi {
        public final String nombreDeporte, fecha, hora, ayuntamiento;
        public final String docId, aytoId; // nuevos

        public DeporteUi(String n, String f, String h, String a, String docId, String aytoId) {
            this.nombreDeporte = n;
            this.fecha = f;
            this.hora = h;
            this.ayuntamiento = a;
            this.docId = docId;
            this.aytoId = aytoId;
        }
    }
}
