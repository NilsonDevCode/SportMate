package com.nilson.appsportmate.ui.auth.signUp.FormUsuarioFragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FormUsuarioViewModel extends ViewModel {

    private final MutableLiveData<FormUsuarioUiState> _uiState =
            new MutableLiveData<>(new FormUsuarioUiState());
    public LiveData<FormUsuarioUiState> uiState = _uiState;

    private FormUsuarioUiState current() {
        return _uiState.getValue();
    }

    private void update(FormUsuarioUiState nuevo) {
        _uiState.setValue(nuevo);
    }

    // SETTERS ----------------------

    public void setAlias(String value) {
        FormUsuarioUiState s = current().copy();
        s.alias = value;
        update(s);
    }

    public void setNombre(String value) {
        FormUsuarioUiState s = current().copy();
        s.nombre = value;
        update(s);
    }

    public void setApellidos(String value) {
        FormUsuarioUiState s = current().copy();
        s.apellidos = value;
        update(s);
    }

    public void setComunidad(String value) {
        FormUsuarioUiState s = current().copy();
        s.comunidad = value;
        update(s);
    }

    public void setProvincia(String value) {
        FormUsuarioUiState s = current().copy();
        s.provincia = value;
        update(s);
    }

    public void setCiudad(String value) {
        FormUsuarioUiState s = current().copy();
        s.ciudad = value;
        update(s);
    }

    public void setPueblo(String value) {
        FormUsuarioUiState s = current().copy();
        s.pueblo = value;
        update(s);
    }

    public void setAyuntamiento(String value) {
        FormUsuarioUiState s = current().copy();
        s.ayuntamiento = value;
        update(s);
    }

    public void setPassword(String value) {
        FormUsuarioUiState s = current().copy();
        s.password = value;
        update(s);
    }

    public void setPassword2(String value) {
        FormUsuarioUiState s = current().copy();
        s.password2 = value;
        update(s);
    }

    // VALIDACIÓN ----------------------

    private String validarCampos(FormUsuarioUiState s) {
        if (s.alias.isEmpty()) return "El alias es obligatorio";
        if (s.nombre.isEmpty()) return "El nombre es obligatorio";
        if (s.apellidos.isEmpty()) return "Los apellidos son obligatorios";

        if (s.comunidad.isEmpty()) return "Selecciona una comunidad";
        if (s.provincia.isEmpty()) return "Selecciona una provincia";
        if (s.ciudad.isEmpty()) return "Selecciona una ciudad";
        if (s.pueblo.isEmpty()) return "Selecciona un pueblo";

        if (s.password.isEmpty()) return "La contraseña es obligatoria";
        if (!s.password.equals(s.password2)) return "Las contraseñas no coinciden";

        return null;
    }

    // REGISTRO ----------------------

    public void registrarUsuario() {
        FormUsuarioUiState s = current().copy();
        s.errorMessage = validarCampos(s);

        if (s.errorMessage != null) {
            update(s);
            return;
        }

        s.loading = true;
        update(s);

        // Llamada simulada: sustituye con Firebase o tu backend
        new Thread(() -> {
            try {
                Thread.sleep(1500);

                FormUsuarioUiState finalState = current().copy();
                finalState.loading = false;
                finalState.registroExitoso = true;

                update(finalState);

            } catch (InterruptedException e) {
                FormUsuarioUiState finalState = current().copy();
                finalState.loading = false;
                finalState.errorMessage = "Error inesperado";
                update(finalState);
            }
        }).start();
    }
}

