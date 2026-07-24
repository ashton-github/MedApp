<script setup>
import { ref, computed } from 'vue'
import {
  ChevronLeft,
  ChevronRight,
  Check,
  CheckCircle2,
  Loader2,
  Phone,
  MapPin,
  Shield
} from 'lucide-vue-next'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { screens } from '../../constants/medapp.js'
import { cn } from '../../lib/utils.js'

const { showScreen, patientToEdit } = useMedAppState()

const isEditMode = computed(() => !!patientToEdit.value)

const step = ref(1)
const submitting = ref(false)
const done = ref(false)

const form = ref({
  firstName: patientToEdit.value?.firstName || '',
  lastName: patientToEdit.value?.lastName || '',
  dob: patientToEdit.value?.dob || '',
  gender: patientToEdit.value?.gender || '',
  phone: patientToEdit.value?.phone || '',
  address: patientToEdit.value?.address || '',
  numeroSecuriteSociale: patientToEdit.value?.numeroSecuriteSociale || '',
  medecinReferent: patientToEdit.value?.medecinReferent || '',
  antecedents: patientToEdit.value?.antecedents || (patientToEdit.value?.allergies?.length ? patientToEdit.value.allergies.join(', ') : '')
})

const goBack = () => {
  showScreen(isEditMode.value ? screens.patientDetail : screens.patients)
}

const STEPS = [
  { n: 1, label: "Identité" },
  { n: 2, label: "Coordonnées" },
  { n: 3, label: "Médical" }
]

const submit = () => {
  submitting.value = true
  setTimeout(() => {
    submitting.value = false
    done.value = true
    setTimeout(() => {
      goBack()
    }, 1400)
  }, 1400)
}
</script>

