package com.medapp.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

import com.medapp.backend.TestDataFactory;
import com.medapp.backend.exception.AccesRefuseException;
import com.medapp.backend.exception.OrdonnanceDejaArchiveeException;
import com.medapp.backend.exception.OrdonnanceIntrouvableException;
import com.medapp.backend.exception.PatientIntrouvableException;
import com.medapp.backend.model.Medicament;
import com.medapp.backend.model.Ordonnance;
import com.medapp.backend.model.Patient;
import com.medapp.backend.model.StatutOrdonnance;
import com.medapp.backend.repository.OrdonnanceRepository;
import com.medapp.backend.repository.PatientRepository;

import static org.mockito.Mockito.mock; 

@ExtendWith(MockitoExtension.class)
public class OrdonnanceServiceTest {

    @Mock
    private OrdonnanceRepository ordonnanceRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private OrdonnanceService ordonnanceService;


    @Test
    void creeOrdonnance_reussit_siPatientExistantEtMedecinAutorise(){
        String patientId = "patient-123";
        String medecinId = "medecin-1";

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent(medecinId);

        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientId, medecinId);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(ordonnanceRepository.save(any(Ordonnance.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordonnance result = ordonnanceService.creerOrdonnance(ordonnance, medecinId);

        assertNotNull(result);
        assertEquals(patientId, result.getPatientId());
    }

    @Test
    void creerOrdonnance_lanceException_siPatientInexistant(){
        String patientIdInexistant = "patient-inexistant";
        String medecinId = "medecin-1";

        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientIdInexistant, medecinId);

        when(patientRepository.findById(patientIdInexistant)).thenReturn(Optional.empty());

        assertThrows(PatientIntrouvableException.class,
            () -> ordonnanceService.creerOrdonnance(ordonnance, medecinId));

        verify(ordonnanceRepository, never()).save(any(Ordonnance.class));
    }

    @Test
    void creerOrdonnance_calculeStatutActive_siDateValiditeFuture() {
        String patientId = "patient-123";
        String medecinId = "medecin-1";

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent(medecinId);

        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientId, medecinId);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(ordonnanceRepository.save(any(Ordonnance.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordonnance resultat = ordonnanceService.creerOrdonnance(ordonnance, medecinId);

        assertEquals(StatutOrdonnance.ACTIVE, resultat.getStatut());
    }

    @Test
    void creerOrdonnance_calculeStatutExpiree_siDateValiditeDansLePasse(){
        String patientId = "patient-123";
        String medecinId = "medecin-1";

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent(medecinId);

        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientId, medecinId);
        ordonnance.setDateEmission(LocalDate.now().minusMonths(2));
        ordonnance.setDateValidite(LocalDate.now().minusDays(5));
        ordonnance.setStatut(null);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(ordonnanceRepository.save(any(Ordonnance.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordonnance resultat = ordonnanceService.creerOrdonnance(ordonnance, medecinId);

        assertEquals(StatutOrdonnance.EXPIREE, resultat.getStatut());
    }

    @Test
    void creerOrdonnance_lanceException_siMedecinNestPasLeReferentDuPatient(){
        String patientId = "patient-123";

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent("medecin-1");

        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientId, "medecin-2");

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        assertThrows(AccesRefuseException.class,
            () -> ordonnanceService.creerOrdonnance(ordonnance, "medecin-2"));

        verify(ordonnanceRepository, never()).save(any(Ordonnance.class));
    }

    @Test
    void archiverOrdonnance_passeStatutAArichivee_siOrdonnanceExiste() {
        String id = "ordonnance-1";
        String patientId = "patient-123";
        String medecinId = "medecin-1";

        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientId, medecinId);
        ordonnance.setId(id);

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent(medecinId);

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(ordonnanceRepository.save(any(Ordonnance.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordonnance result = ordonnanceService.archiverOrdonnance(id, medecinId);

        assertEquals(StatutOrdonnance.ARCHIVEE, result.getStatut());
    }

    @Test
    void archiverOrdonnance_lanceException_siIdInexistant(){
        String idInexistant = "id-inexistant";

        when(ordonnanceRepository.findById(idInexistant)).thenReturn(Optional.empty());

        assertThrows(OrdonnanceIntrouvableException.class,
            () -> ordonnanceService.archiverOrdonnance(idInexistant , "medecin-1") );

        verify(ordonnanceRepository , never()).save(any(Ordonnance.class));
    }


    @Test
    void obtenirHistorique_retourneOrdonnancesDuPatient_trieesParDate(){
        String patientId = "patient-123";
        String medecinId = "medecin-1";


        Ordonnance ancienne = TestDataFactory.uneOrdonnance(patientId, "medecin-1");
        ancienne.setDateEmission(LocalDate.now().minusMonths(2));
        ancienne.setDateValidite(LocalDate.now().minusMonths(1));
        ancienne.setStatut(StatutOrdonnance.EXPIREE);
        ancienne.setId("ordo-1");

        Ordonnance recente = TestDataFactory.uneOrdonnance(patientId, medecinId);

        recente.setId("ordo-2");

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent(medecinId);

        //repository returns them out of order , service must sort 
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(ordonnanceRepository.findByPatientId(patientId)).thenReturn(List.of(ancienne , recente));

        List<Ordonnance> result = ordonnanceService.obtenirHistorique(patientId , null , medecinId);

        assertEquals(2, result.size());
        assertEquals("ordo-2", result.get(0).getId());
        assertEquals("ordo-1", result.get(1).getId());

    }

    @Test
    void obtenirHistorique_filtreParStatut_siStatutFourni() {
        String patientId = "patient-123";
        String medecinId = "medecin-1";

        Ordonnance active = TestDataFactory.uneOrdonnance(patientId, "medecin-1");
        active.setId("ordo-active");

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent(medecinId);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(ordonnanceRepository.findByPatientIdAndStatut(patientId, StatutOrdonnance.ACTIVE)).thenReturn(List.of(active));

        List<Ordonnance> resultat = ordonnanceService.obtenirHistorique(patientId, StatutOrdonnance.ACTIVE , medecinId);

        assertEquals(1, resultat.size());
        assertEquals("ordo-active", resultat.get(0).getId());
    }

    @Test
    void obtenirHistorique_lanceException_siPatientInexistant() {
        String patientIdInexistant = "patient-inexistant";
        when(patientRepository.findById(patientIdInexistant)).thenReturn(Optional.empty());

        assertThrows(PatientIntrouvableException.class,
            () -> ordonnanceService.obtenirHistorique(patientIdInexistant, null , "medecin-1"));
    }

    @Test
    void obtenirHistorique_lanceException_siMedecinNestPasLeReferentDuPatient(){
        String patientId = "patient-123";

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent("medecin-1");

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        assertThrows(AccesRefuseException.class,
            () -> ordonnanceService.obtenirHistorique(patientId, null, "medecin-2"));

        verify(ordonnanceRepository, never()).findByPatientId(anyString());
        verify(ordonnanceRepository, never()).findByPatientIdAndStatut(anyString(), any());
    }


    @Test
    void archiverOrdonnance_lanceException_siMedecinNestPasLeReferentDuPatient(){
        String id = "ordonnance-1";
        String patientId = "patient-123";

        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientId, "medecin-1");
        ordonnance.setId(id);

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent("medecin-1");

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        assertThrows(AccesRefuseException.class, 
            () -> ordonnanceService.archiverOrdonnance(id , "medecin-2")
        );

        verify(ordonnanceRepository , never()).save(any(Ordonnance.class));
    }
      

   @Test
    void archiverOrdonnance_lanceException_siDejaArchivee() {
        String id = "ordonnance-1";
        String patientId = "patient-123";
        String medecinId = "medecin-1";

        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientId, medecinId);
        ordonnance.setStatut(StatutOrdonnance.ARCHIVEE);
        ordonnance.setId(id);

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent(medecinId);

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        assertThrows(OrdonnanceDejaArchiveeException.class,
            () -> ordonnanceService.archiverOrdonnance(id, medecinId));

        verify(ordonnanceRepository, never()).save(any(Ordonnance.class));
    }


    @Test
    void modifierOrdonnance_reussit_siMedecinEstLeReferentDuPatient(){
        String id = "ordonnance-1";
        String patientId = "patient-123";
        String medecinId = "medecin-1";

        Ordonnance ordonnanceExistante = TestDataFactory.uneOrdonnance(patientId, medecinId);
        ordonnanceExistante.setId(id);

        Ordonnance ordonnanceModifiee = TestDataFactory.uneOrdonnance(patientId, medecinId);
        ordonnanceModifiee.setStatut(null);
        ordonnanceModifiee.setRemarques("Dosage ajuste");
        ordonnanceModifiee.setMedicaments(List.of(new Medicament("Doliprance", "500mg", "2x/jour", "3 jours")));
        ordonnanceModifiee.setDateValidite(LocalDate.now().plusMonths(2));

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent(medecinId);

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnanceExistante));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(ordonnanceRepository.save(any(Ordonnance.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordonnance result = ordonnanceService.modifierOrdonnance(id, ordonnanceModifiee, medecinId);

        assertEquals("500mg", result.getMedicaments().get(0).getDosage());
        assertEquals("Dosage ajuste", result.getRemarques());
    }

    @Test
    void modifierOrdonnance_lanceException_siMedecinNestPasLeReferentDuPatient(){
        String id = "ordonnance-1";
        String patientId = "patient-123";

        Ordonnance ordonnanceExistante = TestDataFactory.uneOrdonnance(patientId, "medecin-1");
        ordonnanceExistante.setId(id);

        Ordonnance ordonnanceModifiee = TestDataFactory.uneOrdonnance(patientId, "medecin-1");
        ordonnanceModifiee.setStatut(null);
        ordonnanceModifiee.setRemarques("Dosage ajuste");
        ordonnanceModifiee.setMedicaments(List.of(new Medicament("Doliprance", "500mg", "2x/jour", "3 jours")));
        ordonnanceModifiee.setDateValidite(LocalDate.now().plusMonths(2));

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent("medecin-1");

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnanceExistante));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        assertThrows(AccesRefuseException.class,
            () -> ordonnanceService.modifierOrdonnance(id, ordonnanceModifiee, "medecin-2")
        );

        verify(ordonnanceRepository, never()).save(any(Ordonnance.class));
    }

    @Test
    void modifierOrdonnance_lanceException_siIdInexistant() {
        String idInexistant = "id-inexistant";
        Ordonnance ordonnanceModifiee = TestDataFactory.uneOrdonnance("patient-123", "medecin-1");

        when(ordonnanceRepository.findById(idInexistant)).thenReturn(Optional.empty());

        assertThrows(OrdonnanceIntrouvableException.class,
            () -> ordonnanceService.modifierOrdonnance(idInexistant, ordonnanceModifiee, "medecin-1"));

        verify(ordonnanceRepository, never()).save(any(Ordonnance.class));
    }

    @Test
    void modifierOrdonnance_lanceException_siOrdonnanceEstArchive(){
        String id = "ordonnance-1";
        String patientId = "patient-123";
        String medecinId = "medecin-1";

        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientId, medecinId);
        ordonnance.setStatut(StatutOrdonnance.ARCHIVEE);
        ordonnance.setId(id);

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent(medecinId);

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        assertThrows(OrdonnanceDejaArchiveeException.class,
            () -> ordonnanceService.modifierOrdonnance(id, ordonnance, medecinId)
        );
    }
 

    @Test
    void generatePdf_retourneDesBytesNonVides_siOrdonnanceExiste(){
        String id = "ordonnance-1";
        String patientId = "patient-123";
        String medecinId = "medecin-1";

        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientId, medecinId);
        ordonnance.setRemarques("RAS");
        ordonnance.setId(id);

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent(medecinId);

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        
        byte[] result = ordonnanceService.generatePdf(id , medecinId);

        assertNotNull(result);
        assertTrue(result.length > 0);
        //a real PDF file always starts with this exact byte signature
        assertEquals("%PDF", new String(result , 0 , 4));
    }

    @Test
    void generatePdf_lanceException_siIdInexistant(){
        String idInexistant = "id-inexistant";
        when(ordonnanceRepository.findById(idInexistant)).thenReturn(Optional.empty());

        assertThrows(OrdonnanceIntrouvableException.class,
            () -> ordonnanceService.generatePdf(idInexistant, "medecin-1"));
    }

    @Test
    void generatePdf_lanceException_siMedecinNestPasLeReferentDuPatient(){
        String id = "ordonnance-1";
        String patientId = "patient-123";

        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientId, "medecin-1");
        ordonnance.setId(id);

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent("medecin-1");

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        assertThrows(AccesRefuseException.class,
            () -> ordonnanceService.generatePdf(id, "medecin-2"));
    }


