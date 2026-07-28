<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import {
  Users,
  Search,
  Plus,
  LayoutGrid,
  List,
  Eye,
  Pencil,
  Phone,
  Mail,
  Trash2,
  AlertCircle,
  Filter
} from 'lucide-vue-next'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { usePatientStore } from '../../stores/patientStore.js'
import { useAuthStore } from '../../stores/authStore.js'
import { cn } from '../../lib/utils.js'

const { openNewPatient, editPatient, viewPatient } = useMedAppState()
const patientStore = usePatientStore()
const authStore = useAuthStore()
const isAdmin = authStore.role === 'admin'

const view = ref('grid')
const q    = ref('')

// Debounced search vs full list
let searchTimeout = null
watch(q, (val) => {
  clearTimeout(searchTimeout)
  if (val.trim()) {
    searchTimeout = setTimeout(() => patientStore.searchPatients(val.trim()), 350)
  } else {
    patientStore.fetchPatients()
  }
})

onMounted(() => patientStore.fetchPatients())

const patientToDelete = ref(null)
const patientToDeleteName = computed(() => {
  if (!patientToDelete.value) return ''
  const p = patientStore.patients.find(x => x.id === patientToDelete.value)
  return p ? `${p.firstName} ${p.lastName}` : ''
})

const confirmDelete = (id) => { patientToDelete.value = id }
const deletePatient = async () => {
  try {
    await patientStore.deletePatient(patientToDelete.value)
    patientToDelete.value = null
  } catch {
    // error already stored in patientStore.error
  }
}

const AVATAR_COLORS = [
  'bg-blue-100 text-blue-700', 'bg-emerald-100 text-emerald-700',
  'bg-violet-100 text-violet-700', 'bg-amber-100 text-amber-700',
  'bg-rose-100 text-rose-700', 'bg-cyan-100 text-cyan-700',
]
const avatarColor = (n) => AVATAR_COLORS[n.charCodeAt(0) % AVATAR_COLORS.length]
const initials    = (f, l) => `${f[0]}${l[0]}`.toUpperCase()
const fmt         = (d) => d ? new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' }) : '–'
</script>

