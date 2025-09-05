package com.nilson.appsportmate.domain.usecase;

import com.nilson.appsportmate.common.utils.Result;
import com.nilson.appsportmate.domain.models.User;
import com.nilson.appsportmate.domain.repository.AuthRepository;

public class LoginUserUseCase {
    private final AuthRepository repository;

    public LoginUserUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public Result<User> execute(String email, String password) {
        return repository.login(email, password);
    }
}
