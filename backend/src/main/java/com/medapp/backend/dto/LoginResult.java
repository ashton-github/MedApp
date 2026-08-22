package com.medapp.backend.dto;

import com.medapp.backend.model.Role;


//utilisation interne 
public record LoginResult(String userId , String accessToken ,String refreshToken ,  Role role , String prenom , String nom) {
    
}