    @Test
    void obtenirOrdonnance_recalculeEtSauvegardeStatut_siDateValiditeDepassee() {
        String id = "ordonnance-1";
        String patientId = "patient-123";
        String medecinId = "medecin-1";

        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientId, medecinId);
        ordonnance.setDateEmission(LocalDate.now().minusMonths(2));
        ordonnance.setDateValidite(LocalDate.now().minusDays(1));
        ordonnance.setId(id);


         Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent(medecinId);

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(ordonnanceRepository.save(any(Ordonnance.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordonnance resultat = ordonnanceService.obtenirOrdonnance(id , medecinId);

        assertEquals(StatutOrdonnance.EXPIREE, resultat.getStatut());
        verify(ordonnanceRepository).save(any(Ordonnance.class));
    }


    @Test
    void obtenirOrdonnance_neResauvegardePas_siStatutDejaCorrect() {
        String id = "ordonnance-2";
        String patientId = "patient-123";
        String medecinId = "medecin-1";
        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientId, medecinId);
        ordonnance.setId(id);


         Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent(medecinId);

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        Ordonnance resultat = ordonnanceService.obtenirOrdonnance(id , medecinId);

        assertEquals(StatutOrdonnance.ACTIVE, resultat.getStatut());
        verify(ordonnanceRepository, never()).save(any(Ordonnance.class));
    }

    @Test
    void obtenirOrdonnance_lanceException_siMedecinNestPasLeReferentDuPatient(){
        String id = "ordonnance-1";
        String patientId = "patient-123";

        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientId, "medecin-1");
        ordonnance.setId(id);

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent("medecin-1"); // le référent, pas forcément le prescripteur

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        assertThrows(AccesRefuseException.class,
            () -> ordonnanceService.obtenirOrdonnance(id, "medecin-2"));

        verify(ordonnanceRepository, never()).save(any(Ordonnance.class));
    }

