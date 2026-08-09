package com.medapp.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.medapp.backend.dto.UserResponse;
import com.medapp.backend.model.Role;
import com.medapp.backend.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public List<UserResponse> listerMedecinsActifs() {
    return userRepository.findByRoleAndActifTrue(Role.MEDECIN).stream()
        .map(u -> new UserResponse(u.getId(), u.getNom(), u.getPrenom()))
        .toList();
}
    
}
