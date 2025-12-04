package com.nilson.appsportmate.features.user.ui.eventosPrivados.gestionEventosPrivate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GestionEventosUserPrivateUiState {

    public final boolean loading;
    public final String error;
    public final List<Map<String, Object>> eventos;

    private GestionEventosUserPrivateUiState(boolean loading, String error, List<Map<String, Object>> eventos) {
        this.loading = loading;
        this.error = error;
        this.eventos = eventos == null ? new ArrayList<>() : eventos;
    }

    public static GestionEventosUserPrivateUiState loading() {
        return new GestionEventosUserPrivateUiState(true, null, new ArrayList<>());
    }

    public static GestionEventosUserPrivateUiState success(List<Map<String, Object>> eventos) {
        return new GestionEventosUserPrivateUiState(false, null, eventos);
    }

    public static GestionEventosUserPrivateUiState error(String message) {
        return new GestionEventosUserPrivateUiState(false, message, new ArrayList<>());
    }
}