    @Test
    void obtenirOrdonnance_reussit_siMedecinEstLeReferentDuPatient_memeSiPasLePrescripteur(){
        // Vérifie explicitement que l'accès dépend du médecin référent du PATIENT,
        // pas du médecin prescripteur (medecinId) de l'ordonnance elle-même.
        String id = "ordonnance-1";
        String patientId = "patient-123";

        Ordonnance ordonnance = TestDataFactory.uneOrdonnance(patientId, "medecin-prescripteur");
        ordonnance.setId(id);

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent("medecin-referent"); // différent du prescripteur

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        Ordonnance result = ordonnanceService.obtenirOrdonnance(id, "medecin-referent");

        assertNotNull(result);
    }

    @Test
    void obtenirHistorique_recalculeStatutDesOrdonnancesPerimees(){
        String patientId = "patient-123";
        String medecinId = "medecin-1";


        Ordonnance perimee = TestDataFactory.uneOrdonnance(patientId, "medecin-1");
        perimee.setDateEmission(LocalDate.now().minusMonths(2));
        perimee.setDateValidite(LocalDate.now().minusDays(1));
        perimee.setStatut(StatutOrdonnance.ACTIVE); // stale: should be EXPIREE
        perimee.setId("ordo-perimee");

        Ordonnance active = TestDataFactory.uneOrdonnance(patientId, "medecin-1");
        active.setId("ordo-active");

        Patient patient = TestDataFactory.unPatient();
        patient.setId(patientId);
        patient.setMedecinReferent(medecinId);


        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(ordonnanceRepository.findByPatientId(patientId)).thenReturn(List.of(perimee , active));
        when(ordonnanceRepository.save(any(Ordonnance.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Ordonnance> result = ordonnanceService.obtenirHistorique(patientId, null , medecinId);

        Ordonnance perimeeDansResultat = result.stream()
            .filter(o -> o.getId().equals("ordo-perimee"))
            .findFirst().orElseThrow();

        Ordonnance activeDansResultat = result.stream()
            .filter(o -> o.getId().equals("ordo-active"))
            .findFirst().orElseThrow();


        assertEquals(StatutOrdonnance.EXPIREE,perimeeDansResultat.getStatut());
        assertEquals(StatutOrdonnance.ACTIVE, activeDansResultat.getStatut());

        verify(ordonnanceRepository , times(1)).save(any(Ordonnance.class));//only the stale one gets saved 

    }




    
}
