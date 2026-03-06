package siem.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import siem.account.dto.CreateUserRequest;
import siem.account.dto.UserResponse;
import siem.account.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(CreateUserRequest request);

    UserResponse toResponse(User entity);
}
