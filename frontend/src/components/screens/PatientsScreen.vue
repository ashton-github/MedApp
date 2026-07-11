<script setup>
import { patientRecords } from '../../constants/medapp.js'
import { useMedAppState } from '../../composables/useMedAppState.js'

const { editPatient, openNewPatient } = useMedAppState()

const getAvatarClass = (name) => {
  const code = name.charCodeAt(0)
  if (code % 6 === 0) return 'avatar-blue'
  if (code % 6 === 1) return 'avatar-green'
  if (code % 6 === 2) return 'avatar-violet'
  if (code % 6 === 3) return 'avatar-amber'
  if (code % 6 === 4) return 'avatar-rose'
  return 'avatar-cyan'
}

const getInitials = (nom, prenom) => {
  return `${prenom.charAt(0)}${nom.charAt(0)}`.toUpperCase()
}
</script>

<template>
  <section class="screen">
    <div class="screen-header">
      <div>
        <h2>Patients</h2>
        <p>{{ patientRecords.length }} patients enregistrés</p>
      </div>
      <div class="screen-actions">
        <div class="search-bar">
          <svg class="search-bar-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
          <input placeholder="Rechercher un patient…" type="search" />
        </div>
        <button class="btn btn-secondary" type="button">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon>
          </svg>
          Filtrer
        </button>
        <button class="btn btn-primary" type="button" @click="openNewPatient">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          Nouveau patient
        </button>
      </div>
    </div>

    <div class="table-card card-hover">
      <table>
        <thead>
          <tr>
            <th>Patient</th>
            <th>Naissance</th>
            <th>Téléphone</th>
            <th>Dernière visite</th>
            <th>Statut</th>
            <th class="right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="patient in patientRecords" :key="`${patient.nom}-${patient.prenom}`" @click="editPatient">
            <td>
              <div class="patient-cell">
                <div class="patient-avatar" :class="getAvatarClass(patient.nom)">
                  {{ getInitials(patient.nom, patient.prenom) }}
                </div>
                <div>
                  <div class="patient-name">{{ patient.prenom }} {{ patient.nom }}</div>
                  <div class="patient-sub">{{ patient.mutuelle || 'CPAM' }}</div>
                </div>
              </div>
            </td>
            <td class="td-muted">{{ patient.naissance }}</td>
            <td class="td-muted">{{ patient.telephone || '+33 6 12 34 56 78' }}</td>
            <td class="td-muted">Il y a 2 jours</td>
            <td>
              <span class="badge badge-success">Actif</span>
            </td>
            <td class="right">
              <div class="action-btns" style="justify-content: flex-end;">
                <button class="btn-sq" type="button" @click.stop="editPatient" title="Voir">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                </button>
                <button class="btn-sq" type="button" @click.stop="editPatient" title="Modifier">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                  </svg>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>