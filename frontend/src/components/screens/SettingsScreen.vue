<script setup>
import { ref, computed } from 'vue'
import {
  Pencil,
  Check,
  Loader2,
  Mail,
  Phone,
  Stethoscope,
  Shield,
  Lock,
  CheckCircle2
} from 'lucide-vue-next'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { cn } from '../../lib/utils.js'

const { authUser } = useMedAppState()

const editing = ref(false)
const saving = ref(false)
const saved = ref(false)
const toggles = ref([true, true, false, true])

const initials = computed(() => {
  if (!authUser.value?.name) return 'DR'
  return authUser.value.name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()
})

const pf = ref({
  firstName: authUser.value?.name?.split(' ').slice(1).join(' ') || authUser.value?.name || 'Martin',
  lastName: authUser.value?.name?.split(' ')[0] || 'Dr.',
  email: authUser.value?.email || 'dr.martin@medapp.fr',
  phone: '+33 6 12 34 56 78',
  specialty: authUser.value?.role === 'medecin' ? 'Médecine générale' : authUser.value?.role === 'secretaire' ? 'Secrétariat médical' : 'Administration',
  rpps: authUser.value?.role === 'medecin' ? '10004589231' : '',
})

const isDoctor = computed(() => authUser.value?.role === 'medecin')

const PREFS = [
  "Notifications email",
  "Rappels de rendez-vous",
  "Alertes d'expiration d'ordonnances",
  "Rapport hebdomadaire"
]

const togglePref = (i) => {
  toggles.value = toggles.value.map((v, idx) => idx === i ? !v : v)
}

const saveProfile = (e) => {
  e.preventDefault()
  saving.value = true
  setTimeout(() => {
    saving.value = false
    saved.value = true
    editing.value = false
    setTimeout(() => saved.value = false, 3000)
  }, 1200)
}
</script>

<template>
  <div class="p-6 space-y-6 max-w-2xl">
    <h1 class="text-2xl font-bold text-foreground">Paramètres</h1>

    <!-- Profile card -->
    <div class="rounded-2xl border border-border bg-card p-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="font-semibold text-foreground">Profil</h2>
        <button v-if="!editing" @click="editing = true" class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
          <Pencil class="w-4 h-4" /> Modifier
        </button>
      </div>

      <!-- View mode -->
      <transition mode="out-in" enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100" leave-active-class="transition duration-100 ease-in" leave-from-class="opacity-100" leave-to-class="opacity-0">
        <div v-if="!editing" key="view">
          <div class="flex items-center gap-4">
            <div class="w-16 h-16 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white text-xl font-bold shrink-0">
              {{ initials }}
            </div>
            <div class="flex-1">
              <p class="font-semibold text-foreground">{{ authUser?.name || 'Dr. Martin' }}</p>
              <p class="text-sm text-muted-foreground">{{ authUser?.email || 'dr.martin@medapp.fr' }}</p>
              <p class="text-xs text-muted-foreground mt-0.5">{{ pf.specialty }}</p>
            </div>
            <span v-if="saved" class="flex items-center gap-1.5 text-xs text-emerald-600 font-medium">
              <CheckCircle2 class="w-4 h-4" /> Enregistré
            </span>
          </div>
          <div class="grid grid-cols-2 gap-4 mt-5 text-sm">
            <div v-for="it in [
              { l: 'Téléphone', v: pf.phone, icon: Phone },
              { l: 'Spécialité', v: pf.specialty, icon: Stethoscope },
              ...(pf.rpps ? [{ l: 'N° RPPS', v: pf.rpps, icon: Shield }] : [])
            ]" :key="it.l">
              <p class="text-xs text-muted-foreground flex items-center gap-1 mb-0.5"><component :is="it.icon" class="w-3 h-3" />{{ it.l }}</p>
              <p class="font-medium text-foreground">{{ it.v }}</p>
            </div>
          </div>
        </div>

        <!-- Edit form -->
        <form v-else key="edit" @submit.prevent="saveProfile" class="space-y-4">
          <div class="flex items-center gap-4 mb-5">
            <div class="w-16 h-16 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white text-xl font-bold shrink-0">
              {{ initials }}
            </div>
            <div>
              <p class="text-sm font-medium text-foreground">Photo de profil</p>
              <button type="button" class="text-xs text-blue-600 hover:underline mt-0.5">Changer la photo</button>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">Prénom</label>
              <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
                <input v-model="pf.firstName" placeholder="Jean" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none px-3" />
              </div>
            </div>
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">Nom</label>
              <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
                <input v-model="pf.lastName" placeholder="Martin" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none px-3" />
              </div>
            </div>
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">Email professionnel</label>
            <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
              <Mail class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
              <input type="email" v-model="pf.email" placeholder="jean.martin@clinique.fr" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-3" />
            </div>
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">Téléphone</label>
            <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
              <Phone class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
              <input v-model="pf.phone" placeholder="+33 6 12 34 56 78" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-3" />
            </div>
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">{{ isDoctor ? 'Spécialité' : 'Poste' }}</label>
            <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
              <Stethoscope class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
              <input v-model="pf.specialty" placeholder="Médecine générale" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-3" />
            </div>
          </div>

          <div v-if="isDoctor" class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">N° RPPS</label>
            <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
              <Shield class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
              <input v-model="pf.rpps" placeholder="10004589231" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-3" />
            </div>
          </div>

          <!-- Password change section -->
          <div class="pt-1">
            <h3 class="text-sm font-medium text-foreground mb-3">Changer le mot de passe</h3>
            <div class="space-y-3">
              <div class="space-y-1.5">
                <label class="text-sm font-medium text-foreground">Mot de passe actuel</label>
                <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
                  <Lock class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
                  <input type="password" placeholder="••••••••" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-3" />
                </div>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div class="space-y-1.5">
                  <label class="text-sm font-medium text-foreground">Nouveau mot de passe</label>
                  <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
                    <Lock class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
                    <input type="password" placeholder="••••••••" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-3" />
                  </div>
                </div>
                <div class="space-y-1.5">
                  <label class="text-sm font-medium text-foreground">Confirmation</label>
                  <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
                    <Lock class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
                    <input type="password" placeholder="••••••••" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-3" />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="flex gap-3 pt-2">
            <button type="button" @click="editing = false" class="text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-4 py-2 text-sm gap-2">
              Annuler
            </button>
            <button type="submit" :disabled="saving" class="flex-1 bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 px-4 py-2 text-sm gap-2">
              <template v-if="saving"><Loader2 class="w-4 h-4 animate-spin" />Enregistrement…</template>
              <template v-else><Check class="w-4 h-4" />Enregistrer les modifications</template>
            </button>
          </div>
        </form>
      </transition>
    </div>

    <!-- Preferences -->
    <div class="rounded-2xl border border-border bg-card p-6">
      <h2 class="font-semibold text-foreground mb-4">Préférences de notifications</h2>
      <div class="space-y-1">
        <div v-for="(pref, i) in PREFS" :key="pref" class="flex items-center justify-between py-3 border-b border-border last:border-0">
          <span class="text-sm text-foreground">{{ pref }}</span>
          <button type="button" @click="togglePref(i)" :class="cn('w-10 h-6 rounded-full relative transition-colors duration-200', toggles[i] ? 'bg-blue-600' : 'bg-muted')">
            <div :class="cn('absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-transform duration-200', toggles[i] ? 'translate-x-5' : 'translate-x-1')" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
