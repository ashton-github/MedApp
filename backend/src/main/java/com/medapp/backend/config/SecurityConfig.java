package com.medapp.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.medapp.backend.security.JwtAuthenticationFilter;

/**
 * Configuration de sécurité MINIMALE pour permettre à l'équipe de démarrer
 * le développement (accès libre à tous les endpoints /api/**).
 *
 * >>> A REMPLACER lors de la Semaine 2 (Authentification sécurisée) <<<
 * par une configuration basée sur des tokens JWT :
 *   - filtre JwtAuthenticationFilter
 *   - règles d'autorisation par rôle (MEDECIN, SECRETAIRE, ADMIN)
 *   - endpoints /api/auth/** publics, le reste protégé
 *
 * Le PasswordEncoder (BCrypt) est déjà exposé ici car il sera nécessaire
 * dès les premiers tests du service d'inscription (TDD).
 */
@Configuration
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter){
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/health" , "/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
