package com.nilson.appsportmate.features.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.features.auth.presentacion.LoginPresenter;
import com.nilson.appsportmate.features.auth.presentacion.LoginView;
import com.nilson.appsportmate.common.utils.AuthAliasHelper;
import com.nilson.appsportmate.features.townhall.ui.GestionDeportesAyuntamientoActivity;
import com.nilson.appsportmate.features.user.ui.DeportesDisponiblesActivity;

public class LoginActivity extends AppCompatActivity implements LoginView {

    private TextInputEditText etAlias, etPassword;
    private MaterialButton btnLogin, btnIrRegistro;
    private LoginPresenter presenter;

    private boolean aliasUpdating = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        etAlias = findViewById(R.id.etAlias);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnIrRegistro = findViewById(R.id.btnNavRegister);

        presenter = new LoginPresenter(this, this);

        configurarValidacionesTiempoReal();

        btnLogin.setOnClickListener(v -> presenter.onLoginClicked());
        btnIrRegistro.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void configurarValidacionesTiempoReal() {
        etAlias.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (aliasUpdating) return;
                aliasUpdating = true;

                String input = s.toString();
                if (!input.isEmpty()) {
                    String first = input.substring(0, 1).toUpperCase();
                    String rest = input.substring(1);
                    String fixed = first + rest;
                    if (!fixed.equals(input)) {
                        etAlias.setText(fixed);
                        etAlias.setSelection(fixed.length());
                    }
                }

                String err = AuthAliasHelper.getAliasValidationError(
                        etAlias.getText() == null ? "" : etAlias.getText().toString());
                etAlias.setError(err);

                aliasUpdating = false;
            }
        });
    }

    // ===== Implementación LoginView =====
    @Override public String getAliasInput() { return etAlias.getText() == null ? "" : etAlias.getText().toString().trim(); }
    @Override public String getPasswordInput() { return etPassword.getText() == null ? "" : etPassword.getText().toString().trim(); }

    @Override public void mostrarErrorAlias(String msg) { etAlias.setError(msg); etAlias.requestFocus(); }
    @Override public void mostrarErrorPassword(String msg) { etPassword.setError(msg); etPassword.requestFocus(); }

    @Override public void mostrarMensaje(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override public void navegarAyuntamiento(String uid) {
        startActivity(new Intent(this, GestionDeportesAyuntamientoActivity.class));
        finish();
    }

    @Override public void navegarUsuario(String ayuntamientoId) {
        startActivity(new Intent(this, DeportesDisponiblesActivity.class));
        finish();
    }
}
