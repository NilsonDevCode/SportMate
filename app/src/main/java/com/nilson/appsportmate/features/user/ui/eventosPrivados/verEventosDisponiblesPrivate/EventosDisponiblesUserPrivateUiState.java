package com.nilson.appsportmate.features.user.ui.eventosPrivados.verEventosDisponiblesPrivate;

import androidx.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EventosDisponiblesUserPrivateUiState {

    public final boolean loading;
    public final List<Map<String, Object>> disponibles;
    public final List<Map<String, Object>> mis;
    public final @Nullable String message;
    public final boolean actionInProgress;

    private EventosDisponiblesUserPrivateUiState(
            boolean loading,
            List<Map<String, Object>> disponibles,
            List<Map<String, Object>> mis,
            @Nullable String message,
            boolean actionInProgress
    ) {
        this.loading = loading;
        this.disponibles = disponibles;
        this.mis = mis;
        this.message = message;
        this.actionInProgress = actionInProgress;
    }

    public static EventosDisponiblesUserPrivateUiState loading() {
        return new EventosDisponiblesUserPrivateUiState(
                true,
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                false
        );
    }

    public static EventosDisponiblesUserPrivateUiState success(
            List<Map<String, Object>> disponibles,
            List<Map<String, Object>> mis
    ) {
        return new EventosDisponiblesUserPrivateUiState(
                false,
                disponibles,
                mis,
                null,
                false
        );
    }

    public static EventosDisponiblesUserPrivateUiState message(
            EventosDisponiblesUserPrivateUiState prev,
            String msg
    ) {
        return new EventosDisponiblesUserPrivateUiState(
                prev.loading,
                prev.disponibles,
                prev.mis,
                msg,
                prev.actionInProgress
        );
    }

    public static EventosDisponiblesUserPrivateUiState withAction(
            EventosDisponiblesUserPrivateUiState prev,
            boolean inProgress
    ) {
        return new EventosDisponiblesUserPrivateUiState(
                prev.loading,
                prev.disponibles,
                prev.mis,
                prev.message,
                inProgress
        );
    }

    public EventosDisponiblesUserPrivateUiState clearMessage() {
        return new EventosDisponiblesUserPrivateUiState(
                loading,
                disponibles,
                mis,
                null,
                actionInProgress
        );
    }
}
