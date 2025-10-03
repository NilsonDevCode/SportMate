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
        this.deportes = deportes == null ? new ArrayList<>() : deportes;
    }

    public static InicioUiState loading() {
        return new InicioUiState(true, null, null, new ArrayList<>());
    }

    public static InicioUiState success(List<DeporteUi> d) {
        return new InicioUiState(false, null, null, d);
    }

    public static InicioUiState error(String e) {
        return new InicioUiState(false, null, e, new ArrayList<>());
    }

    // DTO para cada deporte
    public static class DeporteUi {
        public final String nombreDeporte;
        public final String fecha;
        public final String hora;
        public final String ayuntamiento;

        public DeporteUi(String nombreDeporte, String fecha, String hora, String ayuntamiento) {
            this.nombreDeporte = nombreDeporte;
            this.fecha = fecha;
            this.hora = hora;
            this.ayuntamiento = ayuntamiento;
        }
    }
}
