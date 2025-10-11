package com.nilson.appsportmate.domain.models;

public record User(
        String uid,
        String alias,
        AuthRole role,
        String name,
        String surname,
        String address // We have to check this field
) {
}
