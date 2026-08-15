<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  FileText,
  Search,
  Plus,
  Pencil,
  Eye,
  Download,
  Archive,
  Pill
} from 'lucide-vue-next'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { useOrdonnanceStore } from '../../stores/ordonnanceStore.js'
import { usePatientStore } from '../../stores/patientStore.js'
import { screens } from '../../constants/medapp.js'
import { cn } from '../../lib/utils.js'

const { authUser, openNewOrdonnance, openEditOrdonnance, showScreen } = useMedAppState()
const ordonnanceStore = useOrdonnanceStore()
const patientStore = usePatientStore()

const filter = ref('ALL')
const q = ref('')
const archiveId = ref(null)  // ID of ordonnance pending archive confirmation
const archiving = ref(false)

const confirmArchive = async () => {
  if (!archiveId.value) return
  archiving.value = true
  try {
    await ordonnanceStore.archiveOrdonnance(archiveId.value)
  } finally {
    archiving.value = false
    archiveId.value = null
  }
}
onMounted(async () => {
  ordonnanceStore.ordonnances = []
  if (patientStore.patients.length === 0) {
    await patientStore.fetchPatients()
  }
  for (const p of patientStore.patients) {
    const rx = await ordonnanceStore.fetchOrdonnancesByPatientId(p.id)
    ordonnanceStore.ordonnances.push(...rx)
  }
})

const getPatientName = (id) => {
  const p = patientStore.patients.find(x => x.id === id)
  return p ? `${p.firstName} ${p.lastName}` : 'Patient inconnu'
}

const list = computed(() => {
  const defaultDocName = authUser.value?.email 
    ? `Dr. ${authUser.value.email.split('@')[0].charAt(0).toUpperCase() + authUser.value.email.split('@')[0].slice(1)}` 
    : 'Dr. inconnu'

  return ordonnanceStore.ordonnances.map(r => ({
    ...r,
    patientName: r.patientName || getPatientName(r.patientId),
    doctorName: r.doctorName || defaultDocName
  })).filter(r => {
    const mf = filter.value === 'ALL' || r.status === filter.value
    const ms = r.patientName.toLowerCase().includes(q.value.toLowerCase()) || r.medications.some(m => m.name.toLowerCase().includes(q.value.toLowerCase()))
    return mf && ms
  })
})

const TABS = computed(() => [
  { v: 'ALL', label: 'Toutes', n: ordonnanceStore.ordonnances.length },
  { v: 'ACTIVE', label: 'Actives', n: ordonnanceStore.ordonnances.filter(r => r.status === 'ACTIVE').length },
  { v: 'EXPIRED', label: 'Expirées', n: ordonnanceStore.ordonnances.filter(r => r.status === 'EXPIRED').length },
  { v: 'ARCHIVED', label: 'Archivées', n: ordonnanceStore.ordonnances.filter(r => r.status === 'ARCHIVED').length },
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
        <p class="text-muted-foreground text-sm mt-0.5">{{ ordonnanceStore.ordonnances.length }} ordonnances au total</p>
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

    <template v-if="ordonnanceStore.loading">
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
                  <p class="text-xs text-muted-foreground mt-0.5">{{ fmt(rx.issueDate) }} · {{ rx.doctorName }}</p>
                  <div class="flex flex-wrap gap-1.5 mt-2">
                    <span v-for="m in rx.medications" :key="m.name" class="inline-flex items-center gap-1 px-2 py-0.5 bg-muted rounded-lg text-xs text-muted-foreground">
                      <Pill class="w-2.5 h-2.5" />{{ m.name }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="flex gap-1 shrink-0">
                <button @click="ordonnanceStore.currentOrdonnance = rx; showScreen(screens.pdfPreview)" class="p-2 rounded-lg hover:bg-accent text-muted-foreground hover:text-foreground transition-colors" title="Aperçu"><Eye class="w-4 h-4" /></button>
                <button @click="ordonnanceStore.downloadPdf(rx.id)" class="p-2 rounded-lg hover:bg-accent text-muted-foreground hover:text-foreground transition-colors" title="Télécharger PDF"><Download class="w-4 h-4" /></button>
                <button
                  v-if="rx.status !== 'ARCHIVED' && authUser?.role === 'medecin'"
                  @click="openEditOrdonnance(rx)"
                  class="p-2 rounded-lg hover:bg-blue-50 dark:hover:bg-blue-900/20 text-muted-foreground hover:text-blue-600 transition-colors"
                  title="Modifier"
                ><Pencil class="w-4 h-4" /></button>
                <button
                  v-if="rx.status !== 'ARCHIVED' && authUser?.role === 'medecin'"
                  @click="archiveId = rx.id"
                  class="p-2 rounded-lg hover:bg-amber-50 dark:hover:bg-amber-900/20 text-muted-foreground hover:text-amber-600 transition-colors"
                  title="Archiver"
                ><Archive class="w-4 h-4" /></button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>

  <!-- Archive confirmation modal -->
  <Teleport to="body">
    <div v-if="archiveId" class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div v-motion :initial="{ scale: 0.9, opacity: 0 }" :enter="{ scale: 1, opacity: 1 }" class="bg-card rounded-2xl border border-border shadow-xl p-6 max-w-sm w-full">
        <div class="w-12 h-12 bg-amber-100 dark:bg-amber-900/40 rounded-xl flex items-center justify-center mb-4">
          <Archive class="w-6 h-6 text-amber-600" />
        </div>
        <h3 class="text-base font-bold text-foreground mb-1">Archiver cette ordonnance ?</h3>
        <p class="text-sm text-muted-foreground mb-5">Cette action est irréversible. L'ordonnance sera marquée comme archivée et ne pourra plus être modifiée.</p>
        <div class="flex gap-3">
          <button @click="archiveId = null" class="flex-1 border border-border text-foreground hover:bg-accent rounded-xl py-2 text-sm font-medium transition-colors">Annuler</button>
          <button @click="confirmArchive" :disabled="archiving" class="flex-1 bg-amber-500 hover:bg-amber-600 text-white rounded-xl py-2 text-sm font-medium transition-colors disabled:opacity-50 flex items-center justify-center gap-2">
            <span v-if="archiving" class="animate-spin inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full"></span>
            {{ archiving ? 'Archivage...' : 'Confirmer' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>