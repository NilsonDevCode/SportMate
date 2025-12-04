package com.nilson.appsportmate.features.user.ui.eventosPrivados.verEventosDisponiblesPrivate;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EventosDisponiblesUserPrivateUiState {

    public final boolean loading;
    public final boolean actionInProgress;

    @Nullable
    public final String message;

    public final List<Map<String, Object>> disponibles;
    public final List<Map<String, Object>> mis;

    // -------------------------------------------------------------------------
    // CONSTRUCTOR PRIVADO
    // -------------------------------------------------------------------------
    private EventosDisponiblesUserPrivateUiState(
            boolean loading,
            boolean actionInProgress,
            @Nullable String message,
            List<Map<String, Object>> disponibles,
            List<Map<String, Object>> mis
    ) {
        this.loading = loading;
        this.actionInProgress = actionInProgress;
        this.message = message;
        this.disponibles = disponibles;
        this.mis = mis;
    }

    // -------------------------------------------------------------------------
    // FACTORY: LOADING
    // -------------------------------------------------------------------------
    public static EventosDisponiblesUserPrivateUiState loading() {
        return new EventosDisponiblesUserPrivateUiState(
                true,
                false,
                null,
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    // -------------------------------------------------------------------------
    // FACTORY: SUCCESS
    // -------------------------------------------------------------------------
    public static EventosDisponiblesUserPrivateUiState success(
            List<Map<String, Object>> disponibles,
            List<Map<String, Object>> mis
    ) {
        return new EventosDisponiblesUserPrivateUiState(
                false,
                false,
                null,
                disponibles,
                mis
        );
    }

    // -------------------------------------------------------------------------
    // FACTORY: MENSAJE
    // -------------------------------------------------------------------------
    public static EventosDisponiblesUserPrivateUiState message(
            EventosDisponiblesUserPrivateUiState prev,
            String msg
    ) {
        return new EventosDisponiblesUserPrivateUiState(
                prev.loading,
                prev.actionInProgress,
                msg,
                prev.disponibles,
                prev.mis
        );
    }

    // -------------------------------------------------------------------------
    // FACTORY: ACTION PROGRESS
    // -------------------------------------------------------------------------
    public static EventosDisponiblesUserPrivateUiState withAction(
            EventosDisponiblesUserPrivateUiState prev,
            boolean inProgress
    ) {
        return new EventosDisponiblesUserPrivateUiState(
                prev.loading,
                inProgress,
                prev.message,
                prev.disponibles,
                prev.mis
        );
    }

    // -------------------------------------------------------------------------
    // BORRAR MENSAJE
    // -------------------------------------------------------------------------
    public EventosDisponiblesUserPrivateUiState clearMessage() {
        return new EventosDisponiblesUserPrivateUiState(
                this.loading,
                this.actionInProgress,
                null,
                this.disponibles,
                this.mis
        );
    }
}
