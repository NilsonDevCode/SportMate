package com.nilson.appsportmate.ui.auth.signUp.FormAytoFragment;

public class FormAytoUiState {

    public String alias = "";
    public String razonSocial = "";
    public String comunidad = "";
    public String provincia = "";
    public String ciudad = "";
    public String puebloNuevo = "";

    public String password = "";
    public String password2 = "";

    public boolean loading = false;
    public String errorMessage = null;

    public boolean registroExitoso = false;

    public FormAytoUiState copy() {
        FormAytoUiState n = new FormAytoUiState();

        n.alias = alias;
        n.razonSocial = razonSocial;
        n.comunidad = comunidad;
        n.provincia = provincia;
        n.ciudad = ciudad;
        n.puebloNuevo = puebloNuevo;

        n.password = password;
        n.password2 = password2;

        n.loading = loading;
        n.errorMessage = errorMessage;
        n.registroExitoso = registroExitoso;

        return n;
    }
}

