<script setup>
import { ref, computed } from 'vue'
import {
  ChevronLeft,
  ChevronRight,
  Plus,
  Clock
} from 'lucide-vue-next'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { cn } from '../../lib/utils.js'

const { showScreen } = useMedAppState()

const weekOffset = ref(0)

const BASE = new Date(2026, 6, 13) // Monday 2026-07-13

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
  "Consultation": "bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-900/40 dark:text-blue-300 dark:border-blue-800",
  "Suivi":        "bg-violet-50 text-violet-700 border-violet-200 dark:bg-violet-900/40 dark:text-violet-300 dark:border-violet-800",
  "Urgence":      "bg-red-50 text-red-700 border-red-200 dark:bg-red-900/40 dark:text-red-300 dark:border-red-800",
  "Bilan":        "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-900/40 dark:text-amber-300 dark:border-amber-800",
}

const APPOINTMENTS = [
  { id: "a1", patientName: "Sophie Laurent",  time: "09:00", duration: 30, type: "Consultation", day: "2026-07-13" },
  { id: "a2", patientName: "Marc Dubois",     time: "10:00", duration: 45, type: "Suivi",        day: "2026-07-13" },
  { id: "a3", patientName: "Amira Benali",    time: "11:30", duration: 30, type: "Urgence",      day: "2026-07-14" },
  { id: "a4", patientName: "Claire Fontaine", time: "14:00", duration: 30, type: "Consultation", day: "2026-07-14" },
  { id: "a5", patientName: "Lucas Petit",     time: "15:00", duration: 30, type: "Suivi",        day: "2026-07-15" },
  { id: "a6", patientName: "Théo Moreau",     time: "09:30", duration: 45, type: "Bilan",        day: "2026-07-16" },
  { id: "a7", patientName: "Sophie Laurent",  time: "16:00", duration: 30, type: "Consultation", day: "2026-07-17" },
]

const toISO = (d) => d.toISOString().split("T")[0]
const getAppts = (d) => APPOINTMENTS.filter(a => a.day === toISO(d))

const totalThisWeek = computed(() => DAYS.value.reduce((acc, d) => acc + getAppts(d).length, 0))

const fmtWeekRange = computed(() => {
  const d0 = DAYS.value[0]
  const d4 = DAYS.value[4]
  return `Semaine du ${d0.toLocaleDateString("fr-FR", { day: "numeric", month: "long" })} au ${d4.toLocaleDateString("fr-FR", { day: "numeric", month: "long", year: "numeric" })}`
})
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
        <div class="flex border border-border rounded-xl overflow-hidden">
          <button @click="weekOffset--" class="p-2.5 hover:bg-accent text-muted-foreground hover:text-foreground transition-colors">
            <ChevronLeft class="w-4 h-4" />
          </button>
          <button @click="weekOffset = 0" class="px-3 text-xs font-medium text-foreground hover:bg-accent transition-colors border-x border-border">
            Aujourd'hui
          </button>
          <button @click="weekOffset++" class="p-2.5 hover:bg-accent text-muted-foreground hover:text-foreground transition-colors">
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>
        <button class="bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
          <Plus class="w-4 h-4" /> Nouveau RDV
        </button>
      </div>
    </div>

    <!-- Week grid -->
    <div class="rounded-2xl border border-border bg-card p-4 overflow-hidden">
      <div class="grid grid-cols-5 gap-3">
        <div v-for="(d, i) in DAYS" :key="i" class="space-y-2">
          <!-- Day header -->
          <div :class="cn('text-center pb-3 border-b-2 transition-colors', toISO(d) === '2026-07-13' ? 'border-blue-500' : 'border-border')">
            <p class="text-xs text-muted-foreground font-medium uppercase tracking-wide">{{ DAY_NAMES[i] }}</p>
            <div :class="cn('w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold mx-auto mt-1.5', toISO(d) === '2026-07-13' ? 'bg-blue-600 text-white' : 'text-foreground')">
              {{ d.getDate() }}
            </div>
            <p class="text-xs text-muted-foreground mt-1">{{ getAppts(d).length }} RDV</p>
          </div>

          <!-- Appointments -->
          <div class="space-y-2 min-h-40">
            <template v-if="getAppts(d).length === 0">
              <div class="flex items-center justify-center h-28 rounded-xl border-2 border-dashed border-border mt-1">
                <p class="text-xs text-muted-foreground">Libre</p>
              </div>
            </template>
            <template v-else>
              <div
                v-for="(a, ai) in getAppts(d)"
                :key="a.id"
                v-motion
                :initial="{ opacity: 0, y: 6 }"
                :enter="{ opacity: 1, y: 0, transition: { delay: ai * 60 } }"
                :class="cn('p-2.5 rounded-xl border cursor-pointer transition-all duration-200 hover:shadow-md hover:scale-[1.02]', TYPE_CLS[a.type] || 'bg-muted border-border')"
              >
                <p class="text-xs font-semibold leading-tight truncate">{{ a.patientName.split(' ')[0] }}</p>
                <p class="text-xs font-semibold leading-tight truncate opacity-70">{{ a.patientName.split(' ')[1] }}</p>
                <div class="flex items-center gap-1 mt-1.5">
                  <Clock class="w-2.5 h-2.5 opacity-60" />
                  <p class="text-xs opacity-75 font-mono">{{ a.time }}</p>
                </div>
                <span class="text-xs font-medium block mt-1 opacity-80">{{ a.type }}</span>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- Upcoming list -->
    <div class="rounded-2xl border border-border bg-card p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="font-semibold text-foreground">Liste des rendez-vous</h3>
        <div class="flex gap-1 text-xs">
          <span v-for="(cls, type) in TYPE_CLS" :key="type" :class="cn('px-2 py-0.5 rounded-full border font-medium', cls)">{{ type }}</span>
        </div>
      </div>
      <div class="space-y-2">
        <div
          v-for="(a, i) in APPOINTMENTS"
          :key="a.id"
          v-motion
          :initial="{ opacity: 0, x: -8 }"
          :enter="{ opacity: 1, x: 0, transition: { delay: i * 50 } }"
          :class="cn('flex items-center gap-3 p-3 rounded-xl hover:bg-accent/50 transition-colors cursor-pointer', a.day < '2026-07-13' ? 'opacity-50' : '')"
          @click="showScreen('patientDetail')"
        >
          <div class="w-8 h-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold shrink-0">
            {{ a.patientName.split(' ')[0][0] }}{{ a.patientName.split(' ')[1][0] }}
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-foreground">{{ a.patientName }}</p>
            <p class="text-xs text-muted-foreground">
              {{ new Date(a.day).toLocaleDateString("fr-FR", { weekday: "short", day: "numeric", month: "short" }) }} à {{ a.time }}
            </p>
          </div>
          <div class="flex items-center gap-2 shrink-0">
            <span :class="cn('text-xs px-2 py-0.5 rounded-full border font-medium', TYPE_CLS[a.type] || 'bg-muted border-border')">{{ a.type }}</span>
            <span class="text-xs text-muted-foreground font-mono">{{ a.duration }}min</span>
            <span v-if="a.day < '2026-07-13'" class="text-xs text-muted-foreground">Passé</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
