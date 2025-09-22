package com.nilson.appsportmate.features.auth.presentacion;

public interface LoginView {
    String getAliasInput();
    String getPasswordInput();

    void mostrarErrorAlias(String msg);
    void mostrarErrorPassword(String msg);

    void mostrarMensaje(String msg);

    void navegarAyuntamiento(String uid);
    void navegarUsuario(String ayuntamientoId);
}
