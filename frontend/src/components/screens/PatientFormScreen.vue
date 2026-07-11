<script setup>
import { useMedAppState } from '../../composables/useMedAppState.js'
import { screens } from '../../constants/medapp.js'

const { patientDraft, showScreen } = useMedAppState()
</script>

<template>
  <section class="screen">
    <div class="screen-header" style="margin-bottom: 16px;">
      <div style="display: flex; align-items: center; gap: 8px;">
        <button class="btn btn-ghost btn-sm" @click="showScreen(screens.patients)" style="padding: 0 8px;">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="15 18 9 12 15 6"></polyline>
          </svg>
          Retour
        </button>
      </div>
    </div>
    
    <div class="form-card card-hover">
      <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 24px;">
        <div class="patient-avatar avatar-blue" style="width: 56px; height: 56px; font-size: 1.25rem;">
          SM
        </div>
        <div>
          <h2 style="font-size: 1.25rem; font-weight: 700;">Sophie Martin</h2>
          <div style="display: flex; gap: 8px; margin-top: 4px;">
            <span class="badge badge-success">Actif</span>
            <span class="badge badge-muted">CPAM</span>
          </div>
        </div>
      </div>

      <h3 class="form-section-title">Informations personnelles</h3>
      <div class="form-grid">
        <div class="form-group">
          <label>Nom</label>
          <input v-model="patientDraft.nom" type="text" placeholder="Ex: Martin" />
        </div>
        <div class="form-group">
          <label>Prénom</label>
          <input v-model="patientDraft.prenom" type="text" placeholder="Ex: Sophie" />
        </div>
        <div class="form-group">
          <label>Date de naissance</label>
          <input v-model="patientDraft.naissance" type="date" />
        </div>
        <div class="form-group">
          <label>Téléphone</label>
          <input v-model="patientDraft.telephone" type="tel" placeholder="+33 6 XX XX XX XX" />
        </div>
        <div class="form-group form-full">
          <label>Adresse</label>
          <input v-model="patientDraft.adresse" type="text" placeholder="123 rue de la République, 75001 Paris" />
        </div>
      </div>

      <h3 class="form-section-title">Dossier médical</h3>
      <div class="form-group">
        <label>Antécédents & allergies</label>
        <textarea v-model="patientDraft.antecedents" rows="3" placeholder="Aucun antécédent particulier connu..."></textarea>
      </div>

      <div class="button-row">
        <button class="btn btn-primary" type="button">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
            <polyline points="17 21 17 13 7 13 7 21"></polyline>
            <polyline points="7 3 7 8 15 8"></polyline>
          </svg>
          Enregistrer
        </button>
        <button class="btn btn-ghost" type="button" @click="showScreen(screens.patients)">Annuler</button>
      </div>

      <div class="history">
        <h3>Historique des ordonnances</h3>
        <div class="history-row">
          <div style="display: flex; align-items: center; gap: 12px;">
            <div style="width: 32px; height: 32px; background: var(--muted); border-radius: 8px; display: flex; align-items: center; justify-content: center; color: var(--primary);">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
                <polyline points="10 9 9 9 8 9"></polyline>
              </svg>
            </div>
            <div>
              <div style="font-weight: 500;">Prescription standard</div>
              <div style="font-size: 0.75rem; color: var(--muted-foreground);">12/06/2026</div>
            </div>
          </div>
          <span class="badge badge-success">Active</span>
        </div>
        <div class="history-row">
          <div style="display: flex; align-items: center; gap: 12px;">
            <div style="width: 32px; height: 32px; background: var(--muted); border-radius: 8px; display: flex; align-items: center; justify-content: center; color: var(--muted-foreground);">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
                <polyline points="10 9 9 9 8 9"></polyline>
              </svg>
            </div>
            <div>
              <div style="font-weight: 500;">Antibiotiques</div>
              <div style="font-size: 0.75rem; color: var(--muted-foreground);">03/01/2026</div>
            </div>
          </div>
          <span class="badge badge-error">Expirée</span>
        </div>
      </div>
    </div>
  </section>
</template>