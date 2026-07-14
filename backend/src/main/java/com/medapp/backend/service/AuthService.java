package com.medapp.backend.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.medapp.backend.exception.EmailDejaUtiliseException;
import com.medapp.backend.exception.MotDePasseInvalideException;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.User;
import com.medapp.backend.repository.UserRepository;

public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
    
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String email , String password , String nom , String prenom, Role role, boolean isActive , LocalDateTime createdAt , LocalDateTime updatedAt) {
        if(userRepository.findByEmail(email).isPresent()){
            throw new EmailDejaUtiliseException(email);
        }

        if(password == null || !password.matches(PASSWORD_REGEX)){
            throw new MotDePasseInvalideException("Le mot de passe ne respecte pas les critères de sécurité.");
        }
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(email , hashedPassword , nom , prenom , role , isActive , createdAt , updatedAt);
        return userRepository.save(user);
    }
    
}
