package com.medapp.backend;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.medapp.backend.dto.LoginRequest;
import com.medapp.backend.dto.OrdonnanceRequest;
import com.medapp.backend.dto.PatientRequest;
import com.medapp.backend.dto.RegisterRequest;
import com.medapp.backend.model.*;

public class TestDataFactory {

    private static final AtomicLong COMPTEUR = new AtomicLong(1000000000000L);

    
    public static User unUtilisateur(Role role) {
        long unique = COMPTEUR.incrementAndGet();
        return new User(
            "user" + unique + "@medapp.com", "hashedpw", "Dupont", "Jean",
            role, true, LocalDateTime.now(), null
        );
    }

    private static final String MOT_DE_PASSE_VALIDE = "MotDePasse123!";

    public static RegisterRequest unRegisterRequest(String email, Role role) {
        return new RegisterRequest(email, MOT_DE_PASSE_VALIDE, "Dupont", "Jean", role);
    }

    public static RegisterRequest unRegisterRequest(String email) {
        return unRegisterRequest(email, Role.MEDECIN);
    }

    public static RegisterRequest unRegisterRequestMotDePasseFaible(String email) {
        return new RegisterRequest(email, "123", "Dupont", "Jean", Role.MEDECIN);
    }

    public static LoginRequest unLoginRequest(String email) {
        return new LoginRequest(email, MOT_DE_PASSE_VALIDE);
    }

    public static LoginRequest unLoginRequest(String email, String motDePasse) {
        return new LoginRequest(email, motDePasse);
    }

    public static Patient unPatient() {
        long unique = COMPTEUR.incrementAndGet();
        return new Patient(
            "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
            "12345678", "12 rue de la Paix", String.valueOf(unique),
            List.of(), null, null, null
        );
    }



    public static PatientRequest unPatientRequest() {
        long unique = COMPTEUR.incrementAndGet();
        return new PatientRequest(
            "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
            "12345678", "12 rue de la Paix", String.valueOf(unique),
            List.of(), null
        );
    }

    // convenience overload when a test needs to vary one field (e.g. invalid NSS, missing nom)
    public static PatientRequest unPatientRequest(String nom, String numeroSecuriteSociale) {
        return new PatientRequest(
            nom, "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
            "12345678", "12 rue de la Paix", numeroSecuriteSociale,
            List.of(), null
        );
    }

    // varies only the NSS — covers most PatientControllerIT cases
    public static PatientRequest unPatientRequest(String numeroSecuriteSociale) {
        return new PatientRequest(
            "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
            "12345678", "12 rue de la Paix", numeroSecuriteSociale,
            List.of(), null
        );
    }

    // invalid: blank nom + future birthdate, for @NotBlank / @Past validation tests
    public static PatientRequest unPatientRequestInvalide(String numeroSecuriteSociale) {
        return new PatientRequest(
            "", "Marie", LocalDate.now().plusDays(1), Sexe.F,
            "12345678", "12 rue de la Paix", numeroSecuriteSociale,
            List.of(), null
        );
    }

    // same patient, different telephone — for update tests
    public static PatientRequest unPatientRequestAvecTelephone(String numeroSecuriteSociale, String telephone) {
        return new PatientRequest(
            "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
            telephone, "12 rue de la Paix", numeroSecuriteSociale,
            List.of(), null
        );
    }

    public static Ordonnance uneOrdonnance(String patientId, String medecinId) {
        return new Ordonnance(
            patientId, medecinId, LocalDate.now(), LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            StatutOrdonnance.ACTIVE, null
        );
    }


    public static OrdonnanceRequest uneOrdonnanceRequest(String patientId) {
        return new OrdonnanceRequest(
            patientId,
            LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            null
        );
    }

    // override when a test needs to force expiry/edge cases
    public static OrdonnanceRequest uneOrdonnanceRequest(String patientId, LocalDate dateValidite) {
        return new OrdonnanceRequest(
            patientId,
            dateValidite,
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            null
        );
    }

    public static OrdonnanceRequest uneOrdonnanceRequest(String patientId, List<Medicament> medicaments) {
        return new OrdonnanceRequest(
            patientId, LocalDate.now().plusMonths(1), medicaments, null
        );
    }
}