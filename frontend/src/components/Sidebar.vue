<script setup>
import { computed } from 'vue'
import {
  LayoutDashboard,
  Users,
  FileText,
  Settings,
  LogOut,
  Stethoscope,
  ChevronLeft
} from 'lucide-vue-next'
import { useMedAppState } from '../composables/useMedAppState.js'
import { screens } from '../constants/medapp.js'
import { cn } from '../lib/utils.js'

const props = defineProps({
  collapsed: { type: Boolean, default: false }
})

const emit = defineEmits(['toggle'])
const { currentScreen, showScreen, authUser, logout } = useMedAppState()

const NAV = [
  { s: screens.dashboard, label: "Tableau de bord", icon: LayoutDashboard },
  { s: screens.patients, label: "Patients", icon: Users },
  { s: screens.ordonnances, label: "Ordonnances", icon: FileText },
  { s: screens.settings, label: "Paramètres", icon: Settings },
]

const isActive = (screen) => {
  if (screen === screens.patients && currentScreen.value === screens.patientForm) return true
  if (screen === screens.ordonnances && currentScreen.value === screens.ordonnanceForm) return true
  return currentScreen.value === screen
}

const initials = computed(() => {
  if (!authUser.value?.name) return 'DR'
  const parts = authUser.value.name.split(' ')
  return parts.map(n => n[0]).join('').slice(0, 2).toUpperCase()
})
</script>

<template>
  <aside
    :class="cn(
      'flex flex-col bg-sidebar border-r border-sidebar-border shrink-0 overflow-hidden transition-all duration-300 ease-[cubic-bezier(0.4,0,0.2,1)]',
      collapsed ? 'w-[72px]' : 'w-[240px]'
    )"
  >
    <div class="h-16 flex items-center px-4 border-b border-sidebar-border shrink-0">
      <div class="flex items-center gap-3 min-w-0">
        <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center shrink-0">
          <Stethoscope class="w-4 h-4 text-white" />
        </div>
        <span
          v-if="!collapsed"
          class="font-bold text-foreground text-lg tracking-tight whitespace-nowrap overflow-hidden transition-opacity duration-200"
        >
          MedApp
        </span>
      </div>
      <button
        @click="emit('toggle')"
        class="ml-auto p-1.5 rounded-lg text-muted-foreground hover:bg-sidebar-accent hover:text-foreground transition-colors"
      >
        <ChevronLeft
          class="w-4 h-4 transition-transform duration-200"
          :class="{ 'rotate-180': collapsed }"
        />
      </button>
    </div>

    <div class="flex-1 py-6 px-3 flex flex-col gap-1 overflow-y-auto overflow-x-hidden">
      <button
        v-for="item in NAV"
        :key="item.s"
        @click="showScreen(item.s)"
        v-motion
        :initial="{ opacity: 0, x: -10 }"
        :enter="{ opacity: 1, x: 0 }"
        :class="cn(
          'flex items-center gap-3 w-full p-2 rounded-xl transition-all duration-200 group relative focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring',
          isActive(item.s) ? 'bg-sidebar-accent text-sidebar-accent-foreground font-medium shadow-sm' : 'text-sidebar-foreground/70 hover:bg-sidebar-accent/50 hover:text-sidebar-foreground'
        )"
      >
        <div
          :class="cn(
            'flex items-center justify-center w-8 h-8 rounded-lg shrink-0 transition-colors',
            isActive(item.s) ? 'bg-white dark:bg-sidebar border border-sidebar-border shadow-sm' : 'bg-transparent group-hover:bg-white/50 dark:group-hover:bg-sidebar/50'
          )"
        >
          <component :is="item.icon" class="w-4 h-4" />
        </div>
        <span
          v-if="!collapsed"
          class="text-sm whitespace-nowrap"
        >
          {{ item.label }}
        </span>
        
        <div
          v-if="isActive(item.s) && !collapsed"
          v-motion
          :initial="{ scaleY: 0 }"
          :enter="{ scaleY: 1 }"
          class="absolute left-0 top-2 bottom-2 w-1 bg-sidebar-primary rounded-r-full"
        ></div>
      </button>
    </div>

    <div class="p-3 border-t border-sidebar-border shrink-0">
      <div
        :class="cn(
          'flex items-center gap-3 p-2 rounded-xl border transition-colors',
          collapsed ? 'justify-center border-transparent' : 'bg-sidebar-accent/30 border-sidebar-border'
        )"
      >
        <div class="w-8 h-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-xs shrink-0">
          {{ initials }}
        </div>
        <div v-if="!collapsed" class="flex-1 min-w-0">
          <p class="text-sm font-semibold text-foreground truncate">{{ authUser?.name || 'Dr. Martin' }}</p>
          <p class="text-xs text-muted-foreground truncate">Médecin</p>
        </div>
        <button
          v-if="!collapsed"
          @click="logout"
          class="p-1.5 text-muted-foreground hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-950/30 rounded-lg transition-colors shrink-0"
          title="Déconnexion"
        >
          <LogOut class="w-4 h-4" />
        </button>
      </div>
    </div>
  </aside>
</template>