package com.medapp.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medapp.backend.dto.LoginRequest;
import com.medapp.backend.dto.LoginResponse;
import com.medapp.backend.dto.RegisterRequest;
import com.medapp.backend.dto.RegisterResponse;
import com.medapp.backend.exception.IdentifiantsInvalidesException;
import com.medapp.backend.model.User;
import com.medapp.backend.service.AuthService;
import com.medapp.backend.dto.LoginResult;

import jakarta.validation.Valid;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.HttpHeaders;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(
            request.email(), request.password(), request.nom(), request.prenom(), 
            request.role(), true, java.time.LocalDateTime.now(), null);

        RegisterResponse response = new RegisterResponse(
            user.getId() , user.getEmail() , user.getNom() , user.getPrenom() , user.getRole()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request.email(), request.password());

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", result.refreshToken())
                .httpOnly(true)
                .secure(false) // à passer à true en production
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        LoginResponse response = new LoginResponse(result.accessToken(), result.role());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new IdentifiantsInvalidesException();
        }

        LoginResult result = authService.refreshToken(refreshToken);
        LoginResponse response = new LoginResponse(result.accessToken(), result.role());

        ResponseCookie refreshedCookie = ResponseCookie.from("refresh_token", result.refreshToken())
                .httpOnly(true)
                .secure(false) // à passer à true en production
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshedCookie.toString())
                .body(response);
    }
    
    
    
    
}
