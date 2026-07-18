package com.medapp.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

        //
        LoginResult result = authService.login(email , password);


        //then
        assertEquals("fake-jwt-token", result.token());
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
    
}
