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

    public String generateToken(UUID userId) {
        String id = userId.toString();
        
        return Jwts.builder()
            .subject(id)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(KEY)
            .compact();
    }

    public String extractUserId(String token) {
        return Jwts.parser()
            .verifyWith(KEY)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }
}

