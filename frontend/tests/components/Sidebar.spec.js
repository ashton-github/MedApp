import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import Sidebar from '../src/components/Sidebar.vue'
import { screens } from '../../src/constants/medapp.js'

const mockShowScreen = vi.fn()
const mockLogout = vi.fn()
const mockAuthUser = ref({ name: 'Dr. House', email: 'house@medapp.fr', role: 'medecin' })
const mockCurrentScreen = ref(screens.dashboard)

vi.mock('../src/composables/useMedAppState.js', () => ({
  useMedAppState: () => ({
    currentScreen: mockCurrentScreen,
    showScreen: mockShowScreen,
    authUser: mockAuthUser,
    logout: mockLogout
  })
}))

describe('Sidebar.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockAuthUser.value = { name: 'Dr. House', email: 'house@medapp.fr', role: 'medecin' }
    mockCurrentScreen.value = screens.dashboard
  })

  const createWrapper = (props = { collapsed: false }) => {
    return mount(Sidebar, {
      props,
      global: {
        directives: {
          motion: () => {} // mock v-motion directive
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

  it('renders Demandes d\'accès for admin role', () => {
    mockAuthUser.value.role = 'admin'
    const wrapper = createWrapper()
    
    expect(wrapper.text()).toContain("Demandes d'accès")
  })

  it('renders user details and correct role label dynamically', () => {
    const wrapper = createWrapper()
    const text = wrapper.text()
    
    // Check if name is displayed
    expect(text).toContain('Dr. House')
    // Check if the computed roleLabel is correct
    expect(text).toContain('Médecin')
    // Check if initials are generated correctly (first letter)
    expect(text).toContain('D')
  })

  it('renders email as fallback and extracts initials if name is missing', () => {
    mockAuthUser.value = { name: '', email: 'house@medapp.fr', role: 'secretaire' }
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

  it('calls logout when the logout button is clicked', async () => {
    const wrapper = createWrapper()
    const logoutBtn = wrapper.findAll('button').find(b => b.attributes('title') === 'Déconnexion')
    await logoutBtn.trigger('click')
    
    expect(mockLogout).toHaveBeenCalledOnce()
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
    // The user name and role should be hidden
    expect(text).not.toContain('Dr. House')
    expect(text).not.toContain('Médecin')
    
    // The initials should still be visible inside the circular avatar
    expect(text).toContain('D')
  })
})
