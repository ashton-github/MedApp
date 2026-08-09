package com.medapp.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.medapp.backend.model.Role;
import com.medapp.backend.model.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    private final JwtService jwtService = new JwtService(
        "une-cle-secrete-suffisamment-longue-pour-les-tests-hs256",
        1800000,
        604800000
    );

    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void nettoyageContextSecurity(){
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_authentifieUtilisateur_siTokenValide()throws Exception {
        //
        User user = new User("medcin@medapp.com" , "hashedPassword" , "Dupont" , "Jean" , Role.MEDECIN , true , LocalDateTime.now() , null);
        user.setId("user-id-123");
        String token = jwtService.generateAccessToken(user);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        //when
        filter.doFilterInternal(request , response , filterChain);
        
        //then
        Authentication auth  = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(UtilisateurAuthentifie.class , auth.getPrincipal().getClass());

        UtilisateurAuthentifie principal = (UtilisateurAuthentifie) auth.getPrincipal();
        assertEquals("user-id-123", principal.id());
        assertEquals(Role.MEDECIN, principal.role());
        verify(filterChain).doFilter(request , response);

    }

    @Test 
    void doFilterInternal_neFaitRien_siAucunHeaderAuthorization()throws Exception {
        //

        when(request.getHeader("Authorization")).thenReturn(null);

        //when
        filter.doFilterInternal(request , response , filterChain);

        //then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(null, auth);
        verify(filterChain).doFilter(request , response);
    }

    @Test
    void doFilterInternal_neFaitRien_siTokenInvalid()throws Exception {
        //
        when(request.getHeader("Authorization")).thenReturn("Bearer token-invalide-ou-corrompu");

        //when
        filter.doFilterInternal(request, response, filterChain);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(null, auth);
        verify(filterChain).doFilter(request , response);

    }

   @Test
    void doFilterInternal_neFaitRien_siRefreshTokenUtiliseCommeAccessToken() throws Exception {
        // Given
        User user = new User("medecin@medapp.com", "hashedPassword", "Dupont", "Jean",
                Role.MEDECIN, true, LocalDateTime.now(), null);
        user.setId("user-id-123");
        String refreshToken = jwtService.generateRefreshToken(user);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + refreshToken);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(null, auth);
        verify(filterChain).doFilter(request, response);
    }

}
