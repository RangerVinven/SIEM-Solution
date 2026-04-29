package siem.account.controller;

import siem.account.service.UserService;
import siem.account.dto.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import siem.models.UserPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping("/schools/{id}/users")
    public List<UserResponse> getUsersBySchool(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String id) {
        return service.getUsersBySchool(id, principal);
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request) {
        String token = service.login(request);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, buildTokenCookie(token).toString())
            .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from("token", "")
            .httpOnly(true)
            .path("/")
            .maxAge(0)
            .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    @GetMapping("")
    public UserResponse getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return service.getUser(principal.userId());
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> registerUser(@RequestBody CreateUserRequest request) {
        UserResponse user = service.registerUser(request);
        String token = service.generateTokenForUser(user.id());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildTokenCookie(token).toString())
                .body(user);
    }

    @GetMapping("/users/{id}")
    public UserResponse getUser(@PathVariable String id) {
        return service.getUser(id);
    }

    @PutMapping("/users/{id}")
    public UserResponse updateUser(@PathVariable String id, @RequestBody UpdateUserRequest request) {
        return service.updateUser(id, request);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseCookie buildTokenCookie(String token) {
        return ResponseCookie.from("token", token)
                .httpOnly(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();
    }
}
