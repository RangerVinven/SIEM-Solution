package siem.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import siem.account.dto.CreateOrganisationRequest;
import siem.account.dto.OrganisationResponse;
import siem.account.entity.Organisation;

@Mapper(componentModel = "spring")
public interface OrganisationMapper {
    @Mapping(target="id", ignore = true)
    Organisation toEntity(CreateOrganisationRequest request);

    OrganisationResponse toResponse(Organisation entity);
}
