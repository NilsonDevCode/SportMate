package com.nilson.appsportmate.domain.usecase;

import com.nilson.appsportmate.domain.repository.AuthRepository;

public class LoginUserUseCase {
    private final AuthRepository repository;

    public LoginUserUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public void execute(String email, String password) {
        repository.login(email, password);
    }
}
