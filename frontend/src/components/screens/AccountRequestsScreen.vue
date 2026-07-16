<script setup>
import { ref, computed } from 'vue'
import {
  UserPlus,
  Clock,
  CheckCircle2,
  X,
  Mail,
  Stethoscope,
  ClipboardList,
  Shield,
  AlertCircle,
  Check,
  Loader2
} from 'lucide-vue-next'
import { cn } from '../../lib/utils.js'

const INIT_REQUESTS = [
  { id: "req1", firstName: "Isabelle", lastName: "Dupont",   email: "i.dupont@clinique-paris.fr",    role: "doctor",    specialty: "Cardiologie",           requestedAt: "2026-07-12", status: "pending" },
  { id: "req2", firstName: "Paul",     lastName: "Bernard",  email: "p.bernard@hopital-lyon.fr",     role: "doctor",    specialty: "Médecine générale",      requestedAt: "2026-07-11", status: "pending" },
  { id: "req3", firstName: "Léa",      lastName: "Simon",    email: "l.simon@cabinet-renard.fr",     role: "secretary", specialty: "Cabinet Dr. Renard",     requestedAt: "2026-07-10", status: "pending" },
  { id: "req4", firstName: "Antoine",  lastName: "Leroy",    email: "a.leroy@chu-bordeaux.fr",       role: "doctor",    specialty: "Pédiatrie",              requestedAt: "2026-07-08", status: "approved" },
  { id: "req5", firstName: "Camille",  lastName: "Roux",     email: "c.roux@clinique-nord.fr",       role: "secretary", specialty: "Clinique du Parc",       requestedAt: "2026-07-07", status: "rejected", comment: "Informations incomplètes" },
  { id: "req6", firstName: "Hugo",     lastName: "Marchand", email: "h.marchand@medecine-douce.fr",  role: "doctor",    specialty: "Médecine du sport",      requestedAt: "2026-07-06", status: "approved" },
  { id: "req7", firstName: "Nadia",    lastName: "Ferreira", email: "n.ferreira@hopital-est.fr",     role: "secretary", specialty: "Service de cardiologie", requestedAt: "2026-07-05", status: "pending" },
]

const requests = ref(INIT_REQUESTS)
const filter = ref("all")
const rejectTarget = ref(null)
const rejectComment = ref("")
const approveTarget = ref(null)
const processing = ref(null)
const toastMsg = ref(null)
const toastType = ref(null)

const ROLE_CLS = {
  doctor:    "bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-900/40 dark:text-blue-300 dark:border-blue-800",
  secretary: "bg-violet-50 text-violet-700 border-violet-200 dark:bg-violet-900/40 dark:text-violet-300 dark:border-violet-800",
  admin:     "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-900/40 dark:text-amber-300 dark:border-amber-800",
}
const ROLE_LABEL = { doctor: "Médecin", secretary: "Secrétaire", admin: "Administrateur" }
const ROLE_ICON = { doctor: Stethoscope, secretary: ClipboardList, admin: Shield }

const statusConfig = {
  pending:  { label: "En attente", cls: "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-900/30 dark:text-amber-400 dark:border-amber-800", dot: "bg-amber-500" },
  approved: { label: "Approuvé",   cls: "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-900/30 dark:text-emerald-400 dark:border-emerald-800", dot: "bg-emerald-500" },
  rejected: { label: "Refusé",     cls: "bg-red-50 text-red-600 border-red-200 dark:bg-red-900/30 dark:text-red-400 dark:border-red-800", dot: "bg-red-500" },
}

const AVATAR_COLORS = [
  "bg-blue-100 text-blue-700", "bg-emerald-100 text-emerald-700",
  "bg-violet-100 text-violet-700", "bg-amber-100 text-amber-700",
  "bg-rose-100 text-rose-700", "bg-cyan-100 text-cyan-700",
]
const avatarColor = (n) => AVATAR_COLORS[n.charCodeAt(0) % AVATAR_COLORS.length]
const initials = (f, l) => `${f[0]}${l[0]}`.toUpperCase()

const fmt = (d) => new Date(d).toLocaleDateString("fr-FR", { day: "2-digit", month: "short", year: "numeric" })

