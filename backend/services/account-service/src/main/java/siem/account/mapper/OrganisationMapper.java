package siem.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import siem.account.dto.CreateOrganisationRequest;
import siem.account.dto.OrganisationResponse;
import siem.account.dto.UpdateOrganisationRequest;
import siem.account.entity.Organisation;

@Mapper(componentModel = "spring")
public interface OrganisationMapper {
    @Mapping(target="id", ignore = true)
    Organisation toEntity(CreateOrganisationRequest request);

    OrganisationResponse toResponse(Organisation entity);

    @Mapping(target="id", ignore = true)
    void updateEntity(UpdateOrganisationRequest request, @MappingTarget Organisation entity);
}
