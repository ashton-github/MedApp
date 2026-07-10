export const screens = {
  login: 'login',
  patients: 'patients',
  patientForm: 'patient-form',
  ordonnances: 'ordonnances',
  ordonnanceForm: 'ordonnance-form'
}

export const screenButtons = [
  { id: screens.login, label: 'Connexion' },
  { id: screens.patients, label: 'Liste des patients' },
  { id: screens.patientForm, label: 'Fiche patient' },
  { id: screens.ordonnances, label: 'Liste des ordonnances' },
  { id: screens.ordonnanceForm, label: 'Nouvelle ordonnance' }
]

export const addressMap = {
  login: 'localhost:5173/login',
  patients: 'localhost:5173/patients',
  'patient-form': 'localhost:5173/patients/1',
  ordonnances: 'localhost:5173/ordonnances',
  'ordonnance-form': 'localhost:5173/ordonnances/nouvelle'
}

export const navMap = {
  patients: 'patients',
  'patient-form': 'patients',
  ordonnances: 'ordonnances',
  'ordonnance-form': 'ordonnances'
}

export const patientRecords = [
  { nom: 'Martin', prenom: 'Sophie', naissance: '14/03/1985' },
  { nom: 'Dubois', prenom: 'Karim', naissance: '02/11/1990' },
  { nom: 'Leroy', prenom: 'Alice', naissance: '27/07/1978' }
]

export const ordonnanceRecords = [
  { patient: 'Sophie Martin', emission: '12/06/2026', validite: '12/07/2026', statut: 'Active' },
  { patient: 'Karim Dubois', emission: '01/05/2026', validite: '01/06/2026', statut: 'Expiree' },
  { patient: 'Alice Leroy', emission: '28/06/2026', validite: '28/07/2026', statut: 'Active' }
]