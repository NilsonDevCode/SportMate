package com.nilson.appsportmate.features.townhall.ui.gestioneventos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GestionEventosMasPlazasUiState {
    public final boolean loading;
    public final String error; // null si no hay error
    public final List<Map<String, Object>> eventos; // cada Map lleva al menos "idDoc" y los campos del evento

    private GestionEventosMasPlazasUiState(boolean loading, String error, List<Map<String, Object>> eventos) {
        this.loading = loading;
        this.error = error;
        this.eventos = eventos == null ? new ArrayList<>() : eventos;
    }

    public static GestionEventosMasPlazasUiState loading() {
        return new GestionEventosMasPlazasUiState(true, null, new ArrayList<>());
    }

    public static GestionEventosMasPlazasUiState success(List<Map<String, Object>> eventos) {
        return new GestionEventosMasPlazasUiState(false, null, eventos);
    }

    public static GestionEventosMasPlazasUiState error(String message) {
        return new GestionEventosMasPlazasUiState(false, message, new ArrayList<>());
    }
}
