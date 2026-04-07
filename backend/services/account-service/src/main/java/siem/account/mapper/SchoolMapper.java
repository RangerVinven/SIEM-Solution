package siem.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import siem.account.dto.CreateSchoolRequest;
import siem.account.dto.SchoolResponse;
import siem.account.dto.UpdateSchoolRequest;
import siem.account.entity.School;

@Mapper(componentModel = "spring")
public interface SchoolMapper {
    @Mapping(target="id", ignore = true)
    @Mapping(target="apiKey", ignore = true)
    School toEntity(CreateSchoolRequest request);

    SchoolResponse toResponse(School entity);

    @Mapping(target="id", ignore = true)
    @Mapping(target="apiKey", ignore = true)
    void updateEntity(UpdateSchoolRequest request, @MappingTarget School entity);
}
