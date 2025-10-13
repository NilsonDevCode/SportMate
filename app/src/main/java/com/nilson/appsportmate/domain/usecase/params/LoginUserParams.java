package com.nilson.appsportmate.domain.usecase.params;

public record LoginUserParams(
        String alias,
        String password
) { }
