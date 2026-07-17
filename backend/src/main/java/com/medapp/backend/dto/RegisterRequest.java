package com.medapp.backend.dto;

import com.medapp.backend.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @Email @NotBlank String email,
    @NotBlank String password,
    @NotBlank String nom,
    @NotBlank String prenom,
    Role role
) {
    
}
