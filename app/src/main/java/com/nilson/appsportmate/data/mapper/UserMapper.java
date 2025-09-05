package com.nilson.appsportmate.data.mapper;

import com.nilson.appsportmate.data.dto.UserDto;
import com.nilson.appsportmate.domain.models.User;

public class UserMapper {
    public static User fromDto(UserDto dto) {
        return new User(dto.getUid(), dto.getAlias(), dto.getRole());
    }
}

