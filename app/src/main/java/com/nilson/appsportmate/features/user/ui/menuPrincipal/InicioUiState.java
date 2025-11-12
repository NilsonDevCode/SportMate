package com.nilson.appsportmate.features.user.ui.menuPrincipal;

import java.util.ArrayList;
import java.util.List;

public class InicioUiState {
    public final boolean loading;
    public final String error;
    public final String message;
    public final List<DeporteUi> deportes;

    public InicioUiState(boolean loading, String error, String message, List<DeporteUi> deportes) {
        this.loading = loading;
        this.error = error;
        this.message = message;
        this.deportes = deportes == null ? new ArrayList<>() : deportes;
    }

    public static InicioUiState loading() { return new InicioUiState(true, null, null, null); }
    public static InicioUiState success(List<DeporteUi> items) { return new InicioUiState(false, null, null, items); }
    public static InicioUiState error(String msg) { return new InicioUiState(false, msg, null, null); }

    public InicioUiState withMessage(String msg) {
        return new InicioUiState(this.loading, this.error, msg, this.deportes);
    }

    public static class DeporteUi {
        public String docId;
        public String aytoId;

        public String nombreDeporte;
        public String descripcion;
        public String fecha;   // dd/MM/yyyy
        public String hora;    // HH:mm
        public String lugar;

        public int plazasMax;
        public int inscritos;

        public String ayuntamiento; // nombre visible

        public DeporteUi() {}

        public DeporteUi(String nombreDeporte, String fecha, String hora, String ayuntamiento,
                         String docId, String aytoId) {
            this.nombreDeporte = nombreDeporte;
            this.fecha = fecha;
            this.hora = hora;
            this.ayuntamiento = ayuntamiento;
            this.docId = docId;
            this.aytoId = aytoId;
        }
    }
}
