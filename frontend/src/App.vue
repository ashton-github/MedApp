<script setup>
import { computed, onMounted, ref } from 'vue'
import api from './services/api.js'
import { screens } from './constants/medapp.js'
import { useMedAppState } from './composables/useMedAppState.js'
import AppHeader from './components/AppHeader.vue'
import ScreenTabs from './components/ScreenTabs.vue'
import Sidebar from './components/Sidebar.vue'
import LoginScreen from './components/screens/LoginScreen.vue'
import PatientsScreen from './components/screens/PatientsScreen.vue'
import PatientFormScreen from './components/screens/PatientFormScreen.vue'
import OrdonnancesScreen from './components/screens/OrdonnancesScreen.vue'
import OrdonnanceFormScreen from './components/screens/OrdonnanceFormScreen.vue'

const { currentScreen } = useMedAppState()

const backendStatus = ref('en cours de vérification...')
const mongoStatus = ref('-')
const backendUnavailable = ref(false)

const screenComponents = {
  [screens.login]: LoginScreen,
  [screens.patients]: PatientsScreen,
  [screens.patientForm]: PatientFormScreen,
  [screens.ordonnances]: OrdonnancesScreen,
  [screens.ordonnanceForm]: OrdonnanceFormScreen
}

const currentScreenComponent = computed(() => screenComponents[currentScreen.value] || LoginScreen)

onMounted(async () => {
  try {
    const { data } = await api.get('/health')
    backendStatus.value = data.status
    mongoStatus.value = data.mongodb
  } catch (error) {
    backendUnavailable.value = true
    backendStatus.value = 'injoignable'
  }
})
</script>

<template>
  <main class="app-shell">
    <AppHeader
      :backend-status="backendStatus"
      :mongo-status="mongoStatus"
      :backend-unavailable="backendUnavailable"
    />

    <ScreenTabs />

    <Sidebar>
      <component :is="currentScreenComponent" />
    </Sidebar>
  </main>
</template>
