package siem.account.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import siem.account.dto.CreateOrganisationRequest;
import siem.account.dto.OrganisationResponse;
import siem.account.dto.UpdateOrganisationRequest;
import siem.account.dto.AddEmployeeRequest;
import siem.account.dto.UserResponse;
import siem.account.entity.Organisation;
import siem.account.entity.User;
import siem.account.enums.Role;
import siem.account.mapper.OrganisationMapper;
import siem.account.mapper.UserMapper;
import siem.account.repository.OrganisationRepository;
import siem.account.repository.UserRepository;
import siem.models.UserPrincipal;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganisationService {
    private final OrganisationRepository repo;
    private final OrganisationMapper mapper;
    private final UserRepository userRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    public OrganisationResponse createOrganisation(CreateOrganisationRequest request) {
        Organisation newOrg = mapper.toEntity(request);
        return mapper.toResponse(repo.save(newOrg));
    }

    public OrganisationResponse getOrganisation(String id) {
        return mapper.toResponse(
            repo.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Organisation not found"))
        );
    }

    public OrganisationResponse updateOrganisation(String id, UpdateOrganisationRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentUser.organisationId() == null || !currentUser.organisationId().equals(id)) {
            throw new AccessDeniedException("Forbidden");
        }

        if (!"OWNER".equals(currentUser.role())) {
            throw new AccessDeniedException("Forbidden");
        }

        Organisation existingOrg = repo.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Organisation not found"));

        mapper.updateEntity(request, existingOrg);
        return mapper.toResponse(repo.save(existingOrg));
    }

    public void deleteOrganisation(String id) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentUser.organisationId() == null || !currentUser.organisationId().equals(id)) {
            throw new AccessDeniedException("Forbidden");
        }

        if (!"OWNER".equals(currentUser.role())) {
            throw new AccessDeniedException("Forbidden");
        }

        if(!repo.existsById(UUID.fromString(id))) {
            throw new RuntimeException("Organisation not found");
        }

        repo.deleteById(UUID.fromString(id));
    }

    public UserResponse addEmployee(String orgId, AddEmployeeRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentUser.organisationId() == null || !currentUser.organisationId().equals(orgId)) {
            throw new AccessDeniedException("Forbidden");
        }

        if (!"OWNER".equals(currentUser.role()) && !"MANAGER".equals(currentUser.role())) {
            throw new AccessDeniedException("Forbidden");
        }

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User newUser = userMapper.toEntity(request);
        newUser.setPasswordHash(encoder.encode(request.getPassword()));
        
        Role role = Role.EMPLOYEE;
        if (request.getRole() != null) {
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
            }
        }
        
        if (role == Role.OWNER && !"OWNER".equals(currentUser.role())) {
            throw new AccessDeniedException("Only owners can create other owners");
        }
        
        newUser.setRole(role);
        newUser.setOrganisationId(UUID.fromString(orgId));

        User savedUser = userRepo.save(newUser);
        return userMapper.toResponse(savedUser);
    }

    public void validateAPIKey(String apiKey) {
        if (!repo.existsByApiKey(apiKey)) {
            throw new AccessDeniedException("Invalid API key");
        }
    }
}
