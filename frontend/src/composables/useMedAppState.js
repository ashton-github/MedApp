import { ref } from 'vue'
import { screens } from '../constants/medapp.js'

const currentScreen = ref(screens.login)
const authUser = ref(null)

const authForm = ref({
  email: '',
  password: ''
})

const patientDraft = ref({
  nom: '',
  prenom: '',
  naissance: '',
  telephone: '',
  adresse: '',
  antecedents: '',
  mutuelle: ''
})

const prescriptionDraft = ref({
  patient: '',
  validite: '',
  medicament1: '',
  dosage1: '',
  frequence1: '',
  duree1: '',
  medicament2: '',
  dosage2: '',
  frequence2: '',
  duree2: '',
  remarques: ''
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

  const editPatient = () => {
    showScreen(screens.patientForm)
  }

  const openNewPatient = () => {
    patientDraft.value = { nom: '', prenom: '', naissance: '', telephone: '', adresse: '', antecedents: '', mutuelle: '' }
    showScreen(screens.patientForm)
  }

  const openNewOrdonnance = () => {
    prescriptionDraft.value = { patient: '', validite: '', medicament1: '', dosage1: '', frequence1: '', duree1: '', medicament2: '', dosage2: '', frequence2: '', duree2: '', remarques: '' }
    showScreen(screens.ordonnanceForm)
  }

  return {
    currentScreen,
    authUser,
    authForm,
    patientDraft,
    prescriptionDraft,
    showScreen,
    signIn,
    logout,
    editPatient,
    openNewPatient,
    openNewOrdonnance
  }
}