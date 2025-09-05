package com.nilson.appsportmate.data.repository;

import com.nilson.appsportmate.common.utils.Result;
import com.nilson.appsportmate.data.dto.UserDto;
import com.nilson.appsportmate.data.local.AuthLocalDataSource;
import com.nilson.appsportmate.data.mapper.UserMapper;
import com.nilson.appsportmate.data.remote.AuthRemoteDataSource;
import com.nilson.appsportmate.domain.models.User;
import com.nilson.appsportmate.domain.repository.AuthRepository;

public class AuthRepositoryImpl implements AuthRepository {
    private AuthRemoteDataSource remoteDataSource;
    private AuthLocalDataSource localDataSource;

    public AuthRepositoryImpl(AuthRemoteDataSource remoteDataSource,
                              AuthLocalDataSource localDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
    }

    @Override
    public Result<User> login(String email, String password) {
        Result<UserDto> remoteResult = remoteDataSource.login(email, password);

        if (remoteResult instanceof Result.Error<UserDto>) {
            return new Result.Error<>(((Result.Error<UserDto>) remoteResult).exception);
        }

        UserDto dto = ((Result.Success<UserDto>) remoteResult).data;
        localDataSource.saveUser(dto.getUid(), dto.getAlias(), dto.getRole().name());
        return new Result.Success<>(UserMapper.fromDto(dto));
    }

    @Override
    public Result<User> signIn(String email, String password) {
        return null;
    }
}