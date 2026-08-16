package com.medapp.backend.model;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rendezvous")
public class RendezVous {

    @Id
    private String id;
    private String patientId;
    private String medecinId;
    private LocalDate date;
    private LocalTime heure;
    private int duree; // en minutes
    private TypeRendezVous type;
    private StatutRendezVous statut;
    private String remarques;

    public RendezVous() {}

    public RendezVous(String patientId, String medecinId, LocalDate date, LocalTime heure,
                      int duree, TypeRendezVous type, StatutRendezVous statut, String remarques) {
        this.patientId = patientId;
        this.medecinId = medecinId;
        this.date = date;
        this.heure = heure;
        this.duree = duree;
        this.type = type;
        this.statut = statut;
        this.remarques = remarques;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getMedecinId() { return medecinId; }
    public void setMedecinId(String medecinId) { this.medecinId = medecinId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getHeure() { return heure; }
    public void setHeure(LocalTime heure) { this.heure = heure; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public TypeRendezVous getType() { return type; }
    public void setType(TypeRendezVous type) { this.type = type; }

    public StatutRendezVous getStatut() { return statut; }
    public void setStatut(StatutRendezVous statut) { this.statut = statut; }

    public String getRemarques() { return remarques; }
    public void setRemarques(String remarques) { this.remarques = remarques; }
}
