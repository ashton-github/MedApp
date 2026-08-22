package com.medapp.backend.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
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

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;

@Service
public class OrdonnanceService {

    final private OrdonnanceRepository ordonnanceRepository;
    final private PatientRepository patientRepository;

    public OrdonnanceService(OrdonnanceRepository ordonnanceRepository , PatientRepository patientRepository){
        this.ordonnanceRepository = ordonnanceRepository;
        this.patientRepository = patientRepository;
    }

    public Ordonnance creerOrdonnance(Ordonnance ordonnance, String medecinConnecte){
        Patient patient = patientRepository.findById(ordonnance.getPatientId())
            .orElseThrow(() -> new PatientIntrouvableException(ordonnance.getPatientId()));

        verifierAccesMedecin(patient, medecinConnecte);

        ordonnance.setDateEmission(LocalDate.now());
        ordonnance.setStatut(StatutOrdonnanceCalculator.calculer(ordonnance.getDateValidite(), ordonnance.getStatut()));
        return ordonnanceRepository.save(ordonnance);
    }

    public Ordonnance archiverOrdonnance(String ordonnanceId, String medecinConnected) {
        Ordonnance ordonnance = ordonnanceRepository.findById(ordonnanceId)
                    .orElseThrow(() -> new OrdonnanceIntrouvableException(ordonnanceId));

        verifierAccesMedecin(ordonnance, medecinConnected);

        if(ordonnance.getStatut() == StatutOrdonnance.ARCHIVEE){
            throw new OrdonnanceDejaArchiveeException("Cette ordonnance est deja archivee.");
        }

        ordonnance.setStatut(StatutOrdonnance.ARCHIVEE);
        return ordonnanceRepository.save(ordonnance);
    }

    public List<Ordonnance> obtenirHistorique(String patientId, StatutOrdonnance statut, String medecinConnecte) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new PatientIntrouvableException(patientId));

        verifierAccesMedecin(patient, medecinConnecte);

        List<Ordonnance> ordonnances = (statut != null)
            ? ordonnanceRepository.findByPatientIdAndStatut(patientId, statut)
            : ordonnanceRepository.findByPatientId(patientId);

        return ordonnances.stream()
            .map(this::synchroniserStatut)
            .sorted(Comparator.comparing(Ordonnance::getDateEmission).reversed()
                .thenComparing(Ordonnance::getId))
            .toList();
    }

    public Ordonnance obtenirOrdonnance(String id, String medecinConnecte) {
        Ordonnance ordonnance = ordonnanceRepository.findById(id)
            .orElseThrow(() -> new OrdonnanceIntrouvableException(id));

        verifierAccesMedecin(ordonnance, medecinConnecte);

        return synchroniserStatut(ordonnance);
    }

    private void verifierAccesMedecin(Ordonnance ordonnance, String medecinConnecte) {
        Patient patient = patientRepository.findById(ordonnance.getPatientId())
            .orElseThrow(() -> new PatientIntrouvableException(ordonnance.getPatientId()));

        if (!medecinConnecte.equals(patient.getMedecinReferent())) {
            throw new AccesRefuseException("Seul le medecin referent du patient peut acceder a cette ordonnance.");
        }
    }

    private void verifierAccesMedecin(Patient patient, String medecinConnecte) {
        if (!medecinConnecte.equals(patient.getMedecinReferent())) {
            throw new AccesRefuseException("Seul le medecin referent du patient peut acceder a cette ressource.");
        }
    }

    private Ordonnance synchroniserStatut(Ordonnance ordonnance){
        StatutOrdonnance statutRecalcule = StatutOrdonnanceCalculator.calculer(
            ordonnance.getDateValidite(),ordonnance.getStatut());

        if(statutRecalcule != ordonnance.getStatut()){
            ordonnance.setStatut(statutRecalcule);
            return ordonnanceRepository.save(ordonnance);
        }
        return ordonnance;
    }

    public Ordonnance modifierOrdonnance(String ordonnanceId, Ordonnance ordonnanceModifiee, String medecinId) {
        Ordonnance ordonnance = ordonnanceRepository.findById(ordonnanceId)
                    .orElseThrow(() -> new OrdonnanceIntrouvableException(ordonnanceId));

        verifierAccesMedecin(ordonnance, medecinId);

        if(ordonnance.getStatut() == StatutOrdonnance.ARCHIVEE){
            throw new OrdonnanceDejaArchiveeException(
                "une ordonnance archivee ne peut pas etre modifier"
            );
        }

        ordonnance.setDateValidite(ordonnanceModifiee.getDateValidite());
        ordonnance.setMedicaments(ordonnanceModifiee.getMedicaments());
        ordonnance.setRemarques(ordonnanceModifiee.getRemarques());

        ordonnance.setStatut(StatutOrdonnanceCalculator.calculer(ordonnanceModifiee.getDateValidite(), ordonnanceModifiee.getStatut()));

        return ordonnanceRepository.save(ordonnance);
    }

    

    public byte[] generatePdf(String id , String medecinConnecte) {
        Ordonnance ordonnance = ordonnanceRepository.findById(id)
            .orElseThrow(() -> new OrdonnanceIntrouvableException(id));


        verifierAccesMedecin(ordonnance, medecinConnecte);

        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();
            document.add(new Paragraph("Ordonnance medicale"));
            document.add(new Paragraph("Date d'emission : " + ordonnance.getDateEmission()));
            document.add(new Paragraph("Date de validite : " + ordonnance.getDateValidite()));
            document.add(new Paragraph(" "));

            for (Medicament medicament : ordonnance.getMedicaments()) {
                document.add(new Paragraph(
                    medicament.getNom() + " - " + medicament.getDosage()
                    + " - " + medicament.getFrequence() + " - " + medicament.getDuree()
                ));
            }

            if (ordonnance.getRemarques() != null && !ordonnance.getRemarques().isBlank()) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Remarques : " + ordonnance.getRemarques()));
            }

            document.close();
            return outputStream.toByteArray();

        }catch(Exception e){
            throw new RuntimeException("Erreur lors de la generation du PDF." , e);
        }

    }

    
}
