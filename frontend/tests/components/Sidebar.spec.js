import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import Sidebar from '../../src/components/Sidebar.vue'
import { screens } from '../../src/constants/medapp.js'
import { useAuthStore } from '../../src/stores/authStore.js'

const mockShowScreen = vi.fn()
const mockCurrentScreen = ref(screens.dashboard)

vi.mock('../../src/composables/useMedAppState.js', () => ({
  useMedAppState: () => ({
    currentScreen: mockCurrentScreen,
    showScreen: mockShowScreen
  })
}))

// authStore.logout makes a real API call internally — mock the api module
// so the store's action can be called safely in isolation.
vi.mock('../../src/services/api.js', () => {
  const mockApi = {
    post: vi.fn().mockResolvedValue({ data: {} }),
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() }
    }
  }
  return {
    default: mockApi,
    setAccessToken: vi.fn(),
    clearAccessToken: vi.fn(),
    getAccessToken: vi.fn(() => null),
    ROLE_MAP: { medecin: 'MEDECIN', secretaire: 'SECRETAIRE' },
    ROLE_MAP_REVERSE: { MEDECIN: 'medecin', SECRETAIRE: 'secretaire' }
  }
})

describe('Sidebar.vue', () => {
  let authStore

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    mockCurrentScreen.value = screens.dashboard

    authStore = useAuthStore()
    authStore.user = { email: 'house@medapp.fr' }
    authStore.role = 'medecin'
  })

  const createWrapper = (props = { collapsed: false }) => {
    return mount(Sidebar, {
      props,
      global: {
        directives: {
          motion: () => { } // mock v-motion directive
        },
        stubs: {
          LayoutDashboard: true,
          Users: true,
          FileText: true,
          Settings: true,
          LogOut: true,
          Stethoscope: true,
          ChevronLeft: true,
          Calendar: true,
          UserPlus: true
        }
      }
    })
  }

  it('renders default navigation items for a doctor', () => {
    const wrapper = createWrapper()
    const text = wrapper.text()

    expect(text).toContain('Tableau de bord')
    expect(text).toContain('Patients')
    expect(text).toContain('Ordonnances')
    expect(text).toContain('Agenda')
    expect(text).toContain('Paramètres')

    // Admin screen should not be visible
    expect(text).not.toContain("Demandes d'accès")
  })


  it('renders user details and correct role label dynamically', () => {
    const wrapper = createWrapper()
    const text = wrapper.text()

    // Check if email is displayed (Sidebar shows authStore.user.email)
    expect(text).toContain('house@medapp.fr')
    // Check if the computed roleLabel is correct
    expect(text).toContain('Médecin')
    // Check if initials are generated correctly (first letter of email)
    expect(text).toContain('H')
  })

  it('renders correct role label for secretaire', () => {
    authStore.user = { email: 'house@medapp.fr' }
    authStore.role = 'secretaire'
    const wrapper = createWrapper()
    const text = wrapper.text()

    expect(text).toContain('house@medapp.fr')
    expect(text).toContain('Secrétaire')
    expect(text).toContain('H') // First letter of email
  })

  it('emits "toggle" event when collapse button is clicked', async () => {
    const wrapper = createWrapper()
    const buttons = wrapper.findAll('button')
    // The toggle button is the first button in the template
    await buttons[0].trigger('click')

    expect(wrapper.emitted()).toHaveProperty('toggle')
  })

  it('calls authStore.logout when the logout button is clicked', async () => {
    const logoutSpy = vi.spyOn(authStore, 'logout').mockResolvedValue()
    const wrapper = createWrapper()
    const logoutBtn = wrapper.findAll('button').find(b => b.attributes('title') === 'Déconnexion')
    await logoutBtn.trigger('click')

    expect(logoutSpy).toHaveBeenCalledOnce()
  })

  it('calls showScreen when a navigation item is clicked', async () => {
    const wrapper = createWrapper()
    // Find the button for "Patients"
    const patientBtn = wrapper.findAll('button').find(b => b.text().includes('Patients'))
    await patientBtn.trigger('click')

    expect(mockShowScreen).toHaveBeenCalledWith(screens.patients)
  })

  it('hides text when collapsed is true', () => {
    const wrapper = createWrapper({ collapsed: true })
    const text = wrapper.text()

    // The main app title should be hidden
    expect(text).not.toContain('MedApp')
    // The navigation labels should be hidden
    expect(text).not.toContain('Tableau de bord')
    // The user email and role should be hidden
    expect(text).not.toContain('house@medapp.fr')
    expect(text).not.toContain('Médecin')

    // The initials should still be visible inside the circular avatar
    expect(text).toContain('H')
  })
})
