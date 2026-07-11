<script setup>
import { useMedAppState } from '../../composables/useMedAppState.js'
import { screens } from '../../constants/medapp.js'

const { prescriptionDraft, showScreen } = useMedAppState()
</script>

<template>
  <section class="screen">
    <div class="screen-header" style="margin-bottom: 16px;">
      <div style="display: flex; align-items: center; gap: 8px;">
        <button class="btn btn-ghost btn-sm" @click="showScreen(screens.ordonnances)" style="padding: 0 8px;">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="15 18 9 12 15 6"></polyline>
          </svg>
          Retour
        </button>
      </div>
    </div>
    
    <div class="form-card card-hover">
      <div style="margin-bottom: 24px;">
        <h2 style="font-size: 1.5rem; font-weight: 700; color: var(--foreground);">Nouvelle ordonnance</h2>
        <p style="font-size: 0.875rem; color: var(--muted-foreground); margin-top: 4px;">Créez une prescription médicale numérique sécurisée.</p>
      </div>

      <h3 class="form-section-title">Informations générales</h3>
      <div class="form-grid" style="margin-bottom: 24px;">
        <div class="form-group">
          <label>Patient</label>
          <div class="field">
            <svg class="field-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <select v-model="prescriptionDraft.patient" style="padding-left: 40px;">
              <option>Sophie Martin</option>
              <option>Marc Dubois</option>
              <option>Lucas Petit</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label>Date de validité</label>
          <div class="field">
            <svg class="field-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
            <input v-model="prescriptionDraft.validite" type="date" style="padding-left: 40px;" />
          </div>
        </div>
      </div>

      <h3 class="form-section-title" style="display: flex; justify-content: space-between;">
        <span>Médicaments prescrits</span>
        <span class="badge badge-muted">2 médicaments</span>
      </h3>
      
      <div class="medication-grid">
        <div class="form-group" style="margin: 0;">
          <label style="font-size: 0.75rem;">Médicament</label>
          <input v-model="prescriptionDraft.medicament1" placeholder="Nom du médicament" type="text" />
        </div>
        <div class="form-group" style="margin: 0;">
          <label style="font-size: 0.75rem;">Dosage</label>
          <input v-model="prescriptionDraft.dosage1" placeholder="Ex: 500mg" type="text" />
        </div>
        <div class="form-group" style="margin: 0;">
          <label style="font-size: 0.75rem;">Fréquence</label>
          <input v-model="prescriptionDraft.frequence1" placeholder="Ex: 3x/jour" type="text" />
        </div>
        <div class="form-group" style="margin: 0;">
          <label style="font-size: 0.75rem;">Durée</label>
          <input v-model="prescriptionDraft.duree1" placeholder="Ex: 7 jours" type="text" />
        </div>
        <button class="btn-sq btn-danger" type="button" style="height: 42px; margin-bottom: 0;">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            <line x1="10" y1="11" x2="10" y2="17"></line>
            <line x1="14" y1="11" x2="14" y2="17"></line>
          </svg>
        </button>
      </div>

      <div class="medication-grid">
        <div class="form-group" style="margin: 0;">
          <label style="font-size: 0.75rem; visibility: hidden;">Médicament</label>
          <input v-model="prescriptionDraft.medicament2" placeholder="Nom du médicament" type="text" />
        </div>
        <div class="form-group" style="margin: 0;">
          <label style="font-size: 0.75rem; visibility: hidden;">Dosage</label>
          <input v-model="prescriptionDraft.dosage2" placeholder="Ex: 500mg" type="text" />
        </div>
        <div class="form-group" style="margin: 0;">
          <label style="font-size: 0.75rem; visibility: hidden;">Fréquence</label>
          <input v-model="prescriptionDraft.frequence2" placeholder="Ex: 3x/jour" type="text" />
        </div>
        <div class="form-group" style="margin: 0;">
          <label style="font-size: 0.75rem; visibility: hidden;">Durée</label>
          <input v-model="prescriptionDraft.duree2" placeholder="Ex: 7 jours" type="text" />
        </div>
        <button class="btn-sq btn-danger" type="button" style="height: 42px; margin-bottom: 0;">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            <line x1="10" y1="11" x2="10" y2="17"></line>
            <line x1="14" y1="11" x2="14" y2="17"></line>
          </svg>
        </button>
      </div>

      <div style="margin-bottom: 24px;">
        <button class="btn btn-secondary btn-sm" type="button">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          Ajouter un médicament
        </button>
      </div>

      <div class="form-group">
        <label>Remarques (optionnel)</label>
        <textarea v-model="prescriptionDraft.remarques" rows="2" placeholder="Remarques complémentaires pour le pharmacien ou le patient..."></textarea>
      </div>

      <div class="button-row">
        <button class="btn btn-primary" type="button">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
            <polyline points="17 21 17 13 7 13 7 21"></polyline>
            <polyline points="7 3 7 8 15 8"></polyline>
          </svg>
          Signer et enregistrer
        </button>
        <button class="btn btn-ghost" type="button" @click="showScreen(screens.ordonnances)">Annuler</button>
      </div>
    </div>
  </section>
</template>