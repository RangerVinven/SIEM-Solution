package siem.account.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import siem.account.dto.CreateOrganisationRequest;
import siem.account.dto.UpdateOrganisationRequest;
import siem.account.dto.OrganisationResponse;
import siem.account.dto.AddEmployeeRequest;
import siem.account.dto.UserResponse;
import org.springframework.web.bind.annotation.RestController;
import siem.account.service.OrganisationService;
import siem.account.service.UserService;

@RestController
public class OrganisationController {
   private final OrganisationService service;
   private final UserService userService;

    public OrganisationController(OrganisationService service, UserService userService) {
        this.service = service;
        this.userService = userService;
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
    public OrganisationResponse updateOrganisation(@PathVariable String id, @RequestBody UpdateOrganisationRequest request) {
        return service.updateOrganisation(id, request);
    }

    @DeleteMapping("/organisations/{id}")
    public void deleteOrganisation(@PathVariable String id) {
        service.deleteOrganisation(id);
    }

    @PostMapping("/organisations/{id}/employees")
    public UserResponse addEmployee(@PathVariable String id, @RequestBody AddEmployeeRequest request) {
        return service.addEmployee(id, request);
    }

    @GetMapping("/organisations/api-keys/{id}")
    public void getApiKey(@PathVariable String id) {
        service.validateAPIKey(id);
    }
}
