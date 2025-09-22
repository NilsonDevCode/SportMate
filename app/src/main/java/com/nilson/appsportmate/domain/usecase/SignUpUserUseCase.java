package com.nilson.appsportmate.domain.usecase;

import com.nilson.appsportmate.domain.repository.AuthRepository;

public class SignUpUserUseCase {
    private final AuthRepository repository;

    public SignUpUserUseCase(AuthRepository repository) { this.repository = repository; };

    public void execute(String email, String password) {
        repository.signUp(email, password);
    }
}
