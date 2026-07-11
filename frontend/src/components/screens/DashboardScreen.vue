<script setup>
import { ref, onMounted } from 'vue'
import {
  Users,
  FileText,
  Calendar,
  Activity,
  ArrowRight,
  UserPlus,
  Pencil,
  AlertCircle
} from 'lucide-vue-next'

const loading = ref(true)
onMounted(() => {
  setTimeout(() => loading.value = false, 1100)
})

const KPIS = [
  { label: "Patients aujourd'hui", value: "12", delta: "+3", icon: Users,     bg: "bg-blue-50 dark:bg-blue-900/30",   ic: "text-blue-600" },
  { label: "Ordonnances actives",  value: "47", delta: "+8", icon: FileText,  bg: "bg-emerald-50 dark:bg-emerald-900/30", ic: "text-emerald-600" },
  { label: "RDV cette semaine",    value: "34", delta: "-2", icon: Calendar,  bg: "bg-violet-50 dark:bg-violet-900/30",   ic: "text-violet-600" },
  { label: "Patients actifs",      value: "284",delta: "+12",icon: Activity,  bg: "bg-amber-50 dark:bg-amber-900/30",     ic: "text-amber-600" },
]

const PATIENTS = [
  { id: "p1", firstName: "Sophie", lastName: "Laurent", lastVisit: "2026-07-08", status: "active" },
  { id: "p2", firstName: "Marc", lastName: "Dubois", lastVisit: "2026-07-05", status: "active" },
  { id: "p3", firstName: "Amira", lastName: "Benali", lastVisit: "2026-06-28", status: "active" },
  { id: "p4", firstName: "Théo", lastName: "Moreau", lastVisit: "2026-07-01", status: "inactive" },
]

const ACTIVITY = [
  { id: 1, text: "Ordonnance créée pour Sophie Laurent", time: "Il y a 2h", icon: FileText },
  { id: 2, text: "Nouveau patient ajouté : Lucas Petit", time: "Il y a 4h", icon: UserPlus },
  { id: 3, text: "Rendez-vous confirmé — Amira Benali", time: "Il y a 6h", icon: Calendar },
  { id: 4, text: "Dossier mis à jour — Marc Dubois", time: "Hier", icon: Pencil },
  { id: 5, text: "Ordonnance expirée — Théo Moreau", time: "Hier", icon: AlertCircle },
]

const PIE = [
  { name: "Actives", v: 47, c: "#10B981" },
  { name: "Expirées", v: 18, c: "#EF4444" },
  { name: "Archivées", v: 31, c: "#8B5CF6" },
]

