import { mount, flushPromises } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { reactive } from 'vue'
import PatientFormScreen from '../../../src/components/screens/PatientFormScreen.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────────
// The component now derives edit-mode/patient id from the route itself
// (/patients/:id/modifier) rather than from data held in memory. We control
// the route seen by the component via a mutable mock object.
const { mockRoute, mockPush, mockShowScreen } = vi.hoisted(() => ({
  mockRoute: { name: 'patient-new', params: {}, query: {} },
  mockPush: vi.fn(),
  mockShowScreen: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute
}))

vi.mock('../../../src/router/index.js', () => ({
  router: { push: mockPush }
}))

vi.mock('../../../src/composables/useMedAppState.js', () => ({
  useMedAppState: () => ({
    showScreen: mockShowScreen
  })
}))

const mockPatientStore = {
  loading: false,
  error: null,
  currentPatient: null,
  createPatient: vi.fn(),
  updatePatient: vi.fn(),
  getPatientById: vi.fn()
}

vi.mock('../../../src/stores/patientStore.js', () => ({
  usePatientStore: () => mockPatientStore
}))

const mockDoctorStore = {
  doctors: [],
  fetchDoctors: vi.fn().mockResolvedValue()
}

vi.mock('../../../src/stores/doctorStore.js', () => ({
  useDoctorStore: () => mockDoctorStore
}))

// ── Helper ─────────────────────────────────────────────────────────────────────
const createWrapper = () =>
  mount(PatientFormScreen, {
    global: {
      plugins: [createPinia()],
      stubs: {
        'v-motion': { template: '<div><slot /></div>' },
        ChevronLeft: true, ChevronRight: true, Check: true,
        CheckCircle2: true, Loader2: true, Phone: true,
        MapPin: true, Shield: true, AlertCircle: true
      }
    }
  })

// ── Tests ──────────────────────────────────────────────────────────────────────
describe('PatientFormScreen.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockRoute.name = 'patient-new'
    mockRoute.params = {}
    mockPatientStore.loading = false
    mockPatientStore.error = null
    mockPatientStore.currentPatient = null
    mockPatientStore.createPatient.mockClear()
    mockPatientStore.updatePatient.mockClear()
    mockPatientStore.getPatientById.mockClear()
    mockDoctorStore.fetchDoctors.mockClear()
    mockShowScreen.mockClear()
    mockPush.mockClear()
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('renders step 1 with "Nouveau patient" title in create mode', () => {
    const wrapper = createWrapper()
    expect(wrapper.text()).toContain('Nouveau patient')
    expect(wrapper.text()).toContain('Informations personnelles')
    expect(wrapper.find('input[placeholder="Sophie"]').exists()).toBe(true)
  })

  it('renders in edit mode and pre-fills form from the fetched patient', async () => {
    mockRoute.name = 'patient-edit'
    mockRoute.params = { id: 'p1' }
    mockPatientStore.getPatientById.mockImplementation(async (id) => {
      mockPatientStore.currentPatient = {
        id: 'p1', firstName: 'Marc', lastName: 'Dubois',
        birthDate: '1972-11-22', gender: 'M',
        phone: '+33 6 98 76 54 32', address: null,
        socialSecurityNumber: '1721167098023',
        referringDoctor: 'doc1', medicalHistory: ['Asthme']
      }
    })

    const wrapper = createWrapper()
    await flushPromises()

    expect(mockPatientStore.getPatientById).toHaveBeenCalledWith('p1')
    expect(wrapper.text()).toContain('Modifier le patient')
    const firstNameInput = wrapper.find('input[placeholder="Sophie"]')
    expect(firstNameInput.element.value).toBe('Marc')
    const lastNameInput = wrapper.find('input[placeholder="Laurent"]')
    expect(lastNameInput.element.value).toBe('Dubois')
  })

  it('can navigate to step 2 and step 3 via Suivant button', async () => {
    const wrapper = createWrapper()
    // Step 1 → 2
    let nextBtn = wrapper.findAll('button').find(b => b.text().includes('Suivant'))
    await nextBtn.trigger('click')
    expect(wrapper.text()).toContain('Coordonnées')
    // Step 2 → 3
    nextBtn = wrapper.findAll('button').find(b => b.text().includes('Suivant'))
    await nextBtn.trigger('click')
    expect(wrapper.text()).toContain('Antécédents médicaux')
  })

  it('calls createPatient on submit in create mode', async () => {
    mockPatientStore.createPatient.mockResolvedValue({ id: 'new1' })
    const wrapper = createWrapper()
    // Navigate to step 3
    await wrapper.findAll('button').find(b => b.text().includes('Suivant')).trigger('click')
    await wrapper.findAll('button').find(b => b.text().includes('Suivant')).trigger('click')
    // Submit
    await wrapper.findAll('button').find(b => b.text().includes('Créer')).trigger('click')
    expect(mockPatientStore.createPatient).toHaveBeenCalled()
  })

  it('calls updatePatient on submit in edit mode', async () => {
    mockRoute.name = 'patient-edit'
    mockRoute.params = { id: 'p1' }
    mockPatientStore.getPatientById.mockImplementation(async () => {
      mockPatientStore.currentPatient = {
        id: 'p1', firstName: 'Marc', lastName: 'Dubois',
        birthDate: '1972-11-22', gender: 'M',
        phone: '', address: '', socialSecurityNumber: '1721167098023',
        referringDoctor: '', medicalHistory: []
      }
    })
    mockPatientStore.updatePatient.mockResolvedValue({ id: 'p1' })

    const wrapper = createWrapper()
    await flushPromises()

    // Navigate to step 3
    await wrapper.findAll('button').find(b => b.text().includes('Suivant')).trigger('click')
    await wrapper.findAll('button').find(b => b.text().includes('Suivant')).trigger('click')
    // Submit
    await wrapper.findAll('button').find(b => b.text().includes('Enregistrer')).trigger('click')
    expect(mockPatientStore.updatePatient).toHaveBeenCalledWith('p1', expect.any(Object))
  })

  it('shows error banner if API call fails', async () => {
    mockPatientStore.createPatient.mockRejectedValue(new Error('Serveur indisponible'))
    mockPatientStore.error = 'Serveur indisponible'
    const wrapper = createWrapper()
    // Navigate to step 3
    await wrapper.findAll('button').find(b => b.text().includes('Suivant')).trigger('click')
    await wrapper.findAll('button').find(b => b.text().includes('Suivant')).trigger('click')
    // Submit
    await wrapper.findAll('button').find(b => b.text().includes('Créer')).trigger('click')
    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('Serveur indisponible')
  })
})
