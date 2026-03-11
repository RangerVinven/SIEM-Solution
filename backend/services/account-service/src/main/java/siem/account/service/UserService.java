package siem.account.service;

import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import siem.account.dto.AddEmployeeRequest;
import siem.account.dto.CreateUserRequest;
import siem.account.dto.LoginRequest;
import siem.account.dto.LoginResponse;
import siem.account.dto.UpdateUserRequest;
import siem.account.dto.UserResponse;
import siem.account.entity.User;
import siem.account.mapper.UserMapper;
import siem.account.repository.UserRepository;
import siem.account.enums.Role;
import siem.utils.JwtService;
import siem.models.UserPrincipal;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public UserResponse registerUser(CreateUserRequest request) {
        if(repo.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User newUser = mapper.toEntity(request);
        newUser.setPasswordHash(encoder.encode(request.getPassword()));
        newUser.setRole(Role.OWNER);

        User savedUser = repo.save(newUser);
        return mapper.toResponse(savedUser);
    }

    public String login(LoginRequest request) {
        User user = repo.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if(!encoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return jwtService.generateToken(
            user.getId(), 
            user.getOrganisationId(), 
            user.getRole().name(), 
            user.getFirstName(), 
            user.getLastName()
        );
    }

    private User getUserEntityWithAccessCheck(String id) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = repo.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("User not found"));
        
        // If the user is trying to access their data, or the owner is trying to access the data of users belonging to their organisation
        if (
            !currentUser.userId().equals(id) &&
            !("OWNER".equals(currentUser.role()) && currentUser.organisationId().equals(user.getOrganisationId().toString()))
    ) {
            throw new AccessDeniedException("Forbidden");
        }
        return user;
    }

    public UserResponse getUser(String id) {
        User user = getUserEntityWithAccessCheck(id);
        return mapper.toResponse(user);
    }

    public UserResponse updateUser(String id, UpdateUserRequest request) {
        User existingUser = getUserEntityWithAccessCheck(id);
        mapper.updateEntity(request, existingUser);
        return mapper.toResponse(repo.save(existingUser));
    }

    public void deleteUser(String id) {
        User user = getUserEntityWithAccessCheck(id);
        repo.delete(user);
    }
}
