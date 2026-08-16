package com.medapp.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.medapp.backend.TestDataFactory;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.User;

public class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
        "une-cle-secret-suffisamment-longue-pour-les-tests-hs256" , 
        1800000,
        604800000
    );

    @Test
    void generateToken_creeUnTokenValide(){
        //
        User user = TestDataFactory.unUtilisateur(Role.MEDECIN);

        //
        String token = jwtService.generateAccessToken(user);
        //
        assertNotNull(token);


    }

    @Test 
    void extractUserId_retourneEdUtilisateur_pourTokenValide(){
        User user = TestDataFactory.unUtilisateur(Role.MEDECIN);

        user.setId("user-id-123");
        String token = jwtService.generateAccessToken(user);
        
        //when
        String userId = jwtService.extractUserId(token);

        //then
        assertEquals("user-id-123" , userId);
    }

    @Test
    void extractRole_retourneRoleUtilisateur_pourTokenValide(){
        //
        User user = TestDataFactory.unUtilisateur(Role.MEDECIN);
        user.setId("user-id-123");

        String token = jwtService.generateAccessToken(user);

        //when
        String role  = jwtService.extractRole(token);

        //then
        assertEquals("MEDECIN", role);

    }

    @Test
    void isTokenValid_retourneFalse_pourTokenExpire(){
        //
        JwtService jwtServiceExpirationImmediate = new JwtService("une-cle-secrete-suffisament-longue-pour-les-tests-hs356",  -1000 , 604800000);//expiration dans le passe 

        User user = new User("medcin@medapp.com" , "hashedPassword" , "Dupont" , "Jean" , Role.MEDECIN , true , LocalDateTime.now() , null);
        user.setId("user-id-123");

        String tokenExpire = jwtServiceExpirationImmediate.generateAccessToken(user);

        //when
        boolean estValide = jwtService.isAccessTokenValid(tokenExpire);

        //then
        assertEquals(false, estValide);        

    }

    @Test 
    void isTokenValid_retourneFalse_pourTokenSigneAvecMauvaiseCle(){
        //
        JwtService jwtSericeAutreCle = new JwtService("une-cle-secrete-suffisament-longue-pour-les-tests-hs356",  1800000 , 604800000);
        User user = TestDataFactory.unUtilisateur(Role.MEDECIN);
        user.setId("user-id-123");

        String tokenSigneAvecAutreCle = jwtSericeAutreCle.generateAccessToken(user);

        //when
        boolean estValide = jwtService.isAccessTokenValid(tokenSigneAvecAutreCle);

        //then
        assertEquals(false, estValide);

    }

    @Test
    void generateRefreshToken_creeUnTokenAvecTypeRefresh(){
        //
        User user = TestDataFactory.unUtilisateur(Role.MEDECIN);
        user.setId("user-id-123");

        String refreshToken = jwtService.generateRefreshToken(user);

        //then
        assertNotNull(refreshToken);
        assertEquals("refresh", jwtService.extractTokenType(refreshToken));

    }


    @Test 
    void isTokenValid_retourneFalse_siRefreshTokenUtiliseCommeAccessToken(){
        User user = TestDataFactory.unUtilisateur(Role.MEDECIN);
        user.setId("user-id-123");

        String refreshToken = jwtService.generateRefreshToken(user);

        boolean estValideCommeAccessToken = jwtService.isAccessTokenValid(refreshToken);

        assertEquals(false, estValideCommeAccessToken);
    }
    
}
