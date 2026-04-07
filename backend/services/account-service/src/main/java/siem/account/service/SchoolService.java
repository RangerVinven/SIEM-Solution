package siem.account.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import siem.account.dto.CreateSchoolRequest;
import siem.account.dto.SchoolResponse;
import siem.account.dto.UpdateSchoolRequest;
import siem.account.dto.AddEmployeeRequest;
import siem.account.dto.UserResponse;
import siem.account.entity.School;
import siem.account.entity.User;
import siem.account.enums.Role;
import siem.account.mapper.SchoolMapper;
import siem.account.mapper.UserMapper;
import siem.account.repository.SchoolRepository;
import siem.account.repository.UserRepository;
import siem.models.UserPrincipal;

import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolService {
    private final SchoolRepository repo;
    private final SchoolMapper mapper;
    private final UserRepository userRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public SchoolResponse createSchool(CreateSchoolRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentUser.schoolId() != null) {
            throw new IllegalStateException("User already belongs to a school");
        }

        School newSchool = mapper.toEntity(request);
        newSchool.setApiKey(UUID.randomUUID());
        School saved = repo.save(newSchool);
        
        kafkaTemplate.send("school-created", saved.getId().toString());

        // Links the user who created the school
        User user = userRepo.findById(UUID.fromString(currentUser.userId())).orElseThrow();
        user.setSchoolId(saved.getId());

        userRepo.save(user);

        return mapper.toResponse(saved);
    }

    public SchoolResponse getSchool(String id) {
        return mapper.toResponse(getSchoolEntity(id));
    }

    public School getSchoolEntity(String id) {
        return repo.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("School not found"));
    }

    public SchoolResponse updateSchool(String id, UpdateSchoolRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentUser.schoolId() == null || !currentUser.schoolId().equals(id)) {
            throw new AccessDeniedException("Forbidden");
        }

        School existingSchool = getSchoolEntity(id);

        existingSchool.setName(request.getName());
        existingSchool.setBuildingCode(request.getBuildingCode());
        existingSchool.setSiteLocation(request.getSiteLocation());
        existingSchool.setServerRoomLocation(request.getServerRoomLocation());
        existingSchool.setServerCabinetKeyInfo(request.getServerCabinetKeyInfo());

        return mapper.toResponse(repo.save(existingSchool));
    }

    public void deleteSchool(String id) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentUser.schoolId() == null || !currentUser.schoolId().equals(id)) {
            throw new AccessDeniedException("Forbidden");
        }

        if (!"TECHNICAL_ADMIN".equals(currentUser.role())) {
            throw new AccessDeniedException("Forbidden");
        }

        if(!repo.existsById(UUID.fromString(id))) {
            throw new RuntimeException("School not found");
        }

        repo.deleteById(UUID.fromString(id));
    }

    public UserResponse addEmployee(String schoolId, AddEmployeeRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentUser.schoolId() == null || !currentUser.schoolId().equals(schoolId)) {
            throw new AccessDeniedException("Forbidden");
        }

        if (!"TECHNICAL_ADMIN".equals(currentUser.role()) && !"HEADTEACHER".equals(currentUser.role())) {
            throw new AccessDeniedException("Forbidden");
        }

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User newUser = userMapper.toEntity(request);
        newUser.setPasswordHash(encoder.encode(request.getPassword()));
        
        Role role = Role.HEADTEACHER;
        if (request.getRole() != null) {
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        
        if (role == Role.TECHNICAL_ADMIN && !"TECHNICAL_ADMIN".equals(currentUser.role())) {
            throw new AccessDeniedException("Only TECHNICAL_ADMINs can create other TECHNICAL_ADMINs");
        }
        
        newUser.setRole(role);
        newUser.setSchoolId(UUID.fromString(schoolId));

        User savedUser = userRepo.save(newUser);
        return userMapper.toResponse(savedUser);
    }

    public String validateAPIKey(String apiKey) {
        School school = repo.findByApiKey(UUID.fromString(apiKey))
            .orElseThrow(() -> new AccessDeniedException("Invalid API key"));

        return school.getId().toString();
    }

    public String getApiKey(String schoolId) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!schoolId.equals(currentUser.schoolId())) {
            throw new AccessDeniedException("Forbidden");
        }

        return getSchoolEntity(schoolId).getApiKey().toString();
    }
}
