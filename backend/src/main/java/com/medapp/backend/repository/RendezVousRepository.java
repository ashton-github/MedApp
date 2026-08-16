package com.medapp.backend.repository;

import com.medapp.backend.model.RendezVous;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface RendezVousRepository extends MongoRepository<RendezVous, String> {
    List<RendezVous> findByMedecinId(String medecinId);
    List<RendezVous> findByPatientId(String patientId);
}
