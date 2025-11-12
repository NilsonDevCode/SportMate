package com.nilson.appsportmate.features.auth.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ✅ ViewModel falso usado solo en tests.
 * Evita tocar Firebase ni ninguna capa real.
 */
public class FakeLoginViewModel extends ViewModel {

    private final MutableLiveData<String> message = new MutableLiveData<>();

    public void onLoginClicked(String alias, String password) {
        message.setValue("Alias o contraseña incorrectos");
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<String> getErrorAlias() { return new MutableLiveData<>(); }
    public LiveData<String> getErrorPassword() { return new MutableLiveData<>(); }
    public LiveData<String> getNavUser() { return new MutableLiveData<>(); }
    public LiveData<String> getNavTownhall() { return new MutableLiveData<>(); }

    public void consumeMessage() {
        message.setValue(null);
    }
}
