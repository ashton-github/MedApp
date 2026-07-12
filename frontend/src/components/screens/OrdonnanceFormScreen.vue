<script setup>
import { ref, computed } from 'vue'
import {
  ChevronLeft,
  CheckCircle2,
  Check,
  Search,
  Plus,
  Trash2,
  Loader2,
  Calendar,
  AlertCircle,
  Eye,
  FileCheck
} from 'lucide-vue-next'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { screens } from '../../constants/medapp.js'
import { cn } from '../../lib/utils.js'

const { showScreen } = useMedAppState()

const pq = ref('')
const sel = ref(null)
const showSug = ref(false)
const meds = ref([{ name: '', dosage: '', duration: '' }])
const notes = ref('')
const submitting = ref(false)
const done = ref(false)

const PATIENTS = [
  { id: "p1", firstName: "Sophie", lastName: "Laurent" },
  { id: "p2", firstName: "Marc", lastName: "Dubois" },
  { id: "p3", firstName: "Amira", lastName: "Benali" },
  { id: "p4", firstName: "Théo", lastName: "Moreau" },
  { id: "p5", firstName: "Claire", lastName: "Fontaine" },
  { id: "p6", firstName: "Lucas", lastName: "Petit" },
]

const sug = computed(() => {
  if (pq.value.length < 2) return []
  return PATIENTS.filter(p => `${p.firstName} ${p.lastName}`.toLowerCase().includes(pq.value.toLowerCase()))
})

const addMed = () => meds.value.push({ name: '', dosage: '', duration: '' })
const rmMed = (i) => meds.value.splice(i, 1)

const submit = () => {
  submitting.value = true
  setTimeout(() => {
    submitting.value = false
    done.value = true
    setTimeout(() => {
      showScreen(screens.ordonnances)
    }, 1400)
  }, 1400)
}

const AVATAR_COLORS = [
  "bg-blue-100 text-blue-700", "bg-emerald-100 text-emerald-700",
  "bg-violet-100 text-violet-700", "bg-amber-100 text-amber-700",
  "bg-rose-100 text-rose-700", "bg-cyan-100 text-cyan-700",
]
const avatarColor = (name) => AVATAR_COLORS[name.charCodeAt(0) % AVATAR_COLORS.length]
const initials = (f, l) => `${f[0]}${l[0]}`.toUpperCase()
</script>

