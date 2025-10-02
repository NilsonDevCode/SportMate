package com.nilson.appsportmate.ui.auth.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.databinding.FragmentLoginBinding;
import com.nilson.appsportmate.features.auth.presentacion.LoginPresenter;
import com.nilson.appsportmate.features.auth.presentacion.LoginView;
import com.nilson.appsportmate.common.utils.AuthAliasHelper;
import com.nilson.appsportmate.features.townhall.ui.GestionDeportesAyuntamientoActivity;
import com.nilson.appsportmate.features.user.ui.DeportesDisponiblesActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment implements LoginView {

    private FragmentLoginBinding binding;
    private LoginPresenter presenter;

    private TextInputEditText etAlias, etPassword;
    private MaterialButton btnLogin, btnNavRegister;

    private boolean aliasUpdating = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etAlias       = binding.etAlias;
        etPassword    = binding.etPassword;
        btnLogin      = binding.btnLogin;
        btnNavRegister= binding.btnNavRegister;

        presenter = new LoginPresenter(this, requireContext());

        configurarValidacionesTiempoReal();

        btnLogin.setOnClickListener(v -> presenter.onLoginClicked());
        btnNavRegister.setOnClickListener(v ->
                // usa la acción del nav_graph
                androidx.navigation.Navigation.findNavController(v)
                        .navigate(R.id.action_loginFragment_to_signInFragment)
        );
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

    // ===== LoginView =====
    @Override public String getAliasInput() {
        return etAlias.getText() == null ? "" : etAlias.getText().toString().trim();
    }
    @Override public String getPasswordInput() {
        return etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
    }

    @Override public void mostrarErrorAlias(String msg) { etAlias.setError(msg); etAlias.requestFocus(); }
    @Override public void mostrarErrorPassword(String msg) { etPassword.setError(msg); etPassword.requestFocus(); }

    @Override public void mostrarMensaje(String msg) {
        if (!isAdded()) return;
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override public void navegarAyuntamiento(String uid) {
        if (!isAdded()) return;
        startActivity(new Intent(requireContext(), GestionDeportesAyuntamientoActivity.class));
        requireActivity().finish();
    }

    @Override public void navegarUsuario(String ayuntamientoId) {
        if (!isAdded()) return;
        startActivity(new Intent(requireContext(), DeportesDisponiblesActivity.class));
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
