package com.nilson.appsportmate.domain.repository;

import com.nilson.appsportmate.common.utils.Result;
import com.nilson.appsportmate.domain.models.AuthRole;
import com.nilson.appsportmate.domain.models.User;

/**
 * Repository interface for user-related operations.
 * <p>
 * This interface defines methods for creating and retrieving users.
 * Implementations of this interface should handle the actual data operations,
 * such as interacting with remote services or local data sources.
 *
 * @author Jordy Pinos
 * @version 1.0
 * @since 2024-10-12
 */
public interface UserRepository {

    /**
     * Creates a new user with the provided details.
     *
     * @param uid      The unique identifier of the user.
     * @param alias    The alias of the user.
     * @param name     The name of the user.
     * @param surname  The surname of the user.
     * @param role     The role of the user.
     * @param munCode  The address code (CMUN + CPRO) of the user.
     * @return A Result with the created User, or an error if the creation failed.
     */
    Result<User> createUser(
            String uid,
            String alias,
            String name,
            String surname,
            AuthRole role,
            String munCode
    );

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param uid The unique identifier of the user.
     * @return A Result with the User, or an error if the retrieval failed.
     */
    Result<User> getUser(String uid);
}
