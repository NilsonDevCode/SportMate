package com.nilson.appsportmate.domain.models;

public class User {
    private String uid;
    private String alias;
    private AuthRole role;

    public User(String uid, String alias, AuthRole role) {
        this.uid = uid;
        this.alias = alias;
        this.role = role;
    }
}
