package siem.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import siem.account.dto.AddEmployeeRequest;
import siem.account.dto.CreateUserRequest;
import siem.account.dto.UpdateUserRequest;
import siem.account.dto.UserResponse;
import siem.account.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(CreateUserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "organisationId", ignore = true)
    User toEntity(AddEmployeeRequest request);

    UserResponse toResponse(User entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "organisationId", ignore = true)
    void updateEntity(UpdateUserRequest request, @MappingTarget User entity);
}
