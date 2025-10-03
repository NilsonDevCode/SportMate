package com.nilson.appsportmate.features.user.ui.menuPrincipal.opcionesMenu;

import java.util.ArrayList;
import java.util.List;

public class SeleccionarNuevoAyuntamientoUiState {
    public final boolean loading;
    public final String message;
    public final String error;

    public final List<Opcion> comunidades;
    public final List<Opcion> provincias;
    public final List<Opcion> ciudades;
    public final List<PuebloOpcion> pueblos;

    public final String puebloNombre;       // solo lectura (rellenado tras elegir pueblo)
    public final String ayuntamientoNombre; // auto (desde pueblo o fetch de ayuntamiento)

    // IDs seleccionadas (para precarga determinista en el Fragment)
    public final String comunidadIdSel;
    public final String provinciaIdSel;
    public final String ciudadIdSel;
    public final String puebloIdSel;
    public final String ayuntamientoIdSel;

    public SeleccionarNuevoAyuntamientoUiState(boolean loading, String message, String error,
                                               List<Opcion> comunidades, List<Opcion> provincias,
                                               List<Opcion> ciudades, List<PuebloOpcion> pueblos,
                                               String puebloNombre, String ayuntamientoNombre,
                                               String comunidadIdSel, String provinciaIdSel, String ciudadIdSel,
                                               String puebloIdSel, String ayuntamientoIdSel) {
        this.loading = loading;
        this.message = message;
        this.error = error;
        this.comunidades = comunidades == null ? new ArrayList<>() : comunidades;
        this.provincias = provincias == null ? new ArrayList<>() : provincias;
        this.ciudades   = ciudades   == null ? new ArrayList<>() : ciudades;
        this.pueblos    = pueblos    == null ? new ArrayList<>() : pueblos;
        this.puebloNombre = puebloNombre == null ? "" : puebloNombre;
        this.ayuntamientoNombre = ayuntamientoNombre == null ? "" : ayuntamientoNombre;

        this.comunidadIdSel = comunidadIdSel;
        this.provinciaIdSel = provinciaIdSel;
        this.ciudadIdSel = ciudadIdSel;
        this.puebloIdSel = puebloIdSel;
        this.ayuntamientoIdSel = ayuntamientoIdSel;
    }

    public static SeleccionarNuevoAyuntamientoUiState idle() {
        return new SeleccionarNuevoAyuntamientoUiState(
                false, null, null,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                "", "",
                null, null, null, null, null
        );
    }

    public static SeleccionarNuevoAyuntamientoUiState loading(SeleccionarNuevoAyuntamientoUiState prev) {
        return new SeleccionarNuevoAyuntamientoUiState(
                true, prev.message, prev.error,
                prev.comunidades, prev.provincias, prev.ciudades, prev.pueblos,
                prev.puebloNombre, prev.ayuntamientoNombre,
                prev.comunidadIdSel, prev.provinciaIdSel, prev.ciudadIdSel, prev.puebloIdSel, prev.ayuntamientoIdSel
        );
    }

    public static SeleccionarNuevoAyuntamientoUiState withLists(SeleccionarNuevoAyuntamientoUiState prev,
                                                                List<Opcion> comunidades,
                                                                List<Opcion> provincias,
                                                                List<Opcion> ciudades,
                                                                List<PuebloOpcion> pueblos) {
        return new SeleccionarNuevoAyuntamientoUiState(
                false, null, null,
                comunidades != null ? comunidades : prev.comunidades,
                provincias  != null ? provincias  : prev.provincias,
                ciudades    != null ? ciudades    : prev.ciudades,
                pueblos     != null ? pueblos     : prev.pueblos,
                prev.puebloNombre, prev.ayuntamientoNombre,
                prev.comunidadIdSel, prev.provinciaIdSel, prev.ciudadIdSel, prev.puebloIdSel, prev.ayuntamientoIdSel
        );
    }

    public static SeleccionarNuevoAyuntamientoUiState withSelection(SeleccionarNuevoAyuntamientoUiState prev,
                                                                    String puebloNombre,
                                                                    String ayuntamientoNombre) {
        return new SeleccionarNuevoAyuntamientoUiState(
                false, null, null,
                prev.comunidades, prev.provincias, prev.ciudades, prev.pueblos,
                puebloNombre, ayuntamientoNombre,
                prev.comunidadIdSel, prev.provinciaIdSel, prev.ciudadIdSel, prev.puebloIdSel, prev.ayuntamientoIdSel
        );
    }

    public static SeleccionarNuevoAyuntamientoUiState withSelectedIds(SeleccionarNuevoAyuntamientoUiState prev,
                                                                      String comunidadIdSel,
                                                                      String provinciaIdSel,
                                                                      String ciudadIdSel,
                                                                      String puebloIdSel,
                                                                      String ayuntamientoIdSel) {
        return new SeleccionarNuevoAyuntamientoUiState(
                prev.loading, prev.message, prev.error,
                prev.comunidades, prev.provincias, prev.ciudades, prev.pueblos,
                prev.puebloNombre, prev.ayuntamientoNombre,
                comunidadIdSel, provinciaIdSel, ciudadIdSel, puebloIdSel, ayuntamientoIdSel
        );
    }

    public static SeleccionarNuevoAyuntamientoUiState error(SeleccionarNuevoAyuntamientoUiState prev, String err) {
        return new SeleccionarNuevoAyuntamientoUiState(
                false, null, err,
                prev.comunidades, prev.provincias, prev.ciudades, prev.pueblos,
                prev.puebloNombre, prev.ayuntamientoNombre,
                prev.comunidadIdSel, prev.provinciaIdSel, prev.ciudadIdSel, prev.puebloIdSel, prev.ayuntamientoIdSel
        );
    }

    /* Modelos simples para los spinners */
    public static class Opcion {
        public final String id;
        public final String nombre;
        public Opcion(String id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }

    public static class PuebloOpcion {
        public final String id;
        public final String nombre;
        public final String ayuntamientoId;       // puede venir null
        public final String ayuntamientoNombre;   // opcional (si lo guardas en el doc de pueblo)
        public PuebloOpcion(String id, String nombre, String aytoId, String aytoNombre) {
            this.id = id; this.nombre = nombre; this.ayuntamientoId = aytoId; this.ayuntamientoNombre = aytoNombre;
        }
        @Override public String toString() { return nombre; }
    }
}
