<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  ChevronRight,
  Pencil,
  Plus,
  Phone,
  Shield,
  Heart,
  Lock,
  Activity,
  Trash2,
  Loader2,
  AlertCircle,
  FileText,
  Pill
} from 'lucide-vue-next'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { usePatientStore } from '../../stores/patientStore.js'
import { useDoctorStore } from '../../stores/doctorStore.js'
import { useOrdonnanceStore } from '../../stores/ordonnanceStore.js'
import { useAuthStore } from '../../stores/authStore.js'
import { screens } from '../../constants/medapp.js'
import { cn } from '../../lib/utils.js'

const { showScreen, editPatient, selectedPatientId, openNewOrdonnance } = useMedAppState()
const patientStore = usePatientStore()
const doctorStore = useDoctorStore()
const ordonnanceStore = useOrdonnanceStore()
const authStore = useAuthStore()
const isDoc = computed(() => authStore.role === 'medecin')
const isAdmin = computed(() => authStore.role === 'admin')
const tab   = ref('overview')
const patientOrdonnances = ref([])

const showDeleteModal = ref(false)
const confirmDelete = () => { showDeleteModal.value = true }
const deletePatient = async () => {
  try {
    await patientStore.deletePatient(patientStore.currentPatient.id)
    showScreen(screens.patients)
  } catch {
    // error in patientStore.error
  }
}

const TABS = [
  { id: 'overview',       label: 'Aperçu' },
  { id: 'prescriptions',  label: 'Ordonnances' },
  { id: 'history',        label: 'Historique' },
]

const fmt = (d) => d ? new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' }) : '–'
const initials    = (f, l) => `${f?.[0] ?? '?'}${l?.[0] ?? '?'}`.toUpperCase()
const AVATAR_COLORS = [
  'bg-blue-100 text-blue-700', 'bg-emerald-100 text-emerald-700',
  'bg-violet-100 text-violet-700', 'bg-amber-100 text-amber-700',
  'bg-rose-100 text-rose-700', 'bg-cyan-100 text-cyan-700',
]
const avatarColor = (name) => AVATAR_COLORS[(name?.charCodeAt(0) ?? 0) % AVATAR_COLORS.length]

const p = patientStore.currentPatient  // reactive ref from store

const doctorName = computed(() => {
  return doctorStore.getDoctorFullName(patientStore.currentPatient?.referringDoctor)
})

onMounted(async () => {
  if (selectedPatientId.value) {
    await patientStore.getPatientById(selectedPatientId.value)
    patientOrdonnances.value = await ordonnanceStore.fetchOrdonnancesByPatientId(selectedPatientId.value)
  }
  await doctorStore.fetchDoctors()
})
</script>

