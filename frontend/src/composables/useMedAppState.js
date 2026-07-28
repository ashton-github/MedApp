import { ref } from 'vue'
import { screens } from '../constants/medapp.js'

const currentScreen = ref(screens.login)
const authUser = ref(null)
const selectedPatientId = ref(null)  // ID of the patient currently displayed in detail view
const patientToEdit = ref(null)

const authForm = ref({
  email: '',
  password: ''
})

export function useMedAppState() {
  const showScreen = (screen) => {
    currentScreen.value = screen
  }

  const signIn = (user = null) => {
    if (user) {
      authUser.value = user
    }
    showScreen(screens.dashboard)
  }

  const logout = () => {
    authUser.value = null
    showScreen(screens.login)
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

  const openNewOrdonnance = () => {
    showScreen(screens.ordonnanceForm)
  }

  return {
    currentScreen,
    authUser,
    authForm,
    selectedPatientId,
    patientToEdit,
    showScreen,
    signIn,
    logout,
    editPatient,
    viewPatient,
    openNewPatient,
    openNewOrdonnance
  }
}