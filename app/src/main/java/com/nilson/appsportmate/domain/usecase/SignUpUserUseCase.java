package com.nilson.appsportmate.domain.usecase;

import com.nilson.appsportmate.common.utils.Result;
import com.nilson.appsportmate.domain.repository.AuthRepository;
import com.nilson.appsportmate.domain.repository.UserRepository;

public class SignUpUserUseCase {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    public SignUpUserUseCase(AuthRepository authRepository, UserRepository userRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    public void execute(String email, String password) {
        Result<String> result = authRepository.signUp(email, password);
    }
}
