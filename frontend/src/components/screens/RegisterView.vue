<script setup>
import { ref, computed } from 'vue'
import {
  ClipboardList,
  Stethoscope,
  Shield,
  ChevronLeft,
  Mail,
  Lock,
  Loader2,
  Check,
  CheckCircle2,
  AlertCircle,
  Eye,
  EyeOff
} from 'lucide-vue-next'
import { cn } from '../../lib/utils.js'
import { useAuthStore } from '../../stores/authStore.js'

const emit = defineEmits(['backToLogin'])
const authStore = useAuthStore()

const loading = ref(false)
const regDone = ref(false)
const submitted = ref(false)
const serverError = ref('')
const showPassword = ref(false)
const showConfirm = ref(false)
const reg = ref({ firstName: '', lastName: '', email: '', role: 'medecin', password: '', confirm: '' })
const updReg = (k, v) => { reg.value = { ...reg.value, [k]: v } }

const REG_ROLES = [
  { v: 'medecin', label: 'Médecin', icon: Stethoscope },
  { v: 'secretaire', label: 'Secrétaire', icon: ClipboardList },
  { v: 'admin', label: 'Administrateur', icon: Shield }
]

// Password regex aligned exactly with backend: ^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$
const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/

const errors = computed(() => {
  const e = {}
  if (!reg.value.firstName.trim()) e.firstName = 'Le prénom est requis.'
  if (!reg.value.lastName.trim()) e.lastName = 'Le nom est requis.'
  if (!reg.value.email.trim()) {
    e.email = "L'email est requis."
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(reg.value.email)) {
    e.email = "L'adresse email n'est pas valide."
  }
  if (!reg.value.password) {
    e.password = 'Le mot de passe est requis.'
  } else if (!PASSWORD_REGEX.test(reg.value.password)) {
    e.password = 'Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre.'
  }
  if (!reg.value.confirm) {
    e.confirm = 'Veuillez confirmer le mot de passe.'
  } else if (reg.value.password !== reg.value.confirm) {
    e.confirm = 'Les mots de passe ne correspondent pas.'
  }
  return e
})

const isFormValid = computed(() => Object.keys(errors.value).length === 0)

