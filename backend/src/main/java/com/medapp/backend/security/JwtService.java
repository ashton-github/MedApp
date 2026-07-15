package com.medapp.backend.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.medapp.backend.model.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    //cle secrete generee une fois au demarrage a deplace en config(application.yml)
    private final SecretKey secretKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
    
    private static final long EXPIRATION_MS = 30*60*1000; //30 mins


    public String generateToken(User user) {
        Date maintenant = new Date();
        Date expiration = new Date(maintenant.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .subject(user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(maintenant)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public String extractUserId(String token){
        return Jwts.parser()        
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    
}
