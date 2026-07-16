<script setup>
import { ref } from 'vue'
import {
  Stethoscope,
  ClipboardList,
  Shield,
  ChevronLeft,
  Mail,
  MapPin,
  Lock,
  Loader2,
  Check,
  CheckCircle2
} from 'lucide-vue-next'
import { cn } from '../../lib/utils.js'

const emit = defineEmits(['backToLogin'])

const loading = ref(false)
const regDone = ref(false)
const reg = ref({ firstName: '', lastName: '', email: '', role: 'doctor', specialty: '', password: '', confirm: '' })
const updReg = (k, v) => { reg.value = { ...reg.value, [k]: v } }

const REG_ROLES = [
  { v: 'doctor', label: 'Médecin', icon: Stethoscope },
  { v: 'secretary', label: 'Secrétaire', icon: ClipboardList },
  { v: 'admin', label: 'Administrateur', icon: Shield }
]

const submitRegister = (e) => {
  e.preventDefault()
  loading.value = true
  setTimeout(() => {
    loading.value = false
    regDone.value = true
  }, 1400)
}

const backToLogin = () => {
  emit('backToLogin')
}
</script>

<template>
  <!-- Register success state -->
  <div v-if="regDone" class="text-center space-y-4">
    <div v-motion :initial="{ scale: 0 }" :enter="{ scale: 1, transition: { type: 'spring', stiffness: 220 } }"
      class="w-20 h-20 bg-emerald-100 dark:bg-emerald-900/40 rounded-full flex items-center justify-center mx-auto"
    >
      <CheckCircle2 class="w-10 h-10 text-emerald-600" />
    </div>
    <h2 class="text-xl font-bold text-foreground">Demande envoyée !</h2>
    <p class="text-muted-foreground text-sm">Un administrateur validera votre compte sous 24h.</p>
    <button type="button" @click="backToLogin" class="mx-auto border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors px-4 py-2 text-sm gap-2">
      Retour à la connexion
    </button>
  </div>

  <!-- Register form -->
  <template v-else>
    <div class="mb-6">
      <button type="button" @click="backToLogin" class="inline-flex items-center gap-1.5 text-sm text-foreground bg-muted/60 hover:bg-muted px-3 py-1.5 rounded-lg font-medium transition-colors border border-border/50">
        <ChevronLeft class="w-4 h-4" /> Retour
      </button>
    </div>
    <h2 class="text-2xl font-bold text-foreground mb-1">Créer un compte</h2>
    <p class="text-muted-foreground text-sm mb-6">Demandez un accès professionnel MedApp</p>

    <form @submit.prevent="submitRegister" class="space-y-4">
      <div class="grid grid-cols-2 gap-3">
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-foreground">Prénom *</label>
          <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
            <input type="text" :value="reg.firstName" @input="updReg('firstName', $event.target.value)" placeholder="Jean" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none px-3" />
          </div>
        </div>
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-foreground">Nom *</label>
          <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
            <input type="text" :value="reg.lastName" @input="updReg('lastName', $event.target.value)" placeholder="Martin" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none px-3" />
          </div>
        </div>
      </div>

      <div class="space-y-1.5">
        <label class="text-sm font-medium text-foreground">Email professionnel *</label>
        <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
          <Mail class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
          <input type="email" :value="reg.email" @input="updReg('email', $event.target.value)" placeholder="jean.martin@clinique.fr" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-3" />
        </div>
      </div>

      <div class="space-y-1.5">
        <label class="text-sm font-medium text-foreground">Rôle *</label>
        <div class="flex gap-1.5 p-1 bg-muted rounded-xl">
          <button v-for="r in REG_ROLES" :key="r.v" type="button" @click="updReg('role', r.v)"
            :class="cn('flex-1 flex items-center justify-center gap-1.5 py-2 rounded-lg text-xs font-medium transition-all', reg.role === r.v ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground')"
          >
            <component :is="r.icon" :class="cn('w-3.5 h-3.5', reg.role === r.v ? 'text-blue-600' : '')" />
            {{ r.label }}
          </button>
        </div>
      </div>

      <div class="space-y-1.5">
        <label class="text-sm font-medium text-foreground">{{ reg.role === 'doctor' ? 'Spécialité' : 'Établissement' }}</label>
        <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
          <Stethoscope v-if="reg.role === 'doctor'" class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
          <MapPin v-else class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
          <input type="text" :value="reg.specialty" @input="updReg('specialty', $event.target.value)" :placeholder="reg.role === 'doctor' ? 'Médecine générale' : 'Clinique Saint-Louis'" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-3" />
        </div>
      </div>

      <div class="space-y-1.5">
        <label class="text-sm font-medium text-foreground">Mot de passe *</label>
        <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
          <Lock class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
          <input type="password" :value="reg.password" @input="updReg('password', $event.target.value)" placeholder="••••••••" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-3" />
        </div>
      </div>

      <div class="space-y-1.5">
        <label class="text-sm font-medium text-foreground">Confirmer le mot de passe *</label>
        <div class="relative flex items-center rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20 transition-all">
          <Lock class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
          <input type="password" :value="reg.confirm" @input="updReg('confirm', $event.target.value)" placeholder="••••••••" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-3" />
        </div>
      </div>

      <div class="flex flex-col gap-3 mt-4 pt-2">
        <button type="submit" :disabled="loading" class="w-full inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 disabled:cursor-not-allowed bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 px-5 py-2.5 text-base gap-2">
          <template v-if="loading"><Loader2 class="w-4 h-4 animate-spin" />Envoi en cours…</template>
          <template v-else><Check class="w-4 h-4" />Envoyer la demande</template>
        </button>
        <button type="button" @click="backToLogin" class="w-full inline-flex items-center justify-center rounded-xl font-medium transition-colors border border-border bg-transparent text-foreground hover:bg-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-5 py-2.5 text-base gap-2">
          Annuler
        </button>
      </div>
    </form>
  </template>
</template>
