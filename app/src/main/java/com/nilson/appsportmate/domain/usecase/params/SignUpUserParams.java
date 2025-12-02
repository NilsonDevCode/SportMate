package com.nilson.appsportmate.domain.usecase.params;

import com.nilson.appsportmate.domain.models.AuthRole;

public record SignUpUserParams(
        String alias,
        AuthRole role,
        String name,
        String surname,
        String munCode
) { }
