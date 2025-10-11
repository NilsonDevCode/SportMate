package com.nilson.appsportmate.features.user.ui.deportesdisponibles;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeportesDisponiblesUiState {
    public final boolean loading;
    public final List<Map<String, Object>> disponibles;
    public final List<Map<String, Object>> mis;
    public final String message; // para toasts/avisos puntuales (consumir y limpiar)
    public final boolean actionInProgress; // para bloquear doble tap durante transacción

    private DeportesDisponiblesUiState(boolean loading,
                                       List<Map<String, Object>> disponibles,
                                       List<Map<String, Object>> mis,
                                       String message,
                                       boolean actionInProgress) {
        this.loading = loading;
        this.disponibles = disponibles;
        this.mis = mis;
        this.message = message;
        this.actionInProgress = actionInProgress;
    }

    public static DeportesDisponiblesUiState loading() {
        return new DeportesDisponiblesUiState(true, Collections.emptyList(), Collections.emptyList(), null, false);
    }

    public static DeportesDisponiblesUiState success(List<Map<String, Object>> disponibles,
                                                     List<Map<String, Object>> mis) {
        return new DeportesDisponiblesUiState(false, disponibles, mis, null, false);
    }

    public static DeportesDisponiblesUiState message(DeportesDisponiblesUiState prev, String msg) {
        return new DeportesDisponiblesUiState(prev.loading, prev.disponibles, prev.mis, msg, prev.actionInProgress);
    }

    public static DeportesDisponiblesUiState withAction(DeportesDisponiblesUiState prev, boolean inProgress) {
        return new DeportesDisponiblesUiState(prev.loading, prev.disponibles, prev.mis, prev.message, inProgress);
    }

    public DeportesDisponiblesUiState clearMessage() {
        return new DeportesDisponiblesUiState(this.loading, this.disponibles, this.mis, null, this.actionInProgress);
    }
}
