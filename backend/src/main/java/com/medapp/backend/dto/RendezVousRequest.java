package com.medapp.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class RendezVousRequest {
    private String patientId;
    private LocalDate date;
    private LocalTime heure;
    private int duree;
    private String type;
    private String remarques;
    private String medecinId;

    public RendezVousRequest() {}

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getHeure() { return heure; }
    public void setHeure(LocalTime heure) { this.heure = heure; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRemarques() { return remarques; }
    public void setRemarques(String remarques) { this.remarques = remarques; }

    public String getMedecinId() { return medecinId; }
    public void setMedecinId(String medecinId) { this.medecinId = medecinId; }
}
