package com.nilson.appsportmate.domain.repository;

import com.nilson.appsportmate.common.utils.Result;
import com.nilson.appsportmate.domain.models.User;

public interface AuthRepository {
    Result<User> login(String email, String password);
    Result<User> signIn(String email, String password);
}
