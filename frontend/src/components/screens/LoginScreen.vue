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
  ChevronLeft,
  ClipboardList,
  Shield,
  Loader2,
  Check,
  AlertCircle,
  MapPin
} from 'lucide-vue-next'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { cn } from '../../lib/utils.js'

const { signIn } = useMedAppState()

// login state
const view = ref('login') // 'login' | 'register'
const role = ref('doctor')
const email = ref('dr.martin@medapp.fr')
const password = ref('')
const loading = ref(false)
const success = ref(false)
const errs = ref({})

// register state
const regDone = ref(false)
const reg = ref({ firstName: '', lastName: '', email: '', role: 'doctor', specialty: '', password: '', confirm: '' })
const updReg = (k, v) => { reg.value = { ...reg.value, [k]: v } }

const ROLES = [
  { v: 'doctor', label: 'Médecin', icon: Stethoscope, demo: 'dr.martin@medapp.fr', name: 'Dr. Martin' },
  { v: 'secretary', label: 'Secrétaire', icon: ClipboardList, demo: 'sec.dupont@medapp.fr', name: 'Marie Dupont' },
  { v: 'admin', label: 'Admin', icon: Shield, demo: 'admin@medapp.fr', name: 'Admin Principal' }
]

const REG_ROLES = [
  { v: 'doctor', label: 'Médecin', icon: Stethoscope },
  { v: 'secretary', label: 'Secrétaire', icon: ClipboardList },
  { v: 'admin', label: 'Administrateur', icon: Shield }
]

const validate = () => {
  const e = {}
  if (!email.value) e.email = 'Email requis'
  else if (!/\S+@\S+\.\S+/.test(email.value)) e.email = 'Email invalide'
  if (!password.value) e.password = 'Mot de passe requis'
  return e
}

const submit = (e) => {
  e.preventDefault()
  const e2 = validate()
  if (Object.keys(e2).length) { errs.value = e2; return }
  errs.value = {}
  loading.value = true
  setTimeout(() => {
    loading.value = false
    success.value = true
    const r = ROLES.find(r => r.v === role.value)
    setTimeout(() => { signIn({ name: r.name, role: r.v, email: r.demo }) }, 1200)
  }, 1400)
}

const submitRegister = (e) => {
  e.preventDefault()
  loading.value = true
  setTimeout(() => {
    loading.value = false
    regDone.value = true
  }, 1400)
}

const goToRegister = () => { view.value = 'register'; regDone.value = false; reg.value = { firstName: '', lastName: '', email: '', role: 'doctor', specialty: '', password: '', confirm: '' } }
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
          <div v-for="s in ['Sécurisé RGPD', 'Multi-rôles', '99.9% uptime']" :key="s" class="bg-white/10 backdrop-blur-sm rounded-xl px-3 py-2 border border-white/20">
            <span class="text-white text-xs font-medium">{{ s }}</span>
          </div>
        </div>
      </div>
      
      <p class="relative z-10 text-blue-200/70 text-xs">© 2026 MedApp · Tous les droits sont réservés</p>
    </div>

    <!-- Right panel -->
    <div class="flex-1 flex items-center justify-center p-8 overflow-y-auto">
      <div v-motion :initial="{ opacity: 0, y: 20 }" :enter="{ opacity: 1, y: 0 }" class="w-full max-w-sm">

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

          <div class="flex gap-1.5 p-1 bg-muted rounded-xl mb-6">
            <button
              v-for="r in ROLES"
              :key="r.v"
              type="button"
              @click="role = r.v; email = r.demo"
              :class="cn('flex-1 flex items-center justify-center gap-1.5 py-2 rounded-lg text-xs font-medium transition-all duration-200', role === r.v ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground')"
            >
              <component :is="r.icon" :class="cn('w-3.5 h-3.5', role === r.v ? 'text-blue-600' : '')" />
              {{ r.label }}
            </button>
          </div>

          <form @submit.prevent="submit" class="space-y-4">
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
                <input type="password" v-model="password" placeholder="••••••••" class="w-full h-10 bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none pl-9 pr-3" />
              </div>
              <p v-if="errs.password" class="text-xs text-red-500 flex items-center gap-1"><AlertCircle class="w-3 h-3" />{{ errs.password }}</p>
            </div>

            <div class="flex justify-end">
              <button type="button" class="text-xs text-blue-600 hover:text-blue-700 font-medium">Mot de passe oublié ?</button>
            </div>

            <button type="submit" :disabled="loading" class="w-full inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 disabled:cursor-not-allowed bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 px-5 py-2.5 text-base gap-2">
              <template v-if="loading"><Loader2 class="w-4 h-4 animate-spin" />Connexion…</template>
              <template v-else><ArrowRight class="w-4 h-4" />Se connecter</template>
            </button>
          </form>

          <p class="text-center text-sm text-muted-foreground mt-6">
            Pas encore de compte ?
            <button type="button" @click="goToRegister" class="text-blue-600 font-semibold hover:underline ml-1">Créer un compte</button>
          </p>

          <div class="mt-5 p-3 bg-blue-50 dark:bg-blue-950/30 rounded-xl border border-blue-100 dark:border-blue-900">
            <p class="text-xs font-semibold text-blue-700 dark:text-blue-300 mb-0.5">Mode démo</p>
            <p class="text-xs text-blue-600/70 dark:text-blue-400/70">Email auto-rempli · n'importe quel mot de passe</p>
          </div>
        </template>

        <!-- REGISTER FORM -->
        <template v-else-if="view === 'register'">
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
            <button type="button" @click="backToLogin" class="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground mb-6 transition-colors">
              <ChevronLeft class="w-4 h-4" /> Retour
            </button>
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

              <button type="submit" :disabled="loading" class="w-full inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 disabled:cursor-not-allowed bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 px-5 py-2.5 text-base gap-2 mt-2">
                <template v-if="loading"><Loader2 class="w-4 h-4 animate-spin" />Envoi en cours…</template>
                <template v-else><Check class="w-4 h-4" />Envoyer la demande</template>
              </button>
            </form>
          </template>
        </template>

      </div>
    </div>
  </div>
</template>