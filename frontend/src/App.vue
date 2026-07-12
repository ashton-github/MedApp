<script setup>
import { computed, ref } from 'vue'
import { useMedAppState } from './composables/useMedAppState.js'
import { screens } from './constants/medapp.js'
import AppHeader from './components/AppHeader.vue'
import Sidebar from './components/Sidebar.vue'
import LoginScreen from './components/screens/LoginScreen.vue'
import DashboardScreen from './components/screens/DashboardScreen.vue'
import PatientsScreen from './components/screens/PatientsScreen.vue'
import PatientFormScreen from './components/screens/PatientFormScreen.vue'
import PatientDetailScreen from './components/screens/PatientDetailScreen.vue'
import OrdonnancesScreen from './components/screens/OrdonnancesScreen.vue'
import OrdonnanceFormScreen from './components/screens/OrdonnanceFormScreen.vue'
import PDFPreviewScreen from './components/screens/PDFPreviewScreen.vue'
import SettingsScreen from './components/screens/SettingsScreen.vue'

const { currentScreen } = useMedAppState()

const isSidebarCollapsed = ref(false)
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

const screenComponents = {
  [screens.login]: LoginScreen,
  [screens.dashboard]: DashboardScreen,
  [screens.patients]: PatientsScreen,
  [screens.patientDetail]: PatientDetailScreen,
  [screens.patientForm]: PatientFormScreen,
  [screens.ordonnances]: OrdonnancesScreen,
  [screens.ordonnanceForm]: OrdonnanceFormScreen,
  [screens.pdfPreview]: PDFPreviewScreen,
  [screens.settings]: SettingsScreen
}

const currentScreenComponent = computed(() => screenComponents[currentScreen.value] || LoginScreen)
</script>

<template>
  <div class="min-h-screen flex bg-background text-foreground">
    <template v-if="currentScreen === screens.login">
      <LoginScreen />
    </template>
    
    <template v-else>
      <Sidebar :collapsed="isSidebarCollapsed" @toggle="toggleSidebar" />
      <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
        <AppHeader />
        <main class="flex-1 overflow-y-auto bg-background/50">
          <component :is="currentScreenComponent" />
        </main>
      </div>
    </template>
  </div>
</template>
