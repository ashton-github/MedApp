package com.medapp.backend.model;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ordonnances")
public class Ordonnance {
    
    @Id
    private String id;
    private String patientId;
    private String medecinId;
    private LocalDate dateEmission;
    private LocalDate dateValidite;
    private List<Medicament> medicaments;
    private StatutOrdonnance statut;
    private String remarques;

    public Ordonnance(){}

    public Ordonnance(String patientId, String medecinId, LocalDate dateEmission,
                       LocalDate dateValidite, List<Medicament> medicaments,
                       StatutOrdonnance statut, String remarques){

        this.patientId = patientId;
        this.medecinId = medecinId;
        this.dateEmission = dateEmission;
        this.dateValidite = dateValidite;
        this.medicaments = medicaments;
        this.statut = statut;
        this.remarques = remarques;

    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getMedecinId() { return medecinId; }
    public void setMedecinId(String medecinId) { this.medecinId = medecinId; }

    public LocalDate getDateEmission() { return dateEmission; }
    public void setDateEmission(LocalDate dateEmission) { this.dateEmission = dateEmission; }

    public LocalDate getDateValidite() { return dateValidite; }
    public void setDateValidite(LocalDate dateValidite) { this.dateValidite = dateValidite; }

    public List<Medicament> getMedicaments() { return medicaments; }
    public void setMedicaments(List<Medicament> medicaments) { this.medicaments = medicaments; }

    public StatutOrdonnance getStatut() { return statut; }
    public void setStatut(StatutOrdonnance statut) { this.statut = statut; }

    public String getRemarques() { return remarques; }
    public void setRemarques(String remarques) { this.remarques = remarques; }
    
}
