package siem.account.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import siem.account.dto.CreateUserRequest;
import siem.account.dto.LoginRequest;
import siem.account.dto.LoginResponse;
import siem.account.dto.UserResponse;
import siem.account.entity.User;
import siem.account.mapper.UserMapper;
import siem.account.repository.UserRepository;
import siem.account.enums.Role;
import siem.utils.JwtService;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    // public UserService(UserRepository repo, UserMapper mapper, PasswordEncoder encoder, JwtService jwtService) {
    //     this.repo = repo;
    //     this.mapper = mapper;
    //     this.encoder = encoder;
    //     this.jwtService = jwtService;
    // }

    public UserResponse registerUser(CreateUserRequest request) {
        if(repo.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Convert to user entity
        User newUser = mapper.toEntity(request);
        newUser.setPasswordHash(encoder.encode(request.getPassword()));
        newUser.setRole(Role.OWNER);

        // Saves the user and returns them
        User savedUser = repo.save(newUser);
        return mapper.toResponse(savedUser);
    }

    public String login(LoginRequest request) {
        User user = repo.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if(!encoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return jwtService.generateToken(user.getId());
    }
}