<template>
  <div class="p-6 space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between flex-wrap gap-3">
      <div>
        <h1 class="text-2xl font-bold text-foreground">Patients</h1>
        <p class="text-muted-foreground text-sm mt-0.5">{{ patientStore.totalItems }} patient(s) enregistré(s)</p>
      </div>
      <button @click="openNewPatient" class="bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
        <Plus class="w-4 h-4" /> Nouveau patient
      </button>
    </div>

    <!-- Error banner -->
    <div v-if="patientStore.error" class="flex items-center gap-2 p-3 rounded-xl bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-800 text-red-600 dark:text-red-400 text-sm">
      <AlertCircle class="w-4 h-4 shrink-0" />
      {{ patientStore.error }}
    </div>

    <!-- Search & view toggle -->
    <div class="flex gap-3 flex-wrap items-center">
      <div class="relative flex-1 min-w-56">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
        <input type="search" v-model="q" placeholder="Rechercher un patient…"
          class="w-full h-10 pl-9 pr-4 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground"
        />
      </div>
      <button class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
        <Filter class="w-4 h-4" /> Filtrer
      </button>
      <div class="flex border border-border rounded-xl overflow-hidden">
        <button v-for="vv in ['grid', 'list']" :key="vv" @click="view = vv"
          :class="cn('p-2.5 transition-colors', view === vv ? 'bg-blue-600 text-white' : 'text-muted-foreground hover:bg-accent')"
        >
          <LayoutGrid v-if="vv === 'grid'" class="w-4 h-4" />
          <List v-else class="w-4 h-4" />
        </button>
      </div>
    </div>

    <!-- Skeleton loading -->
    <template v-if="patientStore.loading">
      <div :class="view === 'grid' ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4' : 'space-y-3'">
        <div v-for="i in 6" :key="i" :class="cn('animate-pulse rounded-xl bg-muted', view === 'grid' ? 'h-52' : 'h-16')"></div>
      </div>
    </template>

    <!-- Empty state -->
    <template v-else-if="patientStore.patients.length === 0">
      <div class="flex flex-col items-center py-20 text-center">
        <div class="w-16 h-16 bg-muted rounded-2xl flex items-center justify-center mb-4">
          <Users class="w-8 h-8 text-muted-foreground" />
        </div>
        <h3 class="font-medium text-foreground">Aucun patient trouvé</h3>
        <p class="text-muted-foreground text-sm mt-1">Modifiez votre recherche ou ajoutez un patient</p>
      </div>
    </template>

    <!-- Grid view -->
    <template v-else-if="view === 'grid'">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <div v-for="(p, i) in patientStore.patients" :key="p.id"
          v-motion :initial="{ opacity: 0, y: 14 }" :enter="{ opacity: 1, y: 0, transition: { delay: i * 50 } }"
          class="transform transition hover:-translate-y-0.5"
        >
          <div class="rounded-2xl border border-border bg-card p-5 cursor-pointer hover:shadow-lg hover:border-blue-200 dark:hover:border-blue-800/60 transition-all duration-200"
            @click="viewPatient(p.id)"
          >
            <div class="flex items-start justify-between mb-3">
              <div :class="['rounded-full flex items-center justify-center font-semibold shrink-0 w-10 h-10 text-sm', avatarColor(p.firstName)]">
                {{ initials(p.firstName, p.lastName) }}
              </div>
            </div>
            <h3 class="font-semibold text-foreground">{{ p.firstName }} {{ p.lastName }}</h3>
            <p class="text-xs text-muted-foreground mt-0.5">Né(e) le {{ fmt(p.birthDate) }}</p>
            <div class="mt-3 space-y-1.5 text-xs text-muted-foreground">
              <div class="flex items-center gap-2"><Phone class="w-3 h-3 shrink-0" />{{ p.phone || '–' }}</div>
              <div class="flex items-center gap-2"><Mail class="w-3 h-3 shrink-0" /><span class="truncate">{{ p.address || '–' }}</span></div>
            </div>
            <div class="mt-3 pt-3 border-t border-border flex items-center justify-between">
              <span class="text-xs text-muted-foreground">N° Sécurité Sociale</span>
              <div class="flex items-center gap-2">
                <span class="text-xs font-semibold text-foreground truncate max-w-[120px]">{{ p.socialSecurityNumber || '–' }}</span>
                <button @click.stop="editPatient(p)" class="p-1 rounded-md text-muted-foreground hover:bg-accent hover:text-foreground transition-colors" title="Modifier">
                  <Pencil class="w-3.5 h-3.5" />
                </button>
                <button v-if="isAdmin" @click.stop="confirmDelete(p.id)" class="p-1 rounded-md text-red-500 hover:bg-red-50 dark:hover:bg-red-950/50 transition-colors" title="Supprimer">
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- List view -->
    <template v-else>
      <div class="rounded-2xl border border-border bg-card overflow-hidden">
        <table class="w-full">
          <thead class="border-b border-border bg-muted/40">
            <tr>
              <th v-for="h in ['Patient', 'Naissance', 'Téléphone', 'N° SS', '']" :key="h" class="text-left px-4 py-3 text-xs font-semibold text-muted-foreground">{{ h }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(p, i) in patientStore.patients" :key="p.id"
              v-motion :initial="{ opacity: 0 }" :enter="{ opacity: 1, transition: { delay: i * 40 } }"
              class="border-b border-border last:border-0 hover:bg-accent/30 transition-colors cursor-pointer"
              @click="viewPatient(p.id)"
            >
              <td class="px-4 py-3">
                <div class="flex items-center gap-3">
                  <div :class="['rounded-full flex items-center justify-center font-semibold shrink-0 w-8 h-8 text-xs', avatarColor(p.firstName)]">
                    {{ initials(p.firstName, p.lastName) }}
                  </div>
                  <div>
                    <p class="text-sm font-medium text-foreground">{{ p.firstName }} {{ p.lastName }}</p>
                    <p class="text-xs text-muted-foreground">{{ p.referringDoctor || '–' }}</p>
                  </div>
                </div>
              </td>
              <td class="px-4 py-3 text-sm text-muted-foreground">{{ fmt(p.birthDate) }}</td>
              <td class="px-4 py-3 text-sm text-muted-foreground">{{ p.phone || '–' }}</td>
              <td class="px-4 py-3 text-sm text-muted-foreground">{{ p.socialSecurityNumber || '–' }}</td>
              <td class="px-4 py-3">
                <div class="flex gap-1">
                  <button @click.stop="viewPatient(p.id)" class="p-1.5 rounded-lg hover:bg-accent text-muted-foreground hover:text-foreground transition-colors"><Eye class="w-3.5 h-3.5" /></button>
                  <button @click.stop="editPatient(p)" class="p-1.5 rounded-lg hover:bg-accent text-muted-foreground hover:text-foreground transition-colors"><Pencil class="w-3.5 h-3.5" /></button>
                  <button v-if="isAdmin" @click.stop="confirmDelete(p.id)" class="p-1.5 rounded-lg text-red-500 hover:bg-red-50 dark:hover:bg-red-950/50 transition-colors" title="Supprimer"><Trash2 class="w-3.5 h-3.5" /></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- FAB -->
    <button @click="openNewPatient" class="fixed bottom-6 right-6 w-14 h-14 bg-blue-600 text-white rounded-2xl shadow-lg shadow-blue-600/30 flex items-center justify-center hover:bg-blue-700 transition-all active:scale-95 lg:hidden">
      <Plus class="w-6 h-6" />
    </button>

    <!-- Delete confirmation modal -->
    <div v-if="patientToDelete" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm">
      <div v-motion :initial="{ opacity: 0, scale: 0.95 }" :enter="{ opacity: 1, scale: 1, transition: { duration: 150 } }" class="w-full max-w-md rounded-2xl border border-border bg-card p-6 shadow-lg">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
            <Trash2 class="w-5 h-5 text-red-600 dark:text-red-500" />
          </div>
          <h2 class="text-lg font-semibold text-foreground">Supprimer le patient</h2>
        </div>
        <p class="text-sm text-muted-foreground mb-6">
          Êtes-vous sûr de vouloir supprimer <strong>{{ patientToDeleteName }}</strong> ? Cette action est irréversible.
        </p>
        <div class="flex justify-end gap-3">
          <button @click="patientToDelete = null" :disabled="patientStore.loading" class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-4 py-2 text-sm">Annuler</button>
          <button @click="deletePatient" :disabled="patientStore.loading" class="bg-red-600 text-white hover:bg-red-700 shadow-sm shadow-red-200/50 dark:shadow-red-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500 px-4 py-2 text-sm disabled:opacity-50">
            {{ patientStore.loading ? 'Suppression…' : 'Supprimer' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>