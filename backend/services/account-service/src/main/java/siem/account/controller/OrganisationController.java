package siem.account.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import siem.account.dto.CreateOrganisationRequest;
import siem.account.dto.OrganisationResponse;
import org.springframework.web.bind.annotation.RestController;
import siem.account.service.OrganisationService;

@RestController
public class OrganisationController {
   private final OrganisationService service;

    public OrganisationController(OrganisationService service) {
        this.service = service;
    }

    @PostMapping("/organisations")
    public OrganisationResponse createOrganisation(@RequestBody CreateOrganisationRequest request) {
        return service.createOrganisation(request);
    }

    @GetMapping("/organisations/{id}")
    public OrganisationResponse getOrganisation(@PathVariable String id) {
        return service.getOrganisation(id);
    }

    @PutMapping("/organisations/{id}")
    public OrganisationResponse updateOrganisation(@PathVariable String id, @RequestBody CreateOrganisationRequest request) {
        return service.updateOrganisation(id, request);
    }
}

