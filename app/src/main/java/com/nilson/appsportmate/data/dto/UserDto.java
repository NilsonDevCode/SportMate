package com.nilson.appsportmate.data.dto;


import com.nilson.appsportmate.domain.models.AuthRole;

public class UserDto {
    private String uid;
    private String alias;
    private AuthRole role;

    public UserDto(String uid, String alias, AuthRole role) {
        this.uid = uid;
        this.alias = alias;
        this.role = role;
    }

    public String getUid() {
        return uid;
    }

    public String getAlias() {
        return alias;
    }

    public AuthRole getRole() {
        return role;
    }
}