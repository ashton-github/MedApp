<script setup>
import { ordonnanceRecords } from '../../constants/medapp.js'
import { useMedAppState } from '../../composables/useMedAppState.js'

const { openNewOrdonnance } = useMedAppState()
</script>

<template>
  <section class="screen">
    <div class="screen-header">
      <h2>Ordonnances</h2>
      <button class="primary" type="button" @click="openNewOrdonnance">Nouvelle ordonnance</button>
    </div>

    <table>
      <thead>
        <tr>
          <th>Patient</th>
          <th>Emission</th>
          <th>Validite</th>
          <th>Statut</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="ordonnance in ordonnanceRecords" :key="`${ordonnance.patient}-${ordonnance.emission}`">
          <td>{{ ordonnance.patient }}</td>
          <td>{{ ordonnance.emission }}</td>
          <td>{{ ordonnance.validite }}</td>
          <td>
            <span class="badge" :class="ordonnance.statut === 'Active' ? 'success' : 'muted'">
              {{ ordonnance.statut }}
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>