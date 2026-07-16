import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import LoginScreen from '../src/components/screens/LoginScreen.vue'

// Mock useMedAppState
const mockSignIn = vi.fn()
vi.mock('../src/composables/useMedAppState.js', () => ({
  useMedAppState: () => ({
    signIn: mockSignIn
  })
}))

describe('LoginScreen.vue', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    mockSignIn.mockClear()
  })

  afterEach(() => {
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
  })

  const createWrapper = () => {
    return mount(LoginScreen, {
      global: {
        directives: {
          motion: () => {}
        },
        stubs: {
          // Stub icons to avoid warnings or deep rendering issues
          Stethoscope: true,
          Heart: true,
          CheckCircle2: true,
          Users: true,
          Mail: true,
          Lock: true,
          ArrowRight: true,
          ChevronLeft: true,
          ClipboardList: true,
          Shield: true,
          Loader2: true,
          Check: true,
          AlertCircle: true,
          MapPin: true
        }
      }
    })
  }

  it('renders login form by default', () => {
    const wrapper = createWrapper()
    expect(wrapper.text()).toContain('Connexion')
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
  })

  it('shows error messages on invalid submission (empty fields)', async () => {
    const wrapper = createWrapper()
    
    // Clear the default email value which is 'dr.martin@medapp.fr'
    await wrapper.find('input[type="email"]').setValue('')
    await wrapper.find('input[type="password"]').setValue('')
    
    await wrapper.find('form').trigger('submit.prevent')
    
    // Check for error text
    const text = wrapper.text()
    expect(text).toContain('Email requis')
    expect(text).toContain('Mot de passe requis')
  })
  
  it('shows email invalid error on wrong email format', async () => {
    const wrapper = createWrapper()
    
    await wrapper.find('input[type="email"]').setValue('not-an-email')
    await wrapper.find('input[type="password"]').setValue('password123')
    
    await wrapper.find('form').trigger('submit.prevent')
    
    expect(wrapper.text()).toContain('Email invalide')
  })

  it('handles valid submission, shows success and redirects', async () => {
    const wrapper = createWrapper()
    
    // Set valid credentials
    await wrapper.find('input[type="email"]').setValue('dr.martin@medapp.fr')
    await wrapper.find('input[type="password"]').setValue('password123')
    
    // Ensure form is submitted
    await wrapper.find('form').trigger('submit.prevent')
    
    // Initially, there shouldn't be success text yet, it's loading
    expect(wrapper.text()).not.toContain('Connexion réussie')
    
    // Fast-forward first timeout (1400ms) for success state
    await vi.advanceTimersByTimeAsync(1400)
    
    // Now it should show success
    expect(wrapper.text()).toContain('Connexion réussie')
    
    // Fast-forward second timeout (1200ms) for redirect
    await vi.advanceTimersByTimeAsync(1200)
    
    // Ensure signIn is called with the doctor role data
    expect(mockSignIn).toHaveBeenCalledWith({
      name: 'Dr. Martin',
      role: 'doctor',
      email: 'dr.martin@medapp.fr'
    })
  })
})
