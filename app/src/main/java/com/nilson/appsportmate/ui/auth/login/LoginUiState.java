package com.nilson.appsportmate.ui.auth.login;

public class LoginUiState {

    private String alias;

    private String password;

    private boolean isLoading;

    private boolean isLoadingEnabled;

    private String aliasError;
    private String passwordError;
    private String generalError;

    private boolean showPassword;

    private boolean loginSuccess;

    private boolean rememberMe;

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
        return new Builder().loading(true).build();
    }

    public static LoginUiState success() {
        return new Builder().loginSuccess(true).build();
    }

    public static LoginUiState error(String errorMessage) {
        return new Builder().generalError(errorMessage).build();
    }

    public String getAlias() {
        return alias;
    }

    public String getPassword() {
        return password;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isLoadingEnabled() {
        return isLoadingEnabled;
    }

    public String getAliasError() {
        return aliasError;
    }

    public String getPasswordError() {
        return passwordError;
    }

    public String getGeneralError() {
        return generalError;
    }

    public boolean isShowPassword() {
        return showPassword;
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public static class Builder {
        private String alias;
        private String password;
        private boolean isLoading;
        private boolean isLoadingEnabled;
        private String aliasError;
        private String passwordError;
        private String generalError;
        private boolean showPassword;
        private boolean loginSuccess;
        private boolean rememberMe;

        public Builder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder loading(boolean loading) {
            this.isLoading = loading;
            return this;
        }

        public Builder loadingEnabled(boolean loadingEnabled) {
            this.isLoadingEnabled = loadingEnabled;
            return this;
        }

        public Builder aliasError(String aliasError) {
            this.aliasError = aliasError;
            return this;
        }

        public Builder passwordError(String passwordError) {
            this.passwordError = passwordError;
            return this;
        }

        public Builder generalError(String generalError) {
            this.generalError = generalError;
            return this;
        }

        public Builder showPassword(boolean showPassword) {
            this.showPassword = showPassword;
            return this;
        }

        public Builder loginSuccess(boolean loginSuccess) {
            this.loginSuccess = loginSuccess;
            return this;
        }

        public Builder rememberMe(boolean rememberMe) {
            this.rememberMe = rememberMe;
            return this;
        }

        public LoginUiState build() {
            return new LoginUiState(this);
        }
    }
}
