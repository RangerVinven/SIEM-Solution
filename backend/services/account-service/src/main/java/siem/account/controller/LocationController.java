package siem.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import siem.account.dto.CreateLocationRequest;
import siem.account.dto.UpdateLocationRequest;
import siem.account.entity.Location;
import siem.account.service.LocationService;
import siem.models.UserPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService service;

    @GetMapping("/schools/{schoolId}/locations")
    public List<Location> getLocations(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String schoolId) {

        if (!schoolId.equals(principal.schoolId())) {
            throw new AccessDeniedException("Forbidden");
        }

        return service.getLocationsForSchool(schoolId);
    }

    @PostMapping("/locations")
    public Location createLocation(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateLocationRequest request) {

        return service.createLocation(principal.schoolId(), request);
    }

    @GetMapping("/locations/{id}")
    public Location getLocation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {

        return service.getLocation(principal.schoolId(), id);
    }

    @PutMapping("/locations/{id}")
    public Location updateLocation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody UpdateLocationRequest request) {

        return service.updateLocation(principal.schoolId(), id, request);
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<Void> deleteLocation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {

        service.deleteLocation(principal.schoolId(), id);
        return ResponseEntity.noContent().build();
    }
}