<template>
  <div v-if="done" class="flex flex-col items-center justify-center min-h-96 p-6">
    <div v-motion :initial="{ scale: 0 }" :enter="{ scale: 1, transition: { type: 'spring', stiffness: 220 } }"
      class="w-20 h-20 bg-emerald-100 dark:bg-emerald-900/40 rounded-full flex items-center justify-center mb-4"
    >
      <CheckCircle2 class="w-10 h-10 text-emerald-600" />
    </div>
    <h2 class="text-xl font-bold text-foreground">Ordonnance créée !</h2>
    <p class="text-muted-foreground text-sm mt-1">Redirection en cours…</p>
  </div>

  <div v-else class="p-6 space-y-6">
    <div>
      <div class="flex items-center gap-2 text-sm text-muted-foreground mb-4">
        <button @click="showScreen(screens.ordonnances)" class="hover:text-foreground transition-colors">Ordonnances</button>
        <ChevronRight class="w-3 h-3" />
        <span class="text-foreground">Nouvelle ordonnance</span>
      </div>
      <h1 class="text-2xl font-bold text-foreground">Nouvelle ordonnance</h1>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 space-y-6">
        <div class="rounded-2xl border border-border bg-card p-5">
          <h2 class="font-semibold text-foreground mb-4">Patient</h2>
          <template v-if="!sel">
            <div class="relative">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <input v-model="pq" @focus="showSug = true" @blur="setTimeout(() => showSug = false, 200)" placeholder="Rechercher un patient…" class="w-full h-10 pl-9 pr-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground" />
              <div v-if="showSug && sug.length > 0" class="absolute left-0 right-0 top-full mt-2 bg-card border border-border rounded-xl shadow-lg z-10 py-1 max-h-60 overflow-y-auto">
                <button v-for="p in sug" :key="p.id" @click="sel = p; pq = ''; showSug = false" class="w-full text-left px-4 py-2 text-sm hover:bg-accent flex items-center gap-2">
                  <div :class="['w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-medium shrink-0', avatarColor(p.firstName)]">
                    {{ initials(p.firstName, p.lastName) }}
                  </div>
                  <span class="text-foreground">{{ p.firstName }} {{ p.lastName }}</span>
                </button>
              </div>
              <div v-else-if="showSug && pq.length > 1" class="absolute left-0 right-0 top-full mt-2 bg-card border border-border rounded-xl shadow-lg z-10 py-3 text-center text-sm text-muted-foreground">
                Aucun patient trouvé
              </div>
            </div>
          </template>
          <template v-else>
            <div class="flex items-center justify-between p-3 border border-blue-200 dark:border-blue-800 bg-blue-50/50 dark:bg-blue-900/20 rounded-xl">
              <div class="flex items-center gap-3">
                <div :class="['w-10 h-10 rounded-full flex items-center justify-center text-sm font-semibold shrink-0', avatarColor(sel.firstName)]">
                  {{ initials(sel.firstName, sel.lastName) }}
                </div>
                <div>
                  <p class="font-medium text-foreground text-sm">{{ sel.firstName }} {{ sel.lastName }}</p>
                  <p class="text-xs text-muted-foreground">Patient sélectionné</p>
                </div>
              </div>
              <button @click="sel = null" class="p-1.5 rounded-lg hover:bg-white dark:hover:bg-background text-muted-foreground transition-colors"><Trash2 class="w-4 h-4 text-red-500" /></button>
            </div>
            <div class="mt-4 p-3 bg-amber-50 dark:bg-amber-950/30 border border-amber-100 dark:border-amber-900 rounded-xl flex items-start gap-2 text-xs text-amber-800 dark:text-amber-400">
              <AlertCircle class="w-4 h-4 shrink-0" />
              <span>Vérifiez les allergies connues (ex: Pénicilline) avant de prescrire.</span>
            </div>
          </template>
        </div>

        <div class="rounded-2xl border border-border bg-card p-5">
          <h2 class="font-semibold text-foreground mb-4">Paramètres</h2>
          <div class="space-y-4">
            <div class="space-y-1.5">
              <label class="text-xs font-medium text-foreground">Date de prescription</label>
              <div class="relative">
                <Calendar class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <input type="date" :value="new Date().toISOString().split('T')[0]" class="w-full h-9 pl-9 pr-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground" />
              </div>
            </div>
            <div class="space-y-1.5">
              <label class="text-xs font-medium text-foreground">Validité (mois)</label>
              <select class="w-full h-9 px-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground">
                <option value="1">1 mois</option>
                <option value="3">3 mois</option>
                <option value="6">6 mois</option>
              </select>
            </div>
          </div>
        </div>

        <div v-for="(m, i) in meds" :key="i" v-motion :initial="{ opacity: 0, y: 10 }" :enter="{ opacity: 1, y: 0 }" class="rounded-2xl border border-border bg-card p-5 relative group">
          <div class="flex items-center justify-between mb-4">
            <h3 class="font-medium text-sm text-foreground flex items-center gap-2">Médicament {{ i + 1 }}</h3>
            <button v-if="meds.length > 1" @click="rmMed(i)" class="text-muted-foreground hover:text-red-500 transition-colors"><Trash2 class="w-4 h-4" /></button>
          </div>
          <div class="space-y-4">
            <div class="space-y-1.5">
              <label class="text-xs font-medium text-foreground">Nom et format *</label>
              <input v-model="m.name" placeholder="ex: Doliprane 1000mg comprimés" class="w-full h-10 px-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground" />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-xs font-medium text-foreground">Posologie *</label>
                <input v-model="m.dosage" placeholder="ex: 1 matin et soir" class="w-full h-10 px-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground" />
              </div>
              <div class="space-y-1.5">
                <label class="text-xs font-medium text-foreground">Durée *</label>
                <input v-model="m.duration" placeholder="ex: 5 jours" class="w-full h-10 px-3 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground" />
              </div>
            </div>
          </div>
        </div>

        <button @click="addMed" class="w-full border-2 border-dashed border-border text-muted-foreground hover:border-blue-400 hover:text-blue-600 hover:bg-blue-50/50 dark:hover:bg-blue-900/10 transition-all duration-200 rounded-2xl p-4 flex items-center justify-center gap-2 font-medium text-sm">
          <Plus class="w-4 h-4" /> Ajouter un médicament
        </button>

        <div class="rounded-2xl border border-border bg-card p-5">
          <h2 class="font-semibold text-foreground mb-4">Instructions supplémentaires (optionnel)</h2>
          <textarea v-model="notes" placeholder="Recommandations hygiéno-diététiques, précautions particulières…" rows="3" class="w-full px-3 py-2.5 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 transition-all text-foreground placeholder:text-muted-foreground resize-none" />
        </div>
        <div class="flex gap-3">
          <button @click="showScreen(screens.ordonnances)" class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-4 py-2 text-sm gap-2">
            Annuler
          </button>
          <button @click="submit" :disabled="!sel || meds[0].name === '' || submitting" class="flex-1 bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 px-4 py-2 text-sm gap-2">
            <template v-if="submitting">
              <Loader2 class="w-4 h-4 animate-spin" /> Création…
            </template>
            <template v-else>
              <FileCheck class="w-4 h-4" /> Créer l'ordonnance
            </template>
          </button>
        </div>
      </div>

      <div class="space-y-4">
        <div class="rounded-2xl border border-border bg-card p-5">
          <h3 class="font-semibold text-foreground mb-4">Aperçu</h3>
          <div class="space-y-3 text-sm">
            <div>
              <p class="text-xs text-muted-foreground">Patient</p>
              <p class="font-medium text-foreground mt-0.5">{{ sel ? `${sel.firstName} ${sel.lastName}` : "—" }}</p>
            </div>
            <div>
              <p class="text-xs text-muted-foreground">Date</p>
              <p class="font-medium text-foreground mt-0.5 font-mono">{{ new Date().toLocaleDateString("fr-FR") }}</p>
            </div>
            <div>
              <p class="text-xs text-muted-foreground mb-1.5">Médicaments ({{ meds.filter(m => m.name).length }})</p>
              <div class="space-y-1">
                <div v-for="(m, i) in meds.filter(m => m.name)" :key="i" class="p-2 bg-muted rounded-xl">
                  <p class="text-xs font-semibold text-foreground">{{ m.name }}</p>
                  <p v-if="m.dosage || m.duration" class="text-xs text-muted-foreground">{{ m.dosage }}{{ m.dosage && m.duration ? ' · ' : '' }}{{ m.duration }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <button @click="showScreen(screens.pdfPreview)" class="w-full border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-2 text-sm gap-2">
          <Eye class="w-4 h-4" /> Aperçu PDF
        </button>
      </div>
    </div>
  </div>
</template>