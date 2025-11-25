package com.nilson.appsportmate.ui.auth.signUp.FormAytoFragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FormAytoViewModel extends ViewModel {

    private final MutableLiveData<FormAytoUiState> _ui =
            new MutableLiveData<>(new FormAytoUiState());

    public LiveData<FormAytoUiState> uiState = _ui;

    private FormAytoUiState current() {
        return _ui.getValue();
    }

    private void update(FormAytoUiState s) {
        _ui.setValue(s);
    }

    // ----- SETTERS -----

    public void setAlias(String v) {
        FormAytoUiState s = current().copy();
        s.alias = v;
        update(s);
    }

    public void setRazonSocial(String v) {
        FormAytoUiState s = current().copy();
        s.razonSocial = v;
        update(s);
    }

    public void setComunidad(String v) {
        FormAytoUiState s = current().copy();
        s.comunidad = v;
        update(s);
    }

    public void setProvincia(String v) {
        FormAytoUiState s = current().copy();
        s.provincia = v;
        update(s);
    }

    public void setCiudad(String v) {
        FormAytoUiState s = current().copy();
        s.ciudad = v;
        update(s);
    }

    public void setPuebloNuevo(String v) {
        FormAytoUiState s = current().copy();
        s.puebloNuevo = v;
        update(s);
    }

    public void setPassword(String v) {
        FormAytoUiState s = current().copy();
        s.password = v;
        update(s);
    }

    public void setPassword2(String v) {
        FormAytoUiState s = current().copy();
        s.password2 = v;
        update(s);
    }

    // ----- VALIDACIÓN -----

    private String validar(FormAytoUiState s) {

        if (s.alias.isEmpty()) return "El alias es obligatorio";
        if (s.razonSocial.isEmpty()) return "La razón social es obligatoria";

        if (s.comunidad.isEmpty()) return "Selecciona una comunidad";
        if (s.provincia.isEmpty()) return "Selecciona una provincia";
        if (s.ciudad.isEmpty()) return "Selecciona una ciudad";

        if (s.puebloNuevo.isEmpty()) return "Debes crear un nombre de pueblo";

        if (s.password.isEmpty()) return "La contraseña es obligatoria";
        if (!s.password.equals(s.password2)) return "Las contraseñas no coinciden";

        return null;
    }

    // ----- REGISTRO -----

    public void registrar() {
        FormAytoUiState s = current().copy();
        s.errorMessage = validar(s);

        if (s.errorMessage != null) {
            update(s);
            return;
        }

        s.loading = true;
        update(s);

        // Simulamos llamada a backend
        new Thread(() -> {
            try {
                Thread.sleep(1500);

                FormAytoUiState f = current().copy();
                f.loading = false;
                f.registroExitoso = true;
                update(f);

            } catch (Exception e) {
                FormAytoUiState f = current().copy();
                f.loading = false;
                f.errorMessage = "Error inesperado";
                update(f);
            }
        }).start();
    }
}

