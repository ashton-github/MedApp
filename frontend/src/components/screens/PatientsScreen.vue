<script setup>
import { patientRecords } from '../../constants/medapp.js'
import { useMedAppState } from '../../composables/useMedAppState.js'

const { editPatient, openNewPatient } = useMedAppState()
</script>

<template>
  <section class="screen">
    <div class="screen-header">
      <h2>Patients</h2>
      <div class="screen-actions">
        <input placeholder="Rechercher un patient" type="search" />
        <button class="primary" type="button" @click="openNewPatient">Nouveau patient</button>
      </div>
    </div>

    <table>
      <thead>
        <tr>
          <th>Nom</th>
          <th>Prenom</th>
          <th>Naissance</th>
          <th class="right">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="patient in patientRecords" :key="`${patient.nom}-${patient.prenom}`">
          <td>{{ patient.nom }}</td>
          <td>{{ patient.prenom }}</td>
          <td>{{ patient.naissance }}</td>
          <td class="right">
            <button class="ghost link" type="button" @click="editPatient">Voir</button>
            <button class="ghost link" type="button" @click="editPatient">Modifier</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>