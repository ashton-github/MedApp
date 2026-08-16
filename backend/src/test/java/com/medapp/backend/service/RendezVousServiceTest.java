package com.medapp.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.medapp.backend.dto.RendezVousRequest;
import com.medapp.backend.dto.RendezVousResponse;
import com.medapp.backend.exception.DonneesInvalidesException;
import com.medapp.backend.mapper.RendezVousMapper;
import com.medapp.backend.model.Patient;
import com.medapp.backend.model.RendezVous;
import com.medapp.backend.model.Sexe;
import com.medapp.backend.model.StatutRendezVous;
import com.medapp.backend.model.TypeRendezVous;
import com.medapp.backend.repository.PatientRepository;
import com.medapp.backend.repository.RendezVousRepository;

@ExtendWith(MockitoExtension.class)
public class RendezVousServiceTest {

    @Mock
    private RendezVousRepository rendezVousRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private RendezVousMapper rendezVousMapper;

    @InjectMocks
    private RendezVousService rendezVousService;

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Patient creerPatient(String id) {
        Patient p = new Patient("Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "0612345678", "12 rue de la Paix", "1234567890123",
                List.of(), null, null, null);
        p.setId(id);
        return p;
    }

    private RendezVousRequest creerRequest(String patientId, LocalDate date, LocalTime heure) {
        RendezVousRequest req = new RendezVousRequest();
        req.setPatientId(patientId);
        req.setDate(date);
        req.setHeure(heure);
        req.setDuree(30);
        req.setType("CONSULTATION");
        return req;
    }

    private RendezVous creerEntite(String id, String patientId, String medecinId, LocalDate date, LocalTime heure) {
        RendezVous rv = new RendezVous(patientId, medecinId, date, heure, 30,
                TypeRendezVous.CONSULTATION, StatutRendezVous.PLANIFIE, null);
        rv.setId(id);
        return rv;
    }

    // ── creerRendezVous ────────────────────────────────────────────────────────

    @Test
    void creerRendezVous_reussit_siPatientExisteEtDateFuture() {
        Patient patient = creerPatient("patient-1");
        RendezVousRequest req = creerRequest("patient-1",
                LocalDate.now().plusDays(1), LocalTime.of(9, 0));

        RendezVous entite = creerEntite("rv-1", "patient-1", "medecin-1",
                LocalDate.now().plusDays(1), LocalTime.of(9, 0));
        RendezVousResponse expected = new RendezVousResponse();
        expected.setId("rv-1");
        expected.setStatut("PLANIFIE");

        when(patientRepository.findById("patient-1")).thenReturn(Optional.of(patient));
        when(rendezVousMapper.toEntity(req, "medecin-1")).thenReturn(entite);
        when(rendezVousRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rendezVousMapper.toResponse(any(), eq("Marie Dupont"))).thenReturn(expected);

        RendezVousResponse result = rendezVousService.creerRendezVous(req, "medecin-1");

        assertNotNull(result);
        assertEquals("rv-1", result.getId());
        assertEquals("PLANIFIE", result.getStatut());
        verify(rendezVousRepository).save(any(RendezVous.class));
    }

    @Test
    void creerRendezVous_lanceException_siPatientInexistant() {
        RendezVousRequest req = creerRequest("patient-inexistant",
                LocalDate.now().plusDays(1), LocalTime.of(9, 0));

        when(patientRepository.findById("patient-inexistant")).thenReturn(Optional.empty());

        assertThrows(DonneesInvalidesException.class,
                () -> rendezVousService.creerRendezVous(req, "medecin-1"));

        verify(rendezVousRepository, never()).save(any());
    }

    @Test
    void creerRendezVous_lanceException_siDateNulle() {
        Patient patient = creerPatient("patient-1");
        RendezVousRequest req = creerRequest("patient-1", null, LocalTime.of(9, 0));

        when(patientRepository.findById("patient-1")).thenReturn(Optional.of(patient));

        assertThrows(DonneesInvalidesException.class,
                () -> rendezVousService.creerRendezVous(req, "medecin-1"));

        verify(rendezVousRepository, never()).save(any());
    }

    @Test
    void creerRendezVous_lanceException_siHeureNulle() {
        Patient patient = creerPatient("patient-1");
        RendezVousRequest req = creerRequest("patient-1", LocalDate.now().plusDays(1), null);

        when(patientRepository.findById("patient-1")).thenReturn(Optional.of(patient));

        assertThrows(DonneesInvalidesException.class,
                () -> rendezVousService.creerRendezVous(req, "medecin-1"));

        verify(rendezVousRepository, never()).save(any());
    }

    @Test
    void creerRendezVous_lanceException_siDateDansLePasse() {
        Patient patient = creerPatient("patient-1");
        RendezVousRequest req = creerRequest("patient-1",
                LocalDate.now().minusDays(1), LocalTime.of(9, 0));

        when(patientRepository.findById("patient-1")).thenReturn(Optional.of(patient));

        assertThrows(DonneesInvalidesException.class,
                () -> rendezVousService.creerRendezVous(req, "medecin-1"));

        verify(rendezVousRepository, never()).save(any());
    }

    // ── obtenirRendezVousParMedecin ────────────────────────────────────────────

