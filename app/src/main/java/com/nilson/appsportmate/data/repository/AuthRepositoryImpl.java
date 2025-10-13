package com.nilson.appsportmate.data.repository;

import android.util.Log;

import com.nilson.appsportmate.common.utils.AuthAliasHelper;
import com.nilson.appsportmate.common.utils.Result;
import com.nilson.appsportmate.data.remote.AuthRemoteDataSource;
import com.nilson.appsportmate.domain.repository.AuthRepository;

public class AuthRepositoryImpl implements AuthRepository {
    private final AuthRemoteDataSource remoteDataSource;

    public AuthRepositoryImpl(AuthRemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    @Override
    public Result<String> login(String alias, String password) {
        String aliasEmail = AuthAliasHelper.aliasToEmail(alias);
        Result<String> remoteResult = remoteDataSource.login(aliasEmail, password);

        return handleAuthResult(remoteResult);
    }

    @Override
    public Result<String> signUp(String alias, String password) {
        String aliasEmail = AuthAliasHelper.aliasToEmail(alias);
        Result<String> remoteResult = remoteDataSource.signUp(aliasEmail, password);

        return handleAuthResult(remoteResult);
    }

    /**
     * Handles the result of an authentication operation.
     *
     * @param result The result of the authentication operation.
     * @return A Result containing the uid if successful, or an error if failed.
     */
    private Result<String> handleAuthResult(Result<String> result) {
        if (result instanceof Result.Error<String>) {
            Log.e("AuthRepositoryImpl", "handleAuthResult: Error " + ((Result.Error<String>) result).exception.getLocalizedMessage());
            return new Result.Error<>(((Result.Error<String>) result).exception);
        }

        String uid = ((Result.Success<String>) result).data;
        Log.d("AuthRepositoryImpl", "handleAuthResult: Success " + uid);
        return new Result.Success<>(uid);
    }
}