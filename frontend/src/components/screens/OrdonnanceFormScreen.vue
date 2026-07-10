<script setup>
import { useMedAppState } from '../../composables/useMedAppState.js'
import { screens } from '../../constants/medapp.js'

const { prescriptionDraft, showScreen } = useMedAppState()
</script>

<template>
  <section class="screen">
    <h2>Nouvelle ordonnance</h2>

    <div class="form-grid">
      <div>
        <label>Patient</label>
        <select v-model="prescriptionDraft.patient">
          <option>Sophie Martin</option>
        </select>
      </div>
      <div>
        <label>Date de validite</label>
        <input v-model="prescriptionDraft.validite" type="date" />
      </div>
    </div>

    <h3>Medicaments</h3>

    <div class="medication-grid">
      <input v-model="prescriptionDraft.medicament1" placeholder="Nom du medicament" type="text" />
      <input v-model="prescriptionDraft.dosage1" placeholder="Dosage" type="text" />
      <input v-model="prescriptionDraft.frequence1" placeholder="Frequence" type="text" />
      <input v-model="prescriptionDraft.duree1" placeholder="Duree" type="text" />
    </div>

    <div class="medication-grid">
      <input v-model="prescriptionDraft.medicament2" placeholder="Nom du medicament" type="text" />
      <input v-model="prescriptionDraft.dosage2" placeholder="Dosage" type="text" />
      <input v-model="prescriptionDraft.frequence2" placeholder="Frequence" type="text" />
      <input v-model="prescriptionDraft.duree2" placeholder="Duree" type="text" />
    </div>

    <button class="ghost add-row" type="button">Ajouter un medicament</button>

    <label>Remarques</label>
    <textarea v-model="prescriptionDraft.remarques" rows="2" placeholder="Remarques complementaires"></textarea>

    <div class="button-row">
      <button class="primary" type="button">Enregistrer l'ordonnance</button>
      <button class="ghost" type="button" @click="showScreen(screens.ordonnances)">Annuler</button>
    </div>
  </section>
</template>