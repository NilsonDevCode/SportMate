package com.nilson.appsportmate.ui.auth.login;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nilson.appsportmate.domain.usecase.LoginUserUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final LoginUserUseCase loginUserUseCase;

    private final MutableLiveData<LoginUiState> _uiState = new MutableLiveData<>();
    public final LiveData<LoginUiState> uiState = _uiState;

    @Inject
    public LoginViewModel(LoginUserUseCase loginUserUseCase) {
        this.loginUserUseCase = loginUserUseCase;
        _uiState.setValue(LoginUiState.initial());
    }

    public void onAliasChanged(String alias) {
        LoginUiState currentState = getCurrentState();
        String aliasError = validateAlias(alias);

        _uiState.setValue(new LoginUiState.Builder()
                .setAlias(alias)
                .setPassword(currentState.password)
                .setAliasError(aliasError)
                .setPasswordError(currentState.passwordError)
                .setShowPassword(currentState.showPassword)
                .setRememberMe(currentState.rememberMe)
                .build());
    }

    public void onPasswordChanged(String password) {
        LoginUiState currentState = getCurrentState();
        String passwordError = validatePassword(password);

        _uiState.setValue(new LoginUiState.Builder()
                .setAlias(currentState.alias)
                .setPassword(password)
                .setAliasError(currentState.aliasError)
                .setPasswordError(passwordError)
                .setShowPassword(currentState.showPassword)
                .setRememberMe(currentState.rememberMe)
                .build());
    }

    public void onLoginClicked() {
        LoginUiState currentState = getCurrentState();

        if (!currentState.isLoadingEnabled) {
            return;
        }

        Log.d("LoginViewModel", "onLoginClicked");

        loginUserUseCase.execute(currentState.alias, currentState.password);
    }

    private LoginUiState getCurrentState() {
        LoginUiState state = _uiState.getValue();
        return state != null ? state : LoginUiState.initial();
    }

    private String validateAlias(String alias) {
        if (alias.isEmpty()) {
            return "El Alias es requerido";
        }

        // TODO(Jordy Pinos): Completar la validacion del alias

        return null;
    }

    private String validatePassword(String password) {
        if (password.isEmpty()) {
            return "La contrasena es requerida";
        }

        // TODO(Jordy Pinos): Completar la validacion de la contrasena

        return null;
    }
}
