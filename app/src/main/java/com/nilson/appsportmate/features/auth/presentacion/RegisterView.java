package com.nilson.appsportmate.features.auth.presentacion;

public interface RegisterView {
    // Inputs
    String getAliasInput();
    String getPassword1();
    String getPassword2();
    String getNombre();
    String getApellidos();
    String getComunidad();
    String getProvincia();
    String getCiudad();
    String getPueblo();
    String getRazonSocial();
    String getRol();
    String getAyuntamientoSeleccionadoId();

    // IDs seleccionados (para correlación)
    String getComunidadIdSel();
    String getProvinciaIdSel();
    String getCiudadIdSel();

    // Errores
    void mostrarErrorAlias(String msg);
    void mostrarErrorPassword(String msg);
    void mostrarErrorNombre(String msg);
    void mostrarErrorApellidos(String msg);
    void mostrarErrorRazonSocial(String msg);

    // UI feedback
    void mostrarMensaje(String msg);

    // Navegación
    void navegarAyuntamiento();
    void navegarUsuario();
}

