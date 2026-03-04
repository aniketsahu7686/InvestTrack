package com.investtrack.auth.mapper;

import com.investtrack.auth.entity.User;
import com.investtrack.common.dto.RegisterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between User entity and DTOs.
 * The password field is ignored during mapping — it is set explicitly in the service layer
 * after BCrypt encoding.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Maps a registration request to a User entity.
     * Password is ignored — must be encoded and set separately.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterRequest request);
}
