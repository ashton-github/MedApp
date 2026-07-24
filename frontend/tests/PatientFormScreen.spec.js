import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import PatientFormScreen from '../src/components/screens/PatientFormScreen.vue'

// Mock useMedAppState with vi.hoisted
const { mockShowScreen, mockPatientToEdit } = vi.hoisted(() => {
  return {
    mockShowScreen: vi.fn(),
    mockPatientToEdit: { value: null }
  }
})

vi.mock('../src/composables/useMedAppState.js', () => ({
  useMedAppState: () => ({
    showScreen: mockShowScreen,
    patientToEdit: mockPatientToEdit
  })
}))

describe('PatientFormScreen.vue', () => {
  beforeEach(() => {
    mockShowScreen.mockClear()
    mockPatientToEdit.value = null
    vi.useFakeTimers()
  })

  const createWrapper = () => {
    return mount(PatientFormScreen, {
      global: {
        directives: {
          motion: () => { }
        },
        stubs: {
          ChevronLeft: true,
          ChevronRight: true,
          Check: true,
          CheckCircle2: true,
          Loader2: true,
          Phone: true,
          MapPin: true,
          Shield: true
        }
      }
    })
  }

  it('renders step 1 by default', () => {
    const wrapper = createWrapper()
    expect(wrapper.text()).toContain('Nouveau patient')
    expect(wrapper.text()).toContain('Informations personnelles')
    expect(wrapper.find('input[placeholder="Sophie"]').exists()).toBe(true)
  })

  it('renders in edit mode and prepopulates form when patientToEdit is set', () => {
    mockPatientToEdit.value = {
      firstName: 'Marc',
      lastName: 'Dubois',
      phone: '+33 6 98 76 54 32'
    }
    const wrapper = createWrapper()
    
    expect(wrapper.text()).toContain('Modifier le patient')
    
    // Check if input is prepopulated
    const firstNameInput = wrapper.find('input[placeholder="Sophie"]')
    expect(firstNameInput.element.value).toBe('Marc')
    
    const lastNameInput = wrapper.find('input[placeholder="Laurent"]')
    expect(lastNameInput.element.value).toBe('Dubois')
  })

  it('can navigate to step 2 and step 3', async () => {
    const wrapper = createWrapper()
    
    // Fill required fields (optional for test, but good practice)
    await wrapper.find('input[placeholder="Sophie"]').setValue('Jean')
    
    // Go to Step 2
    const nextBtn = wrapper.findAll('button').find(b => b.text().includes('Suivant'))
    await nextBtn.trigger('click')
    
    expect(wrapper.text()).toContain('Coordonnées')
    expect(wrapper.text()).toContain('Numéro de sécurité sociale')
    
    // Go to Step 3
    const nextBtn2 = wrapper.findAll('button').find(b => b.text().includes('Suivant'))
    await nextBtn2.trigger('click')
    
    expect(wrapper.text()).toContain('Antécédents médicaux')
    expect(wrapper.text()).toContain('Médecin référent (traitant)')
  })

  it('submits form on last step and shows success message', async () => {
    const wrapper = createWrapper()
    
    // Skip to step 3
    wrapper.vm.step = 3
    await wrapper.vm.$nextTick()
    
    const submitBtn = wrapper.findAll('button').find(b => b.text().includes('Créer le patient'))
    expect(submitBtn.exists()).toBe(true)
    
    await submitBtn.trigger('click')
    
    // Fast-forward timers for the setTimeout in submit()
    await vi.runAllTimersAsync()
    await wrapper.vm.$nextTick()
    
    // Check if success view is displayed
    expect(wrapper.text()).toContain('Patient créé !')
    expect(wrapper.text()).toContain('Redirection')
  })
})
