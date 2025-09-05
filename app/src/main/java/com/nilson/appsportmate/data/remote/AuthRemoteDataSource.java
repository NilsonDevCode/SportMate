package com.nilson.appsportmate.data.remote;

import android.util.Log;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.common.utils.Result;
import com.nilson.appsportmate.data.dto.UserDto;

import java.util.Optional;


public class AuthRemoteDataSource {
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    public AuthRemoteDataSource() {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public Result<UserDto> login(String email, String password) {
        try {
            AuthResult result = auth.signInWithEmailAndPassword(email, password).getResult();

            String uid = Optional.ofNullable(result.getUser())
                    .map(FirebaseUser::getUid)
                    .orElseThrow();

            DocumentSnapshot doc = firestore.collection("users")
                    .document(uid)
                    .get()
                    .getResult();

            if (!doc.exists()) {
                Log.i("AuthRemoteDataSource", "User don't exists");
                throw new Exception("User don't exists");
            }

            UserDto dto = doc.toObject(UserDto.class);
            return new Result.Success<>(dto);
        } catch (Exception e) {
            Log.e("AuthRemoteDataSource", "Error login user: " + e.getLocalizedMessage());
            return new Result.Error<>(e);
        }
    }
}
