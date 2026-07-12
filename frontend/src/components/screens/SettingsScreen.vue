<script setup>
import { computed } from 'vue'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { cn } from '../../lib/utils.js'

const { authUser } = useMedAppState()

const initials = computed(() => {
  if (!authUser.value?.name) return 'DR'
  const parts = authUser.value.name.split(' ')
  return parts.map(n => n[0]).join('').slice(0, 2).toUpperCase()
})

const PREFS = [
  "Notifications email",
  "Rappels de rendez-vous",
  "Alertes d'expiration d'ordonnances",
  "Rapport hebdomadaire"
]
</script>

<template>
  <div class="p-6 space-y-6 max-w-2xl">
    <h1 class="text-2xl font-bold text-foreground">Paramètres</h1>
    
    <div class="rounded-2xl border border-border bg-card p-6">
      <h2 class="font-semibold text-foreground mb-4">Profil</h2>
      <div class="flex items-center gap-4">
        <div class="w-16 h-16 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white text-xl font-bold shrink-0">
          {{ initials }}
        </div>
        <div class="flex-1">
          <p class="font-semibold text-foreground">{{ authUser?.name || 'Utilisateur' }}</p>
          <p class="text-sm text-muted-foreground">{{ authUser?.email || 'email@exemple.fr' }}</p>
        </div>
        <button class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
          Modifier le profil
        </button>
      </div>
    </div>
    
    <div class="rounded-2xl border border-border bg-card p-6">
      <h2 class="font-semibold text-foreground mb-4">Préférences</h2>
      <div class="space-y-1">
        <div v-for="(pref, i) in PREFS" :key="pref" class="flex items-center justify-between py-3 border-b border-border last:border-0">
          <span class="text-sm text-foreground">{{ pref }}</span>
          <div :class="cn('w-10 h-6 rounded-full relative cursor-pointer transition-colors', i % 2 === 0 ? 'bg-blue-600' : 'bg-muted')">
            <div :class="cn('absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-transform', i % 2 === 0 ? 'translate-x-5' : 'translate-x-1')" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
