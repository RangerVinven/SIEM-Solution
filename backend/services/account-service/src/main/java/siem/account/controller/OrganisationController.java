package siem.account.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import siem.account.dto.CreateOrganisationRequest;
import siem.account.dto.OrganisationResponse;
import siem.account.service.OrganisationService;

public class OrganisationController {
   private final OrganisationService service;

    public OrganisationController(OrganisationService service) {
        this.service = service;
    }

    @PostMapping("/organisations")
    public OrganisationResponse createOrganisation(@RequestBody CreateOrganisationRequest request) {
        return service.createOrganisation(request);
    }
}

