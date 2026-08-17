import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import RegisterView from '../src/components/screens/RegisterView.vue'

// Mock authStore so tests do not make real HTTP calls
vi.mock('../src/stores/authStore.js', () => ({
  useAuthStore: () => ({
    register: vi.fn()
  })
}))

// Mock api.js (used transitively by authStore)
vi.mock('../src/services/api.js', () => ({
  default: {},
  setAccessToken: vi.fn(),
  clearAccessToken: vi.fn(),
  getAccessToken: vi.fn(),
  ROLE_MAP: { medecin: 'MEDECIN', secretaire: 'SECRETAIRE', admin: 'ADMIN' },
  ROLE_MAP_REVERSE: { MEDECIN: 'medecin', SECRETAIRE: 'secretaire', ADMIN: 'admin' }
}))

describe('RegisterView.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
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
          motion: () => { }
        },
        stubs: {
          Stethoscope: true,
          ClipboardList: true,
          Shield: true,
          ChevronLeft: true,
          Mail: true,
          Lock: true,
          Loader2: true,
          Check: true,
          CheckCircle2: true,
          AlertCircle: true,
          Eye: true,
          EyeOff: true
        }
      }
    })
  }

  // Helper to fill all fields with valid data (matches backend password regex)
  const fillValidForm = async (wrapper) => {
    const inputs = wrapper.findAll('input')
    // firstName, lastName, email, password, confirm
    await inputs[0].setValue('Jean')
    await inputs[1].setValue('Martin')
    await inputs[2].setValue('jean.martin@clinique.fr')
    await inputs[3].setValue('Motdepasse1') // ≥8 chars, 1 uppercase, 1 lowercase, 1 digit
    await inputs[4].setValue('Motdepasse1')
  }

  it('renders register form by default', () => {
    const wrapper = createWrapper()
    expect(wrapper.text()).toContain('Créer un compte')
    expect(wrapper.find('form').exists()).toBe(true)
  })

  it('emits backToLogin event when clicking top Retour button', async () => {
    const wrapper = createWrapper()
    const buttons = wrapper.findAll('button')
    await buttons[0].trigger('click')
    expect(wrapper.emitted()).toHaveProperty('backToLogin')
  })

  it('emits backToLogin event when clicking bottom Annuler button', async () => {
    const wrapper = createWrapper()
    const buttons = wrapper.findAll('button')
    const annulerBtn = buttons.find(b => b.text().includes('Annuler'))
    await annulerBtn.trigger('click')
    expect(wrapper.emitted()).toHaveProperty('backToLogin')
  })

  // --- Validation tests ---

  it('shows validation errors when submitting with all empty fields', async () => {
    const wrapper = createWrapper()
    await wrapper.find('form').trigger('submit')

    expect(wrapper.text()).toContain('Le prénom est requis.')
    expect(wrapper.text()).toContain('Le nom est requis.')
    expect(wrapper.text()).toContain("L'email est requis.")
    expect(wrapper.text()).toContain('Le mot de passe est requis.')
    expect(wrapper.text()).toContain('Veuillez confirmer le mot de passe.')
  })

  it('shows an error for invalid email format', async () => {
    const wrapper = createWrapper()
    const inputs = wrapper.findAll('input')
    await inputs[2].setValue('email-invalide')
    await wrapper.find('form').trigger('submit')

    expect(wrapper.text()).toContain("L'adresse email n'est pas valide.")
  })

  it('shows an error if password does not meet complexity requirements', async () => {
    const wrapper = createWrapper()
    const inputs = wrapper.findAll('input')
    // Fill required fields to isolate the password-complexity error
    await inputs[0].setValue('Jean')
    await inputs[1].setValue('Martin')
    await inputs[2].setValue('jean.martin@clinique.fr')
    await inputs[3].setValue('toocourt') // no uppercase, no digit
    await wrapper.find('form').trigger('submit')

    expect(wrapper.text()).toContain(
      'Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre.'
    )
  })

  it('accepts a password that satisfies the backend regex (≥8, uppercase, lowercase, digit)', async () => {
    const wrapper = createWrapper()
    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('Jean')
    await inputs[1].setValue('Martin')
    await inputs[2].setValue('jean.martin@clinique.fr')
    await inputs[3].setValue('Motdepasse1') // valid
    await inputs[4].setValue('Motdepasse1')
    await wrapper.find('form').trigger('submit')

    // No password error should appear
    expect(wrapper.text()).not.toContain(
      'Le mot de passe doit contenir au moins 8 caractères'
    )
  })

  it('shows an error if passwords do not match', async () => {
    const wrapper = createWrapper()
    const inputs = wrapper.findAll('input')
    await inputs[3].setValue('Motdepasse1')   // password
    await inputs[4].setValue('Autremdp1!')    // confirm (different)
    await wrapper.find('form').trigger('submit')

    expect(wrapper.text()).toContain('Les mots de passe ne correspondent pas.')
  })

  it('does not show success when form is invalid', async () => {
    const wrapper = createWrapper()
    await wrapper.find('form').trigger('submit')
    await vi.advanceTimersByTimeAsync(1400)

    // Success should not appear because the form is invalid
    expect(wrapper.text()).not.toContain('Demande envoyée !')
    expect(wrapper.find('form').exists()).toBe(true)
  })

  it('handles valid submission and shows success screen', async () => {
    const wrapper = createWrapper()

    // Ensure form is submitted with valid data
    await fillValidForm(wrapper)
    await wrapper.find('form').trigger('submit')

    // Wait for the async register call to resolve
    await vi.runAllTimersAsync()
    await wrapper.vm.$nextTick()

    // Success screen should now be visible
    expect(wrapper.text()).toContain('Demande envoyée !')
    expect(wrapper.find('form').exists()).toBe(false)

    // Click the "Retour à la connexion" button
    const returnBtn = wrapper.find('button')
    await returnBtn.trigger('click')

    expect(wrapper.emitted()).toHaveProperty('backToLogin')
  })

  it('toggles password visibility when the eye button is clicked for password field', async () => {
    const wrapper = createWrapper()
    const passwordInput = wrapper.find('#reg-password')
    const toggleButton = wrapper.find('#reg-toggle-password')

    expect(passwordInput.attributes('type')).toBe('password')
    await toggleButton.trigger('click')
    expect(passwordInput.attributes('type')).toBe('text')
    await toggleButton.trigger('click')
    expect(passwordInput.attributes('type')).toBe('password')
  })

  it('toggles password visibility when the eye button is clicked for confirm field', async () => {
    const wrapper = createWrapper()
    const confirmInput = wrapper.find('#reg-confirm-password')
    const toggleButton = wrapper.find('#reg-toggle-confirm')

    expect(confirmInput.attributes('type')).toBe('password')
    await toggleButton.trigger('click')
    expect(confirmInput.attributes('type')).toBe('text')
    await toggleButton.trigger('click')
    expect(confirmInput.attributes('type')).toBe('password')
  })
})
