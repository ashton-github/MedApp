package com.medapp.backend.service;

import com.medapp.backend.model.Role;

public record LoginResult(String token , Role role) {
    
}
