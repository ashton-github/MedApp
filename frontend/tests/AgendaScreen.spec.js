import { mount, flushPromises } from '@vue/test-utils'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import AgendaScreen from '../src/components/screens/AgendaScreen.vue'
import { createPinia, setActivePinia } from 'pinia'
import { reactive } from 'vue'

const mockRendezVous = [
  {
    id: 'rv1',
    patientId: 'p1',
    patientName: 'Dupont Marie',
    medecinId: 'm1',
    day: new Date().toISOString().split('T')[0], // Today
    time: '10:00',
    duration: 30,
    type: 'CONSULTATION',
    status: 'PLANIFIE',
    notes: ''
  }
]

const mockRendezVousStore = reactive({
  rendezVous: mockRendezVous,
  loading: false,
  fetchRendezVous: vi.fn().mockResolvedValue()
})
vi.mock('../src/stores/rendezVousStore.js', () => ({
  useRendezVousStore: () => mockRendezVousStore
}))

const mockPatientStore = reactive({
  patients: [
    { id: 'p1', firstName: 'Marie', lastName: 'Dupont' }
  ],
  fetchPatients: vi.fn().mockResolvedValue()
})
vi.mock('../src/stores/patientStore.js', () => ({
  usePatientStore: () => mockPatientStore
}))

describe('AgendaScreen.vue', () => {
  let wrapper

  beforeEach(() => {
    setActivePinia(createPinia())
    mockRendezVousStore.fetchRendezVous.mockClear()
    mockPatientStore.fetchPatients.mockClear()

    wrapper = mount(AgendaScreen, {
      global: {
        plugins: [createPinia()],
        stubs: {
          'v-motion': { template: '<div><slot /></div>' },
          Teleport: { template: '<div><slot /></div>' },
          FileText: true, Search: true, Plus: true, Pencil: true, Trash2: true,
          Calendar: true, Clock: true, User: true, FileEdit: true, CheckCircle: true, XCircle: true
        }
      }
    })
  })

  it('renders the header correctly', () => {
    expect(wrapper.find('h1').text()).toBe('Agenda')
  })

  it('renders appointments for today', () => {
    // The appointment for today should be visible in the grid
    const apptBlocks = wrapper.findAll('.group.relative')
    expect(apptBlocks.length).toBeGreaterThan(0)
    expect(wrapper.html()).toContain('Dupont Marie')
    expect(wrapper.html()).toContain('10:00')
  })
})
