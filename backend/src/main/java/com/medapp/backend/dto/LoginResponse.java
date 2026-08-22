package com.medapp.backend.dto;

import com.medapp.backend.model.Role;

public record LoginResponse(String userId ,String accessToken , Role role , String prenom , String nom) {

 
    
}
