<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  ChevronLeft,
  ChevronRight,
  Plus,
  Clock,
  Search,
  X,
  Trash2,
  Calendar,
  Loader2,
  FileCheck,
  AlertCircle
} from 'lucide-vue-next'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { useAuthStore } from '../../stores/authStore.js'
import { useRendezVousStore } from '../../stores/rendezVousStore.js'
import { usePatientStore } from '../../stores/patientStore.js'
import { screens } from '../../constants/medapp.js'
import { cn } from '../../lib/utils.js'

const { showScreen, viewPatient } = useMedAppState()
const authStore = useAuthStore()
const rendezVousStore = useRendezVousStore()
const patientStore = usePatientStore()

const weekOffset = ref(0)
// Base is current Monday
const getMonday = (d) => {
  d = new Date(d);
  var day = d.getDay(), diff = d.getDate() - day + (day == 0 ? -6: 1); // adjust when day is sunday
  return new Date(d.setDate(diff));
}
const BASE = getMonday(new Date())

const weekStart = computed(() => {
  const d = new Date(BASE)
  d.setDate(BASE.getDate() + weekOffset.value * 7)
  return d
})

const DAYS = computed(() => {
  return [...Array(5)].map((_, i) => {
    const d = new Date(weekStart.value)
    d.setDate(weekStart.value.getDate() + i)
    return d
  })
})

const DAY_NAMES = ["Lun", "Mar", "Mer", "Jeu", "Ven"]

const TYPE_CLS = {
  "CONSULTATION": "bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-900/40 dark:text-blue-300 dark:border-blue-800",
  "SUIVI":        "bg-violet-50 text-violet-700 border-violet-200 dark:bg-violet-900/40 dark:text-violet-300 dark:border-violet-800",
  "URGENCE":      "bg-red-50 text-red-700 border-red-200 dark:bg-red-900/40 dark:text-red-300 dark:border-red-800",
  "BILAN":        "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-900/40 dark:text-amber-300 dark:border-amber-800",
}
const TYPE_LABELS = {
  "CONSULTATION": "Consultation",
  "SUIVI": "Suivi",
  "URGENCE": "Urgence",
  "BILAN": "Bilan"
}

const APPOINTMENTS = computed(() => rendezVousStore.rendezVous)

