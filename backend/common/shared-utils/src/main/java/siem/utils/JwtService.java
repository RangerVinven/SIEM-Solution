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
    @Value("${jwt.secret.key}")
    private String secretKey;
    private SecretKey KEY;

    @PostConstruct
    public void init() {
        this.KEY = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(UUID userId, UUID organisationId, String role, String firstName, String lastName) {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("orgId", organisationId != null ? organisationId.toString() : null)
            .claim("role", role)
            .claim("firstName", firstName)
            .claim("lastName", lastName)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(KEY)
            .compact();
    }

    public String extractUserId(String token) {
        return extractClaim(token, "sub");
    }

    public String extractOrgId(String token) {
        return extractClaim(token, "orgId");
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

