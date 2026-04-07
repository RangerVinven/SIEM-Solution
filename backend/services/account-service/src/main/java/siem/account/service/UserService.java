package siem.account.service;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import siem.account.dto.CreateUserRequest;
import siem.account.dto.LoginRequest;
import siem.account.dto.UpdateUserRequest;
import siem.account.dto.UserResponse;
import siem.account.entity.School;
import siem.account.entity.User;
import siem.account.mapper.UserMapper;
import siem.account.repository.SchoolRepository;
import siem.account.repository.UserRepository;
import siem.account.enums.Role;
import siem.utils.JwtService;
import siem.models.UserPrincipal;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final SchoolRepository schoolRepo;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public List<UserResponse> getUsersBySchool(String schoolId, UserPrincipal principal) {
        if (!schoolId.equals(principal.schoolId())) {
            throw new AccessDeniedException("Forbidden");
        }
        return repo.findBySchoolId(UUID.fromString(schoolId)).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public UserResponse registerUser(CreateUserRequest request) {
        if(repo.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User newUser = mapper.toEntity(request);
        newUser.setPasswordHash(encoder.encode(request.getPassword()));
        newUser.setRole(Role.TECHNICAL_ADMIN);

        User savedUser = repo.save(newUser);
        return mapper.toResponse(savedUser);
    }

    public String login(LoginRequest request) {
        User user = repo.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if(!encoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String schoolName = resolveSchoolName(user);

        return jwtService.generateToken(user.getId(), user.getSchoolId(), user.getRole().name(), user.getFirstName(), user.getLastName(), schoolName);
    }

    public String generateTokenForUser(String userId) {
        User user = repo.findById(UUID.fromString(userId)).orElseThrow(() -> new RuntimeException("User not found"));
        String schoolName = resolveSchoolName(user);

        return jwtService.generateToken(user.getId(), user.getSchoolId(), user.getRole().name(), user.getFirstName(), user.getLastName(), schoolName);
    }

    private String resolveSchoolName(User user) {
        if (user.getSchoolId() == null) return null;
        return schoolRepo.findById(user.getSchoolId()).map(School::getName).orElse(null);
    }

    private User getUserEntity(String id) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = repo.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("User not found"));
        
        if (
            !currentUser.userId().equals(id) &&
            !("TECHNICAL_ADMIN".equals(currentUser.role()) && currentUser.schoolId() != null && currentUser.schoolId().equals(user.getSchoolId().toString()))
        ) {
            throw new AccessDeniedException("Forbidden");
        }
        return user;
    }

    public UserResponse getUser(String id) {
        User user = getUserEntity(id);
        return mapper.toResponse(user);
    }

    public UserResponse updateUser(String id, UpdateUserRequest request) {
        User existingUser = getUserEntity(id);
        mapper.updateEntity(request, existingUser);
        return mapper.toResponse(repo.save(existingUser));
    }

    public void deleteUser(String id) {
        User user = getUserEntity(id);
        repo.delete(user);
    }
}
