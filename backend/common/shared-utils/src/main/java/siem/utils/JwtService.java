package siem.utils;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    @Value("${JWT_SECRET_KEY}")
    private String secretKey;
    private SecretKey KEY;

    @PostConstruct
    public void init() {
        this.KEY = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(UUID userId, UUID schoolId, String role, String firstName, String lastName, String schoolName) {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("schoolId", schoolId != null ? schoolId.toString() : null)
            .claim("role", role)
            .claim("firstName", firstName)
            .claim("lastName", lastName)
            .claim("schoolName", schoolName)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(KEY)
            .compact();
    }

    public String extractUserId(String token) {
        return extractClaim(token, "sub");
    }

    public String extractSchoolId(String token) {
        return extractClaim(token, "schoolId");
    }

    public String extractRole(String token) {
        return extractClaim(token, "role");
    }

    public String extractFirstName(String token) {
        return extractClaim(token, "firstName");
    }

    public String extractLastName(String token) {
        return extractClaim(token, "lastName");
    }

    public String extractSchoolName(String token) {
        return extractClaim(token, "schoolName");
    }

    private String extractClaim(String token, String claimKey) {
        try {
            if ("sub".equals(claimKey)) {
                return Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            }
            return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get(claimKey, String.class);
        } catch (Exception e) {
            return null;
        }
    }
}
