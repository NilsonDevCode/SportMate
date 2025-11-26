package com.nilson.appsportmate.ui.auth.login;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.AuthAliasHelper;
import com.nilson.appsportmate.databinding.FragmentLoginBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    protected LoginViewModel viewModel;

    // ⚡ Nuevo flag global: permite desactivar Firebase solo durante tests
    public static boolean disableFirebaseForTest = false;

    private TextInputEditText etAlias, etPassword;
    private MaterialButton btnLogin;
    private TextView btnNavRegister;
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

        // ⚡ Solo inicializa Firebase si no estamos en modo test
        if (!disableFirebaseForTest) {
            viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        }

        final NavController navController = Navigation.findNavController(view);

        etAlias = binding.etAlias;
        etPassword = binding.etPassword;
        btnLogin = binding.btnLogin;
        btnNavRegister = binding.btnNavRegister;

        // Validación y normalización del alias (primera letra mayúscula)
        etAlias.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (aliasUpdating) return;
                aliasUpdating = true;

                String input = s.toString();
                if (!input.isEmpty()) {
                    String fixed = input.substring(0, 1).toUpperCase() + input.substring(1);
                    if (!fixed.equals(input)) {
                        etAlias.setText(fixed);
                        etAlias.setSelection(fixed.length());
                    }
                }
                String err = AuthAliasHelper.getAliasValidationError(getText(etAlias));
                etAlias.setError(err);

                aliasUpdating = false;
            }
        });

        // Acción: login
        btnLogin.setOnClickListener(v ->
                viewModel.onLoginClicked(getText(etAlias), getText(etPassword), requireContext())
        );

        // Acción: ir a registro
        btnNavRegister.setOnClickListener(v ->
                navController.navigate(R.id.action_authFragment_to_rolSelectionFragment)
        );

        // Observadores ViewModel
        viewModel.getErrorAlias().observe(getViewLifecycleOwner(), err -> {
            if (err != null) etAlias.setError(err);
        });

        viewModel.getErrorPassword().observe(getViewLifecycleOwner(), err -> {
            if (err != null) etPassword.setError(err);
        });

        // Acción login correcto o no
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && isAdded()) {
                // binding.tvMensaje.setVisibility(View.VISIBLE);
                // binding.tvMensaje.setText(msg);

                // int color = msg.toLowerCase().contains("correcto")
                //         ? R.color.green
                //         : R.color.red;

                // binding.tvMensaje.setTextColor(
                //         ContextCompat.getColor(requireContext(), color)
                // );

                viewModel.consumeMessage();
            }
        });

        // Navegación tras login correcto
        viewModel.getNavUser().observe(getViewLifecycleOwner(), aytoId -> {
            if (aytoId != null && isAdded()) {
                navController.navigate(R.id.inicioFragment);
                viewModel.consumeNavUser();
            }
        });

        viewModel.getNavTownhall().observe(getViewLifecycleOwner(), uid -> {
            if (uid != null && isAdded()) {
                navController.navigate(R.id.menuAyuntamientoFragment);
                viewModel.consumeNavTownhall();
            }
        });
    }

    private String getText(TextInputEditText t) {
        return t.getText() == null ? "" : t.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
