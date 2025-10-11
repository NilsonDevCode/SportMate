package com.nilson.appsportmate.data.remote;

import android.util.Log;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.common.utils.Result;
import com.nilson.appsportmate.domain.models.AuthRole;
import com.nilson.appsportmate.domain.models.User;

import java.util.Optional;


public class AuthRemoteDataSource {
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public AuthRemoteDataSource() {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public Result<User> login(String email, String password) {
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

            User user = doc.toObject(User.class);
            return new Result.Success<>(user);
        } catch (Exception e) {
            Log.e("AuthRemoteDataSource", "Error login user: " + e.getLocalizedMessage());
            return new Result.Error<>(e);
        }
    }

    public Result<User> signUp(String email, String password, AuthRole role) {
        try {
            AuthResult result = auth.createUserWithEmailAndPassword(email, password).getResult();

            String uid = Optional.ofNullable(result.getUser())
                    .map(FirebaseUser::getUid)
                    .orElseThrow();

            User newUser = new User(uid, "", role);

            firestore.collection("users")
                    .document(uid)
                    .set(newUser);

            return new Result.Success<>(newUser);
        } catch (Exception e) {
            Log.e("AuthRemoteDataSource", "Error sign in user: " + e.getLocalizedMessage());
            return new Result.Error<>(e);
        }
    }
}
