package siem.account.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import siem.account.dto.CreateSchoolRequest;
import siem.account.dto.UpdateSchoolRequest;
import siem.account.dto.SchoolResponse;
import siem.account.dto.AddEmployeeRequest;
import siem.account.dto.UserResponse;
import siem.account.service.SchoolService;
import siem.account.service.UserService;
import siem.models.UserPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class SchoolController {
    private final SchoolService service;
    private final UserService userService;

    @PostMapping("/schools")
    public ResponseEntity<SchoolResponse> createSchool(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateSchoolRequest request) {

        SchoolResponse school = service.createSchool(request);

        String token = userService.generateTokenForUser(principal.userId());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildTokenCookie(token).toString())
                .body(school);
    }

    private ResponseCookie buildTokenCookie(String token) {
        return ResponseCookie.from("token", token)
                .httpOnly(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();
    }

    @GetMapping("/schools/{id}")
    public SchoolResponse getSchool(@PathVariable String id) {
        return service.getSchool(id);
    }

    @PutMapping("/schools/{id}")
    public SchoolResponse updateSchool(@PathVariable String id, @RequestBody UpdateSchoolRequest request) {
        return service.updateSchool(id, request);
    }

    @DeleteMapping("/schools/{id}")
    public void deleteSchool(@PathVariable String id) {
        service.deleteSchool(id);
    }

    @PostMapping("/schools/{id}/employees")
    public UserResponse addEmployee(@PathVariable String id, @RequestBody AddEmployeeRequest request) {
        return service.addEmployee(id, request);
    }

    @GetMapping("/schools/api-keys/{id}")
    public String validateApiKey(@PathVariable String id) {
        return service.validateAPIKey(id);
    }

    @GetMapping("/schools/{id}/api-key")
    public String getApiKey(@PathVariable String id) {
        return service.getApiKey(id);
    }

    @GetMapping("/internal/schools/{id}/name")
    public String getSchoolName(@PathVariable String id) {
        return service.getSchoolEntity(id).getName();
    }
}