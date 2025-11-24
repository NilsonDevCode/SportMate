package com.nilson.appsportmate.ui.auth.signUp.rol;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class RolSelectionViewModel extends ViewModel {

    private final MutableLiveData<RolSelectionUiState> uiState =
            new MutableLiveData<>(new RolSelectionUiState(null, false));

    @Inject
    public RolSelectionViewModel() {}

    public LiveData<RolSelectionUiState> getUiState() {
        return uiState;
    }

    public void selectRole(UserRol role) {
        uiState.setValue(new RolSelectionUiState(role, false));
    }
}
