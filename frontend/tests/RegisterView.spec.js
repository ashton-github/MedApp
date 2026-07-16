import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import RegisterView from '../src/components/screens/RegisterView.vue'

describe('RegisterView.vue', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
  })

  const createWrapper = () => {
    return mount(RegisterView, {
      global: {
        directives: {
          motion: () => {}
        },
        stubs: {
          Stethoscope: true,
          ClipboardList: true,
          Shield: true,
          ChevronLeft: true,
          Mail: true,
          MapPin: true,
          Lock: true,
          Loader2: true,
          Check: true,
          CheckCircle2: true
        }
      }
    })
  }

  it('renders register form by default', () => {
    const wrapper = createWrapper()
    expect(wrapper.text()).toContain('Créer un compte')
    expect(wrapper.find('form').exists()).toBe(true)
  })

  it('emits backToLogin event when clicking top Retour button', async () => {
    const wrapper = createWrapper()
    
    // Find the first button which is the top Retour button
    const buttons = wrapper.findAll('button')
    await buttons[0].trigger('click')
    
    expect(wrapper.emitted()).toHaveProperty('backToLogin')
  })
  
  it('emits backToLogin event when clicking bottom Annuler button', async () => {
    const wrapper = createWrapper()
    
    // Annuler button is inside form
    const buttons = wrapper.findAll('button')
    // Find the button with text containing "Annuler"
    const annulerBtn = buttons.find(b => b.text().includes('Annuler'))
    await annulerBtn.trigger('click')
    
    expect(wrapper.emitted()).toHaveProperty('backToLogin')
  })

  it('handles valid submission, shows success and allows return to login', async () => {
    const wrapper = createWrapper()
    
    // Ensure form is submitted
    await wrapper.find('form').trigger('submit.prevent')
    
    // Initially, there shouldn't be success text yet, it's loading
    expect(wrapper.text()).not.toContain('Demande envoyée !')
    
    // Fast-forward timeout (1400ms) for success state
    await vi.advanceTimersByTimeAsync(1400)
    
    // Now it should show success
    expect(wrapper.text()).toContain('Demande envoyée !')
    expect(wrapper.find('form').exists()).toBe(false)
    
    // Click "Retour à la connexion" on success screen
    const returnBtn = wrapper.find('button')
    await returnBtn.trigger('click')
    
    expect(wrapper.emitted()).toHaveProperty('backToLogin')
  })
})
