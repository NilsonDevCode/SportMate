package com.nilson.appsportmate.ui.auth.signUp.FormUsuarioFragment;
public class FormUsuarioUiState {

    public String alias = "";
    public String nombre = "";
    public String apellidos = "";
    public String comunidad = "";
    public String provincia = "";
    public String ciudad = "";
    public String pueblo = "";
    public String ayuntamiento = ""; // autocompletado al elegir pueblo

    public String password = "";
    public String password2 = "";

    public boolean loading = false;
    public String errorMessage = null;

    public boolean registroExitoso = false;

    public FormUsuarioUiState copy() {
        FormUsuarioUiState nuevo = new FormUsuarioUiState();
        nuevo.alias = this.alias;
        nuevo.nombre = this.nombre;
        nuevo.apellidos = this.apellidos;

        nuevo.comunidad = this.comunidad;
        nuevo.provincia = this.provincia;
        nuevo.ciudad = this.ciudad;
        nuevo.pueblo = this.pueblo;
        nuevo.ayuntamiento = this.ayuntamiento;

        nuevo.password = this.password;
        nuevo.password2 = this.password2;

        nuevo.loading = this.loading;
        nuevo.errorMessage = this.errorMessage;
        nuevo.registroExitoso = this.registroExitoso;

        return nuevo;
    }
}