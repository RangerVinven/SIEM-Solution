package siem.account.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import siem.account.dto.CreateOrganisationRequest;
import siem.account.dto.OrganisationResponse;
import siem.account.entity.Organisation;
import siem.account.mapper.OrganisationMapper;
import siem.account.repository.OrganisationRepository;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganisationService {
    private final OrganisationRepository repo;
    private final OrganisationMapper mapper;

    public OrganisationResponse createOrganisation(CreateOrganisationRequest request) {
        Organisation newOrg = mapper.toEntity(request);
        return mapper.toResponse(repo.save(newOrg));
    }

    public OrganisationResponse getOrganisation(String id) {
        return mapper.toResponse(repo.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Organisation not found")));
    }

    public OrganisationResponse updateOrganisation(String id, CreateOrganisationRequest request) {
        Organisation existingOrg = repo.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Organisation not found"));
        mapper.updateEntity(request, existingOrg);
        return mapper.toResponse(repo.save(existingOrg));
    }
}
