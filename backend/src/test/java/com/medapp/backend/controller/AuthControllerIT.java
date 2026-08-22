package com.medapp.backend.controller;

import org.junit.jupiter.api.AfterEach;
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
import com.medapp.backend.TestDataFactory;
import com.medapp.backend.dto.LoginRequest;
import com.medapp.backend.dto.RegisterRequest;
import com.medapp.backend.model.User;
import com.medapp.backend.repository.UserRepository;

import org.testcontainers.junit.jupiter.Container;

import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AuthControllerIT {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void nettoyageBase() {
        userRepository.deleteAll();
    }

    private void enregistrer(RegisterRequest request) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private MvcResult connecter(LoginRequest request) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private Cookie enregistrerEtRecupererCookieRefresh(String email) throws Exception {
        enregistrer(TestDataFactory.unRegisterRequest(email));
        MvcResult loginResult = connecter(TestDataFactory.unLoginRequest(email));
        return loginResult.getResponse().getCookie("refresh_token");
    }

    @Test
    void register_retourne201_siInscriptionValide() throws Exception {
        RegisterRequest request = TestDataFactory.unRegisterRequest("medecin@medapp.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("medecin@medapp.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void register_retourne409_siEmailDejaUtilise() throws Exception {
        RegisterRequest request = TestDataFactory.unRegisterRequest("medecin@medapp.com");

        enregistrer(request);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_retourne400_siMotDePasseInvalide() throws Exception {
        RegisterRequest requete = TestDataFactory.unRegisterRequestMotDePasseFaible("motdepasse-faible@medapp.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requete)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_retourne200_siIdentifiantsCorrects() throws Exception {
        String email = "login-test@medapp.com";
        enregistrer(TestDataFactory.unRegisterRequest(email));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(TestDataFactory.unLoginRequest(email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.prenom").exists())
                .andExpect(jsonPath("$.nom").exists())
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true));
    }

    @Test
    void login_retourne401_siMotDePasseIncorrect() throws Exception {
        String email = "mauvaismdp@medapp.com";
        enregistrer(TestDataFactory.unRegisterRequest(email));

        LoginRequest loginRequest = TestDataFactory.unLoginRequest(email, "incorrectPassword1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_retourne401_siCompteInexistant() throws Exception {
        LoginRequest loginRequest = TestDataFactory.unLoginRequest("inconnu@medapp.com", "motDePasse1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_retourne403_siCompteDesactive() throws Exception {
        String email = "desactive@medapp.com";
        enregistrer(TestDataFactory.unRegisterRequest(email));

        // puis on le desactive directement en base
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setActif(false);
        userRepository.save(user);

        LoginRequest loginRequest = TestDataFactory.unLoginRequest(email);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshToken_retourne200_siRefreshtokenValide() throws Exception {
        Cookie refreshCookie = enregistrerEtRecupererCookieRefresh("refresh-test@medapp.com");

        mockMvc.perform(post("/api/auth/refresh-token")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.prenom").exists())
                .andExpect(jsonPath("$.nom").exists())
                .andExpect(header().string("Set-Cookie", Matchers.containsString("refresh_token=")))
                .andExpect(header().string("Set-Cookie", Matchers.containsString("HttpOnly")));
    }

    @Test
    void refreshToken_retourne401_siRefreshTokenInvalide() throws Exception {
        Cookie cookieInvalide = new Cookie("refresh_token", "token-invalide-ou-corrompu");

        mockMvc.perform(post("/api/auth/refresh-token")
                        .cookie(cookieInvalide))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_retourne204_etEffaceLeCookieRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refresh_token", 0));
    }

    @Test
    void accessEndpoitProtege_retourne401_siAucunToken() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType("application/json")
                        .content("{="))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_retourneMessageCoherent_siMotDePasseInvalide() throws Exception {
        RegisterRequest requete = TestDataFactory.unRegisterRequestMotDePasseFaible("format-erreur@medapp.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requete)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void refreshToken_lanceException_siAucunCookiePresent() throws Exception {
        mockMvc.perform(post("/api/auth/refresh-token"))
                // no cookie() attached at all
                .andExpect(status().isUnauthorized());
    }

}