const submitRegister = async (e) => {
  e.preventDefault()
  submitted.value = true
  if (!isFormValid.value) return

  loading.value = true
  serverError.value = ''

  try {
    await authStore.register({
      firstName: reg.value.firstName,
      lastName: reg.value.lastName,
      email: reg.value.email,
      password: reg.value.password,
      role: reg.value.role
    })
    regDone.value = true
  } catch (err) {
    const msg = err.response?.data?.message
    const status = err.response?.status
    if (status === 409) {
      serverError.value = msg || 'Cette adresse email est déjà utilisée.'
    } else if (status === 400) {
      serverError.value = msg || 'Les données saisies sont invalides.'
    } else {
      serverError.value = 'Une erreur est survenue. Veuillez réessayer.'
    }
  } finally {
    loading.value = false
  }
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

    <!-- Server error banner -->
    <div v-if="serverError" class="flex items-start gap-2 p-3 mb-2 rounded-xl bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-900">
      <AlertCircle class="w-4 h-4 text-red-500 mt-0.5 shrink-0" />
      <p class="text-xs text-red-600 dark:text-red-400">{{ serverError }}</p>
    </div>

    <form @submit.prevent="submitRegister" class="space-y-4">
      <div class="grid grid-cols-2 gap-3">
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-foreground">Prénom *</label>
          <div :class="cn('relative flex items-center rounded-xl border bg-background focus-within:ring-2 focus-within:ring-blue-500/20 transition-all', submitted && errors.firstName ? 'border-red-500 focus-within:border-red-500' : 'border-border focus-within:border-blue-500')">
            <input type="text" :value="reg.firstName" @input="updReg('firstName', $event.target.value)" placeholder="Jean" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none px-3" />
          </div>
          <p v-if="submitted && errors.firstName" class="text-xs text-red-500 mt-0.5">{{ errors.firstName }}</p>
        </div>
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-foreground">Nom *</label>
          <div :class="cn('relative flex items-center rounded-xl border bg-background focus-within:ring-2 focus-within:ring-blue-500/20 transition-all', submitted && errors.lastName ? 'border-red-500 focus-within:border-red-500' : 'border-border focus-within:border-blue-500')">
            <input type="text" :value="reg.lastName" @input="updReg('lastName', $event.target.value)" placeholder="Martin" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none px-3" />
          </div>
          <p v-if="submitted && errors.lastName" class="text-xs text-red-500 mt-0.5">{{ errors.lastName }}</p>
        </div>
      </div>

      <div class="space-y-1.5">
        <label class="text-sm font-medium text-foreground">Email professionnel *</label>
        <div :class="cn('relative flex items-center rounded-xl border bg-background focus-within:ring-2 focus-within:ring-blue-500/20 transition-all', submitted && errors.email ? 'border-red-500 focus-within:border-red-500' : 'border-border focus-within:border-blue-500')">
          <Mail class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
          <input type="email" :value="reg.email" @input="updReg('email', $event.target.value)" placeholder="jean.martin@clinique.fr" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-3" />
        </div>
        <p v-if="submitted && errors.email" class="text-xs text-red-500 mt-0.5">{{ errors.email }}</p>
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
        <label class="text-sm font-medium text-foreground">Mot de passe *</label>
        <div :class="cn('relative flex items-center rounded-xl border bg-background focus-within:ring-2 focus-within:ring-blue-500/20 transition-all', submitted && errors.password ? 'border-red-500 focus-within:border-red-500' : 'border-border focus-within:border-blue-500')">
          <Lock class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
          <input
            id="reg-password"
            :type="showPassword ? 'text' : 'password'"
            :value="reg.password"
            @input="updReg('password', $event.target.value)"
            placeholder="••••••••"
            class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-10"
          />
          <button
            id="reg-toggle-password"
            type="button"
            @click="showPassword = !showPassword"
            class="absolute right-3 text-muted-foreground hover:text-foreground transition-colors"
            :aria-label="showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'"
          >
            <EyeOff v-if="showPassword" class="w-4 h-4" />
            <Eye v-else class="w-4 h-4" />
          </button>
        </div>
        <p v-if="submitted && errors.password" class="text-xs text-red-500 mt-0.5">{{ errors.password }}</p>
      </div>

      <div class="space-y-1.5">
        <label class="text-sm font-medium text-foreground">Confirmer le mot de passe *</label>
        <div :class="cn('relative flex items-center rounded-xl border bg-background focus-within:ring-2 focus-within:ring-blue-500/20 transition-all', submitted && errors.confirm ? 'border-red-500 focus-within:border-red-500' : 'border-border focus-within:border-blue-500')">
          <Lock class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
          <input
            id="reg-confirm-password"
            :type="showConfirm ? 'text' : 'password'"
            :value="reg.confirm"
            @input="updReg('confirm', $event.target.value)"
            placeholder="••••••••"
            class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-10"
          />
          <button
            id="reg-toggle-confirm"
            type="button"
            @click="showConfirm = !showConfirm"
            class="absolute right-3 text-muted-foreground hover:text-foreground transition-colors"
            :aria-label="showConfirm ? 'Masquer la confirmation' : 'Afficher la confirmation'"
          >
            <EyeOff v-if="showConfirm" class="w-4 h-4" />
            <Eye v-else class="w-4 h-4" />
          </button>
        </div>
        <p v-if="submitted && errors.confirm" class="text-xs text-red-500 mt-0.5">{{ errors.confirm }}</p>
      </div>

      <div class="flex flex-col gap-3 mt-4 pt-2">
        <button type="submit" :disabled="loading || (submitted && !isFormValid)" class="w-full inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 disabled:cursor-not-allowed bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 px-5 py-2.5 text-base gap-2">
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
