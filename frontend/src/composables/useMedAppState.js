import { ref } from 'vue'
import { screens } from '../constants/medapp.js'

const currentScreen    = ref(screens.login)
const selectedPatientId = ref(null)  // ID of the patient currently displayed in detail view
const patientToEdit    = ref(null)
const ordonnanceToEdit = ref(null)

export function useMedAppState() {
  const showScreen = (screen) => {
    currentScreen.value = screen
  }

  const editPatient = (patient) => {
    patientToEdit.value = patient
    showScreen(screens.patientForm)
  }

  const viewPatient = (patientId) => {
    selectedPatientId.value = patientId
    showScreen(screens.patientDetail)
  }

  const openNewPatient = () => {
    patientToEdit.value = null
    showScreen(screens.patientForm)
  }

  const openNewOrdonnance = (patientId = null) => {
    ordonnanceToEdit.value = null
    // Ensure patientId is not a DOM Event from a click handler
    selectedPatientId.value = (typeof patientId === 'string' || typeof patientId === 'number') ? patientId : null
    showScreen(screens.ordonnanceForm)
  }

  const openEditOrdonnance = (ordonnance) => {
    ordonnanceToEdit.value = ordonnance
    selectedPatientId.value = ordonnance?.patientId ?? null
    showScreen(screens.ordonnanceForm)
  }

  return {
    currentScreen,
    selectedPatientId,
    patientToEdit,
    ordonnanceToEdit,
    showScreen,
    editPatient,
    viewPatient,
    openNewPatient,
    openNewOrdonnance,
    openEditOrdonnance
  }
}