<template>
  <!-- Loading state -->
  <div v-if="patientStore.loading && !patientStore.currentPatient" class="flex items-center justify-center min-h-96">
    <Loader2 class="w-8 h-8 animate-spin text-blue-500" />
  </div>

  <!-- Error state -->
  <div v-else-if="patientStore.error && !patientStore.currentPatient" class="p-6">
    <div class="flex items-center gap-2 p-4 rounded-xl bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-800 text-red-600 dark:text-red-400 text-sm">
      <AlertCircle class="w-5 h-5 shrink-0" />
      {{ patientStore.error }}
    </div>
  </div>

  <!-- Patient loaded -->
  <div v-else-if="patientStore.currentPatient" class="p-6 space-y-6">

    <!-- Breadcrumb -->
    <div class="flex items-center gap-2 text-sm text-muted-foreground">
      <button @click="showScreen(screens.patients)" class="hover:text-foreground transition-colors">Patients</button>
      <ChevronRight class="w-3 h-3" />
      <span class="text-foreground font-medium">{{ patientStore.currentPatient.firstName }} {{ patientStore.currentPatient.lastName }}</span>
    </div>

    <!-- Header card -->
    <div class="rounded-2xl border border-border bg-card p-6">
      <div class="flex flex-col sm:flex-row items-start gap-6">
        <div :class="['w-20 h-20 rounded-full flex items-center justify-center text-2xl font-semibold shrink-0', avatarColor(patientStore.currentPatient.firstName)]">
          {{ initials(patientStore.currentPatient.firstName, patientStore.currentPatient.lastName) }}
        </div>
        <div class="flex-1 min-w-0">
          <div class="flex items-start justify-between flex-wrap gap-3">
            <div>
              <h1 class="text-2xl font-bold text-foreground">{{ patientStore.currentPatient.firstName }} {{ patientStore.currentPatient.lastName }}</h1>
              <div class="flex items-center gap-3 mt-1 flex-wrap">
                <span class="text-sm text-muted-foreground">
                  {{ patientStore.currentPatient.gender === 'M' ? 'Homme' : 'Femme' }} · {{ fmt(patientStore.currentPatient.birthDate) }}
                </span>
              </div>
            </div>
            <div class="flex gap-2">
              <button @click="editPatient(patientStore.currentPatient)" class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
                <Pencil class="w-4 h-4" /> Modifier
              </button>
              <button v-if="isDoc" @click="openNewOrdonnance(patientStore.currentPatient.id)" class="bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
                <Plus class="w-4 h-4" /> Ordonnance
              </button>
              <button v-if="isAdmin" class="border border-red-200 text-red-600 hover:bg-red-50 dark:border-red-900/50 dark:text-red-500 dark:hover:bg-red-950/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500 px-3 py-1.5 text-sm gap-1.5" @click="confirmDelete">
                <Trash2 class="w-4 h-4" /> Supprimer
              </button>
            </div>
          </div>

          <!-- Info grid -->
          <div class="grid grid-cols-2 lg:grid-cols-3 gap-4 mt-5">
            <div v-for="it in [
              { label: 'Téléphone',          val: patientStore.currentPatient.phone,  icon: Phone },
              { label: 'N° Sécurité Sociale', val: isDoc ? patientStore.currentPatient.socialSecurityNumber : 'Restreint', icon: Shield, mask: !isDoc },
              { label: 'Médecin référent',    val: doctorName || '–', icon: Heart },
            ]" :key="it.label">
              <p class="text-xs text-muted-foreground flex items-center gap-1"><component :is="it.icon" class="w-3 h-3" />{{ it.label }}</p>
              <p :class="cn('text-sm font-medium mt-0.5 truncate', it.mask ? 'text-muted-foreground flex items-center gap-1' : 'text-foreground')">
                <Lock v-if="it.mask" class="w-3 h-3" />{{ it.val }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex border-b border-border">
      <button v-for="t in TABS" :key="t.id" @click="tab = t.id"
        :class="cn('px-4 py-3 text-sm font-medium transition-colors relative', tab === t.id ? 'text-blue-600' : 'text-muted-foreground hover:text-foreground')"
      >
        {{ t.label }}
        <div v-if="tab === t.id" class="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-600" />
      </button>
    </div>

    <!-- Tab content -->
    <div class="pt-4">
      <transition mode="out-in" enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0 translate-y-2" enter-to-class="opacity-100 translate-y-0" leave-active-class="transition duration-100 ease-in" leave-from-class="opacity-100 translate-y-0" leave-to-class="opacity-0 -translate-y-2">
        <div :key="tab">
          <!-- Overview -->
          <template v-if="tab === 'overview'">
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
              <div class="rounded-2xl border border-border bg-card p-5 col-span-2">
                <h3 class="font-semibold text-foreground mb-4">Informations médicales</h3>
                <div class="space-y-3">
                  <div v-for="it in [
                    { label: 'Antécédents médicaux', val: isDoc ? (patientStore.currentPatient.medicalHistory?.length ? patientStore.currentPatient.medicalHistory.join(', ') : 'Aucun antécédent renseigné') : null },
                    { label: 'Médecin référent',     val: doctorName || '–' }
                  ]" :key="it.label" class="flex items-center justify-between py-2.5 border-b border-border last:border-0">
                    <span class="text-sm text-muted-foreground">{{ it.label }}</span>
                    <span v-if="it.val !== null" class="text-sm font-medium text-foreground text-right max-w-[60%]">{{ it.val }}</span>
                    <span v-else class="text-sm text-muted-foreground flex items-center gap-1.5"><Lock class="w-3 h-3" />Accès restreint</span>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <!-- Prescriptions -->
          <template v-else-if="tab === 'prescriptions'">
            <div v-if="patientOrdonnances.length === 0" class="flex flex-col items-center py-12 text-center">
              <FileText class="w-10 h-10 text-muted-foreground mb-3" />
              <p class="font-medium text-foreground">Aucune ordonnance</p>
              <p class="text-muted-foreground text-sm mt-1">Les ordonnances seront affichées ici</p>
            </div>
            <div v-else class="space-y-3">
              <div v-for="rx in patientOrdonnances" :key="rx.id" class="rounded-2xl border border-border bg-card p-4">
                <div class="flex items-center justify-between mb-3">
                  <span class="text-sm font-semibold text-foreground">{{ fmt(rx.issueDate) }}</span>
                  <span :class="[
                      'inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border',
                      rx.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700 border-emerald-200' :
                      rx.status === 'EXPIRED' ? 'bg-red-50 text-red-700 border-red-200' :
                      'bg-gray-100 text-gray-500 border-gray-200'
                    ]">
                      {{ rx.status === 'ACTIVE' ? 'Active' : rx.status === 'EXPIRED' ? 'Expirée' : 'Archivée' }}
                  </span>
                </div>
                <div class="space-y-2">
                  <div v-for="m in rx.medications" :key="m.name" class="flex items-start gap-2 text-sm text-muted-foreground">
                    <Pill class="w-4 h-4 shrink-0 mt-0.5 text-blue-500" />
                    <div>
                      <strong class="text-foreground font-medium block">{{ m.name }}</strong>
                      <span>{{ m.dosage }}{{ m.dosage && m.frequency ? ' · ' : '' }}{{ m.frequency }}{{ (m.dosage || m.frequency) && m.duration ? ' · ' : '' }}{{ m.duration }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <!-- History -->
          <template v-else-if="tab === 'history'">
            <div class="flex flex-col items-center py-12 text-center">
              <div class="w-14 h-14 bg-muted rounded-2xl flex items-center justify-center mb-3">
                <Activity class="w-7 h-7 text-muted-foreground" />
              </div>
              <p class="font-medium text-foreground">Historique médical</p>
              <p class="text-muted-foreground text-sm mt-1">Cette section sera disponible prochainement</p>
            </div>
          </template>
        </div>
      </transition>
    </div>

    <!-- Delete modal -->
    <div v-if="showDeleteModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm">
      <div v-motion :initial="{ opacity: 0, scale: 0.95 }" :enter="{ opacity: 1, scale: 1, transition: { duration: 150 } }" class="w-full max-w-md rounded-2xl border border-border bg-card p-6 shadow-lg">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
            <Trash2 class="w-5 h-5 text-red-600 dark:text-red-500" />
          </div>
          <h2 class="text-lg font-semibold text-foreground">Supprimer le patient</h2>
        </div>
        <p class="text-sm text-muted-foreground mb-6">
          Êtes-vous sûr de vouloir supprimer <strong>{{ patientStore.currentPatient.firstName }} {{ patientStore.currentPatient.lastName }}</strong> ? Cette action est irréversible.
        </p>
        <div class="flex justify-end gap-3">
          <button @click="showDeleteModal = false" :disabled="patientStore.loading" class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-4 py-2 text-sm">Annuler</button>
          <button @click="deletePatient" :disabled="patientStore.loading" class="bg-red-600 text-white hover:bg-red-700 shadow-sm shadow-red-200/50 dark:shadow-red-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500 px-4 py-2 text-sm disabled:opacity-50">
            {{ patientStore.loading ? 'Suppression…' : 'Supprimer' }}
          </button>
        </div>
      </div>
    </div>

  </div>

  <!-- No patient selected -->
  <div v-else class="flex flex-col items-center justify-center min-h-96 text-muted-foreground">
    <p>Aucun patient sélectionné.</p>
    <button @click="showScreen(screens.patients)" class="mt-4 text-blue-600 hover:underline text-sm">Retour à la liste</button>
  </div>
</template>
