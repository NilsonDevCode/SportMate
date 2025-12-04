package com.nilson.appsportmate.features.user.ui.eventosPrivados.VerEventosApuntadoPrivate;

import java.util.List;

public class VerEventosApuntadoPrivateUiState {

    public final boolean loading;
    public final String error;
    public final String message;
    public final List<VerEventosApuntadoPrivateViewModel.EventoUi> eventos;

    private VerEventosApuntadoPrivateUiState(
            boolean loading,
            String error,
            String message,
            List<VerEventosApuntadoPrivateViewModel.EventoUi> eventos
    ) {
        this.loading = loading;
        this.error = error;
        this.message = message;
        this.eventos = eventos;
    }

    public static VerEventosApuntadoPrivateUiState loading() {
        return new VerEventosApuntadoPrivateUiState(true, null, null, null);
    }

    public static VerEventosApuntadoPrivateUiState success(
            List<VerEventosApuntadoPrivateViewModel.EventoUi> eventos
    ) {
        return new VerEventosApuntadoPrivateUiState(false, null, null, eventos);
    }

    public static VerEventosApuntadoPrivateUiState error(String e) {
        return new VerEventosApuntadoPrivateUiState(false, e, null, null);
    }

    public static VerEventosApuntadoPrivateUiState message(
            List<VerEventosApuntadoPrivateViewModel.EventoUi> eventos,
            String msg
    ) {
        return new VerEventosApuntadoPrivateUiState(false, null, msg, eventos);
    }
}
