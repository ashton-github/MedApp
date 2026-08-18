<script setup>
import { computed, onMounted } from 'vue'
import {
  Users, FileText, Calendar, Activity, ArrowRight, UserPlus, Pencil
} from 'lucide-vue-next'

import { useMedAppState } from '../../composables/useMedAppState.js'
import { screens } from '../../constants/medapp.js'
import { useDashboardStore } from '../../stores/dashboardStore.js'

const { showScreen } = useMedAppState()
const dashboardStore = useDashboardStore()
const loading = computed(() => dashboardStore.loading)

const KPIS = computed(() => [
  { label: "Patients aujourd'hui", value: dashboardStore.statistics.patientsToday, icon: Users, bg: "bg-blue-50 dark:bg-blue-900/30", ic: "text-blue-600" },
  { label: "Ordonnances actives", value: dashboardStore.statistics.activePrescriptions, icon: FileText, bg: "bg-emerald-50 dark:bg-emerald-900/30", ic: "text-emerald-600" },
  { label: "RDV cette semaine", value: dashboardStore.statistics.appointmentsThisWeek, icon: Calendar, bg: "bg-violet-50 dark:bg-violet-900/30", ic: "text-violet-600" },
  { label: "Patients actifs", value: dashboardStore.statistics.activePatients, icon: Activity, bg: "bg-amber-50 dark:bg-amber-900/30", ic: "text-amber-600" }
])

const PATIENTS = computed(() => dashboardStore.recentPatients)
const PIE = computed(() => dashboardStore.prescriptionStats)
const ACTIVITY = computed(() => dashboardStore.activity)
const CHART = computed(() => dashboardStore.activityChart)

const today = new Date().toLocaleDateString("fr-FR", {
  weekday: "long", day: "numeric", month: "long", year: "numeric"
})

const AVATAR_COLORS = [
  "bg-blue-100 text-blue-700", "bg-emerald-100 text-emerald-700",
  "bg-violet-100 text-violet-700", "bg-amber-100 text-amber-700",
  "bg-rose-100 text-rose-700", "bg-cyan-100 text-cyan-700"
]

const avatarColor = (n = "") => AVATAR_COLORS[n.charCodeAt(0) % AVATAR_COLORS.length]
const initials = (f = "", l = "") => `${f[0] ?? ""}${l[0] ?? ""}`.toUpperCase()

const fmt = (d) => {
  if (!d) return 'Date inconnue'
  const date = new Date(d)
  if (Number.isNaN(date.getTime())) return 'Date inconnue'
  return date.toLocaleDateString("fr-FR", { day: "2-digit", month: "short", year: "numeric" })
}

const formatActivityTime = (date) => {
  if (!date) return ''
  const difference = Math.max(0, Date.now() - new Date(date).getTime())
  const minutes = Math.floor(difference / 60000)
  if (minutes < 1) return "À l'instant"
  if (minutes < 60) return `Il y a ${minutes} min`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `Il y a ${hours}h`
  const days = Math.floor(hours / 24)
  if (days === 1) return "Hier"
  return `Il y a ${days} jours`
}

const activityIcon = (type) => {
  switch (type) {
    case 'patient': return UserPlus
    case 'prescription': return FileText
    case 'appointment': return Calendar
    default: return Pencil
  }
}