const today = new Date().toLocaleDateString("fr-FR", { weekday: "long", day: "numeric", month: "long", year: "numeric" })

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
    <div class="flex items-start justify-between flex-wrap gap-3">
      <div>
        <h1 class="text-2xl font-bold text-foreground">Bonjour, Dr. Martin</h1>
        <p class="text-muted-foreground text-sm mt-0.5 capitalize">{{ today }}</p>
      </div>
      <div class="flex gap-2">
        <button class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
          <Calendar class="w-4 h-4" /> Agenda
        </button>
        <button class="bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
          Nouveau patient
        </button>
      </div>
    </div>

    <!-- KPIs -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <template v-if="loading">
        <div v-for="i in 4" :key="i" class="animate-pulse rounded-xl bg-muted h-28"></div>
      </template>
      <template v-else>
        <div v-for="(k, i) in KPIS" :key="k.label" v-motion :initial="{ opacity: 0, y: 18 }" :enter="{ opacity: 1, y: 0, transition: { delay: i * 80 } }">
          <div class="rounded-2xl border border-border bg-card p-5 hover:shadow-md transition-shadow duration-200">
            <div class="flex items-start justify-between">
              <div>
                <p class="text-xs text-muted-foreground font-medium leading-tight">{{ k.label }}</p>
                <p class="text-3xl font-bold text-foreground mt-1 font-mono">{{ k.value }}</p>
                <span :class="['text-xs font-medium mt-1 inline-block', k.delta.startsWith('+') ? 'text-emerald-600' : 'text-red-500']">
                  {{ k.delta }} ce mois
                </span>
              </div>
              <div :class="['w-10 h-10 rounded-xl flex items-center justify-center', k.bg]">
                <component :is="k.icon" :class="['w-5 h-5', k.ic]" />
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Charts -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
      <div v-motion :initial="{ opacity: 0, y: 18 }" :enter="{ opacity: 1, y: 0, transition: { delay: 360 } }" class="lg:col-span-2">
        <div class="rounded-2xl border border-border bg-card p-6 h-full flex flex-col">
          <div class="flex items-start justify-between mb-6 flex-wrap gap-3">
            <div>
              <h3 class="font-semibold text-foreground">Activité médicale</h3>
              <p class="text-xs text-muted-foreground mt-0.5">Consultations et ordonnances · 2026</p>
            </div>
            <div class="flex gap-4 text-xs text-muted-foreground">
              <span class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-blue-500 inline-block" />Consultations</span>
              <span class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-emerald-500 inline-block" />Ordonnances</span>
            </div>
          </div>
          <div v-if="loading" class="animate-pulse rounded-xl bg-muted h-52"></div>
          <div v-else class="flex-1 flex items-end gap-2 h-[200px] mt-auto">
             <div class="h-full w-full bg-gradient-to-t from-blue-500/10 to-transparent border-b-2 border-blue-500 relative flex items-end justify-between px-4 pb-2">
                 <div v-for="m in ['Jan','Fév','Mar','Avr','Mai','Jun','Jul']" :key="m" class="text-[11px] text-muted-foreground">{{ m }}</div>
             </div>
          </div>
        </div>
      </div>

      <div v-motion :initial="{ opacity: 0, y: 18 }" :enter="{ opacity: 1, y: 0, transition: { delay: 440 } }">
        <div class="rounded-2xl border border-border bg-card p-6 h-full flex flex-col">
          <h3 class="font-semibold text-foreground">Statut ordonnances</h3>
          <p class="text-xs text-muted-foreground mb-4 mt-0.5">Répartition actuelle</p>
          <div v-if="loading" class="animate-pulse rounded-xl bg-muted flex-1"></div>
          <div v-else class="flex flex-col h-full">
            <div class="flex-1 flex items-center justify-center relative">
               <svg viewBox="0 0 100 100" class="w-32 h-32 transform -rotate-90">
                 <circle cx="50" cy="50" r="40" fill="transparent" stroke="#10B981" stroke-width="20" stroke-dasharray="125.6 251.2" />
                 <circle cx="50" cy="50" r="40" fill="transparent" stroke="#EF4444" stroke-width="20" stroke-dasharray="45.2 251.2" stroke-dashoffset="-125.6" />
                 <circle cx="50" cy="50" r="40" fill="transparent" stroke="#8B5CF6" stroke-width="20" stroke-dasharray="80.4 251.2" stroke-dashoffset="-170.8" />
               </svg>
            </div>
            <div class="space-y-2 mt-2">
              <div v-for="p in PIE" :key="p.name" class="flex items-center justify-between text-xs">
                <span class="flex items-center gap-2 text-muted-foreground">
                  <span class="w-2 h-2 rounded-full shrink-0" :style="{ background: p.c }" />
                  {{ p.name }}
                </span>
                <span class="font-semibold text-foreground font-mono">{{ p.v }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom row -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div v-motion :initial="{ opacity: 0, y: 18 }" :enter="{ opacity: 1, y: 0, transition: { delay: 500 } }">
        <div class="rounded-2xl border border-border bg-card p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="font-semibold text-foreground">Patients récents</h3>
            <button class="text-xs text-blue-600 hover:text-blue-700 font-medium flex items-center gap-1">
              Voir tous <ArrowRight class="w-3 h-3" />
            </button>
          </div>
          <div class="space-y-2">
            <template v-if="loading">
              <div v-for="i in 4" :key="i" class="animate-pulse rounded-xl bg-muted h-12"></div>
            </template>
            <template v-else>
              <div v-for="(p, i) in PATIENTS" :key="p.id" v-motion :initial="{ opacity: 0, x: -8 }" :enter="{ opacity: 1, x: 0, transition: { delay: 540 + i * 60 } }"
                class="flex items-center gap-3 p-2 rounded-xl hover:bg-accent/60 transition-colors cursor-pointer"
              >
                <div :class="['rounded-full flex items-center justify-center font-semibold shrink-0 w-8 h-8 text-xs', avatarColor(p.firstName)]">
                  {{ initials(p.firstName, p.lastName) }}
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-foreground truncate">{{ p.firstName }} {{ p.lastName }}</p>
                  <p class="text-xs text-muted-foreground">Vu le {{ fmt(p.lastVisit) }}</p>
                </div>
                <span :class="[
                  'inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border',
                  p.status === 'active' ? 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-400 dark:border-emerald-800' : 'bg-gray-100 text-gray-500 border-gray-200 dark:bg-gray-800 dark:text-gray-400 dark:border-gray-700'
                ]">
                  <span v-if="p.status === 'active'" class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                  {{ p.status === 'active' ? 'Actif' : 'Inactif' }}
                </span>
              </div>
            </template>
          </div>
        </div>
      </div>

      <div v-motion :initial="{ opacity: 0, y: 18 }" :enter="{ opacity: 1, y: 0, transition: { delay: 550 } }">
        <div class="rounded-2xl border border-border bg-card p-6">
          <h3 class="font-semibold text-foreground mb-4">Activité récente</h3>
          <div class="space-y-4">
            <template v-if="loading">
              <div v-for="i in 5" :key="i" class="animate-pulse rounded-xl bg-muted h-9"></div>
            </template>
            <template v-else>
              <div v-for="(item, i) in ACTIVITY" :key="item.id" v-motion :initial="{ opacity: 0, x: 8 }" :enter="{ opacity: 1, x: 0, transition: { delay: 580 + i * 60 } }"
                class="flex items-start gap-3"
              >
                <div class="w-7 h-7 bg-blue-50 dark:bg-blue-900/30 rounded-lg flex items-center justify-center shrink-0 mt-0.5">
                  <component :is="item.icon" class="w-3.5 h-3.5 text-blue-600" />
                </div>
                <div>
                  <p class="text-xs text-foreground font-medium leading-snug">{{ item.text }}</p>
                  <p class="text-xs text-muted-foreground mt-0.5">{{ item.time }}</p>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
