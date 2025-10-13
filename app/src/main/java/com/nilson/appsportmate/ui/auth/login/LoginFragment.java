package com.nilson.appsportmate.ui.auth.login;

import static com.nilson.appsportmate.common.utils.NavControllerExtensions.navigateWithAnimation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.databinding.FragmentLoginBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    private TextInputEditText aliasTI, passwordTI;
    private MaterialButton loginBtn, navRegisterBtn;

    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        navController = Navigation.findNavController(view);

        initViews();
        setupClickListeners();
        setupListeners();
    }

    private void initViews() {
        aliasTI = binding.etAlias;
        passwordTI = binding.etPassword;
        loginBtn = binding.btnLogin;
        navRegisterBtn = binding.btnNavRegister;
    }

    private void setupListeners() {
        aliasTI.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                viewModel.onAliasChanged(s.toString());
                Log.d("LoginFragment", "Alias: " + s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        passwordTI.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.onPasswordChanged(s.toString());
                Log.d("LoginFragment", "Password: " + s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupClickListeners() {
        loginBtn.setOnClickListener(v -> {
            viewModel.onLoginClicked();
        });

        navRegisterBtn.setOnClickListener(v -> {
            navigateWithAnimation(navController, R.id.action_loginFragment_to_signInFragment);
            Log.d("LoginFragment", "Navigate to Register Fragment");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
