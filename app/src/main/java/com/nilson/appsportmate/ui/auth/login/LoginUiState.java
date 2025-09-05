package com.nilson.appsportmate.ui.auth.login;

public class LoginUiState {
    public final String alias;
    public final String password;
    public final boolean isLoading;
    public final boolean isLoadingEnabled;
    public final String aliasError;
    public final String passwordError;
    public final String generalError;
    public final boolean showPassword;
    public final boolean loginSuccess;
    public final boolean rememberMe;

    private LoginUiState(Builder builder) {
        this.alias = builder.alias != null ? builder.alias : "";
        this.password = builder.password != null ? builder.password : "";
        this.isLoading = builder.isLoading;
        this.aliasError = builder.aliasError;
        this.passwordError = builder.passwordError;
        this.generalError = builder.generalError;
        this.showPassword = builder.showPassword;
        this.loginSuccess = builder.loginSuccess;
        this.rememberMe = builder.rememberMe;

        this.isLoadingEnabled = isValidForm() && !isLoading;
    }

    private boolean isValidForm() {
        return !alias.isEmpty() &&
                !password.isEmpty() &&
                aliasError == null &&
                passwordError == null;
    }

    public static LoginUiState initial() {
        return new Builder().build();
    }

    public static LoginUiState loading() {
        return new Builder().setLoading(true).build();
    }

    public static LoginUiState success() {
        return new Builder().setLoginSuccess(true).build();
    }

    public static LoginUiState error(String errorMessage) {
        return new Builder().setGeneralError(errorMessage).build();
    }

    public static class Builder {
        private String alias = "";
        private String password = "";
        private boolean isLoading = false;
        private String aliasError = null;
        private String passwordError = null;
        private String generalError = null;
        private boolean showPassword = false;
        private boolean loginSuccess = false;
        private boolean rememberMe = false;

        public Builder setAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setLoading(boolean isLoading) {
            this.isLoading = isLoading;
            return this;
        }

        public Builder setAliasError(String aliasError) {
            this.aliasError = aliasError;
            return this;
        }

        public Builder setPasswordError(String passwordError) {
            this.passwordError = passwordError;
            return this;
        }

        public Builder setGeneralError(String generalError) {
            this.generalError = generalError;
            return this;
        }

        public Builder setShowPassword(boolean showPassword) {
            this.showPassword = showPassword;
            return this;
        }

        public Builder setLoginSuccess(boolean loginSuccess) {
            this.loginSuccess = loginSuccess;
            return this;
        }

        public Builder setRememberMe(boolean rememberMe) {
            this.rememberMe = rememberMe;
            return this;
        }

        public LoginUiState build() {
            return new LoginUiState(this);
        }
    }
}
