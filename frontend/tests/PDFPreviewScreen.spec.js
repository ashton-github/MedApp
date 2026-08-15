import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import PDFPreviewScreen from '../src/components/screens/PDFPreviewScreen.vue'
import { screens } from '../src/constants/medapp.js'

const { mockShowScreen, mockAuthUser } = vi.hoisted(() => ({
  mockShowScreen: vi.fn(),
  mockAuthUser: { value: { email: 'medecin@medapp.com', role: 'medecin' } }
}))

vi.mock('../src/composables/useMedAppState.js', () => ({
  useMedAppState: () => ({
    showScreen: mockShowScreen,
    authUser: mockAuthUser
  })
}))

const mockOrdonnanceStore = {
  currentOrdonnance: null,
  downloadPdf: vi.fn().mockResolvedValue()
}

vi.mock('../src/stores/ordonnanceStore.js', () => ({
  useOrdonnanceStore: () => mockOrdonnanceStore
}))

const mockPatientStore = {
  patients: []
}

vi.mock('../src/stores/patientStore.js', () => ({
  usePatientStore: () => mockPatientStore
}))

const sampleOrdonnance = {
  id: 'o1',
  patientId: 'p1',
  issueDate: '2026-08-10',
  validityDate: '2026-09-10',
  doctorName: 'Dr. House',
  status: 'ACTIVE',
  medications: [
    { name: 'Doliprane', dosage: '500mg', frequency: '2x/jour', duration: '3 jours' }
  ],
  notes: 'Après les repas'
}

const samplePatient = {
  id: 'p1',
  firstName: 'Ali',
  lastName: 'Ben',
  birthDate: '1990-01-01',
  phone: '+33 6 00 00 00 00'
}

const createWrapper = () =>
  mount(PDFPreviewScreen, {
    global: {
      plugins: [createPinia()],
      stubs: {
        'v-motion': { template: '<div><slot /></div>' },
        ChevronRight: true,
        Download: true,
        Printer: true,
        Stethoscope: true,
        Pill: true,
        QrCode: true
      }
    }
  })

describe('PDFPreviewScreen.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockShowScreen.mockClear()
    mockOrdonnanceStore.currentOrdonnance = null
    mockOrdonnanceStore.downloadPdf.mockClear()
    mockPatientStore.patients = []
    mockAuthUser.value = { email: 'medecin@medapp.com', role: 'medecin' }
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.unstubAllGlobals()
  })

  it('renders fallback ordonnance/patient values when no current ordonnance is set', () => {
    const wrapper = createWrapper()

    expect(wrapper.text()).toContain('Dr. Medecin')
    expect(wrapper.text()).toContain('Patient Inconnu')
    expect(wrapper.text()).toContain('Aperçu PDF')
  })

  it('renders current ordonnance and related patient information', () => {
    mockOrdonnanceStore.currentOrdonnance = sampleOrdonnance
    mockPatientStore.patients = [samplePatient]

    const wrapper = createWrapper()

    expect(wrapper.text()).toContain('Dr. House')
    expect(wrapper.text()).toContain('Ali Ben')
    expect(wrapper.text()).toContain('Doliprane')
    expect(wrapper.text()).toContain('Après les repas')
  })

  it('calls downloadPdf with current ordonnance id when Exporter PDF is clicked', async () => {
    mockOrdonnanceStore.currentOrdonnance = sampleOrdonnance
    mockPatientStore.patients = [samplePatient]

    const wrapper = createWrapper()
    const exportBtn = wrapper.findAll('button').find((b) => b.text().includes('Exporter PDF'))

    await exportBtn.trigger('click')

    expect(mockOrdonnanceStore.downloadPdf).toHaveBeenCalledWith('o1')
  })

  it('navigates back to ordonnances list when breadcrumb is clicked', async () => {
    const wrapper = createWrapper()
    const navBtn = wrapper.findAll('button').find((b) => b.text().trim() === 'Ordonnances')

    await navBtn.trigger('click')

    expect(mockShowScreen).toHaveBeenCalledWith(screens.ordonnances)
  })

  it('opens print window and triggers print flow when Imprimer is clicked', async () => {
    vi.useFakeTimers()

    const documentWrite = vi.fn()
    const documentClose = vi.fn()
    const print = vi.fn()
    const close = vi.fn()
    const focus = vi.fn()

    vi.stubGlobal('open', vi.fn(() => ({
      document: {
        write: documentWrite,
        close: documentClose
      },
      print,
      close,
      focus
    })))

    const wrapper = createWrapper()
    const printBtn = wrapper.findAll('button').find((b) => b.text().includes('Imprimer'))

    await printBtn.trigger('click')
    vi.runAllTimers()

    expect(window.open).toHaveBeenCalled()
    expect(documentWrite).toHaveBeenCalled()
    expect(focus).toHaveBeenCalled()
    expect(print).toHaveBeenCalled()
    expect(close).toHaveBeenCalled()
  })
})
