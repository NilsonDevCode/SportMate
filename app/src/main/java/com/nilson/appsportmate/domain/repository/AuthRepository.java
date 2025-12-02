package com.nilson.appsportmate.domain.repository;

import com.nilson.appsportmate.common.utils.Result;
import com.nilson.appsportmate.domain.models.User;

/**
 * Repository interface for authentication-related operations.
 * <p>
 * This interface defines methods for logging in and signing up users.
 * Implementations of this interface should handle the actual authentication logic,
 * such as interacting with remote services or local data sources.
 *
 * @author Jordy Pinos
 * @version 1.0
 * @since 2024-10-12
 */
public interface AuthRepository {

    /**
     * Logs in a user with the provided alias and password.
     *
     * @param alias The alias of the user.
     * @param password The password of the user.
     *
     * @return A Result with the uid of the auth, or an error if the login failed.
     */
    Result<String> login(String alias, String password);

    /**
     * Signs up a new user with the provided alias and password.
     *
     * @param alias The alias of the new user.
     * @param password The password of the new user.
     * @return A Result with the created User, or an error if the sign-up failed.
     */
    Result<String> signUp(String alias, String password);
}
