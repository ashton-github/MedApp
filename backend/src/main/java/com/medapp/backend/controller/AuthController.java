package com.medapp.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medapp.backend.dto.LoginRequest;
import com.medapp.backend.dto.RegisterRequest;
import com.medapp.backend.dto.RegisterResponse;
import com.medapp.backend.model.User;
import com.medapp.backend.service.AuthService;
import com.medapp.backend.dto.LoginResult;
import com.medapp.backend.dto.RefreshTokenRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


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
    public ResponseEntity<LoginResult> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request.email() , request.password());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResult> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResult result = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(result);
    }
    
    
    
    
}
