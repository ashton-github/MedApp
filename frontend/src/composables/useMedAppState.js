import { computed } from 'vue'
import { router } from '../router/index.js'
import { screens } from '../constants/medapp.js'

// Generic screen navigation used by call sites that don't need to pass an
// id (e.g. a "new patient" quick-action button). Screens that always need
// an id (patient detail, editing a patient/ordonnance) are handled by the
// dedicated functions below (viewPatient, editPatient, openEditOrdonnance),
// not through this map.
const SCREEN_TO_ROUTE = {
  [screens.login]: 'login',
  [screens.dashboard]: 'dashboard',
  [screens.patients]: 'patients',
  [screens.patientForm]: 'patient-new',
  [screens.ordonnances]: 'ordonnances',
  [screens.ordonnanceForm]: 'ordonnance-new',
  [screens.pdfPreview]: 'pdf-preview',
  [screens.agenda]: 'agenda',
  [screens.accountRequests]: 'account-requests',
  [screens.settings]: 'settings'
}

export function useMedAppState() {
  // Kept as `screens.*` values (not route names) so existing components
  // (Sidebar, App.vue) that compare against the screens constants keep
  // working without any change.
  const currentScreen = computed(() => router.currentRoute.value.meta.screen || screens.login)

  // The patient id currently "in view" — read from the route itself so it
  // survives a page reload. Populated for patient-detail/patient-edit routes,
  // or from the ?patientId= query when creating a new ordonnance from a
  // patient's detail page.
  const selectedPatientId = computed(() => {
    const route = router.currentRoute.value
    if (route.params.id && (route.name === 'patient-detail' || route.name === 'patient-edit')) {
      return route.params.id
    }
    if (route.name === 'ordonnance-new' && route.query.patientId) {
      return route.query.patientId
    }
    return null
  })

  const showScreen = (screen) => {
    const routeName = SCREEN_TO_ROUTE[screen]
    if (routeName) router.push({ name: routeName })
  }

  const editPatient = (patient) => {
    if (!patient?.id) {
      router.push({ name: 'patient-new' })
      return
    }
    router.push({ name: 'patient-edit', params: { id: patient.id } })
  }

  const viewPatient = (patientId) => {
    router.push({ name: 'patient-detail', params: { id: patientId } })
  }

  const openNewPatient = () => {
    router.push({ name: 'patient-new' })
  }

  const openNewOrdonnance = (patientId = null) => {
    // Guard against a DOM Event slipping in from a click handler.
    const validId = (typeof patientId === 'string' || typeof patientId === 'number') ? patientId : null
    router.push({ name: 'ordonnance-new', query: validId ? { patientId: validId } : {} })
  }

  const openEditOrdonnance = (ordonnance) => {
    router.push({ name: 'ordonnance-edit', params: { id: ordonnance.id } })
  }

  return {
    currentScreen,
    selectedPatientId,
    showScreen,
    editPatient,
    viewPatient,
    openNewPatient,
    openNewOrdonnance,
    openEditOrdonnance
  }
}
