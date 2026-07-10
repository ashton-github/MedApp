import { reactive, ref } from 'vue'
import { screens } from '../constants/medapp.js'

const currentScreen = ref(screens.login)

const authForm = reactive({
  email: '',
  password: ''
})

const patientDraft = reactive({
  nom: 'Martin',
  prenom: 'Sophie',
  naissance: '14/03/1985',
  telephone: '06 12 34 56 78',
  adresse: '12 rue des Lilas, 75011 Paris',
  antecedents: 'Allergie a la penicilline'
})

const prescriptionDraft = reactive({
  patient: 'Sophie Martin',
  validite: '',
  medicament1: 'Amoxicilline',
  dosage1: '500 mg',
  frequence1: '2 fois / jour',
  duree1: '7 jours',
  medicament2: 'Paracetamol',
  dosage2: '500 mg',
  frequence2: 'Si douleur',
  duree2: '3 jours',
  remarques: 'Prendre pendant le repas si possible'
})

function showScreen(name) {
  currentScreen.value = name
}

function signIn() {
  showScreen(screens.patients)
}

function openNewPatient() {
  showScreen(screens.patientForm)
}

function openNewOrdonnance() {
  showScreen(screens.ordonnanceForm)
}

function editPatient() {
  showScreen(screens.patientForm)
}

export function useMedAppState() {
  return {
    currentScreen,
    authForm,
    patientDraft,
    prescriptionDraft,
    showScreen,
    signIn,
    openNewPatient,
    openNewOrdonnance,
    editPatient
  }
}