const chartPath = (points) =>
  points.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`).join(' ')

const chartAreaPath = (points) =>
  `${chartPath(points)} L ${points.at(-1)?.x ?? 600} 180 L ${points[0]?.x ?? 0} 180 Z`

const openPatients = () => showScreen(screens.patients)
const openAgenda = () => showScreen(screens.agenda)
const openPatientForm = () => showScreen(screens.patientForm)

onMounted(() => dashboardStore.fetchDashboard())
</script>

<template>
  <div class="p-6 space-y-6">
    <div class="flex items-start justify-between flex-wrap gap-3">
      <div>
        <h1 class="text-2xl font-bold text-foreground">Bonjour</h1>
        <p class="text-muted-foreground text-sm mt-0.5 capitalize">{{ today }}</p>
      </div>
      <div class="flex gap-2">
        <button @click="openAgenda" class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors px-3 py-1.5 text-sm gap-1.5">
          <Calendar class="w-4 h-4" /> Agenda
        </button>
        <button @click="openPatientForm" class="bg-blue-600 text-white hover:bg-blue-700 shadow-sm inline-flex items-center justify-center rounded-xl font-medium transition-colors px-3 py-1.5 text-sm gap-1.5">
          Nouveau patient
        </button>
      </div>
    </div>

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
              </div>
              <div :class="['w-10 h-10 rounded-xl flex items-center justify-center', k.bg]">
                <component :is="k.icon" :class="['w-5 h-5', k.ic]" />
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
      <div v-motion :initial="{ opacity: 0, y: 18 }" :enter="{ opacity: 1, y: 0, transition: { delay: 360 } }" class="lg:col-span-2">
        <div class="rounded-2xl border border-border bg-card p-6 h-full flex flex-col">
          <div class="flex items-start justify-between mb-6 flex-wrap gap-3">
            <div>
              <h3 class="font-semibold text-foreground">Activité médicale</h3>
              <p class="text-xs text-muted-foreground mt-0.5">Consultations et ordonnances · {{ new Date().getFullYear() }}</p>
            </div>
            <div class="flex gap-4 text-xs text-muted-foreground">
              <span class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-blue-500 inline-block" />Consultations</span>
              <span class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-emerald-500 inline-block" />Ordonnances</span>
            </div>
          </div>

          <div v-if="loading" class="animate-pulse rounded-xl bg-muted h-52"></div>
          <div v-else class="relative h-[220px]">
            <svg viewBox="0 0 600 200" preserveAspectRatio="none" class="absolute inset-0 w-full h-[190px] overflow-visible">
              <line v-for="y in [0,45,90,135,180]" :key="y" x1="0" :y1="y" x2="600" :y2="y" class="stroke-border" stroke-width="1" />
              <path :d="chartAreaPath(CHART.consultations)" class="fill-blue-500/10" />
              <path :d="chartPath(CHART.consultations)" fill="none" class="stroke-blue-500" stroke-width="2.5" vector-effect="non-scaling-stroke" />
              <path :d="chartPath(CHART.prescriptions)" fill="none" class="stroke-emerald-500" stroke-width="2.5" vector-effect="non-scaling-stroke" />
              <circle v-for="(p, i) in CHART.consultations" :key="'c'+i" :cx="p.x" :cy="p.y" r="3" class="fill-blue-500" />
              <circle v-for="(p, i) in CHART.prescriptions" :key="'p'+i" :cx="p.x" :cy="p.y" r="3" class="fill-emerald-500" />
            </svg>
            <div class="absolute bottom-0 left-0 right-0 flex justify-between px-0 text-[11px] text-muted-foreground">
              <span v-for="m in CHART.months" :key="m">{{ m }}</span>
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
                <circle cx="50" cy="50" r="40" fill="transparent" class="stroke-muted" stroke-width="20" />
                <circle v-for="segment in dashboardStore.prescriptionSegments" :key="segment.name" cx="50" cy="50" r="40" fill="transparent" :stroke="segment.c" stroke-width="20" :stroke-dasharray="segment.dash" :stroke-dashoffset="segment.offset" />
              </svg>
              <span v-if="dashboardStore.prescriptionTotal === 0" class="absolute text-xs text-muted-foreground">Aucune</span>
            </div>
            <div class="space-y-2 mt-2">
              <div v-for="p in PIE" :key="p.name" class="flex items-center justify-between text-xs">
                <span class="flex items-center gap-2 text-muted-foreground"><span class="w-2 h-2 rounded-full shrink-0" :style="{ background: p.c }" />{{ p.name }}</span>
                <span class="font-semibold text-foreground font-mono">{{ p.v }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div v-motion :initial="{ opacity: 0, y: 18 }" :enter="{ opacity: 1, y: 0, transition: { delay: 500 } }">
        <div class="rounded-2xl border border-border bg-card p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="font-semibold text-foreground">Patients récents</h3>
            <button @click="openPatients" class="text-xs text-blue-600 hover:text-blue-700 font-medium flex items-center gap-1">Voir tous <ArrowRight class="w-3 h-3" /></button>
          </div>
          <div class="space-y-2">
            <template v-if="loading"><div v-for="i in 4" :key="i" class="animate-pulse rounded-xl bg-muted h-12"></div></template>
            <template v-else>
              <div v-for="(p, i) in PATIENTS" :key="p.id" v-motion :initial="{ opacity: 0, x: -8 }" :enter="{ opacity: 1, x: 0, transition: { delay: 540 + i * 60 } }" class="flex items-center gap-3 p-2 rounded-xl hover:bg-accent/60 transition-colors">
                <div :class="['rounded-full flex items-center justify-center font-semibold shrink-0 w-8 h-8 text-xs', avatarColor(p.firstName)]">{{ initials(p.firstName, p.lastName) }}</div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-foreground truncate">{{ p.firstName }} {{ p.lastName }}</p>
                  <p class="text-xs text-muted-foreground">Ajouté le {{ fmt(p.createdAt) }}</p>
                </div>
                <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-400 dark:border-emerald-800">
                  <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />Actif
                </span>
              </div>
              <p v-if="PATIENTS.length === 0" class="text-sm text-muted-foreground text-center py-4">Aucun patient récent.</p>
            </template>
          </div>
        </div>
      </div>

      <div v-motion :initial="{ opacity: 0, y: 18 }" :enter="{ opacity: 1, y: 0, transition: { delay: 550 } }">
        <div class="rounded-2xl border border-border bg-card p-6">
          <h3 class="font-semibold text-foreground mb-4">Activité récente</h3>
          <div class="space-y-4">
            <template v-if="loading"><div v-for="i in 5" :key="i" class="animate-pulse rounded-xl bg-muted h-9"></div></template>
            <template v-else>
              <div v-for="(item, i) in ACTIVITY" :key="item.id" v-motion :initial="{ opacity: 0, x: 8 }" :enter="{ opacity: 1, x: 0, transition: { delay: 580 + i * 60 } }" class="flex items-start gap-3">
                <div class="w-7 h-7 bg-blue-50 dark:bg-blue-900/30 rounded-lg flex items-center justify-center shrink-0 mt-0.5"><component :is="activityIcon(item.type)" class="w-3.5 h-3.5 text-blue-600" /></div>
                <div>
                  <p class="text-xs text-foreground font-medium leading-snug">{{ item.text }}</p>
                  <p class="text-xs text-muted-foreground mt-0.5">{{ formatActivityTime(item.time) }}</p>
                </div>
              </div>
              <p v-if="ACTIVITY.length === 0" class="text-sm text-muted-foreground text-center py-4">Aucune activité récente.</p>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>