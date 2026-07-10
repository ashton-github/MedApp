<script setup>
import { useMedAppState } from '../../composables/useMedAppState.js'
import { screens } from '../../constants/medapp.js'

const { patientDraft, showScreen } = useMedAppState()
</script>

<template>
  <section class="screen">
    <h2>Fiche patient — Sophie Martin</h2>

    <div class="form-grid">
      <div>
        <label>Nom</label>
        <input v-model="patientDraft.nom" type="text" />
      </div>
      <div>
        <label>Prenom</label>
        <input v-model="patientDraft.prenom" type="text" />
      </div>
      <div>
        <label>Date de naissance</label>
        <input v-model="patientDraft.naissance" type="text" />
      </div>
      <div>
        <label>Telephone</label>
        <input v-model="patientDraft.telephone" type="text" />
      </div>
    </div>

    <label>Adresse</label>
    <input v-model="patientDraft.adresse" type="text" />

    <label>Antecedents</label>
    <textarea v-model="patientDraft.antecedents" rows="2"></textarea>

    <div class="button-row">
      <button class="primary" type="button">Enregistrer</button>
      <button class="ghost" type="button" @click="showScreen(screens.patients)">Annuler</button>
    </div>

    <div class="history">
      <h3>Historique des ordonnances</h3>
      <div class="history-row">
        <span>12/06/2026</span>
        <span class="badge success">Active</span>
      </div>
      <div class="history-row">
        <span>03/01/2026</span>
        <span class="badge muted">Expiree</span>
      </div>
    </div>
  </section>
</template>