// Use local date parts to avoid UTC timezone shift (e.g. 2026-08-16 becoming 2026-08-15 in UTC+1)
const toISO = (d) => {
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
const getAppts = (d) => APPOINTMENTS.value.filter(a => a.day === toISO(d)).sort((a,b) => a.time.localeCompare(b.time))

const totalThisWeek = computed(() => DAYS.value.reduce((acc, d) => acc + getAppts(d).length, 0))

const fmtWeekRange = computed(() => {
  const d0 = DAYS.value[0]
  const d4 = DAYS.value[4]
  return `Semaine du ${d0.toLocaleDateString("fr-FR", { day: "numeric", month: "long" })} au ${d4.toLocaleDateString("fr-FR", { day: "numeric", month: "long", year: "numeric" })}`
})

const isToday = (d) => toISO(d) === toISO(new Date())

// --- MODAL & FORM LOGIC ---
const showModal = ref(false)
const isEditMode = ref(false)
const currentRvId = ref(null)

const pq = ref('')
const selPatient = ref(null)
const showSug = ref(false)
const formDate = ref(toISO(new Date()))
const formTime = ref('09:00')
const formDuration = ref(30)
const formType = ref('CONSULTATION')
const formNotes = ref('')
const submitting = ref(false)
const showDeleteModal = ref(false)

const sug = computed(() => {
  if (pq.value.length < 2) return []
  return patientStore.patients.filter(p => `${p.firstName} ${p.lastName}`.toLowerCase().includes(pq.value.toLowerCase()))
})

const openNewRv = (dayIso = toISO(new Date())) => {
  isEditMode.value = false
  currentRvId.value = null
  selPatient.value = null
  pq.value = ''
  formDate.value = dayIso
  formTime.value = '09:00'
  formDuration.value = 30
  formType.value = 'CONSULTATION'
  formNotes.value = ''
  showModal.value = true
}

const openEditRv = (rv) => {
  isEditMode.value = true
  currentRvId.value = rv.id
  selPatient.value = patientStore.patients.find(p => p.id === rv.patientId) || { id: rv.patientId, firstName: rv.patientName.split(' ')[0], lastName: rv.patientName.split(' ').slice(1).join(' ') }
  pq.value = ''
  formDate.value = rv.day
  formTime.value = rv.time
  formDuration.value = rv.duration
  formType.value = rv.type
  formNotes.value = rv.notes || ''
  showModal.value = true
}

const saveRv = async () => {
  if (!selPatient.value) return
  submitting.value = true
  try {
    const payload = {
      patientId: selPatient.value.id,
      day: formDate.value,
      time: formTime.value,
      duration: formDuration.value,
      type: formType.value,
      notes: formNotes.value
    }
    if (isEditMode.value) {
      await rendezVousStore.updateRendezVous(currentRvId.value, payload)
    } else {
      await rendezVousStore.createRendezVous(payload)
    }
    showModal.value = false
  } catch (e) {
    // handled by store
  } finally {
    submitting.value = false
  }
}

const confirmDeleteRv = async () => {
  submitting.value = true
  try {
    await rendezVousStore.deleteRendezVous(currentRvId.value)
    showDeleteModal.value = false
    showModal.value = false
  } catch (e) {
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (patientStore.patients.length === 0) {
    await patientStore.fetchPatients()
  }
  await rendezVousStore.fetchRendezVous()
})

const initials = (f = '', l = '') => `${f?.[0] ?? '?'}${l?.[0] ?? '?'}`.toUpperCase()
const AVATAR_COLORS = [
  "bg-blue-100 text-blue-700", "bg-emerald-100 text-emerald-700",
  "bg-violet-100 text-violet-700", "bg-amber-100 text-amber-700",
  "bg-rose-100 text-rose-700", "bg-cyan-100 text-cyan-700",
]
const avatarColor = (name = '') => AVATAR_COLORS[(name?.charCodeAt(0) ?? 0) % AVATAR_COLORS.length]

</script>

<template>
  <div class="p-6 space-y-6">
    <!-- Header -->
    <div class="flex items-start justify-between flex-wrap gap-3">
      <div>
        <h1 class="text-2xl font-bold text-foreground">Agenda</h1>
        <p class="text-sm text-muted-foreground mt-0.5">
          {{ fmtWeekRange }}
          · <span class="text-foreground font-medium">{{ totalThisWeek }} rendez-vous</span>
        </p>
      </div>
      <div class="flex items-center gap-2 flex-wrap">
        <div class="flex border border-border rounded-xl overflow-hidden bg-card">
          <button @click="weekOffset--" class="p-2.5 hover:bg-accent text-muted-foreground hover:text-foreground transition-colors">
            <ChevronLeft class="w-4 h-4" />
          </button>
          <button @click="weekOffset = 0" class="px-3 text-xs font-medium text-foreground hover:bg-accent transition-colors border-x border-border">
            Cette semaine
          </button>
          <button @click="weekOffset++" class="p-2.5 hover:bg-accent text-muted-foreground hover:text-foreground transition-colors">
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>
        <button v-if="authStore.role === 'secretaire'" @click="openNewRv()" class="bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
          <Plus class="w-4 h-4" /> Nouveau RDV
        </button>
      </div>
    </div>

    <div v-if="rendezVousStore.loading && APPOINTMENTS.length === 0" class="flex justify-center p-12">
      <Loader2 class="w-8 h-8 text-blue-500 animate-spin" />
    </div>

    <!-- Week grid -->
    <div v-else class="rounded-2xl border border-border bg-card p-4 overflow-hidden">
      <div class="grid grid-cols-5 gap-3">
        <div v-for="(d, i) in DAYS" :key="i" class="space-y-2">
          <!-- Day header -->
          <div :class="cn('text-center pb-3 border-b-2 transition-colors relative group', isToday(d) ? 'border-blue-500' : 'border-border', authStore.role === 'secretaire' ? 'cursor-pointer' : '')" @click="authStore.role === 'secretaire' ? openNewRv(toISO(d)) : null">
            <p class="text-xs text-muted-foreground font-medium uppercase tracking-wide">{{ DAY_NAMES[i] }}</p>
            <div :class="cn('w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold mx-auto mt-1.5 transition-colors', isToday(d) ? 'bg-blue-600 text-white' : 'text-foreground', authStore.role === 'secretaire' ? 'group-hover:bg-accent' : '')">
              {{ d.getDate() }}
            </div>
            <p class="text-xs text-muted-foreground mt-1">{{ getAppts(d).length }} RDV</p>
            <div v-if="authStore.role === 'secretaire'" class="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity bg-background/50 backdrop-blur-[1px]">
              <Plus class="w-5 h-5 text-blue-600" />
            </div>
          </div>

          <!-- Appointments -->
          <div class="space-y-2 min-h-40 relative">
            <template v-if="getAppts(d).length === 0">
              <div class="flex items-center justify-center h-28 rounded-xl border-2 border-dashed border-border mt-1">
                <p class="text-xs text-muted-foreground">Libre</p>
              </div>
            </template>
            <template v-else>
              <div
                v-for="(a, ai) in getAppts(d)"
                :key="'grid-' + a.id"
                v-motion
                :initial="{ opacity: 0, y: 6 }"
                :enter="{ opacity: 1, y: 0, transition: { delay: ai * 60 } }"
                @click="authStore.role === 'secretaire' ? openEditRv(a) : null"
                :class="cn('p-2.5 rounded-xl border transition-all duration-200', authStore.role === 'secretaire' ? 'cursor-pointer hover:shadow-md hover:scale-[1.02]' : '', TYPE_CLS[a.type] || 'bg-muted border-border')"
              >
                <p class="text-xs font-semibold leading-tight truncate">{{ a.patientName?.split(' ')[0] }}</p>
                <p class="text-xs font-semibold leading-tight truncate opacity-70">{{ a.patientName?.split(' ').slice(1).join(' ') }}</p>
                <div class="flex items-center gap-1 mt-1.5">
                  <Clock class="w-2.5 h-2.5 opacity-60" />
                  <p class="text-xs opacity-75 font-mono">{{ a.time }}</p>
                </div>
                <span class="text-xs font-medium block mt-1 opacity-80">{{ TYPE_LABELS[a.type] }}</span>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- Upcoming list -->
    <div v-if="!rendezVousStore.loading" class="rounded-2xl border border-border bg-card p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="font-semibold text-foreground">Liste des rendez-vous</h3>
        <div class="flex gap-1 text-xs">
          <span v-for="(cls, type) in TYPE_CLS" :key="type" :class="cn('px-2 py-0.5 rounded-full border font-medium', cls)">{{ TYPE_LABELS[type] }}</span>
        </div>
      </div>
      <div class="space-y-2">
        <div v-if="APPOINTMENTS.length === 0" class="text-center py-6 text-muted-foreground text-sm">Aucun rendez-vous trouvé</div>
        <div
          v-for="(a, i) in APPOINTMENTS"
          :key="'list-' + a.id"
          v-motion
          :initial="{ opacity: 0, x: -8 }"
          :enter="{ opacity: 1, x: 0, transition: { delay: i * 30 } }"
          :class="cn('flex items-center gap-3 p-3 rounded-xl transition-colors', authStore.role === 'secretaire' ? 'hover:bg-accent/50 cursor-pointer' : '', a.day < toISO(new Date()) ? 'opacity-50' : '')"
          @click="authStore.role === 'secretaire' ? openEditRv(a) : null"
        >
          <div :class="['w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold shrink-0', avatarColor(a.patientName?.split(' ')[0])]">
            {{ initials(a.patientName?.split(' ')[0], a.patientName?.split(' ')[1]) }}
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-foreground">{{ a.patientName }}</p>
            <p class="text-xs text-muted-foreground">
              {{ new Date(a.day).toLocaleDateString("fr-FR", { weekday: "short", day: "numeric", month: "short" }) }} à {{ a.time }}
            </p>
          </div>
          <div class="flex items-center gap-2 shrink-0">
            <span :class="cn('text-xs px-2 py-0.5 rounded-full border font-medium', TYPE_CLS[a.type] || 'bg-muted border-border')">{{ TYPE_LABELS[a.type] }}</span>
            <span class="text-xs text-muted-foreground font-mono">{{ a.duration }}min</span>
            <span v-if="a.day < toISO(new Date())" class="text-xs text-muted-foreground">Passé</span>
            <button @click.stop="viewPatient(a.patientId)" class="p-1.5 hover:bg-white dark:hover:bg-background rounded-lg border border-border shadow-sm text-xs text-muted-foreground hover:text-foreground">Fiche</button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Modal RendezVous -->
  <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm">
    <div v-motion :initial="{ opacity: 0, scale: 0.95 }" :enter="{ opacity: 1, scale: 1, transition: { duration: 150 } }" class="w-full max-w-lg rounded-2xl border border-border bg-card shadow-lg flex flex-col max-h-[90vh]">
      <div class="flex items-center justify-between p-4 border-b border-border">
        <h2 class="text-lg font-semibold text-foreground">{{ isEditMode ? 'Modifier rendez-vous' : 'Nouveau rendez-vous' }}</h2>
        <button @click="showModal = false" class="text-muted-foreground hover:text-foreground transition-colors p-1 rounded-lg hover:bg-accent"><X class="w-5 h-5" /></button>
      </div>
      
      <div class="p-5 overflow-y-auto space-y-5">
        <div v-if="rendezVousStore.error" class="p-3 bg-red-50 text-red-600 border border-red-200 rounded-xl text-sm flex items-start gap-2">
           <AlertCircle class="w-4 h-4 shrink-0 mt-0.5" /> {{ rendezVousStore.error }}
        </div>

        <!-- Patient Selection -->
        <div class="space-y-1.5">
          <label class="text-xs font-medium text-foreground">Patient *</label>
          <div v-if="!selPatient" class="relative">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <input v-model="pq" @focus="showSug = true" @blur="setTimeout(() => showSug = false, 200)" placeholder="Rechercher un patient…" class="w-full h-10 pl-9 pr-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 text-foreground placeholder:text-muted-foreground" />
            <div v-if="showSug && sug.length > 0" class="absolute left-0 right-0 top-full mt-2 bg-card border border-border rounded-xl shadow-lg z-10 py-1 max-h-48 overflow-y-auto">
              <button v-for="p in sug" :key="p.id" @click="selPatient = p; pq = ''; showSug = false" class="w-full text-left px-4 py-2 text-sm hover:bg-accent flex items-center gap-2">
                <span class="text-foreground">{{ p.firstName }} {{ p.lastName }}</span>
              </button>
            </div>
          </div>
          <div v-else class="flex items-center justify-between p-3 border border-blue-200 dark:border-blue-800 bg-blue-50/50 dark:bg-blue-900/20 rounded-xl">
            <div class="flex items-center gap-3">
              <div :class="['w-8 h-8 rounded-full flex items-center justify-center text-xs font-semibold shrink-0', avatarColor(selPatient.firstName)]">
                {{ initials(selPatient.firstName, selPatient.lastName) }}
              </div>
              <div>
                <p class="font-medium text-foreground text-sm">{{ selPatient.firstName }} {{ selPatient.lastName }}</p>
              </div>
            </div>
            <button @click="selPatient = null" class="p-1.5 rounded-lg hover:bg-white dark:hover:bg-background text-muted-foreground transition-colors"><Trash2 class="w-4 h-4 text-red-500" /></button>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-1.5">
            <label class="text-xs font-medium text-foreground">Date *</label>
            <div class="relative">
              <Calendar class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <input type="date" v-model="formDate" class="w-full h-10 pl-9 pr-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 text-foreground" />
            </div>
          </div>
          <div class="space-y-1.5">
            <label class="text-xs font-medium text-foreground">Heure *</label>
            <div class="relative">
              <Clock class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <input type="time" v-model="formTime" class="w-full h-10 pl-9 pr-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 text-foreground" />
            </div>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-1.5">
            <label class="text-xs font-medium text-foreground">Type *</label>
            <select v-model="formType" class="w-full h-10 px-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 text-foreground">
              <option value="CONSULTATION">Consultation</option>
              <option value="SUIVI">Suivi</option>
              <option value="URGENCE">Urgence</option>
              <option value="BILAN">Bilan</option>
            </select>
          </div>
          <div class="space-y-1.5">
            <label class="text-xs font-medium text-foreground">Durée (min) *</label>
            <input type="number" v-model="formDuration" class="w-full h-10 px-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 text-foreground" />
          </div>
        </div>
        
        <div class="space-y-1.5">
          <label class="text-xs font-medium text-foreground">Remarques (optionnel)</label>
          <textarea v-model="formNotes" rows="2" placeholder="Motif du RDV, informations complémentaires..." class="w-full p-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 text-foreground resize-none"></textarea>
        </div>

      </div>

      <div class="p-4 border-t border-border flex items-center justify-between bg-muted/20">
        <button v-if="isEditMode" @click="showDeleteModal = true" class="text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 px-3 py-2 rounded-xl text-sm font-medium flex items-center gap-1.5 transition-colors"><Trash2 class="w-4 h-4" /> Supprimer</button>
        <div v-else></div>
        
        <div class="flex gap-2">
          <button @click="showModal = false" class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors px-4 py-2 text-sm">Annuler</button>
          <button @click="saveRv" :disabled="!selPatient || !formDate || !formTime || submitting" class="bg-blue-600 text-white hover:bg-blue-700 shadow-sm inline-flex items-center justify-center rounded-xl font-medium transition-colors disabled:opacity-50 px-4 py-2 text-sm gap-2">
            <Loader2 v-if="submitting" class="w-4 h-4 animate-spin" />
            <FileCheck v-else class="w-4 h-4" />
            Enregistrer
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- Delete Confirmation Modal -->
  <div v-if="showDeleteModal" class="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm">
      <div v-motion :initial="{ opacity: 0, scale: 0.95 }" :enter="{ opacity: 1, scale: 1, transition: { duration: 150 } }" class="w-full max-w-sm rounded-2xl border border-border bg-card p-6 shadow-lg">
        <h2 class="text-lg font-semibold text-foreground mb-2">Annuler ce rendez-vous ?</h2>
        <p class="text-sm text-muted-foreground mb-6">Êtes-vous sûr de vouloir supprimer ce rendez-vous ? Cette action est irréversible.</p>
        <div class="flex justify-end gap-3">
          <button @click="showDeleteModal = false" :disabled="submitting" class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors px-4 py-2 text-sm">Non, garder</button>
          <button @click="confirmDeleteRv" :disabled="submitting" class="bg-red-600 text-white hover:bg-red-700 shadow-sm inline-flex items-center justify-center rounded-xl font-medium transition-colors disabled:opacity-50 px-4 py-2 text-sm gap-2">
             <Loader2 v-if="submitting" class="w-4 h-4 animate-spin" />
             Oui, supprimer
          </button>
        </div>
      </div>
  </div>

</template>