<template>
  <div v-if="done" class="flex flex-col items-center justify-center min-h-96 p-6">
    <div v-motion :initial="{ scale: 0 }" :enter="{ scale: 1, transition: { type: 'spring', stiffness: 220 } }"
      class="w-20 h-20 bg-emerald-100 dark:bg-emerald-900/40 rounded-full flex items-center justify-center mb-4"
    >
      <CheckCircle2 class="w-10 h-10 text-emerald-600" />
    </div>
    <h2 class="text-xl font-bold text-foreground">{{ isEditMode ? 'Patient mis à jour !' : 'Patient créé !' }}</h2>
    <p class="text-muted-foreground text-sm mt-1">Redirection…</p>
  </div>

  <div v-else class="p-6 max-w-2xl mx-auto space-y-6">
    <div>
      <div class="flex items-center gap-2 text-sm text-muted-foreground mb-4">
        <button @click="goBack" class="hover:text-foreground">{{ isEditMode ? 'Détails du patient' : 'Patients' }}</button>
        <ChevronRight class="w-3 h-3" />
        <span class="text-foreground">{{ isEditMode ? 'Modifier le patient' : 'Nouveau patient' }}</span>
      </div>
      <h1 class="text-2xl font-bold text-foreground">{{ isEditMode ? 'Modifier le patient' : 'Nouveau patient' }}</h1>
    </div>

    <div class="flex items-center gap-0">
      <div v-for="(s, i) in STEPS" :key="s.n" class="flex items-center flex-1">
        <div class="flex flex-col items-center">
          <div :class="cn(
            'w-9 h-9 rounded-full flex items-center justify-center text-sm font-semibold transition-all duration-300 border-2',
            step > s.n ? 'bg-emerald-500 border-emerald-500 text-white' :
            step === s.n ? 'bg-blue-600 border-blue-600 text-white' :
            'bg-background border-border text-muted-foreground'
          )">
            <Check v-if="step > s.n" class="w-4 h-4" />
            <span v-else>{{ s.n }}</span>
          </div>
          <span class="text-xs text-muted-foreground mt-1.5 hidden sm:block">{{ s.label }}</span>
        </div>
        <div v-if="i < STEPS.length - 1" :class="cn('flex-1 h-0.5 mx-3', step > s.n ? 'bg-emerald-400' : 'bg-border')" />
      </div>
    </div>

    <div class="rounded-2xl border border-border bg-card p-6 overflow-hidden">
      <transition
        mode="out-in"
        enter-active-class="transition duration-150 ease-out"
        enter-from-class="opacity-0 translate-x-4"
        enter-to-class="opacity-100 translate-x-0"
        leave-active-class="transition duration-150 ease-in"
        leave-from-class="opacity-100 translate-x-0"
        leave-to-class="opacity-0 -translate-x-4"
      >
        <div :key="step" class="space-y-4">
          <template v-if="step === 1">
            <h2 class="font-semibold text-foreground">Informations personnelles</h2>
            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-sm font-medium text-foreground">Prénom *</label>
                <input v-model="form.firstName" placeholder="Sophie" class="w-full h-10 px-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground" />
              </div>
              <div class="space-y-1.5">
                <label class="text-sm font-medium text-foreground">Nom *</label>
                <input v-model="form.lastName" placeholder="Laurent" class="w-full h-10 px-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground" />
              </div>
            </div>
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">Date de naissance *</label>
              <input type="date" v-model="form.dob" class="w-full h-10 px-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground" />
            </div>
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">Genre *</label>
              <div class="flex gap-3">
                <button v-for="g in [{ v: 'M', l: 'Homme' }, { v: 'F', l: 'Femme' }]" :key="g.v" type="button" @click="form.gender = g.v"
                  :class="cn('flex-1 py-2.5 rounded-xl border text-sm font-medium transition-all', form.gender === g.v ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400' : 'border-border text-muted-foreground hover:bg-accent')"
                >
                  {{ g.l }}
                </button>
              </div>
            </div>
          </template>

          <template v-else-if="step === 2">
            <h2 class="font-semibold text-foreground">Coordonnées</h2>
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">Téléphone *</label>
              <div class="relative">
                <Phone class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <input v-model="form.phone" placeholder="+33 6 12 34 56 78" class="w-full h-10 pl-9 pr-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground" />
              </div>
            </div>
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">Adresse</label>
              <div class="relative">
                <MapPin class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <input v-model="form.address" placeholder="123 rue de la Paix, 75001 Paris" class="w-full h-10 pl-9 pr-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground" />
              </div>
            </div>
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">Numéro de sécurité sociale</label>
              <div class="relative">
                <Shield class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <input v-model="form.numeroSecuriteSociale" placeholder="1 23 45 67 890 123 45" class="w-full h-10 pl-9 pr-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground" />
              </div>
            </div>
          </template>

          <template v-else-if="step === 3">
            <h2 class="font-semibold text-foreground">Antécédents médicaux</h2>
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">Médecin référent (traitant)</label>
              <input v-model="form.medecinReferent" placeholder="Dr. Martin" class="w-full h-10 px-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground" />
            </div>
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">Antécédents médicaux</label>
              <textarea v-model="form.antecedents" placeholder="Allergies, traitements en cours, etc." rows="5" class="w-full px-3 py-2.5 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground resize-none" />
            </div>
          </template>
        </div>
      </transition>
    </div>

    <div class="flex justify-between">
      <button @click="step === 1 ? goBack() : step--" class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-4 py-2 text-sm gap-2">
        <ChevronLeft class="w-4 h-4" /> {{ step === 1 ? "Annuler" : "Précédent" }}
      </button>
      
      <button v-if="step < 3" @click="step++" class="bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-4 py-2 text-sm gap-2">
        Suivant <ChevronRight class="w-4 h-4" />
      </button>
      <button v-else @click="submit" :disabled="submitting" class="bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 px-4 py-2 text-sm gap-2">
        <template v-if="submitting">
          <Loader2 class="w-4 h-4 animate-spin" /> {{ isEditMode ? 'Enregistrement…' : 'Création…' }}
        </template>
        <template v-else>
          <Check class="w-4 h-4" /> {{ isEditMode ? 'Enregistrer les modifications' : 'Créer le patient' }}
        </template>
      </button>
    </div>
  </div>
</template>