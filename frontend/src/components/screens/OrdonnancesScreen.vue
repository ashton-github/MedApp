<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  FileText,
  Search,
  Plus,
  Eye,
  Download,
  Printer,
  Pill
} from 'lucide-vue-next'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { screens } from '../../constants/medapp.js'
import { cn } from '../../lib/utils.js'

const { authUser, openNewOrdonnance, showScreen } = useMedAppState()

const filter = ref('ALL')
const q = ref('')
const loading = ref(true)

onMounted(() => {
  setTimeout(() => loading.value = false, 700)
})

const PRESCRIPTIONS = [
  { id: "rx1", patientId: "p1", patientName: "Sophie Laurent", date: "2026-07-08", doctorName: "Dr. Martin", status: "ACTIVE", medications: [{ name: "Amoxicilline 1g", dosage: "1 matin, 1 soir", duration: "7 jours" }] },
  { id: "rx2", patientId: "p2", patientName: "Marc Dubois", date: "2026-07-05", doctorName: "Dr. Martin", status: "ACTIVE", medications: [{ name: "Doliprane 1000", dosage: "1 si douleur", duration: "5 jours" }, { name: "Spasfon", dosage: "2 matin, midi, soir", duration: "3 jours" }] },
  { id: "rx3", patientId: "p4", patientName: "Théo Moreau", date: "2026-06-12", doctorName: "Dr. Martin", status: "EXPIRED", medications: [{ name: "Loratadine", dosage: "1 par jour", duration: "1 mois" }] },
  { id: "rx4", patientId: "p6", patientName: "Lucas Petit", date: "2025-11-20", doctorName: "Dr. Martin", status: "ARCHIVED", medications: [{ name: "Augmentin", dosage: "1 matin, 1 soir", duration: "6 jours" }] },
]

const list = computed(() => {
  return PRESCRIPTIONS.filter(r => {
    const mf = filter.value === 'ALL' || r.status === filter.value
    const ms = r.patientName.toLowerCase().includes(q.value.toLowerCase()) || r.medications.some(m => m.name.toLowerCase().includes(q.value.toLowerCase()))
    return mf && ms
  })
})

const TABS = computed(() => [
  { v: 'ALL', label: 'Toutes', n: PRESCRIPTIONS.length },
  { v: 'ACTIVE', label: 'Actives', n: PRESCRIPTIONS.filter(r => r.status === 'ACTIVE').length },
  { v: 'EXPIRED', label: 'Expirées', n: PRESCRIPTIONS.filter(r => r.status === 'EXPIRED').length },
  { v: 'ARCHIVED', label: 'Archivées', n: PRESCRIPTIONS.filter(r => r.status === 'ARCHIVED').length },
])

const fmt = (d) => new Date(d).toLocaleDateString("fr-FR", { day: "2-digit", month: "short", year: "numeric" })
const initials = (name) => {
  const p = name.split(' ')
  return p.length > 1 ? `${p[0][0]}${p[1][0]}`.toUpperCase() : name[0].toUpperCase()
}

const AVATAR_COLORS = [
  "bg-blue-100 text-blue-700", "bg-emerald-100 text-emerald-700",
  "bg-violet-100 text-violet-700", "bg-amber-100 text-amber-700",
  "bg-rose-100 text-rose-700", "bg-cyan-100 text-cyan-700",
]
const avatarColor = (name) => AVATAR_COLORS[name.charCodeAt(0) % AVATAR_COLORS.length]
</script>

