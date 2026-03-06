package siem.account.service;

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

    // public OrganisationService(OrganisationRepository repo, OrganisationMapper mapper) {
    //     this.repo = repo;
    //     this.mapper = mapper;
    // }
    
    public OrganisationResponse createOrganisation(CreateOrganisationRequest request) {
        Organisation newOrg = mapper.toEntity(request);
        return mapper.toResponse(repo.save(newOrg));
    }
}