const pendingCount = computed(() => requests.value.filter(r => r.status === "pending").length)
const approvedCount = computed(() => requests.value.filter(r => r.status === "approved").length)
const rejectedCount = computed(() => requests.value.filter(r => r.status === "rejected").length)

const FILTERS = computed(() => [
  { v: "all",      label: "Toutes",    count: requests.value.length },
  { v: "pending",  label: "En attente",count: pendingCount.value },
  { v: "approved", label: "Approuvées",count: approvedCount.value },
  { v: "rejected", label: "Refusées",  count: rejectedCount.value },
])

const filtered = computed(() => requests.value.filter(r => filter.value === "all" || r.status === filter.value))

const showToast = (msg, type) => {
  toastMsg.value = msg
  toastType.value = type
  setTimeout(() => toastMsg.value = null, 4000)
}

const doApprove = (id) => {
  processing.value = id
  setTimeout(() => {
    requests.value = requests.value.map(r => r.id === id ? { ...r, status: "approved" } : r)
    approveTarget.value = null
    processing.value = null
    showToast("Compte approuvé — un email de confirmation a été envoyé.", "success")
  }, 900)
}

const doReject = (id) => {
  processing.value = id
  setTimeout(() => {
    requests.value = requests.value.map(r => r.id === id ? { ...r, status: "rejected", comment: rejectComment.value || undefined } : r)
    rejectTarget.value = null
    rejectComment.value = ""
    processing.value = null
    showToast("Demande refusée — le candidat a été notifié.", "error")
  }, 900)
}

</script>

