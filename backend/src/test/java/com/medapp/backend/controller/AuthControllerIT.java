package com.medapp.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medapp.backend.dto.LoginRequest;
import com.medapp.backend.dto.RegisterRequest;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.User;
import com.medapp.backend.repository.UserRepository;

import org.testcontainers.junit.jupiter.Container;

import org.springframework.test.web.servlet.MvcResult;
import jakarta.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AuthControllerIT {

    @Container
    @ServiceConnection
    static MongoDBContainer  mongoDBContainer = new MongoDBContainer("mongo:4.4");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository  userRepository;

    @Test
    void register_retourne201_siInscriptionValide()throws Exception {
        //
        RegisterRequest request = new RegisterRequest(
            "medecin@medapp.com" , "MotDePasse123!" , "Dupont" , "Jean" , Role.MEDECIN
        );

        //when/then
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("medecin@medapp.com"))
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());   
    }

    @Test
    void register_retourne409_siEmailDejaUtilise()throws Exception {
        //
        RegisterRequest rquest = new RegisterRequest(
            "medecin@medapp.com" , "MotDePasse123!" , "Dupont" , "Jean" , Role.MEDECIN
        );

        //on inscrit une premiere fois
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(rquest)));

        //when / then la deuxieme inscription avec la meme email doit echouer
        mockMvc.perform(post("/api/auth/register")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(rquest)))
                .andExpect(status().isConflict());

    }

    @Test 
    void register_retourne400_siMotDePasseInvalide()throws Exception {//password is too weak 
        //
        RegisterRequest requet = new RegisterRequest("autre@medapp@com", "123", "Dupont","Jean",Role.MEDECIN);
        
        //when /then
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requet)))
                .andExpect(status().isBadRequest());       
    }

    @Test
    void login_retourne200_siIdentifiantsCorrects() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "login-test@medapp.com", "MotDePasse123!", "Dupont", "Jean", Role.MEDECIN
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("login-test@medapp.com", "MotDePasse123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())  // nouvelle vérification
                .andExpect(cookie().exists("refresh_token"))            // nouvelle vérification
                .andExpect(cookie().httpOnly("refresh_token", true));   // nouvelle vérification
    }


    @Test
    void login_retourne401_siMotDePasseIncorrect()throws Exception {
        //
        RegisterRequest registerRequest = new RegisterRequest(
                "mauvaismdp@medapp.com", "MotDePasse123!", "Dupont", "Jean", Role.MEDECIN
        );
        mockMvc.perform(post("/api/auth/register")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
                
        LoginRequest loginRequest = new LoginRequest("mauvaismdp@medapp.com", "incorrectPassword1!");

        //when then
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
    }

    @Test
    void login_retourne401_siCompteInexistant()throws Exception {
        //
        LoginRequest loginRequest = new LoginRequest("inconnu@medapp.com", "motDePasse1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());      
    }
    

     @Test
    void login_retourne403_siCompteDesactive()throws Exception {
        //
        RegisterRequest registerRequest = new RegisterRequest(
                "desactive@medapp.com", "MotDePasse123!", "Dupont", "Jean", Role.MEDECIN
        );
        mockMvc.perform(post("/api/auth/register")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
            
        //puis on le desactive directement en base 
        User user = userRepository.findByEmail("desactive@medapp.com").orElseThrow();
        user.setActif(false);
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("desactive@medapp.com", "MotDePasse123!");

        //when then
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isForbidden());
    }

    @Test
    void refreshToken_retourne200_siRefreshtokenValide() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "refresh-test@medapp.com", "MotDePasse123!", "Dupont", "Jean", Role.MEDECIN
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("refresh-test@medapp.com", "MotDePasse123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refresh_token");

        // When / Then
        mockMvc.perform(post("/api/auth/refresh-token")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

  
  @Test
    void refreshToken_retourne401_siRefreshTokenInvalide() throws Exception {
        // Given
        Cookie cookieInvalide = new Cookie("refresh_token", "token-invalide-ou-corrompu");

        // When / Then
        mockMvc.perform(post("/api/auth/refresh-token")
                        .cookie(cookieInvalide))
                .andExpect(status().isUnauthorized());
    }

}
