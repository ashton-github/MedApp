package com.medapp.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.medapp.backend.TestDataFactory;
import com.medapp.backend.exception.DonneesInvalidesException;
import com.medapp.backend.exception.NumeroSecuriteSocialeDejaExistantException;
import com.medapp.backend.exception.PatientIntrouvableException;
import com.medapp.backend.model.Patient;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.User;
import com.medapp.backend.repository.PatientRepository;
import com.medapp.backend.repository.UserRepository;


@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    
    @Mock 
    private UserRepository userRepository;

    @InjectMocks
    private PatientService patientService;

    @Test
    void creerPatient_reussit_siDonneesValides(){
        //given
        Patient patient = TestDataFactory.unPatient();
        
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));
        //when
        Patient patientCree = patientService.creerPatient(patient , null , Role.SECRETAIRE);

        //then
        assertEquals("Dupont", patientCree.getNom());
        assertNotNull(patientCree.getDateCreation());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void creerPatient_forceLeMedecinReferentAuMedecinConnecte_siRoleMedecin(){
        Patient patient = TestDataFactory.unPatient();
        patient.setMedecinReferent("medecin-triche"); // valeur envoyée par le client, doit être ignorée

        when(patientRepository.findByNumeroSecuriteSociale(any())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        Patient result = patientService.creerPatient(patient, "medecin-1", Role.MEDECIN);

        assertEquals("medecin-1", result.getMedecinReferent());
        verify(userRepository, never()).findById(anyString()); // validerMedecinReferent pas appelé pour un MEDECIN
    }

    @Test
    void creerPatient_lanceException_siNumeroSecuriteSocialeDejaExistant(){
        //given
        String numero = "1900512123456";
        Patient patientExistant = TestDataFactory.unPatient();
        patientExistant.setNumeroSecuriteSociale(numero);
        
        when(patientRepository.findByNumeroSecuriteSociale(numero)).thenReturn(Optional.of(patientExistant));

        Patient nouveauPatient = TestDataFactory.unPatient();
        nouveauPatient.setNumeroSecuriteSociale(numero);

        assertThrows(NumeroSecuriteSocialeDejaExistantException.class, () -> 
            patientService.creerPatient(nouveauPatient , null , Role.SECRETAIRE)
        );
        verify(patientRepository , never()).save(any(Patient.class));
    }


    @Test
    void creerPatient_lanceException_siDonneesInvalides(){
        //given
        Patient patientDateFuture = TestDataFactory.unPatient();
        patientDateFuture.setDateNaissance(LocalDate.now().plusDays(1)); // date de naissance dans le futur
     
        assertThrows(DonneesInvalidesException.class, () -> 
            patientService.creerPatient(patientDateFuture , null , Role.SECRETAIRE));

        verify(patientRepository , never()).save(any(Patient.class));
    }

    @Test
    void rechercherPatients_retournePatientsCorrespondants_parNomOuPrenom(){
        String requete = "dupo";

        Patient patient1 = TestDataFactory.unPatient();
        patient1.setId("patient-1");

        Patient patient2 = TestDataFactory.unPatient();
        patient2.setNom("Martin");
        patient2.setPrenom("Dupois");
        patient2.setId("patient-2");

        when(patientRepository.findByNomContainingIgnoreCase(requete)).thenReturn(List.of(patient1));
        when(patientRepository.findByPrenomContainingIgnoreCase(requete)).thenReturn(List.of(patient2));

       
        List<Patient> resultats = patientService.rechercherPatients(requete , null , Role.SECRETAIRE);
        assertEquals(2, resultats.size());
    }


    @Test
    void obtenirPatient_reussit_siMedecinEstLeReferent(){
        Patient patient = TestDataFactory.unPatient();
        patient.setId("patient-1");
        patient.setMedecinReferent("medecin-1");

        when(patientRepository.findByIdAndMedecinReferent("patient-1", "medecin-1"))
            .thenReturn(Optional.of(patient));

        Patient result = patientService.obtenirPatient("patient-1", "medecin-1", Role.MEDECIN);

        assertEquals("patient-1", result.getId());
    }

    @Test
    void obtenirPatient_lanceException_siMedecinNestPasLeReferent(){
        when(patientRepository.findByIdAndMedecinReferent("patient-1", "medecin-2"))
            .thenReturn(Optional.empty());

        assertThrows(PatientIntrouvableException.class, () ->
            patientService.obtenirPatient("patient-1", "medecin-2", Role.MEDECIN));
    }

    @Test
    void obtenirPatient_lanceException_siIdInexistant_pourSecretaire(){
        String idInexistant = "id-inexistant";
        when(patientRepository.findById(idInexistant)).thenReturn(Optional.empty());

        assertThrows(PatientIntrouvableException.class, () -> 
            patientService.obtenirPatient(idInexistant, null, Role.SECRETAIRE));
    }

    @Test
    void obtenirPatient_lanceException_siIdInexistant_pourMedecin(){
        String idInexistant = "id-inexistant";
        when(patientRepository.findByIdAndMedecinReferent(idInexistant, "medecin-1"))
            .thenReturn(Optional.empty());

        assertThrows(PatientIntrouvableException.class, () -> 
            patientService.obtenirPatient(idInexistant, "medecin-1", Role.MEDECIN));
    }

    @Test
    void modifierPatient_metAJourDateMiseAJour_siDonneesValides(){
        String id = "patient-existant-id";
        String medecinId = "medecin-1";
        Patient patientExistant = TestDataFactory.unPatient();
        patientExistant.setId(id);

        Patient patientModifie = TestDataFactory.unPatient();
        patientModifie.setTelephone("99999999");

        when(patientRepository.findByIdAndMedecinReferent(id , medecinId)).thenReturn(Optional.of(patientExistant));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Patient resultat = patientService.modifierPatient(id , patientModifie , medecinId);

        assertEquals("99999999", resultat.getTelephone());
        assertNotNull(resultat.getDateMiseAJour());

    }

    @Test
    void modifierPatient_lanceException_siIdInexistant() {
        // given
        String idInexistant = "id-inexistant";
        String medecinId = "medecin-1";
        Patient patientModifie = TestDataFactory.unPatient();

        when(patientRepository.findByIdAndMedecinReferent(idInexistant , medecinId)).thenReturn(Optional.empty());

        // then
        assertThrows(PatientIntrouvableException.class, () ->
            patientService.modifierPatient(idInexistant, patientModifie , medecinId)
        );
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void supprimerPatient_reussit_siPatientExiste() {
        // given
        String id = "patient-existant-id";
        String medecinId = "medecin-1";
        Patient patientExistant = TestDataFactory.unPatient();
        patientExistant.setId(id);

        when(patientRepository.findByIdAndMedecinReferent(id , medecinId)).thenReturn(Optional.of(patientExistant));

        // when
        patientService.supprimerPatient(id , medecinId);

        // then
        verify(patientRepository).deleteById(id);
    }

    @Test
    void supprimerPatient_lanceException_siIdInexistant(){
        String idInexistant = "id-inexistant";
        String medecinId = "medecin-1";
        when(patientRepository.findByIdAndMedecinReferent(idInexistant , medecinId )).thenReturn(Optional.empty());

        assertThrows(PatientIntrouvableException.class , () -> patientService.supprimerPatient(idInexistant , medecinId) );

        verify(patientRepository , never()).deleteById(anyString());
    }

    @Test
    void supprimerPatient_lanceException_siMedecinNestPasLeReferent(){
        String id = "patient-existant-id";
        String medecinId = "medecin-2";

        when(patientRepository.findByIdAndMedecinReferent(id, medecinId)).thenReturn(Optional.empty());

        assertThrows(PatientIntrouvableException.class, () ->
            patientService.supprimerPatient(id, medecinId));

        verify(patientRepository, never()).deleteById(anyString());
    }

    @Test
    void masquerNumeroSecuriteSociale_pourRoleSecretaire(){
        Patient patient = TestDataFactory.unPatient();
        patient.setNumeroSecuriteSociale("1900512123456");

        Patient result = patientService.appliquerMasquageSelonRole(patient , Role.SECRETAIRE);
        assertEquals("XXXXXXXXXX456", result.getNumeroSecuriteSociale());
    }

    @Test 
    void nePasMasquerNumeroSecuriteSociale_pourRoleMedecin(){
       
        Patient patientPourMedecin = TestDataFactory.unPatient();
        patientPourMedecin.setNumeroSecuriteSociale("1900512123456");
       
        Patient resultMedecin = patientService.appliquerMasquageSelonRole(patientPourMedecin , Role.MEDECIN);

        assertEquals("1900512123456", resultMedecin.getNumeroSecuriteSociale());
    }
    

    @Test 
    void listerPatients_retourneUnePageDePatients(){
        Patient patient1 = TestDataFactory.unPatient();
        patient1.setId("patient-1");

        Pageable pageable = PageRequest.of(0 , 10 );
        
        Page<Patient> pageAttendue = new PageImpl<>(List.of(patient1), pageable, 1);
        when(patientRepository.findAll(pageable)).thenReturn(pageAttendue);

        Page<Patient> result = patientService.listerPatients(pageable , null , Role.SECRETAIRE);

        assertEquals(1, result.getTotalElements());
        assertEquals("Dupont", result.getContent().get(0) .getNom());

        
    }

    @Test
    void listerPatients_retourneUniquementPatientsDuMedecin_siRoleMedecin(){
        Patient patient1 = TestDataFactory.unPatient();
        patient1.setId("patient-1");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> pageAttendue = new PageImpl<>(List.of(patient1), pageable, 1);

        when(patientRepository.findByMedecinReferent("medecin-1", pageable)).thenReturn(pageAttendue);

        Page<Patient> result = patientService.listerPatients(pageable, "medecin-1", Role.MEDECIN);

        assertEquals(1, result.getTotalElements());
        verify(patientRepository, never()).findAll(pageable);
    }

    @Test
    void rechercherPatients_neCherchePasQueChezSonMedecin_siRoleMedecin(){
        String requete = "dupo";
        String medecinId = "medecin-1";

        Patient patient1 = TestDataFactory.unPatient();
        patient1.setId("patient-1");

        when(patientRepository.findByMedecinReferentAndNomContainingIgnoreCase(medecinId, requete))
            .thenReturn(List.of(patient1));
        when(patientRepository.findByMedecinReferentAndPrenomContainingIgnoreCase(medecinId, requete))
            .thenReturn(List.of());

        List<Patient> resultats = patientService.rechercherPatients(requete, medecinId, Role.MEDECIN);

        assertEquals(1, resultats.size());
        verify(patientRepository, never()).findByNomContainingIgnoreCase(anyString());
    }

    @Test
    void modifierPatient_metAJourLePrenom_siDonneesValides() {
        String id = "patient-existant-id";
        String medecinId = "medecin-1";
        Patient patientExistant = TestDataFactory.unPatient();
        patientExistant.setId(id);
        
        Patient patientModifie = TestDataFactory.unPatient();
        patientModifie.setPrenom("Marie-Claire");   
        
        when(patientRepository.findByIdAndMedecinReferent(id , medecinId)).thenReturn(Optional.of(patientExistant));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Patient resultat = patientService.modifierPatient(id, patientModifie , medecinId);

        assertEquals("Marie-Claire", resultat.getPrenom());
    }

    @Test
    void modifierPatient_lanceException_siNouveauNumeroSecuriteSocialeDejaExistatn(){
        String id = "patient-existant-id";
        String medecinId = "medecin-1";

        Patient patientExistant = TestDataFactory.unPatient();
        patientExistant.setId(id);

        Patient patientModifie = TestDataFactory.unPatient();
        patientModifie.setNumeroSecuriteSociale("1900513999999"); // NSS belonging to another patient
        patientModifie.setId("patient-modifie-id");

        Patient autrePatient = TestDataFactory.unPatient();
        autrePatient.setNumeroSecuriteSociale("1900513999999"); // NSS belonging to another patient
        autrePatient.setId("autre-patient-id");

        when(patientRepository.findByIdAndMedecinReferent(id , medecinId)).thenReturn(Optional.of(patientExistant));
        when((patientRepository.findByNumeroSecuriteSociale("1900513999999"))).thenReturn(Optional.of(autrePatient));

        assertThrows(NumeroSecuriteSocialeDejaExistantException.class,
            () -> patientService.modifierPatient(id , patientModifie , medecinId));

    }

    @Test
    void creerPatient_reussit_siMedcinReferentAbsent(){
        Patient patient = TestDataFactory.unPatient();
       
        when(patientRepository.findByNumeroSecuriteSociale(any())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        Patient result = patientService.creerPatient(patient , null , Role.SECRETAIRE);
        
        assertNotNull(result);
    }

    @Test
    void creerPatient_lanceException_siMedecinReferentInexistant(){
        Patient patient = TestDataFactory.unPatient();
        patient.setMedecinReferent("id-inexistant");

        when(patientRepository.findByNumeroSecuriteSociale(any())).thenReturn(Optional.empty());
        when(userRepository.findById("id-inexistant")).thenReturn(Optional.empty());

        assertThrows(DonneesInvalidesException.class,
            () ->  patientService.creerPatient(patient , null , Role.SECRETAIRE));
    }
    
    @Test
    void creerPatient_lanceException_siMedecinReferentNaPasLeRoleMedecin(){

        Patient patient = TestDataFactory.unPatient();
        patient.setMedecinReferent("id-secretaire");

        User secretaire = TestDataFactory.unUtilisateur(Role.SECRETAIRE);
        secretaire.setId("id-secretaire");

        when(patientRepository.findByNumeroSecuriteSociale(any())).thenReturn(Optional.empty());
        when(userRepository.findById("id-secretaire")).thenReturn(Optional.of(secretaire));

        assertThrows(DonneesInvalidesException.class,
            () -> patientService.creerPatient(patient , null , Role.SECRETAIRE));
    }

        @Test
        void creerPatient_reussit_siDateNaissanceNulle(){
            // Couvre la branche "dateNaissance == null" (court-circuit du &&, L37)
            Patient patient = TestDataFactory.unPatient();
            patient.setDateNaissance(null);
    
            when(patientRepository.findByNumeroSecuriteSociale(any())).thenReturn(Optional.empty());
            when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));
    
            Patient result = patientService.creerPatient(patient , null , Role.SECRETAIRE);
    
            assertNotNull(result);
        }

            @Test
        void modifierPatient_neVerifiePasDoublon_siNumeroNonFourni(){
            // Couvre la branche "nouveauNumero == null" (court-circuit du &&, L70)
            String id = "patient-existant-id";
            String medecinId = "medecin-1";
            Patient patientExistant = TestDataFactory.unPatient();
            patientExistant.setId(id);
            patientExistant.setNumeroSecuriteSociale("1900512123456");
    
            Patient patientModifie = TestDataFactory.unPatient();
            patientModifie.setNumeroSecuriteSociale(null);
    
            when(patientRepository.findByIdAndMedecinReferent(id , medecinId)).thenReturn(Optional.of(patientExistant));
            when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));
    
            patientService.modifierPatient(id, patientModifie , medecinId);
    
            verify(patientRepository, never()).findByNumeroSecuriteSociale(anyString());
        }

            @Test
        void modifierPatient_neLancePasException_siNumeroAppartientAuPatientLuiMeme(){
            // Couvre la branche du filter() où le patient trouvé EST le patient
            // en cours de modification (L72) — pas un vrai doublon.
            String id = "patient-existant-id";
            String medecinId = "medecin-1";
            Patient patientExistant = TestDataFactory.unPatient();
            patientExistant.setId(id);
            patientExistant.setNumeroSecuriteSociale("1900512111111");
    
            Patient patientModifie = TestDataFactory.unPatient();
            patientModifie.setNumeroSecuriteSociale("1900512222222");
    
            Patient memePatientTrouve = TestDataFactory.unPatient();
            memePatientTrouve.setId(id); // même id que le patient modifié
            memePatientTrouve.setNumeroSecuriteSociale("1900512222222");
    
            when(patientRepository.findByIdAndMedecinReferent(id , medecinId)).thenReturn(Optional.of(patientExistant));
            when(patientRepository.findByNumeroSecuriteSociale("1900512222222"))
                .thenReturn(Optional.of(memePatientTrouve));
            when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));
    
            assertDoesNotThrow(() -> patientService.modifierPatient(id, patientModifie , medecinId));
        }

        @Test
        void modifierPatient_lanceException_siMedecinNestPasLeReferent(){
            String id = "patient-existant-id";
            String medecinId = "medecin-2";
            Patient patientModifie = TestDataFactory.unPatient();

            when(patientRepository.findByIdAndMedecinReferent(id, medecinId)).thenReturn(Optional.empty());

            assertThrows(PatientIntrouvableException.class, () ->
                patientService.modifierPatient(id, patientModifie, medecinId));

            verify(patientRepository, never()).save(any(Patient.class));
        }

            @Test
        void appliquerMasquageSelonRole_neMasquePas_siNumeroNull(){
            // Couvre la branche "numero == null" (L104)
            Patient patient = TestDataFactory.unPatient();
            patient.setNumeroSecuriteSociale(null);
    
            Patient result = patientService.appliquerMasquageSelonRole(patient, Role.SECRETAIRE);
    
            assertNull(result.getNumeroSecuriteSociale());
        }

            @Test
        void appliquerMasquageSelonRole_neMasquePas_siNumeroTropCourt(){
            // Couvre la branche "numero.length() < 3" (L104)
            Patient patient = TestDataFactory.unPatient();
            patient.setNumeroSecuriteSociale("12");
    
            Patient result = patientService.appliquerMasquageSelonRole(patient, Role.SECRETAIRE);
    
            assertEquals("12", result.getNumeroSecuriteSociale());
        }

            @Test
        void creerPatient_reussit_siMedecinReferentEstValide(){
            // Couvre la branche de succès de validerMedecinReferent (L123-126),
            // jamais atteinte jusqu'ici : seuls les cas d'erreur étaient testés.
            Patient patient = TestDataFactory.unPatient();
            patient.setMedecinReferent("id-medecin");
    
            User medecin = TestDataFactory.unUtilisateur(Role.MEDECIN);
            medecin.setId("id-medecin");
    
            when(patientRepository.findByNumeroSecuriteSociale(any())).thenReturn(Optional.empty());
            when(userRepository.findById("id-medecin")).thenReturn(Optional.of(medecin));
            when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));
    
            Patient result = patientService.creerPatient(patient , null , Role.SECRETAIRE);
    
            assertNotNull(result);
            verify(userRepository).findById("id-medecin");
        }








}
