import { mount, flushPromises } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import OrdonnancesScreen from '../src/components/screens/OrdonnancesScreen.vue'
import { screens } from '../src/constants/medapp.js'

const {
  mockAuthUser,
  mockOpenNewOrdonnance,
  mockOpenEditOrdonnance,
  mockShowScreen
} = vi.hoisted(() => ({
  mockAuthUser: { value: { role: 'medecin', email: 'medecin@medapp.com' } },
  mockOpenNewOrdonnance: vi.fn(),
  mockOpenEditOrdonnance: vi.fn(),
  mockShowScreen: vi.fn()
}))

vi.mock('../src/composables/useMedAppState.js', () => ({
  useMedAppState: () => ({
    authUser: mockAuthUser,
    openNewOrdonnance: mockOpenNewOrdonnance,
    openEditOrdonnance: mockOpenEditOrdonnance,
    showScreen: mockShowScreen
  })
}))

const samplePatient = { id: 'p1', firstName: 'Ali', lastName: 'Ben' }
const sampleOrdonnance = {
  id: 'o1',
  patientId: 'p1',
  issueDate: '2026-08-10',
  validityDate: '2026-09-10',
  status: 'ACTIVE',
  notes: '',
  medications: [{ name: 'Doliprane', dosage: '500mg', frequency: '2x/jour', duration: '3 jours' }],
  patientName: '',
  doctorName: ''
}

const mockPatientStore = {
  patients: [samplePatient],
  fetchPatients: vi.fn().mockResolvedValue()
}
vi.mock('../src/stores/patientStore.js', () => ({
  usePatientStore: () => mockPatientStore
}))

const mockOrdonnanceStore = {
  ordonnances: [],
  loading: false,
  currentOrdonnance: null,
  fetchOrdonnancesByPatientId: vi.fn().mockResolvedValue([sampleOrdonnance]),
  archiveOrdonnance: vi.fn().mockResolvedValue(),
  downloadPdf: vi.fn().mockResolvedValue()
}
vi.mock('../src/stores/ordonnanceStore.js', () => ({
  useOrdonnanceStore: () => mockOrdonnanceStore
}))

const createWrapper = () =>
  mount(OrdonnancesScreen, {
    global: {
      plugins: [createPinia()],
      stubs: {
        'v-motion': { template: '<div><slot /></div>' },
        Teleport: true,
        FileText: true, Search: true, Plus: true, Pencil: true,
        Eye: true, Download: true, Archive: true, Pill: true
      }
    }
  })

describe('OrdonnancesScreen.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockAuthUser.value = { role: 'medecin', email: 'medecin@medapp.com' }
    mockOpenNewOrdonnance.mockClear()
    mockOpenEditOrdonnance.mockClear()
    mockShowScreen.mockClear()
    mockPatientStore.patients = [samplePatient]
    mockPatientStore.fetchPatients.mockClear()
    mockOrdonnanceStore.ordonnances = []
    mockOrdonnanceStore.loading = false
    mockOrdonnanceStore.currentOrdonnance = null
    mockOrdonnanceStore.fetchOrdonnancesByPatientId.mockClear()
    mockOrdonnanceStore.archiveOrdonnance.mockClear()
    mockOrdonnanceStore.downloadPdf.mockClear()
  })

  it('loads ordonnances on mount and renders patient name', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    expect(mockOrdonnanceStore.fetchOrdonnancesByPatientId).toHaveBeenCalledWith('p1')
    expect(wrapper.text()).toContain('Ali Ben')
    expect(wrapper.text()).toContain('Doliprane')
  })

  it('calls openNewOrdonnance when clicking Nouvelle ordonnance', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    const newBtn = wrapper.findAll('button').find((b) => b.text().includes('Nouvelle ordonnance'))
    await newBtn.trigger('click')

    expect(mockOpenNewOrdonnance).toHaveBeenCalled()
  })

  it('opens edit flow when clicking Modifier action', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    const editBtn = wrapper.find('button[title="Modifier"]')
    await editBtn.trigger('click')

    expect(mockOpenEditOrdonnance).toHaveBeenCalledWith(expect.objectContaining({ id: 'o1' }))
  })

  it('sets currentOrdonnance and navigates to preview on eye action', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    const previewBtn = wrapper.find('button[title="Aperçu"]')
    await previewBtn.trigger('click')

    expect(mockOrdonnanceStore.currentOrdonnance).toEqual(expect.objectContaining({ id: 'o1' }))
    expect(mockShowScreen).toHaveBeenCalledWith(screens.pdfPreview)
  })

  it('archives ordonnance after confirmation', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    const archiveBtn = wrapper.find('button[title="Archiver"]')
    await archiveBtn.trigger('click')

    const confirmBtn = wrapper.findAll('button').find((b) => b.text().includes('Confirmer'))
    await confirmBtn.trigger('click')
    await flushPromises()

    expect(mockOrdonnanceStore.archiveOrdonnance).toHaveBeenCalledWith('o1')
  })
})
