<script setup>
import { ref } from 'vue'
import {
  Stethoscope,
  Heart,
  CheckCircle2,
  Users,
  Mail,
  Lock,
  ArrowRight,
  ClipboardList,
  Shield,
  Loader2,
  AlertCircle,
  Eye,
  EyeOff
} from 'lucide-vue-next'
import { cn } from '../../lib/utils.js'
import RegisterView from './RegisterView.vue'
import { useAuthStore } from '../../stores/authStore.js'

const authStore = useAuthStore()

// view state
const view = ref('login') // 'login' | 'register'
const role = ref('medecin')
const email = ref('')
const password = ref('')
const loading = ref(false)
const success = ref(false)
const errs = ref({})
const serverError = ref('')
const showPassword = ref(false)

const ROLES = [
  { v: 'medecin', label: 'Médecin', icon: Stethoscope },
  { v: 'secretaire', label: 'Secrétaire', icon: ClipboardList },
  { v: 'admin', label: 'Admin', icon: Shield }
]

const validate = () => {
  const e = {}
  if (!email.value) e.email = 'Email requis'
  else if (!/\S+@\S+\.\S+/.test(email.value)) e.email = 'Email invalide'
  if (!password.value) e.password = 'Mot de passe requis'
  return e
}

const submit = async (e) => {
  e.preventDefault()
  const e2 = validate()
  if (Object.keys(e2).length) { errs.value = e2; return }
  errs.value = {}
  serverError.value = ''
  loading.value = true

  try {
    await authStore.login(email.value, password.value)
    success.value = true
    // Navigation is handled inside authStore.login() via useMedAppState.signIn()
  } catch (err) {
    const msg = err.response?.data?.message
    const status = err.response?.status
    if (status === 401) {
      serverError.value = msg || 'Email ou mot de passe incorrect.'
    } else if (status === 403) {
      serverError.value = msg || 'Ce compte est désactivé. Contactez un administrateur.'
    } else {
      serverError.value = 'Une erreur est survenue. Veuillez réessayer.'
    }
  } finally {
    loading.value = false
  }
}

const goToRegister = () => { view.value = 'register' }
const backToLogin = () => { view.value = 'login' }
</script>

