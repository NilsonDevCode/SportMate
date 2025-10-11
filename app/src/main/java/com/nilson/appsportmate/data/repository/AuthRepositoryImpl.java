package com.nilson.appsportmate.data.repository;

import com.nilson.appsportmate.common.utils.Result;
import com.nilson.appsportmate.data.local.AuthLocalDataSource;
import com.nilson.appsportmate.data.remote.AuthRemoteDataSource;
import com.nilson.appsportmate.domain.models.AuthRole;
import com.nilson.appsportmate.domain.models.User;
import com.nilson.appsportmate.domain.repository.AuthRepository;

public class AuthRepositoryImpl implements AuthRepository {
    private final AuthRemoteDataSource remoteDataSource;
    private final AuthLocalDataSource localDataSource;

    public AuthRepositoryImpl(AuthRemoteDataSource remoteDataSource,
                              AuthLocalDataSource localDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
    }

    @Override
    public Result<User> login(String email, String password) {
        Result<User> remoteResult = remoteDataSource.login(email, password);

        return handleAuthResult(remoteResult);
    }

    @Override
    public Result<User> signUp(String email, String password) {
        Result<User> remoteResult = remoteDataSource.signUp(email, password, AuthRole.USER);

        return handleAuthResult(remoteResult);
    }

    private Result<User> handleAuthResult(Result<User> result) {
        if (result instanceof Result.Error<User>) {
            return new Result.Error<>(((Result.Error<User>) result).exception);
        }

        User user = ((Result.Success<User>) result).data;
        localDataSource.saveUser(user.uid(), user.alias(), user.role().name());
        return new Result.Success<>(user);
    }
}