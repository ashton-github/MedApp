import { mount, flushPromises } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import OrdonnanceFormScreen from '../../../src/components/screens/OrdonnanceFormScreen.vue'
import { screens } from '../../../src/constants/medapp.js'
import { useAuthStore } from '../../../src/stores/authStore.js'

// The component now derives edit-mode/patient id from the route itself
// (/ordonnances/:id/modifier, /ordonnances/nouvelle?patientId=...) rather
// than from data held in memory. We control the route via a mutable object.
const { mockRoute, mockPush, mockShowScreen } = vi.hoisted(() => ({
  mockRoute: { name: 'ordonnance-new', params: {}, query: {} },
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
    showScreen: mockShowScreen,
    selectedPatientId: {
      get value() {
        if (mockRoute.name === 'ordonnance-new' && mockRoute.query.patientId) {
          return mockRoute.query.patientId
        }
        return null
      }
    }
  })
}))

vi.mock('../../../src/services/api.js', () => ({
  default: {
    post: vi.fn().mockResolvedValue({ data: {} }),
    interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } }
  },
  setAccessToken: vi.fn(),
  clearAccessToken: vi.fn(),
  getAccessToken: vi.fn(() => null),
  ROLE_MAP: { medecin: 'MEDECIN', secretaire: 'SECRETAIRE' },
  ROLE_MAP_REVERSE: { MEDECIN: 'medecin', SECRETAIRE: 'secretaire' }
}))

const mockPatientStore = {
  patients: [],
  currentPatient: null,
  fetchPatients: vi.fn().mockResolvedValue(),
  getPatientById: vi.fn().mockResolvedValue()
}
vi.mock('../../../src/stores/patientStore.js', () => ({
  usePatientStore: () => mockPatientStore
}))

const mockDoctorStore = {
  doctors: [],
  fetchDoctors: vi.fn().mockResolvedValue(),
  getDoctorFullName: vi.fn(() => 'Dr. Greg House')
}
vi.mock('../../../src/stores/doctorStore.js', () => ({
  useDoctorStore: () => mockDoctorStore
}))

const mockOrdonnanceStore = {
  currentOrdonnance: null,
  createOrdonnance: vi.fn().mockResolvedValue({ id: 'o-new' }),
  updateOrdonnance: vi.fn().mockResolvedValue({ id: 'o1' }),
  getOrdonnanceById: vi.fn().mockResolvedValue()
}
vi.mock('../../../src/stores/ordonnanceStore.js', () => ({
  useOrdonnanceStore: () => mockOrdonnanceStore
}))

const samplePatient = {
  id: 'p1',
  firstName: 'Ali',
  lastName: 'Ben',
  birthDate: '1990-01-01',
  gender: 'M'
}

const sampleEditOrdonnance = {
  id: 'o1',
  patientId: 'p1',
  issueDate: '2026-08-10',
  validityDate: '2026-09-10',
  notes: 'Réduire le sel',
  medications: [
    { name: 'Doliprane', dosage: '500mg', frequency: '2x/jour', duration: '3 jours' }
  ]
}

let pinia

const createWrapper = () =>
  mount(OrdonnanceFormScreen, {
    global: {
      plugins: [pinia],
      stubs: {
        'v-motion': { template: '<div><slot /></div>' },
        ChevronLeft: true, ChevronRight: true, CheckCircle2: true, Check: true,
        Search: true, Plus: true, Trash2: true, Loader2: true,
        Calendar: true, AlertCircle: true, Eye: true, FileCheck: true
      }
    }
  })

describe('OrdonnanceFormScreen.vue', () => {
  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    const authStore = useAuthStore()
    authStore.role = 'medecin'
    authStore.user = { email: 'medecin@medapp.com' }

    mockRoute.name = 'ordonnance-new'
    mockRoute.params = {}
    mockRoute.query = {}
    mockShowScreen.mockClear()
    mockPush.mockClear()
    mockPatientStore.patients = [samplePatient]
    mockPatientStore.currentPatient = null
    mockPatientStore.fetchPatients.mockClear()
    mockPatientStore.getPatientById.mockClear()
    mockOrdonnanceStore.currentOrdonnance = null
    mockOrdonnanceStore.createOrdonnance.mockClear()
    mockOrdonnanceStore.updateOrdonnance.mockClear()
    mockOrdonnanceStore.getOrdonnanceById.mockClear()
    mockDoctorStore.fetchDoctors.mockClear()
  })

  it('prefills selected patient in creation mode when selectedPatientId is provided', async () => {
    mockRoute.query = { patientId: 'p1' }
    const wrapper = createWrapper()
    await flushPromises()

    expect(wrapper.text()).toContain('Ali Ben')
    expect(wrapper.text()).toContain('Patient sélectionné')
  })

  it('builds a coherent preview payload from current form data', async () => {
    mockRoute.query = { patientId: 'p1' }
    const wrapper = createWrapper()
    await flushPromises()

    const medNameInput = wrapper.find('input[placeholder="ex: Doliprane 1000mg comprimés"]')
    await medNameInput.setValue('Amoxicilline')

    const previewBtn = wrapper.findAll('button').find((b) => b.text().includes('Aperçu PDF'))
    await previewBtn.trigger('click')

    expect(mockShowScreen).toHaveBeenCalledWith(screens.pdfPreview)
    expect(mockOrdonnanceStore.currentOrdonnance).toMatchObject({
      patientId: 'p1',
      patientName: 'Ali Ben',
      medications: [
        expect.objectContaining({ name: 'Amoxicilline' })
      ]
    })
  })

  it('loads edit mode and submits with updateOrdonnance', async () => {
    mockRoute.name = 'ordonnance-edit'
    mockRoute.params = { id: 'o1' }
    mockOrdonnanceStore.getOrdonnanceById.mockImplementation(async () => {
      mockOrdonnanceStore.currentOrdonnance = sampleEditOrdonnance
    })

    const wrapper = createWrapper()
    await flushPromises()

    expect(mockOrdonnanceStore.getOrdonnanceById).toHaveBeenCalledWith('o1')
    expect(wrapper.text()).toContain('Modifier ordonnance')
    expect(wrapper.find('input[placeholder="ex: 1000mg"]').element.value).toBe('500mg')

    const submitBtn = wrapper.findAll('button').find((b) => b.text().includes('Mettre à jour'))
    await submitBtn.trigger('click')
    await flushPromises()

    expect(mockOrdonnanceStore.updateOrdonnance).toHaveBeenCalledWith(
      'o1',
      expect.objectContaining({
        patientId: 'p1',
        notes: 'Réduire le sel'
      })
    )
    expect(mockOrdonnanceStore.createOrdonnance).not.toHaveBeenCalled()
  })
})
