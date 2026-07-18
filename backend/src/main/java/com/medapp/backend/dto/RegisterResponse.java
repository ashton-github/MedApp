package com.medapp.backend.dto;

import com.medapp.backend.model.Role;

public record RegisterResponse(String id , String email , String nom , String prenom , Role role ) {
    
}