    @Test
    void obtenirRendezVousParMedecin_retourneListe_siExistent() {
        RendezVous rv1 = creerEntite("rv-1", "patient-1", "medecin-1",
                LocalDate.now().plusDays(1), LocalTime.of(9, 0));
        RendezVous rv2 = creerEntite("rv-2", "patient-2", "medecin-1",
                LocalDate.now().plusDays(2), LocalTime.of(10, 0));

        Patient p1 = creerPatient("patient-1");
        Patient p2 = creerPatient("patient-2");

        RendezVousResponse resp1 = new RendezVousResponse();
        resp1.setId("rv-1");
        RendezVousResponse resp2 = new RendezVousResponse();
        resp2.setId("rv-2");

        when(rendezVousRepository.findByMedecinId("medecin-1")).thenReturn(List.of(rv1, rv2));
        when(patientRepository.findById("patient-1")).thenReturn(Optional.of(p1));
        when(patientRepository.findById("patient-2")).thenReturn(Optional.of(p2));
        when(rendezVousMapper.toResponse(eq(rv1), any())).thenReturn(resp1);
        when(rendezVousMapper.toResponse(eq(rv2), any())).thenReturn(resp2);

        List<RendezVousResponse> result = rendezVousService.obtenirRendezVousParMedecin("medecin-1");

        assertEquals(2, result.size());
        assertEquals("rv-1", result.get(0).getId());
        assertEquals("rv-2", result.get(1).getId());
    }

    @Test
    void obtenirRendezVousParMedecin_retourneListeVide_siAucunRDV() {
        when(rendezVousRepository.findByMedecinId("medecin-sans-rdv")).thenReturn(List.of());

        List<RendezVousResponse> result = rendezVousService.obtenirRendezVousParMedecin("medecin-sans-rdv");

        assertTrue(result.isEmpty());
    }

    // ── modifierRendezVous ─────────────────────────────────────────────────────

    @Test
    void modifierRendezVous_reussit_siRDVExiste() {
        RendezVous existant = creerEntite("rv-1", "patient-1", "medecin-1",
                LocalDate.now().plusDays(1), LocalTime.of(9, 0));
        Patient patient = creerPatient("patient-1");

        RendezVousRequest req = creerRequest("patient-1",
                LocalDate.now().plusDays(3), LocalTime.of(14, 0));
        req.setDuree(45);

        RendezVousResponse expected = new RendezVousResponse();
        expected.setId("rv-1");

        when(rendezVousRepository.findById("rv-1")).thenReturn(Optional.of(existant));
        when(rendezVousRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(patientRepository.findById("patient-1")).thenReturn(Optional.of(patient));
        when(rendezVousMapper.toResponse(any(), any())).thenReturn(expected);

        RendezVousResponse result = rendezVousService.modifierRendezVous("rv-1", req);

        assertNotNull(result);
        assertEquals("rv-1", result.getId());
        verify(rendezVousRepository).save(any(RendezVous.class));
    }

    @Test
    void modifierRendezVous_lanceException_siIdInexistant() {
        RendezVousRequest req = creerRequest("patient-1",
                LocalDate.now().plusDays(1), LocalTime.of(9, 0));

        when(rendezVousRepository.findById("rv-inexistant")).thenReturn(Optional.empty());

        assertThrows(DonneesInvalidesException.class,
                () -> rendezVousService.modifierRendezVous("rv-inexistant", req));

        verify(rendezVousRepository, never()).save(any());
    }

    // ── changerStatut ──────────────────────────────────────────────────────────

    @Test
    void changerStatut_reussit_siStatutValide() {
        RendezVous existant = creerEntite("rv-1", "patient-1", "medecin-1",
                LocalDate.now().plusDays(1), LocalTime.of(9, 0));
        Patient patient = creerPatient("patient-1");
        RendezVousResponse expected = new RendezVousResponse();
        expected.setId("rv-1");
        expected.setStatut("TERMINE");

        when(rendezVousRepository.findById("rv-1")).thenReturn(Optional.of(existant));
        when(rendezVousRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(patientRepository.findById("patient-1")).thenReturn(Optional.of(patient));
        when(rendezVousMapper.toResponse(any(), any())).thenReturn(expected);

        RendezVousResponse result = rendezVousService.changerStatut("rv-1", "TERMINE");

        assertNotNull(result);
        assertEquals("TERMINE", result.getStatut());
    }

    @Test
    void changerStatut_lanceException_siStatutInvalide() {
        RendezVous existant = creerEntite("rv-1", "patient-1", "medecin-1",
                LocalDate.now().plusDays(1), LocalTime.of(9, 0));

        when(rendezVousRepository.findById("rv-1")).thenReturn(Optional.of(existant));

        assertThrows(DonneesInvalidesException.class,
                () -> rendezVousService.changerStatut("rv-1", "STATUT_INVALIDE"));

        verify(rendezVousRepository, never()).save(any());
    }

    @Test
    void changerStatut_lanceException_siRDVInexistant() {
        when(rendezVousRepository.findById("rv-inexistant")).thenReturn(Optional.empty());

        assertThrows(DonneesInvalidesException.class,
                () -> rendezVousService.changerStatut("rv-inexistant", "ANNULE"));
    }

    // ── supprimerRendezVous ────────────────────────────────────────────────────

    @Test
    void supprimerRendezVous_reussit_siRDVExiste() {
        when(rendezVousRepository.existsById("rv-1")).thenReturn(true);

        rendezVousService.supprimerRendezVous("rv-1");

        verify(rendezVousRepository).deleteById("rv-1");
    }

    @Test
    void supprimerRendezVous_lanceException_siIdInexistant() {
        when(rendezVousRepository.existsById("rv-inexistant")).thenReturn(false);

        assertThrows(DonneesInvalidesException.class,
                () -> rendezVousService.supprimerRendezVous("rv-inexistant"));

        verify(rendezVousRepository, never()).deleteById(any());
    }
}
