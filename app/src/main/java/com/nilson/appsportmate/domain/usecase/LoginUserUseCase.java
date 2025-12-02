package com.nilson.appsportmate.domain.usecase;

import android.util.Log;

import com.nilson.appsportmate.common.utils.Result;
import com.nilson.appsportmate.domain.models.User;
import com.nilson.appsportmate.domain.repository.AuthRepository;
import com.nilson.appsportmate.domain.repository.UserRepository;
import com.nilson.appsportmate.domain.usecase.params.LoginUserParams;

public class LoginUserUseCase {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    public LoginUserUseCase(AuthRepository authRepository,
                            UserRepository userRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    public User execute(LoginUserParams params) {
        Result<String> result = authRepository.login(params.alias(), params.password());

        String userId = Result.handleResult(result, exception -> {
            Log.d("LoginUserUseCase", "execute: Error logging in: " + exception.getLocalizedMessage());
        });

        Result<User> resultUser = userRepository.getUser(userId);

        return Result.handleResult(resultUser, exception -> {
            Log.d("LoginUserUseCase", "execute: Error fetching user: " + exception.getLocalizedMessage());
        });
    }
}
