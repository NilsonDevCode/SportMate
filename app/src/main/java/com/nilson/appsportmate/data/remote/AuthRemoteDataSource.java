package com.nilson.appsportmate.data.remote;

import android.util.Log;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nilson.appsportmate.common.utils.Result;

import java.util.Optional;

public class AuthRemoteDataSource {
    private final FirebaseAuth auth;

    public AuthRemoteDataSource() {
        this.auth = FirebaseAuth.getInstance();
    }

    public Result<String> login(String email, String password) {
        try {
            AuthResult result = auth.signInWithEmailAndPassword(email, password).getResult();

            String uid = Optional.ofNullable(result.getUser())
                    .map(FirebaseUser::getUid)
                    .orElseThrow();

            return new Result.Success<>(uid);
        } catch (Exception e) {
            Log.e("AuthRemoteDataSource", "Error login user: " + e.getLocalizedMessage());
            return new Result.Error<>(e);
        }
    }

    public Result<String> signUp(String email, String password) {
        try {
            AuthResult result = auth.createUserWithEmailAndPassword(email, password).getResult();


            String uid = Optional.ofNullable(result.getUser())
                    .map(FirebaseUser::getUid)
                    .orElseThrow();

            return new Result.Success<>(uid);
        } catch (Exception e) {
            Log.e("AuthRemoteDataSource", "Error sign in user: " + e.getLocalizedMessage());
            return new Result.Error<>(e);
        }
    }
}
