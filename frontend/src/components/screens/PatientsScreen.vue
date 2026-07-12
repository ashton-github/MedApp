<script setup>
import { ref, computed } from 'vue'
import {
  Users,
  Search,
  Filter,
  Plus,
  LayoutGrid,
  List,
  Eye,
  Pencil,
  Phone,
  Mail
} from 'lucide-vue-next'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { screens } from '../../constants/medapp.js'
import { cn } from '../../lib/utils.js'

const { openNewPatient, showScreen } = useMedAppState()

const view = ref('grid')
const q = ref('')
const loading = ref(false)

// We use the same mock data logic as React
const PATIENTS = [
  { id: "p1", firstName: "Sophie", lastName: "Laurent", dob: "1985-03-15", gender: "F", phone: "+33 6 12 34 56 78", email: "sophie.laurent@email.fr", insurance: "CPAM Paris", lastVisit: "2026-07-08", status: "active", bloodType: "A+", allergies: ["Pénicilline"] },
  { id: "p2", firstName: "Marc", lastName: "Dubois", dob: "1972-11-22", gender: "M", phone: "+33 6 98 76 54 32", email: "marc.dubois@email.fr", insurance: "Mutuelle AG2R", lastVisit: "2026-07-05", status: "active", bloodType: "O+", allergies: [] },
  { id: "p3", firstName: "Amira", lastName: "Benali", dob: "1990-07-04", gender: "F", phone: "+33 7 45 23 11 89", email: "amira.benali@email.fr", insurance: "CPAM Lyon", lastVisit: "2026-06-28", status: "active", bloodType: "B+", allergies: ["Aspirine"] },
  { id: "p4", firstName: "Théo", lastName: "Moreau", dob: "2001-02-28", gender: "M", phone: "+33 6 33 44 55 66", email: "theo.moreau@email.fr", insurance: "CPAM Bordeaux", lastVisit: "2026-07-01", status: "inactive", bloodType: "AB-", allergies: [] },
  { id: "p5", firstName: "Claire", lastName: "Fontaine", dob: "1965-09-10", gender: "F", phone: "+33 6 77 88 99 00", email: "claire.fontaine@email.fr", insurance: "MGEN", lastVisit: "2026-07-09", status: "active", bloodType: "A-", allergies: ["Latex"] },
  { id: "p6", firstName: "Lucas", lastName: "Petit", dob: "1995-12-01", gender: "M", phone: "+33 7 11 22 33 44", email: "lucas.petit@email.fr", insurance: "CPAM Nantes", lastVisit: "2026-06-15", status: "active", bloodType: "O-", allergies: [] },
]

const list = computed(() => {
  return PATIENTS.filter(p =>
    `${p.firstName} ${p.lastName}`.toLowerCase().includes(q.value.toLowerCase()) ||
    p.email.toLowerCase().includes(q.value.toLowerCase())
  )
})

const AVATAR_COLORS = [
  "bg-blue-100 text-blue-700", "bg-emerald-100 text-emerald-700",
  "bg-violet-100 text-violet-700", "bg-amber-100 text-amber-700",
  "bg-rose-100 text-rose-700", "bg-cyan-100 text-cyan-700",
]
const avatarColor = (n) => AVATAR_COLORS[n.charCodeAt(0) % AVATAR_COLORS.length]
const initials = (f, l) => `${f[0]}${l[0]}`.toUpperCase()
const fmt = (d) => new Date(d).toLocaleDateString("fr-FR", { day: "2-digit", month: "short", year: "numeric" })
</script>

