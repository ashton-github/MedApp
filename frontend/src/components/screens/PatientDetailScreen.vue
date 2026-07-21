<script setup>
import { ref } from 'vue'
import {
  ChevronRight,
  Pencil,
  Plus,
  Phone,
  Mail,
  Shield,
  Heart,
  Lock,
  Activity
} from 'lucide-vue-next'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { screens } from '../../constants/medapp.js'
import { cn } from '../../lib/utils.js'

const { authUser, showScreen } = useMedAppState()

const isDoc = authUser.value?.role === 'medecin'
const tab = ref('overview')

// Dummy patient data matching the React app
const p = { id: "p1", firstName: "Sophie", lastName: "Laurent", dob: "1985-03-15", gender: "F", phone: "+33 6 12 34 56 78", email: "sophie.laurent@email.fr", insurance: "CPAM Paris", lastVisit: "2026-07-08", status: "active", bloodType: "A+", allergies: ["Pénicilline"] }

const PRESCRIPTIONS = [
  { id: "rx1", patientId: "p1", date: "2026-07-08", doctorName: "Dr. Martin", status: "ACTIVE", medications: [{ name: "Amoxicilline 1g", dosage: "1 matin, 1 soir", duration: "7 jours" }] }
]

const rxs = PRESCRIPTIONS.filter(r => r.patientId === p.id)

const TABS = [
  { id: "overview", label: "Aperçu" },
  { id: "prescriptions", label: "Ordonnances" },
  { id: "history", label: "Historique" },
]

const fmt = (d) => new Date(d).toLocaleDateString("fr-FR", { day: "2-digit", month: "short", year: "numeric" })
const initials = (f, l) => `${f[0]}${l[0]}`.toUpperCase()

const AVATAR_COLORS = [
  "bg-blue-100 text-blue-700", "bg-emerald-100 text-emerald-700",
  "bg-violet-100 text-violet-700", "bg-amber-100 text-amber-700",
  "bg-rose-100 text-rose-700", "bg-cyan-100 text-cyan-700",
]
const avatarColor = (name) => AVATAR_COLORS[name.charCodeAt(0) % AVATAR_COLORS.length]
</script>

<template>
  <div class="p-6 space-y-6">
    <div class="flex items-center gap-2 text-sm text-muted-foreground">
      <button @click="showScreen(screens.patients)" class="hover:text-foreground transition-colors">Patients</button>
      <ChevronRight class="w-3 h-3" />
      <span class="text-foreground font-medium">{{ p.firstName }} {{ p.lastName }}</span>
    </div>

    <div class="rounded-2xl border border-border bg-card p-6">
      <div class="flex flex-col sm:flex-row items-start gap-6">
        <div :class="['w-20 h-20 rounded-full flex items-center justify-center text-2xl font-semibold shrink-0', avatarColor(p.firstName)]">
          {{ initials(p.firstName, p.lastName) }}
        </div>
        <div class="flex-1 min-w-0">
          <div class="flex items-start justify-between flex-wrap gap-3">
            <div>
              <h1 class="text-2xl font-bold text-foreground">{{ p.firstName }} {{ p.lastName }}</h1>
              <div class="flex items-center gap-3 mt-1 flex-wrap">
                <span class="text-sm text-muted-foreground">{{ p.gender === 'M' ? 'Homme' : 'Femme' }} · {{ fmt(p.dob) }}</span>
                <span :class="['inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border', p.status === 'active' ? 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-400 dark:border-emerald-800' : 'bg-gray-100 text-gray-500 border-gray-200 dark:bg-gray-800 dark:text-gray-400 dark:border-gray-700']">
                  <span v-if="p.status === 'active'" class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                  {{ p.status === 'active' ? 'Actif' : 'Inactif' }}
                </span>
              </div>
            </div>
            <div class="flex gap-2">
              <button class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
                <Pencil class="w-4 h-4" /> Modifier
              </button>
              <button v-if="isDoc" @click="showScreen(screens.ordonnanceForm)" class="bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
                <Plus class="w-4 h-4" /> Ordonnance
              </button>
            </div>
          </div>
          <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mt-5">
            <div v-for="it in [
              { label: 'Téléphone', val: p.phone, icon: Phone },
              { label: 'Email', val: p.email, icon: Mail },
              { label: 'Assurance', val: p.insurance, icon: Shield },
              { label: 'Groupe sanguin', val: isDoc ? p.bloodType : 'Restreint', icon: Heart, mask: !isDoc }
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

    <div class="flex border-b border-border">
      <button v-for="t in TABS" :key="t.id" @click="tab = t.id"
        :class="cn('px-4 py-3 text-sm font-medium transition-colors relative', tab === t.id ? 'text-blue-600' : 'text-muted-foreground hover:text-foreground')"
      >
        {{ t.label }}
        <div v-if="tab === t.id" v-motion layoutId="ptab" class="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-600" />
      </button>
    </div>

    <div class="pt-4">
      <transition mode="out-in" enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0 translate-y-2" enter-to-class="opacity-100 translate-y-0" leave-active-class="transition duration-100 ease-in" leave-from-class="opacity-100 translate-y-0" leave-to-class="opacity-0 -translate-y-2">
        <div :key="tab">
          <template v-if="tab === 'overview'">
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
              <div class="rounded-2xl border border-border bg-card p-5 col-span-2">
                <h3 class="font-semibold text-foreground mb-4">Informations médicales</h3>
                <div class="space-y-3">
                  <div v-for="it in [
                    { label: 'Groupe sanguin', val: isDoc ? p.bloodType : null },
                    { label: 'Allergies', val: isDoc ? (p.allergies.length ? p.allergies.join(', ') : 'Aucune allergie connue') : null },
                    { label: 'Dernière consultation', val: fmt(p.lastVisit) }
                  ]" :key="it.label" class="flex items-center justify-between py-2.5 border-b border-border last:border-0">
                    <span class="text-sm text-muted-foreground">{{ it.label }}</span>
                    <span v-if="it.val !== null" class="text-sm font-medium text-foreground text-right">{{ it.val }}</span>
                    <span v-else class="text-sm text-muted-foreground flex items-center gap-1.5"><Lock class="w-3 h-3" />Accès restreint</span>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <template v-else-if="tab === 'prescriptions'">
            <div class="space-y-3">
              <div v-if="rxs.length === 0" class="flex flex-col items-center py-12 text-center">
                <FileText class="w-10 h-10 text-muted-foreground mb-3" />
                <p class="font-medium text-foreground">Aucune ordonnance</p>
              </div>
              <div v-else v-for="rx in rxs" :key="rx.id" class="rounded-2xl border border-border bg-card p-4 hover:shadow-md transition-shadow">
                <div class="flex items-start justify-between">
                  <div class="flex-1">
                    <div class="flex items-center gap-2 mb-2">
                      <span :class="['inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border', rx.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-400 dark:border-emerald-800' : 'bg-gray-100 text-gray-500 border-gray-200 dark:bg-gray-800 dark:text-gray-400 dark:border-gray-700']">
                        {{ rx.status === 'ACTIVE' ? 'Active' : 'Archivée' }}
                      </span>
                      <span class="text-xs text-muted-foreground">{{ fmt(rx.date) }}</span>
                    </div>
                    <p v-for="m in rx.medications" :key="m.name" class="text-sm text-foreground">
                      <span class="font-medium">{{ m.name }}</span>
                      <span class="text-muted-foreground"> · {{ m.dosage }} · {{ m.duration }}</span>
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </template>

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
  </div>
</template>
