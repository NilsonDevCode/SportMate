package com.nilson.appsportmate.data.remote;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nilson.appsportmate.common.utils.Result;
import com.nilson.appsportmate.domain.models.AuthRole;
import com.nilson.appsportmate.domain.models.User;

public class UserRemoteDataSource {
    private final FirebaseFirestore firestore;

    public UserRemoteDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public Result<User> createUser(
            String uid,
            String alias,
            String name,
            String surname,
            AuthRole role,
            String munCode
    ) {
        try {
            User newUser = new User(
                    uid,
                    alias,
                    role,
                    name,
                    surname,
                    munCode
            );

            DocumentSnapshot doc = firestore.collection("users")
                    .document(uid)
                    .get()
                    .getResult();


            if (!doc.exists()) {
                firestore.collection("users")
                        .document(uid)
                        .set(newUser)
                        .getResult();
            } else {
                Log.i("AuthRemoteDataSource", "User already exists");
                throw new Exception("User already exists");
            }

            return new Result.Success<>(newUser);
        } catch (Exception e) {
            Log.e("AuthRemoteDataSource", "Error creating user: " + e.getLocalizedMessage());
            return new Result.Error<>(e);
        }
    }

    public Result<User> getUser(String uid) {
        try {
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
            Log.e("AuthRemoteDataSource", "Error getting user: " + e.getLocalizedMessage());
            return new Result.Error<>(e);
        }
    }
}
