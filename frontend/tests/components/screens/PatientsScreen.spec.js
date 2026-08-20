import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import PatientsScreen from '../../../src/components/screens/PatientsScreen.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────────
const { mockOpenNewPatient, mockEditPatient, mockViewPatient } = vi.hoisted(() => ({
  mockOpenNewPatient: vi.fn(),
  mockEditPatient: vi.fn(),
  mockViewPatient: vi.fn()
}))

vi.mock('../../../src/composables/useMedAppState.js', () => ({
  useMedAppState: () => ({
    openNewPatient: mockOpenNewPatient,
    editPatient: mockEditPatient,
    viewPatient: mockViewPatient
  })
}))

// Mock patientStore
const mockPatientStore = {
  patients: [],
  loading: false,
  error: null,
  totalItems: 0,
  fetchPatients: vi.fn(),
  searchPatients: vi.fn(),
  deletePatient: vi.fn()
}

vi.mock('../../../src/stores/patientStore.js', () => ({
  usePatientStore: () => mockPatientStore
}))

const mockAuthStore = { role: 'medecin' }
vi.mock('../../../src/stores/authStore.js', () => ({
  useAuthStore: () => mockAuthStore
}))

// ── Sample data ────────────────────────────────────────────────────────────────
const SAMPLE_PATIENTS = [
  {
    id: 'p1', firstName: 'Sophie', lastName: 'Laurent',
    birthDate: '1985-03-15', gender: 'F',
    phone: '+33 6 12 34 56 78', address: '10 rue de la Paix, Paris',
    socialSecurityNumber: '1850375075089', referringDoctor: 'Dr. Martin',
    medicalHistory: ['Pénicilline'], status: 'active'
  },
  {
    id: 'p2', firstName: 'Marc', lastName: 'Dubois',
    birthDate: '1972-11-22', gender: 'M',
    phone: '+33 6 98 76 54 32', address: null,
    socialSecurityNumber: '1721167098023', referringDoctor: null,
    medicalHistory: [], status: 'active'
  }
]

// ── Helper ─────────────────────────────────────────────────────────────────────
const createWrapper = () =>
  mount(PatientsScreen, {
    global: {
      plugins: [createPinia()],
      stubs: {
        'v-motion': { template: '<div><slot /></div>' },
        Users: true, Search: true, Filter: true, Plus: true,
        LayoutGrid: true, List: true, Eye: true, Pencil: true,
        Phone: true, Mail: true, Trash2: true, AlertCircle: true
      }
    }
  })

// ── Tests ──────────────────────────────────────────────────────────────────────
describe('PatientsScreen.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockAuthStore.role = 'medecin'
    mockPatientStore.patients = []
    mockPatientStore.loading = false
    mockPatientStore.error = null
    mockPatientStore.totalItems = 0
    mockPatientStore.fetchPatients.mockClear()
    mockPatientStore.searchPatients.mockClear()
    mockPatientStore.deletePatient.mockClear()
    mockOpenNewPatient.mockClear()
    mockEditPatient.mockClear()
    mockViewPatient.mockClear()
    vi.restoreAllMocks()
  })

  it('calls fetchPatients on mount', async () => {
    createWrapper()
    expect(mockPatientStore.fetchPatients).toHaveBeenCalled()
  })

  it('shows empty state when no patients', () => {
    const wrapper = createWrapper()
    expect(wrapper.text()).toContain('Aucun patient trouvé')
  })

  it('renders patient cards in grid view', async () => {
    mockPatientStore.patients = SAMPLE_PATIENTS
    const wrapper = createWrapper()
    expect(wrapper.text()).toContain('Sophie')
    expect(wrapper.text()).toContain('Marc')
  })

  it('shows error banner when patientStore.error is set', () => {
    mockPatientStore.error = 'Erreur de connexion au serveur.'
    const wrapper = createWrapper()
    expect(wrapper.text()).toContain('Erreur de connexion au serveur.')
  })

  it('calls openNewPatient when "Nouveau patient" is clicked', async () => {
    const wrapper = createWrapper()
    const btn = wrapper.findAll('button').find(b => b.text().includes('Nouveau patient'))
    await btn.trigger('click')
    expect(mockOpenNewPatient).toHaveBeenCalled()
  })

  it('switches between grid and list view', async () => {
    mockPatientStore.patients = SAMPLE_PATIENTS
    const wrapper = createWrapper()
    // Grid is default
    expect(wrapper.find('.grid.grid-cols-1').exists()).toBe(true)
    // Find list button by its stub
    const listBtns = wrapper.findAll('button').filter(b => b.html().toLowerCase().includes('list'))
    if (listBtns.length > 0) {
      await listBtns[0].trigger('click')
      expect(wrapper.find('table').exists()).toBe(true)
    }
  })

  it('calls editPatient when pencil icon is clicked', async () => {
    mockPatientStore.patients = SAMPLE_PATIENTS
    const wrapper = createWrapper()
    const editBtn = wrapper.find('button[title="Modifier"]')
    await editBtn.trigger('click')
    expect(mockEditPatient).toHaveBeenCalled()
  })

  it('shows delete confirmation modal when trash icon is clicked', async () => {
    mockAuthStore.role = 'secretaire'
    mockPatientStore.patients = SAMPLE_PATIENTS
    const wrapper = createWrapper()
    const deleteBtn = wrapper.find('button[title="Supprimer"]')
    await deleteBtn.trigger('click')
    expect(wrapper.text()).toContain('Supprimer le patient')
  })
})
