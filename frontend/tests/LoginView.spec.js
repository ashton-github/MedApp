import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import LoginScreen from '../src/components/screens/LoginScreen.vue'
import { useAuthStore } from '../src/stores/authStore.js'

// Mock the authStore so tests do not make real HTTP calls
vi.mock('../src/stores/authStore.js', () => ({
  useAuthStore: vi.fn()
}))

// Mock useMedAppState (used by authStore.login indirectly, but also maybe in components if any)
const mockSignIn = vi.fn()
vi.mock('../src/composables/useMedAppState.js', () => ({
  useMedAppState: () => ({
    signIn: mockSignIn
  })
}))

describe('LoginScreen.vue', () => {
  let mockLogin

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.useFakeTimers()
    mockSignIn.mockClear()

    mockLogin = vi.fn()
    useAuthStore.mockReturnValue({
      login: mockLogin
    })
  })

  afterEach(() => {
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
  })

  const createWrapper = () => {
    return mount(LoginScreen, {
      global: {
        directives: {
          motion: () => { }
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
          ClipboardList: true,
          Shield: true,
          Loader2: true,
          AlertCircle: true,
          Eye: true,
          EyeOff: true
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

    await wrapper.find('input[type="email"]').setValue('')
    // Password is empty by default

    await wrapper.find('form').trigger('submit.prevent')

    // Check for error text
    const text = wrapper.text()
    expect(text).toContain('Email requis')
    expect(text).toContain('Mot de passe requis')
    expect(mockLogin).not.toHaveBeenCalled()
  })

  it('shows email invalid error on wrong email format', async () => {
    const wrapper = createWrapper()

    await wrapper.find('input[type="email"]').setValue('not-an-email')
    await wrapper.find('input[id="login-password"]').setValue('password123')

    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.text()).toContain('Email invalide')
    expect(mockLogin).not.toHaveBeenCalled()
  })

  it('handles valid submission and shows success', async () => {
    const wrapper = createWrapper()
    mockLogin.mockResolvedValueOnce()

    await wrapper.find('input[type="email"]').setValue('dr.martin@medapp.fr')
    await wrapper.find('input[id="login-password"]').setValue('password123')

    await wrapper.find('form').trigger('submit.prevent')

    // Check that login action is called
    expect(mockLogin).toHaveBeenCalledWith('dr.martin@medapp.fr', 'password123')

    await vi.runAllTimersAsync()
    await wrapper.vm.$nextTick()

    // Now it should show success
    expect(wrapper.text()).toContain('Connexion réussie')
  })

  it('displays server error for 401 Unauthorized', async () => {
    const wrapper = createWrapper()
    mockLogin.mockRejectedValueOnce({
      response: { status: 401, data: { message: 'Email ou mot de passe incorrect.' } }
    })

    await wrapper.find('input[type="email"]').setValue('dr.martin@medapp.fr')
    await wrapper.find('input[id="login-password"]').setValue('password123')

    await wrapper.find('form').trigger('submit.prevent')

    await vi.runAllTimersAsync()
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Email ou mot de passe incorrect.')
  })

  it('displays server error for 403 Forbidden', async () => {
    const wrapper = createWrapper()
    mockLogin.mockRejectedValueOnce({
      response: { status: 403, data: { message: 'Ce compte est désactivé. Contactez un administrateur.' } }
    })

    await wrapper.find('input[type="email"]').setValue('disabled@medapp.fr')
    await wrapper.find('input[id="login-password"]').setValue('password123')

    await wrapper.find('form').trigger('submit.prevent')

    await vi.runAllTimersAsync()
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Ce compte est désactivé. Contactez un administrateur.')
  })

  it('toggles password visibility when the eye button is clicked', async () => {
    const wrapper = createWrapper()
    const passwordInput = wrapper.find('#login-password')
    const toggleButton = wrapper.find('#login-toggle-password')

    // Initially type is password
    expect(passwordInput.attributes('type')).toBe('password')

    // Click toggle
    await toggleButton.trigger('click')

    // Now type is text
    expect(passwordInput.attributes('type')).toBe('text')

    // Click again
    await toggleButton.trigger('click')

    // Back to password
    expect(passwordInput.attributes('type')).toBe('password')
  })
})
