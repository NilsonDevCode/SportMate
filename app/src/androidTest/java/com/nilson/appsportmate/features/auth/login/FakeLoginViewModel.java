package com.nilson.appsportmate.features.auth.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nilson.appsportmate.ui.auth.login.LoginViewModel;

/**
 * ✅ ViewModel falso usado solo en tests.
 * Evita tocar Firebase ni ninguna capa real.
 */
public class FakeLoginViewModel extends LoginViewModel {

    private final MutableLiveData<String> message = new MutableLiveData<>();

    @Override
    public void onLoginClicked(String alias, String password, android.content.Context context) {
        message.setValue("Alias o contraseña incorrectos");
    }

    @Override
    public LiveData<String> getMessage() {
        return message;
    }

    @Override
    public void consumeMessage() {
        message.setValue(null);
    }
}
