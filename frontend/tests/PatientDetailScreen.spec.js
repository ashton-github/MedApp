import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import PatientDetailScreen from '../src/components/screens/PatientDetailScreen.vue'

// Mock useMedAppState using vi.hoisted so it works inside vi.mock
const { mockShowScreen, mockAuthUser, mockEditPatient } = vi.hoisted(() => {
  return {
    mockShowScreen: vi.fn(),
    mockAuthUser: { value: { role: 'medecin' } },
    mockEditPatient: vi.fn()
  }
})

vi.mock('../src/composables/useMedAppState.js', () => ({
  useMedAppState: () => ({
    showScreen: mockShowScreen,
    authUser: mockAuthUser,
    editPatient: mockEditPatient
  })
}))

describe('PatientDetailScreen.vue', () => {
  beforeEach(() => {
    mockShowScreen.mockClear()
    mockEditPatient.mockClear()
    mockAuthUser.value = { role: 'medecin' }
  })

  const createWrapper = () => {
    return mount(PatientDetailScreen, {
      global: {
        directives: {
          motion: () => { }
        },
        stubs: {
          ChevronRight: true,
          Pencil: true,
          Plus: true,
          Phone: true,
          Mail: true,
          Shield: true,
          Heart: true,
          Lock: true,
          Activity: true,
          Trash2: true,
          FileText: true
        }
      }
    })
  }

  it('renders patient name and basic info', () => {
    const wrapper = createWrapper()
    expect(wrapper.text()).toContain('Sophie Laurent')
    expect(wrapper.text()).toContain('CPAM Paris')
  })

  it('renders tabs and switches content when a tab is clicked', async () => {
    const wrapper = createWrapper()

    // Overview is active by default
    expect(wrapper.text()).toContain('Informations médicales')

    // Switch to Prescriptions (Ordonnances) tab
    const tabs = wrapper.findAll('button')
    const prescriptionsTab = tabs.find(b => b.text().includes('Ordonnances'))
    await prescriptionsTab.trigger('click')

    // Check prescription content
    expect(wrapper.text()).toContain('Amoxicilline 1g')

    // Switch to History (Historique) tab
    const historyTab = tabs.find(b => b.text().includes('Historique'))
    await historyTab.trigger('click')

    expect(wrapper.text()).toContain('Historique médical')
  })

  it('opens delete confirmation modal when delete button is clicked', async () => {
    const wrapper = createWrapper()
    
    expect(wrapper.text()).not.toContain('Êtes-vous sûr de vouloir supprimer')

    const deleteBtn = wrapper.findAll('button').find(b => b.text().includes('Supprimer'))
    await deleteBtn.trigger('click')

    expect(wrapper.text()).toContain('Êtes-vous sûr de vouloir supprimer Sophie Laurent')
  })

  it('calls editPatient when modifier button is clicked', async () => {
    const wrapper = createWrapper()
    
    const editBtn = wrapper.findAll('button').find(b => b.text().includes('Modifier'))
    await editBtn.trigger('click')
    
    expect(mockEditPatient).toHaveBeenCalled()
  })

  it('hides some buttons and info if user is not doctor', async () => {
    mockAuthUser.value = { role: 'secretaire' }
    const wrapper = createWrapper()

    // Should not see the Ordonnance button (be careful not to match the 'Ordonnances' tab)
    const ordonnanceBtn = wrapper.findAll('button').find(b => b.text().trim() === 'Ordonnance')
    expect(ordonnanceBtn).toBeUndefined()

    // Blood type should be masked
    expect(wrapper.text()).toContain('Restreint')
  })
})
