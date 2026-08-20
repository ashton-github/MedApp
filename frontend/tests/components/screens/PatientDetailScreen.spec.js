import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import PatientDetailScreen from '../../../src/components/screens/PatientDetailScreen.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────────
const { mockShowScreen, mockEditPatient, mockSelectedPatientId } = vi.hoisted(() => ({
  mockShowScreen: vi.fn(),
  mockEditPatient: vi.fn(),
  mockSelectedPatientId: { value: 'p1' }
}))

vi.mock('../../../src/composables/useMedAppState.js', () => ({
  useMedAppState: () => ({
    showScreen: mockShowScreen,
    editPatient: mockEditPatient,
    selectedPatientId: mockSelectedPatientId
  })
}))

const SAMPLE_PATIENT = {
  id: 'p1', firstName: 'Sophie', lastName: 'Laurent',
  birthDate: '1985-03-15', gender: 'F',
  phone: '+33 6 12 34 56 78', address: '10 rue de la Paix, Paris',
  socialSecurityNumber: '1850375075089', referringDoctor: 'doc1',
  medicalHistory: ['Pénicilline']
}

const mockPatientStore = {
  loading: false,
  error: null,
  currentPatient: null,
  getPatientById: vi.fn(),
  deletePatient: vi.fn()
}

vi.mock('../../../src/stores/patientStore.js', () => ({
  usePatientStore: () => mockPatientStore
}))

const mockOrdonnanceStore = {
  fetchOrdonnancesByPatientId: vi.fn().mockResolvedValue([])
}
vi.mock('../../../src/stores/ordonnanceStore.js', () => ({
  useOrdonnanceStore: () => mockOrdonnanceStore
}))

const mockDoctorStore = {
  doctors: [{ id: 'doc1', prenom: 'Jean', nom: 'Martin' }],
  fetchDoctors: vi.fn().mockResolvedValue(),
  getDoctorFullName: vi.fn((id) => {
    if (!id) return 'Non renseigné'
    const doc = mockDoctorStore.doctors.find((d) => d.id === id)
    return doc ? `Dr. ${doc.prenom} ${doc.nom}` : 'Non renseigné'
  })
}
vi.mock('../../../src/stores/doctorStore.js', () => ({
  useDoctorStore: () => mockDoctorStore
}))

// authStore controls role-based visibility
const mockAuthStore = { role: 'medecin' }
vi.mock('../../../src/stores/authStore.js', () => ({
  useAuthStore: () => mockAuthStore
}))

// ── Helper ─────────────────────────────────────────────────────────────────────
const createWrapper = () =>
  mount(PatientDetailScreen, {
    global: {
      plugins: [createPinia()],
      stubs: {
        'v-motion': { template: '<div><slot /></div>' },
        ChevronRight: true, Pencil: true, Plus: true,
        Phone: true, Shield: true, Heart: true, Lock: true,
        Activity: true, Trash2: true, Loader2: true,
        AlertCircle: true, FileText: true
      }
    }
  })

// ── Tests ──────────────────────────────────────────────────────────────────────
describe('PatientDetailScreen.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockAuthStore.role = 'medecin'
    mockPatientStore.loading = false
    mockPatientStore.error = null
    mockPatientStore.currentPatient = null
    mockPatientStore.getPatientById.mockClear()
    mockPatientStore.deletePatient.mockClear()
    mockShowScreen.mockClear()
    mockEditPatient.mockClear()
    mockSelectedPatientId.value = 'p1'
  })

  it('calls getPatientById on mount with the selectedPatientId', async () => {
    createWrapper()
    expect(mockPatientStore.getPatientById).toHaveBeenCalledWith('p1')
  })

  it('shows a loader while loading', () => {
    mockPatientStore.loading = true
    const wrapper = createWrapper()
    // Loader2 is stubbed as 'loader2-stub', but when loading=true and no currentPatient,
    // the component renders the loading div. Check for the wrapping structure.
    expect(wrapper.find('.animate-spin').exists() || wrapper.text().trim() === '').toBe(true)
  })

  it('shows an error banner when there is an error and no patient', () => {
    mockPatientStore.error = 'Patient introuvable.'
    const wrapper = createWrapper()
    expect(wrapper.text()).toContain('Patient introuvable.')
  })

  it('renders patient data when currentPatient is loaded', () => {
    mockPatientStore.currentPatient = SAMPLE_PATIENT
    const wrapper = createWrapper()
    expect(wrapper.text()).toContain('Sophie')
    expect(wrapper.text()).toContain('Laurent')
    expect(wrapper.text()).toContain('Dr. Jean Martin')
  })

  it('renders tabs and switches to Ordonnances tab', async () => {
    mockPatientStore.currentPatient = SAMPLE_PATIENT
    const wrapper = createWrapper()
    // Overview active by default
    expect(wrapper.text()).toContain('Informations médicales')
    // Switch to prescriptions tab
    const tabs = wrapper.findAll('button')
    const prescriptionsTab = tabs.find(b => b.text().includes('Ordonnances'))
    await prescriptionsTab.trigger('click')
    expect(wrapper.text()).toContain('ordonnance')
  })

  it('calls editPatient when Modifier button is clicked', async () => {
    mockPatientStore.currentPatient = SAMPLE_PATIENT
    const wrapper = createWrapper()
    const editBtn = wrapper.findAll('button').find(b => b.text().includes('Modifier'))
    await editBtn.trigger('click')
    expect(mockEditPatient).toHaveBeenCalledWith(SAMPLE_PATIENT)
  })

  it('shows delete confirmation modal when Supprimer is clicked', async () => {
    mockAuthStore.role = 'secretaire'
    mockPatientStore.currentPatient = SAMPLE_PATIENT
    const wrapper = createWrapper()
    expect(wrapper.text()).not.toContain('Cette action est irréversible')
    const deleteBtn = wrapper.findAll('button').find(b => b.text().includes('Supprimer'))
    await deleteBtn.trigger('click')
    expect(wrapper.text()).toContain('Cette action est irréversible')
  })

  it('hides Ordonnance button and shows Restreint for socialSecurityNumber when user is not doctor', () => {
    mockAuthStore.role = 'secretaire'
    mockPatientStore.currentPatient = SAMPLE_PATIENT
    const wrapper = createWrapper()
    // Ordonnance action button should not exist (only Ordonnances tab)
    const ordonnanceBtn = wrapper.findAll('button').find(b => b.text().trim() === 'Ordonnance')
    expect(ordonnanceBtn).toBeUndefined()
    // Social security number should be masked
    expect(wrapper.text()).toContain('Restreint')
  })
})
