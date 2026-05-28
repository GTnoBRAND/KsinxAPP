package org.jas.ksinxapp.mappers;

import org.jas.ksinxapp.dtos.LoginRequest;
import org.jas.ksinxapp.dtos.LoginResponse;
import org.jas.ksinxapp.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoginMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
    User entity(LoginRequest loginRequest);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "role", source = "user.role")
    LoginResponse loginResponse(User user, String token);
}