<template>
  <div class="p-6 space-y-6">
    <div class="flex items-center justify-between flex-wrap gap-3">
      <div>
        <h1 class="text-2xl font-bold text-foreground">Patients</h1>
        <p class="text-muted-foreground text-sm mt-0.5">{{ PATIENTS.length }} patients enregistrés</p>
      </div>
      <button @click="openNewPatient" class="bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
        <Plus class="w-4 h-4" /> Nouveau patient
      </button>
    </div>

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

    <template v-if="loading">
      <div :class="view === 'grid' ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4' : 'space-y-3'">
        <div v-for="i in 6" :key="i" :class="cn('animate-pulse rounded-xl bg-muted', view === 'grid' ? 'h-52' : 'h-16')"></div>
      </div>
    </template>
    <template v-else-if="list.length === 0">
      <div class="flex flex-col items-center py-20 text-center">
        <div class="w-16 h-16 bg-muted rounded-2xl flex items-center justify-center mb-4">
          <Users class="w-8 h-8 text-muted-foreground" />
        </div>
        <h3 class="font-medium text-foreground">Aucun patient trouvé</h3>
        <p class="text-muted-foreground text-sm mt-1">Modifiez votre recherche ou ajoutez un patient</p>
      </div>
    </template>
    <template v-else-if="view === 'grid'">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <div v-for="(p, i) in list" :key="p.id" v-motion :initial="{ opacity: 0, y: 14 }" :enter="{ opacity: 1, y: 0, transition: { delay: i * 50 } }" class="transform transition hover:-translate-y-0.5">
          <div class="rounded-2xl border border-border bg-card p-5 cursor-pointer hover:shadow-lg hover:border-blue-200 dark:hover:border-blue-800/60 transition-all duration-200" @click="showScreen(screens.patientDetail)">
            <div class="flex items-start justify-between mb-3">
              <div :class="['rounded-full flex items-center justify-center font-semibold shrink-0 w-10 h-10 text-sm', avatarColor(p.firstName)]">
                {{ initials(p.firstName, p.lastName) }}
              </div>
              <span :class="['inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border', p.status === 'active' ? 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-400 dark:border-emerald-800' : 'bg-gray-100 text-gray-500 border-gray-200 dark:bg-gray-800 dark:text-gray-400 dark:border-gray-700']">
                <span v-if="p.status === 'active'" class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                {{ p.status === 'active' ? 'Actif' : 'Inactif' }}
              </span>
            </div>
            <h3 class="font-semibold text-foreground">{{ p.firstName }} {{ p.lastName }}</h3>
            <p class="text-xs text-muted-foreground mt-0.5">Né(e) le {{ fmt(p.dob) }}</p>
            <div class="mt-3 space-y-1.5 text-xs text-muted-foreground">
              <div class="flex items-center gap-2"><Phone class="w-3 h-3 shrink-0" />{{ p.phone }}</div>
              <div class="flex items-center gap-2"><Mail class="w-3 h-3 shrink-0" /><span class="truncate">{{ p.email }}</span></div>
            </div>
            <div class="mt-3 pt-3 border-t border-border flex items-center justify-between">
              <span class="text-xs text-muted-foreground">Dernière visite</span>
              <span class="text-xs font-semibold text-foreground">{{ fmt(p.lastVisit) }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
    <template v-else>
      <div class="rounded-2xl border border-border bg-card overflow-hidden">
        <table class="w-full">
          <thead class="border-b border-border bg-muted/40">
            <tr>
              <th v-for="h in ['Patient', 'Naissance', 'Téléphone', 'Dernière visite', 'Statut', '']" :key="h" class="text-left px-4 py-3 text-xs font-semibold text-muted-foreground">{{ h }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(p, i) in list" :key="p.id" v-motion :initial="{ opacity: 0 }" :enter="{ opacity: 1, transition: { delay: i * 40 } }" class="border-b border-border last:border-0 hover:bg-accent/30 transition-colors cursor-pointer" @click="showScreen(screens.patientDetail)">
              <td class="px-4 py-3">
                <div class="flex items-center gap-3">
                  <div :class="['rounded-full flex items-center justify-center font-semibold shrink-0 w-8 h-8 text-xs', avatarColor(p.firstName)]">
                    {{ initials(p.firstName, p.lastName) }}
                  </div>
                  <div>
                    <p class="text-sm font-medium text-foreground">{{ p.firstName }} {{ p.lastName }}</p>
                    <p class="text-xs text-muted-foreground">{{ p.insurance }}</p>
                  </div>
                </div>
              </td>
              <td class="px-4 py-3 text-sm text-muted-foreground">{{ fmt(p.dob) }}</td>
              <td class="px-4 py-3 text-sm text-muted-foreground">{{ p.phone }}</td>
              <td class="px-4 py-3 text-sm text-muted-foreground">{{ fmt(p.lastVisit) }}</td>
              <td class="px-4 py-3">
                <span :class="['inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border', p.status === 'active' ? 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-400 dark:border-emerald-800' : 'bg-gray-100 text-gray-500 border-gray-200 dark:bg-gray-800 dark:text-gray-400 dark:border-gray-700']">
                  <span v-if="p.status === 'active'" class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                  {{ p.status === 'active' ? 'Actif' : 'Inactif' }}
                </span>
              </td>
              <td class="px-4 py-3">
                <div class="flex gap-1">
                  <button class="p-1.5 rounded-lg hover:bg-accent text-muted-foreground hover:text-foreground transition-colors"><Eye class="w-3.5 h-3.5" /></button>
                  <button class="p-1.5 rounded-lg hover:bg-accent text-muted-foreground hover:text-foreground transition-colors"><Pencil class="w-3.5 h-3.5" /></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <button @click="openNewPatient" class="fixed bottom-6 right-6 w-14 h-14 bg-blue-600 text-white rounded-2xl shadow-lg shadow-blue-600/30 flex items-center justify-center hover:bg-blue-700 transition-all active:scale-95 lg:hidden">
      <Plus class="w-6 h-6" />
    </button>
  </div>
</template>