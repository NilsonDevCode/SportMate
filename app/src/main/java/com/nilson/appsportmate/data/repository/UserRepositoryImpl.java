package com.nilson.appsportmate.data.repository;

import com.nilson.appsportmate.common.utils.Result;
import com.nilson.appsportmate.data.local.UserLocalDataSource;
import com.nilson.appsportmate.data.remote.UserRemoteDataSource;
import com.nilson.appsportmate.domain.models.AuthRole;
import com.nilson.appsportmate.domain.models.User;
import com.nilson.appsportmate.domain.repository.UserRepository;

public class UserRepositoryImpl implements UserRepository {

    private final UserRemoteDataSource remoteDataSource;
    private final UserLocalDataSource localDataSource;

    public UserRepositoryImpl(UserRemoteDataSource remoteDataSource,
                              UserLocalDataSource localDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
    }

    @Override
    public Result<User> createUser(
            String uid,
            String alias,
            String name,
            String surname,
            AuthRole role,
            String munCode
    ) {
        Result<User> remoteResult = remoteDataSource.createUser(
                uid,
                alias,
                name,
                surname,
                role,
                munCode
        );

        return handleUserResult(remoteResult);
    }

    @Override
    public Result<User> getUser(String uid) {
        Result<User> remoteResult = remoteDataSource.getUser(uid);

        return handleUserResult(remoteResult);
    }

    /**
     * Handles the result of a user operation.
     *
     * @param result The result of the user operation.
     * @return A Result containing the User if successful, or an error if failed.
     */
    private Result<User> handleUserResult(Result<User> result) {
        if (result instanceof Result.Error<User>) {
            return new Result.Error<>(((Result.Error<User>) result).exception);
        }

        User user = ((Result.Success<User>) result).data;

        localDataSource.saveUser(user.uid(), user.alias(), user.role().name());
        return new Result.Success<>(user);
    }
}