<template>
  <div class="p-6 space-y-6">
    <!-- Toast local (since there is no global toaster easily accessible without vue-sonner) -->
    <div v-if="toastMsg" class="fixed top-4 right-4 z-50 p-4 rounded-xl shadow-lg border text-sm font-medium transition-all"
      :class="toastType === 'success' ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-red-50 border-red-200 text-red-800'">
      {{ toastMsg }}
    </div>

    <!-- Header -->
    <div class="flex items-start justify-between flex-wrap gap-3">
      <div>
        <h1 class="text-2xl font-bold text-foreground">Demandes d'accès</h1>
        <p class="text-sm text-muted-foreground mt-0.5">
          <template v-if="pendingCount > 0">
            <span class="text-amber-600 font-medium">{{ pendingCount }} demande{{ pendingCount > 1 ? "s" : "" }} en attente de validation</span>
          </template>
          <template v-else>Toutes les demandes ont été traitées</template>
        </p>
      </div>
      <div v-if="pendingCount > 0" v-motion :initial="{ scale: 0.9, opacity: 0 }" :enter="{ scale: 1, opacity: 1 }" class="flex items-center gap-2 px-3 py-2 bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-xl">
        <span class="w-2 h-2 rounded-full bg-amber-500 animate-pulse" />
        <span class="text-xs font-semibold text-amber-700 dark:text-amber-400">
          {{ pendingCount }} en attente
        </span>
      </div>
    </div>

    <!-- Stats row -->
    <div class="grid grid-cols-3 gap-3">
      <div v-for="s in [
        { label: 'En attente', count: pendingCount,  bg: 'bg-amber-50 dark:bg-amber-900/20',   ic: 'text-amber-600',   icon: Clock },
        { label: 'Approuvées', count: approvedCount, bg: 'bg-emerald-50 dark:bg-emerald-900/20', ic: 'text-emerald-600', icon: CheckCircle2 },
        { label: 'Refusées',   count: rejectedCount, bg: 'bg-red-50 dark:bg-red-900/20',        ic: 'text-red-500',     icon: X },
      ]" :key="s.label" class="rounded-2xl border border-border bg-card p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-xs text-muted-foreground">{{ s.label }}</p>
            <p class="text-2xl font-bold text-foreground font-mono mt-0.5">{{ s.count }}</p>
          </div>
          <div :class="cn('w-9 h-9 rounded-xl flex items-center justify-center', s.bg)">
            <component :is="s.icon" :class="cn('w-5 h-5', s.ic)" />
          </div>
        </div>
      </div>
    </div>

    <!-- Filter tabs -->
    <div class="flex gap-1 p-1 bg-muted rounded-xl w-fit">
      <button v-for="f in FILTERS" :key="f.v" @click="filter = f.v"
        :class="cn('flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium transition-all duration-200', filter === f.v ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground')"
      >
        {{ f.label }}
        <span :class="cn('text-xs px-1.5 py-0.5 rounded-full font-mono', filter === f.v ? 'bg-blue-100 dark:bg-blue-900/50 text-blue-700 dark:text-blue-400' : 'bg-border text-muted-foreground')">
          {{ f.count }}
        </span>
      </button>
    </div>

    <!-- Cards list -->
    <div class="space-y-3 relative">
      <transition-group name="list" tag="div" class="space-y-3">
        <div v-if="filtered.length === 0" key="empty" class="flex flex-col items-center py-16 text-center">
          <div class="w-14 h-14 bg-muted rounded-2xl flex items-center justify-center mb-3">
            <UserPlus class="w-7 h-7 text-muted-foreground" />
          </div>
          <p class="font-medium text-foreground">Aucune demande</p>
          <p class="text-muted-foreground text-sm mt-1">Aucune demande dans cette catégorie</p>
        </div>
        
        <div v-else v-for="(req, i) in filtered" :key="req.id" class="transition-all">
          <div :class="cn('rounded-2xl border border-border bg-card overflow-hidden transition-all duration-200', req.status === 'pending' ? 'hover:shadow-md hover:border-blue-200 dark:hover:border-blue-800/50' : 'opacity-75')">
            <!-- Main card row -->
            <div class="p-5 flex items-start gap-4">
              <!-- Avatar -->
              <div :class="cn('rounded-full flex items-center justify-center font-semibold shrink-0 w-10 h-10 text-sm', avatarColor(req.firstName))">
                {{ initials(req.firstName, req.lastName) }}
              </div>

              <!-- Info -->
              <div class="flex-1 min-w-0 space-y-2">
                <div class="flex items-start justify-between flex-wrap gap-2">
                  <div>
                    <h3 class="font-semibold text-foreground">{{ req.firstName }} {{ req.lastName }}</h3>
                    <p class="text-sm text-muted-foreground flex items-center gap-1 mt-0.5">
                      <Mail class="w-3 h-3" />{{ req.email }}
                    </p>
                  </div>
                  <!-- Status badge -->
                  <span :class="cn('inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border', statusConfig[req.status].cls)">
                    <span :class="cn('w-1.5 h-1.5 rounded-full', statusConfig[req.status].dot, req.status === 'pending' ? 'animate-pulse' : '')" />
                    {{ statusConfig[req.status].label }}
                  </span>
                </div>

                <!-- Role + specialty -->
                <div class="flex items-center gap-2 flex-wrap">
                  <span :class="cn('inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border', ROLE_CLS[req.role])">
                    <component :is="ROLE_ICON[req.role]" class="w-3 h-3" />
                    {{ ROLE_LABEL[req.role] }}
                  </span>
                  <span class="flex items-center gap-1 text-xs text-muted-foreground bg-muted px-2.5 py-1 rounded-full">
                    <Stethoscope class="w-3 h-3" />{{ req.specialty }}
                  </span>
                  <span class="text-xs text-muted-foreground flex items-center gap-1 ml-auto">
                    <Clock class="w-3 h-3" />Demande du {{ fmt(req.requestedAt) }}
                  </span>
                </div>

                <!-- Rejection comment -->
                <div v-if="req.status === 'rejected' && req.comment" class="text-xs text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg px-3 py-2 flex items-start gap-1.5 mt-1">
                  <AlertCircle class="w-3 h-3 shrink-0 mt-0.5" />
                  <span><span class="font-semibold">Motif :</span> {{ req.comment }}</span>
                </div>
              </div>

              <!-- Actions — only for pending -->
              <div v-if="req.status === 'pending' && approveTarget !== req.id && rejectTarget !== req.id" class="flex gap-2 shrink-0">
                <button @click="approveTarget = req.id; rejectTarget = null" class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
                  <Check class="w-3.5 h-3.5 text-emerald-600" />
                  <span class="hidden sm:inline">Approuver</span>
                </button>
                <button @click="rejectTarget = req.id; approveTarget = null" class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
                  <X class="w-3.5 h-3.5 text-red-500" />
                  <span class="hidden sm:inline">Refuser</span>
                </button>
              </div>
            </div>

            <!-- Approve confirmation panel -->
            <transition enter-active-class="transition-all duration-200" enter-from-class="opacity-0 max-h-0" enter-to-class="opacity-100 max-h-40" leave-active-class="transition-all duration-200" leave-from-class="opacity-100 max-h-40" leave-to-class="opacity-0 max-h-0">
              <div v-if="approveTarget === req.id" class="overflow-hidden border-t border-border">
                <div class="p-4 bg-emerald-50/60 dark:bg-emerald-950/20 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
                  <div class="flex items-center gap-2">
                    <div class="w-8 h-8 bg-emerald-100 dark:bg-emerald-900/50 rounded-xl flex items-center justify-center shrink-0">
                      <CheckCircle2 class="w-4 h-4 text-emerald-600" />
                    </div>
                    <div>
                      <p class="text-sm font-semibold text-foreground">
                        Approuver le compte de {{ req.firstName }} {{ req.lastName }} ?
                      </p>
                      <p class="text-xs text-muted-foreground mt-0.5">
                        Un email avec les identifiants sera envoyé à {{ req.email }}
                      </p>
                    </div>
                  </div>
                  <div class="flex gap-2 shrink-0">
                    <button @click="approveTarget = null" class="text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">Annuler</button>
                    <button :disabled="processing === req.id" @click="doApprove(req.id)" class="bg-emerald-600 text-white hover:bg-emerald-700 shadow-emerald-200/50 dark:shadow-emerald-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 px-3 py-1.5 text-sm gap-1.5">
                      <template v-if="processing === req.id"><Loader2 class="w-3.5 h-3.5 animate-spin" />Validation…</template>
                      <template v-else><Check class="w-3.5 h-3.5" />Confirmer</template>
                    </button>
                  </div>
                </div>
              </div>
            </transition>

            <!-- Reject panel -->
            <transition enter-active-class="transition-all duration-200" enter-from-class="opacity-0 max-h-0" enter-to-class="opacity-100 max-h-48" leave-active-class="transition-all duration-200" leave-from-class="opacity-100 max-h-48" leave-to-class="opacity-0 max-h-0">
              <div v-if="rejectTarget === req.id" class="overflow-hidden border-t border-border">
                <div class="p-4 bg-red-50/60 dark:bg-red-950/20 space-y-3">
                  <div class="flex items-center gap-2">
                    <div class="w-8 h-8 bg-red-100 dark:bg-red-900/50 rounded-xl flex items-center justify-center shrink-0">
                      <X class="w-4 h-4 text-red-500" />
                    </div>
                    <div>
                      <p class="text-sm font-semibold text-foreground">
                        Refuser la demande de {{ req.firstName }} {{ req.lastName }} ?
                      </p>
                      <p class="text-xs text-muted-foreground mt-0.5">Motif (optionnel) — affiché dans l'email envoyé au candidat</p>
                    </div>
                  </div>
                  <textarea
                    v-model="rejectComment"
                    placeholder="Ex. : Informations incomplètes, établissement non référencé…"
                    rows="2"
                    class="w-full px-3 py-2 text-sm bg-background border border-border rounded-xl focus:outline-none focus:border-red-400 focus:ring-2 focus:ring-red-400/20 transition-all text-foreground placeholder:text-muted-foreground resize-none"
                  ></textarea>
                  <div class="flex gap-2 justify-end">
                    <button @click="rejectTarget = null; rejectComment = ''" class="text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">Annuler</button>
                    <button :disabled="processing === req.id" @click="doReject(req.id)" class="bg-red-500 text-white hover:bg-red-600 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 px-3 py-1.5 text-sm gap-1.5">
                      <template v-if="processing === req.id"><Loader2 class="w-3.5 h-3.5 animate-spin" />Refus…</template>
                      <template v-else><X class="w-3.5 h-3.5" />Confirmer le refus</template>
                    </button>
                  </div>
                </div>
              </div>
            </transition>

          </div>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<style scoped>
.list-move,
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(30px);
}
.list-leave-active {
  position: absolute;
}
</style>
