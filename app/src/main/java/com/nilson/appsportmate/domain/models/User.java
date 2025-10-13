package com.nilson.appsportmate.domain.models;

public record User(
        /// The unique identifier of the user
        String uid,

        /// The alias of the user
        String alias,

        /// The role of the user
        AuthRole role,

        ///  The name of the user
        String name,

        /// The surname of the user
        String surname,

        /// The address code (CMUN + CPRO) of the user
        String munCode
) { }
