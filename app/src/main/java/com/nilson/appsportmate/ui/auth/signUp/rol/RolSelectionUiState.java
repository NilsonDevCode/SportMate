package com.nilson.appsportmate.ui.auth.signUp.rol;

public class RolSelectionUiState {
    private UserRol selectedRol;
    private boolean isLoading;

    public RolSelectionUiState(UserRol selectedRole, boolean isLoading) {
        this.selectedRol = selectedRole;
        this.isLoading = isLoading;
    }

    public UserRol getSelectedRole() {
        return selectedRol;
    }

    public boolean isLoading() {
        return isLoading;
    }
}

