package com.medapp.backend.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.medapp.backend.exception.CompteDesactiveException;
import com.medapp.backend.exception.EmailDejaUtiliseException;
import com.medapp.backend.exception.IdentifiantsInvalidesException;
import com.medapp.backend.exception.MotDePasseInvalideException;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.User;
import com.medapp.backend.repository.UserRepository;
import com.medapp.backend.security.JwtService;

public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
    
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

    public LoginResult login(String email  , String password) {
        User user = userRepository.findByEmail(email).orElseThrow(IdentifiantsInvalidesException::new);

        if(!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IdentifiantsInvalidesException();
        }
        if(!user.isActif()){
            throw new CompteDesactiveException();
        }
        
        String token = jwtService.generateToken(user);
        return new LoginResult(token , user.getRole());
    }
    
}