<template>
  <div class="min-h-screen flex bg-background w-full">
    <!-- Left panel -->
    <div class="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-blue-600 via-blue-700 to-indigo-800 relative overflow-hidden flex-col justify-between p-12">
      <div class="absolute inset-0 opacity-10">
        <div v-for="i in 5" :key="i" class="absolute rounded-full border border-white"
          :style="{ width: `${180 + (i-1) * 90}px`, height: `${180 + (i-1) * 90}px`, top: '50%', left: '50%', transform: 'translate(-50%,-50%)' }">
        </div>
      </div>
      <div class="relative z-10 flex items-center gap-3">
        <div class="w-10 h-10 bg-white/20 backdrop-blur-sm rounded-xl flex items-center justify-center">
          <Stethoscope class="w-5 h-5 text-white" />
        </div>
        <span class="text-white font-bold text-xl tracking-tight">MedApp</span>
      </div>
      
      <div class="relative z-10 space-y-8">
        <div class="flex justify-center">
          <div class="relative">
            <div class="w-48 h-48 bg-white/10 backdrop-blur-md rounded-3xl flex items-center justify-center border border-white/20 shadow-2xl">
              <div class="text-center space-y-4">
                <div class="w-16 h-16 bg-white/20 rounded-2xl mx-auto flex items-center justify-center">
                  <Heart class="w-8 h-8 text-white" />
                </div>
                <div class="space-y-1.5">
                  <div class="h-2 bg-white/40 rounded-full w-24 mx-auto"></div>
                  <div class="h-2 bg-white/25 rounded-full w-16 mx-auto"></div>
                  <div class="h-2 bg-white/15 rounded-full w-20 mx-auto"></div>
                </div>
              </div>
            </div>
            
            <div v-motion :initial="{ y: -10 }" :enter="{ y: 0, transition: { repeat: Infinity, repeatType: 'mirror', duration: 2800 } }" class="absolute -top-4 -right-6 bg-white rounded-2xl px-3 py-2 shadow-xl">
              <span class="text-xs font-semibold text-blue-700 flex items-center gap-1.5">
                <CheckCircle2 class="w-3.5 h-3.5 text-emerald-500" /> Version 1.0.0
              </span>
            </div>
            
            <div v-motion :initial="{ y: 10 }" :enter="{ y: 0, transition: { repeat: Infinity, repeatType: 'mirror', duration: 3200, delay: 500 } }" class="absolute -bottom-4 -left-6 bg-white rounded-2xl px-3 py-2 shadow-xl">
              <span class="text-xs font-semibold text-blue-700 flex items-center gap-1.5">
                <Users class="w-3.5 h-3.5 text-blue-600" /> Multi-profil
              </span>
            </div>
          </div>
        </div>
        
        <div class="text-white">
          <h1 class="text-3xl font-bold leading-tight">Gestion médicale<br />nouvelle génération</h1>
          <p class="text-blue-100/80 text-sm mt-3 leading-relaxed max-w-xs">
            Patients, ordonnances et rendez-vous centralisés dans une interface conçue pour les praticiens.
          </p>
        </div>
        
        <div class="flex gap-3">
          <div v-for="s in ['Sécurisé', 'Multi-rôles', '99.9% uptime']" :key="s" class="bg-white/10 backdrop-blur-sm rounded-xl px-3 py-2 border border-white/20">
            <span class="text-white text-xs font-medium">{{ s }}</span>
          </div>
        </div>
      </div>
      
      <p class="relative z-10 text-blue-200/70 text-xs">© 2026 MedApp · Tous les droits sont réservés</p>
    </div>

    <!-- Right panel -->
    <div class="flex-1 flex flex-col p-8 overflow-y-auto">
      <div v-motion :initial="{ opacity: 0, y: 20 }" :enter="{ opacity: 1, y: 0 }" class="w-full max-w-sm m-auto">

        <!-- Login: success state -->
        <template v-if="success">
          <div v-motion :initial="{ scale: 0.85, opacity: 0 }" :enter="{ scale: 1, opacity: 1 }" class="text-center space-y-4">
            <div v-motion :initial="{ scale: 0 }" :enter="{ scale: 1, transition: { type: 'spring', stiffness: 220, delay: 100 } }"
              class="w-20 h-20 bg-emerald-100 dark:bg-emerald-900/40 rounded-full flex items-center justify-center mx-auto"
            >
              <CheckCircle2 class="w-10 h-10 text-emerald-600" />
            </div>
            <h2 class="text-xl font-bold text-foreground">Connexion réussie</h2>
            <p class="text-muted-foreground text-sm">Redirection en cours…</p>
          </div>
        </template>

        <!-- LOGIN FORM -->
        <template v-else-if="view === 'login'">
          <div class="mb-8">
            <div class="flex items-center gap-2 mb-6 lg:hidden">
              <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
                <Stethoscope class="w-4 h-4 text-white" />
              </div>
              <span class="font-bold text-foreground">MedApp</span>
            </div>
            <h2 class="text-2xl font-bold text-foreground">Connexion</h2>
            <p class="text-muted-foreground text-sm mt-1">Accédez à votre espace professionnel</p>
          </div>

          <form @submit.prevent="submit" class="space-y-4">
            <!-- Server error banner -->
            <div v-if="serverError" class="flex items-start gap-2 p-3 rounded-xl bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-900">
              <AlertCircle class="w-4 h-4 text-red-500 mt-0.5 shrink-0" />
              <p class="text-xs text-red-600 dark:text-red-400">{{ serverError }}</p>
            </div>
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">Email professionnel</label>
              <div :class="cn('relative flex items-center rounded-xl border bg-background transition-all duration-200', errs.email ? 'border-red-400 ring-2 ring-red-400/20' : 'border-border focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20')">
                <Mail class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
                <input type="email" v-model="email" placeholder="votre@email.fr" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-3" />
              </div>
              <p v-if="errs.email" class="text-xs text-red-500 flex items-center gap-1"><AlertCircle class="w-3 h-3" />{{ errs.email }}</p>
            </div>

            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">Mot de passe</label>
              <div :class="cn('relative flex items-center rounded-xl border bg-background transition-all duration-200', errs.password ? 'border-red-400 ring-2 ring-red-400/20' : 'border-border focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/20')">
                <Lock class="absolute left-3 w-4 h-4 text-muted-foreground pointer-events-none" />
                <input
                  id="login-password"
                  :type="showPassword ? 'text' : 'password'"
                  v-model="password"
                  placeholder="••••••••"
                  class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-10"
                />
                <button
                  id="login-toggle-password"
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute right-3 text-muted-foreground hover:text-foreground transition-colors"
                  :aria-label="showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'"
                >
                  <EyeOff v-if="showPassword" class="w-4 h-4" />
                  <Eye v-else class="w-4 h-4" />
                </button>
              </div>
              <p v-if="errs.password" class="text-xs text-red-500 flex items-center gap-1"><AlertCircle class="w-3 h-3" />{{ errs.password }}</p>
            </div>

            <div class="flex justify-end">
              <button type="button" class="text-xs text-blue-600 hover:text-blue-700 font-medium">Mot de passe oublié ?</button>
            </div>

            <button id="login-submit" type="submit" :disabled="loading" class="w-full inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 disabled:cursor-not-allowed bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 px-5 py-2.5 text-base gap-2">
              <template v-if="loading"><Loader2 class="w-4 h-4 animate-spin" />Connexion…</template>
              <template v-else><ArrowRight class="w-4 h-4" />Se connecter</template>
            </button>
          </form>

          <p class="text-center text-sm text-muted-foreground mt-6">
            Pas encore de compte ?
            <button type="button" @click="goToRegister" class="text-blue-600 font-semibold hover:underline ml-1">Créer un compte</button>
          </p>
        </template>

        <!-- REGISTER FORM -->
        <template v-else-if="view === 'register'">
          <RegisterView @backToLogin="backToLogin" />
        </template>

      </div>
    </div>
  </div>
</template>