package com.medapp.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.medapp.backend.dto.LoginResult;
import com.medapp.backend.exception.CompteDesactiveException;
import com.medapp.backend.exception.EmailDejaUtiliseException;
import com.medapp.backend.exception.IdentifiantsInvalidesException;
import com.medapp.backend.exception.MotDePasseInvalideException;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.User;
import com.medapp.backend.repository.UserRepository;
import com.medapp.backend.security.JwtService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;



@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_creeUnUtilisateur_siEmailNonUtilise() {
        String email = "medecin@medapp.com";
        String motDePasse = "MotDePasse123!";

        when(passwordEncoder.encode(motDePasse)).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 
        User utilisateurCree = authService.register(email, motDePasse, "Dupont", "Jean", Role.MEDECIN , true , LocalDateTime.now() , null);

        assertEquals(email, utilisateurCree.getEmail());
        assertNotEquals(motDePasse, utilisateurCree.getPasswordHash());
        assertEquals("hashedPassword123", utilisateurCree.getPasswordHash());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void regiter_lanceException_siEmailDejaUtilise(){

        String  email = "medecin@medapp.com";
        User utilisateurExistant = new User(email , "hashedPassword123", "Dupont", "Jean", Role.MEDECIN , true , LocalDateTime.now() , null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(utilisateurExistant));

        assertThrows(EmailDejaUtiliseException.class , () ->
            authService.register(email , "MotDePasse" , "Martin" , "Paul" , Role.MEDECIN , true , LocalDateTime.now() , null)
        );


        verify(userRepository , never()).save(any(User.class));

    }

    @Test
    void register_lanceException_siMotDePasseInvalide(){
        //given 
        String email = "medecin@medapp.com";
        String motDePasseFaible  = "123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(MotDePasseInvalideException.class , () -> 
            authService.register(email , motDePasseFaible , "Martin" , "Paul" , Role.MEDECIN , true , LocalDateTime.now() , null)
        );

        verify(userRepository , never()).save(any(User.class));
    }

    @Mock
    private JwtService jwtService;

    @Test
    void login_retournerToken_siIdentifiantsCorrects(){
        //
        String email= "medecin@medapp.com";
        String password = "MotDePasse123!";
        User user = new User(email , "hashedPassword123", "Dupont", "Jean", Role.MEDECIN , true , LocalDateTime.now() , null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "hashedPassword123")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("fake-jwt-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("fake-refresh-token");

        //
        LoginResult result = authService.login(email , password);


        //then
        assertEquals("fake-jwt-token", result.accessToken());
        assertNotNull(result.refreshToken());
        assertEquals(Role.MEDECIN, result.role());
    }

    @Test
    void login_lanceException_siMotDePasseIncorrect(){
        //given 
        String email = "medecin@medapp.com";
        String motDePasseSaisi = "MauvaisMotDePasse122!";
        User user = new User(email , "hashedPassword123", "Dupont", "Jean", Role.MEDECIN , true , LocalDateTime.now() , null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(motDePasseSaisi , "hashedPassword123")).thenReturn(false);

        //
        assertThrows(IdentifiantsInvalidesException.class , () ->
            authService.login(email, motDePasseSaisi)
        );

    }

    @Test
    void login_lanceException_siCompteInexistant(){
        //
        String email = "inconnue@medapp.com";
        String motDePasse = "MotDePasse123!";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(IdentifiantsInvalidesException.class , () -> 
            authService.login(email, motDePasse)
        );

    }

    @Test
    void login_lanceException_siCompteDesactive(){
        //
        String email = "medecin@medapp.com";
        String password = "MotDePasse123!";
        User user = new User(email , "hashedPassword123", "Dupont", "Jean", Role.MEDECIN , false , LocalDateTime.now() , null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "hashedPassword123")).thenReturn(true);

        assertThrows(CompteDesactiveException.class , () -> 
            authService.login(email , password  )
        ); 

    }

    @Test
    void refreshToken_retouneNouvelAccessToken_siRefreshTokenValide(){
        User user = new User("medecin@medapp.com" , "hashedPassword123!", "Dupont", "Jean", Role.MEDECIN , true , LocalDateTime.now() , null);
        user.setId("user-id-123");
        String refreshToken = "valid-refresh-token";

        when(jwtService.isRefreshTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.extractUserId(refreshToken)).thenReturn("user-id-123");
        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("nouveau-accesss-token");

        LoginResult result = authService.refreshToken(refreshToken);

        assertEquals("nouveau-accesss-token", result.accessToken());
        assertEquals(refreshToken, result.refreshToken());
    }

    @Test 
    void refreshToken_lanceException_siRefreshTokenInvalide() {
        //
        String refreshTokenInvalide = "token-invalide-ou-expire";

        when(jwtService.isRefreshTokenValid(refreshTokenInvalide)).thenReturn(false);

        assertThrows(IdentifiantsInvalidesException.class, () -> authService.refreshToken(refreshTokenInvalide));
    }
    
    @Test 
    void refreshToken_lanceException_siCompteDesactive(){

        User user = new User("medecin@medapp.com" , "hashedPassword123!", "Dupont", "Jean", Role.MEDECIN , false , LocalDateTime.now() , null);
        user.setId("user-id-123");
        String refreshToken = "valid-refresh-token";

        when(jwtService.isRefreshTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.extractUserId(refreshToken)).thenReturn("user-id-123");
        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(user));

        assertThrows(CompteDesactiveException.class, () -> authService.refreshToken(refreshToken));
    }

    @Test
    void login_metAJourDerniereConnexion_siIdentifiantsCorrects() {
        // Given
        User user = new User("medecin@medapp.com", "hashedPassword123", "Dupont", "Jean",
                Role.MEDECIN, true, LocalDateTime.now().minusDays(1), null); // derniereConnexion = null au depart

        when(userRepository.findByEmail("medecin@medapp.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("MotDePasse123!", "hashedPassword123")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("fake-access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("fake-refresh-token");
        when(userRepository.save(user)).thenReturn(user);

        // When
        authService.login("medecin@medapp.com", "MotDePasse123!");

        // Then
        assertNotNull(user.getDerniereConnexion());
        verify(userRepository).save(user);
    }
}