<template>
  <div class="p-6 space-y-6">
    <div class="flex items-center justify-between flex-wrap gap-3">
      <div>
        <h1 class="text-2xl font-bold text-foreground">Ordonnances</h1>
        <p class="text-muted-foreground text-sm mt-0.5">{{ PRESCRIPTIONS.length }} ordonnances au total</p>
      </div>
      <button v-if="authUser?.role === 'medecin'" @click="openNewOrdonnance" class="bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
        <Plus class="w-4 h-4" /> Nouvelle ordonnance
      </button>
    </div>

    <div class="flex gap-1 p-1 bg-muted rounded-xl w-fit">
      <button v-for="t in TABS" :key="t.v" @click="filter = t.v"
        :class="cn('flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium transition-all duration-200', filter === t.v ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground')"
      >
        {{ t.label }}
        <span :class="cn('text-xs px-1.5 py-0.5 rounded-full font-mono', filter === t.v ? 'bg-blue-100 dark:bg-blue-900/50 text-blue-700 dark:text-blue-400' : 'bg-border text-muted-foreground')">
          {{ t.n }}
        </span>
      </button>
    </div>

    <div class="relative max-w-sm">
      <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
      <input type="search" v-model="q" placeholder="Rechercher…"
        class="w-full h-10 pl-9 pr-4 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground"
      />
    </div>

    <template v-if="loading">
      <div class="space-y-3">
        <div v-for="i in 5" :key="i" class="animate-pulse bg-muted rounded-xl h-24"></div>
      </div>
    </template>
    <template v-else-if="list.length === 0">
      <div class="flex flex-col items-center py-16 text-center">
        <div class="w-14 h-14 bg-muted rounded-2xl flex items-center justify-center mb-3">
          <FileText class="w-7 h-7 text-muted-foreground" />
        </div>
        <p class="font-medium text-foreground">Aucune ordonnance trouvée</p>
      </div>
    </template>
    <template v-else>
      <div class="space-y-3">
        <div v-for="(rx, i) in list" :key="rx.id" v-motion :initial="{ opacity: 0, y: 8 }" :enter="{ opacity: 1, y: 0, transition: { delay: i * 50 } }">
          <div class="rounded-2xl border border-border bg-card p-4 hover:shadow-md transition-all duration-200 hover:border-blue-200/70 dark:hover:border-blue-800/50">
            <div class="flex items-start justify-between gap-4">
              <div class="flex items-start gap-3 flex-1 min-w-0">
                <div :class="['rounded-full flex items-center justify-center font-semibold shrink-0 w-8 h-8 text-xs', avatarColor(rx.patientName)]">
                  {{ initials(rx.patientName) }}
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 flex-wrap">
                    <h3 class="text-sm font-semibold text-foreground">{{ rx.patientName }}</h3>
                    <span :class="[
                      'inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border',
                      rx.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-400 dark:border-emerald-800' :
                      rx.status === 'EXPIRED' ? 'bg-red-50 text-red-700 border-red-200 dark:bg-red-950/40 dark:text-red-400 dark:border-red-800' :
                      'bg-gray-100 text-gray-500 border-gray-200 dark:bg-gray-800 dark:text-gray-400 dark:border-gray-700'
                    ]">
                      {{ rx.status === 'ACTIVE' ? 'Active' : rx.status === 'EXPIRED' ? 'Expirée' : 'Archivée' }}
                    </span>
                  </div>
                  <p class="text-xs text-muted-foreground mt-0.5">{{ fmt(rx.date) }} · {{ rx.doctorName }}</p>
                  <div class="flex flex-wrap gap-1.5 mt-2">
                    <span v-for="m in rx.medications" :key="m.name" class="inline-flex items-center gap-1 px-2 py-0.5 bg-muted rounded-lg text-xs text-muted-foreground">
                      <Pill class="w-2.5 h-2.5" />{{ m.name }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="flex gap-1 shrink-0">
                <button @click="showScreen(screens.pdfPreview)" class="p-2 rounded-lg hover:bg-accent text-muted-foreground hover:text-foreground transition-colors"><Eye class="w-4 h-4" /></button>
                <button class="p-2 rounded-lg hover:bg-accent text-muted-foreground hover:text-foreground transition-colors"><Download class="w-4 h-4" /></button>
                <button class="p-2 rounded-lg hover:bg-accent text-muted-foreground hover:text-foreground transition-colors"><Printer class="w-4 h-4" /></button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>