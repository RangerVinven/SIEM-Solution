package siem.account.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import siem.account.dto.SchoolRequest;
import siem.account.repository.SchoolRepository;
import siem.account.entity.School;
import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganisationService {
    private final OrganisationRepository repo;
    private final OrganisationMapper mapper;
    private final UserRepository userRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;
    private final SchoolRepository schoolRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrganisationResponse createOrganisation(CreateOrganisationRequest request) {
        Organisation newOrg = mapper.toEntity(request);
        newOrg.setApiKey(UUID.randomUUID());
        Organisation saved = repo.save(newOrg);
        
        kafkaTemplate.send("organisation-created", saved.getId().toString());

        // Links the user who created the organisation
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepo.findById(UUID.fromString(currentUser.userId())).orElseThrow();
        user.setOrganisationId(saved.getId());
        userRepo.save(user);

        return mapper.toResponse(saved);
    }

    public OrganisationResponse getOrganisation(String id) {
        return mapper.toResponse(getOrganisationEntity(id));
    }

    public Organisation getOrganisationEntity(String id) {
        return repo.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Organisation not found"));
    }

    public OrganisationResponse updateOrganisation(String id, UpdateOrganisationRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentUser.organisationId() == null || !currentUser.organisationId().equals(id)) {
            throw new AccessDeniedException("Forbidden");
        }

        Organisation existingOrg = getOrganisationEntity(id);
        existingOrg.setName(request.getName());
        existingOrg.setCouncilIctPhone(request.getCouncilIctPhone());
        existingOrg.setCouncilPortalUrl(request.getCouncilPortalUrl());
        existingOrg.setCouncilIspProvider(request.getCouncilIspProvider());

        return mapper.toResponse(repo.save(existingOrg));
    }

    public School createSchool(String councilId, SchoolRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentUser.organisationId() == null || !currentUser.organisationId().equals(councilId)) {
            throw new AccessDeniedException("Forbidden");
        }

        School school = new School();
        school.setOrganisationId(UUID.fromString(councilId));
        school.setName(request.getName());
        school.setBuildingCode(request.getBuildingCode());
        school.setSiteLocation(request.getSiteLocation());
        school.setHostnameRegex(request.getHostnameRegex());
        school.setLocalHelpdeskPhone(request.getLocalHelpdeskPhone());
        school.setServerRoomLocation(request.getServerRoomLocation());
        school.setServerCabinetKeyInfo(request.getServerCabinetKeyInfo());

        return schoolRepo.save(school);
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

    public String validateAPIKey(String apiKey) {
        Organisation org = repo.findByApiKey(UUID.fromString(apiKey))
            .orElseThrow(() -> new AccessDeniedException("Invalid API key"));
        return org.getId().toString();
    }

    public List<School> getSchoolsForCouncil(String councilId) {
        return schoolRepo.findByOrganisationId(UUID.fromString(councilId));
    }
}
