<script setup>
import { ordonnanceRecords } from '../../constants/medapp.js'
import { useMedAppState } from '../../composables/useMedAppState.js'

const { openNewOrdonnance } = useMedAppState()

const getAvatarClass = (name) => {
  const code = name.charCodeAt(0)
  if (code % 6 === 0) return 'avatar-blue'
  if (code % 6 === 1) return 'avatar-green'
  if (code % 6 === 2) return 'avatar-violet'
  if (code % 6 === 3) return 'avatar-amber'
  if (code % 6 === 4) return 'avatar-rose'
  return 'avatar-cyan'
}

const getInitials = (name) => {
  const parts = name.split(' ')
  if (parts.length >= 2) return `${parts[0].charAt(0)}${parts[1].charAt(0)}`.toUpperCase()
  return name.charAt(0).toUpperCase()
}
</script>

<template>
  <section class="screen">
    <div class="screen-header">
      <div>
        <h2>Ordonnances</h2>
        <p>{{ ordonnanceRecords.length }} ordonnances enregistrées</p>
      </div>
      <div class="screen-actions">
        <div class="search-bar">
          <svg class="search-bar-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
          <input placeholder="Rechercher une ordonnance…" type="search" />
        </div>
        <button class="btn btn-secondary" type="button">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon>
          </svg>
          Filtrer
        </button>
        <button class="btn btn-primary" type="button" @click="openNewOrdonnance">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          Nouvelle ordonnance
        </button>
      </div>
    </div>

    <div class="table-card card-hover">
      <table>
        <thead>
          <tr>
            <th>Patient</th>
            <th>Émission</th>
            <th>Validité</th>
            <th>Statut</th>
            <th class="right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="ordonnance in ordonnanceRecords" :key="`${ordonnance.patient}-${ordonnance.emission}`">
            <td>
              <div class="patient-cell">
                <div class="patient-avatar" :class="getAvatarClass(ordonnance.patient)">
                  {{ getInitials(ordonnance.patient) }}
                </div>
                <div>
                  <div class="patient-name">{{ ordonnance.patient }}</div>
                </div>
              </div>
            </td>
            <td class="td-muted">{{ ordonnance.emission }}</td>
            <td class="td-muted">{{ ordonnance.validite }}</td>
            <td>
              <span class="badge" :class="ordonnance.statut === 'Active' ? 'badge-success' : 'badge-error'">
                {{ ordonnance.statut }}
              </span>
            </td>
            <td class="right">
              <div class="action-btns" style="justify-content: flex-end;">
                <button class="btn-sq" type="button" title="Voir">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                </button>
                <button class="btn-sq" type="button" title="Imprimer">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="6 9 6 2 18 2 18 9"></polyline>
                    <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"></path>
                    <rect x="6" y="14" width="12" height="8"></rect>
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