import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import PatientsScreen from '../src/components/screens/PatientsScreen.vue'

// Mock useMedAppState with vi.hoisted
const { mockShowScreen, mockOpenNewPatient, mockEditPatient } = vi.hoisted(() => {
  return {
    mockShowScreen: vi.fn(),
    mockOpenNewPatient: vi.fn(),
    mockEditPatient: vi.fn()
  }
})

vi.mock('../src/composables/useMedAppState.js', () => ({
  useMedAppState: () => ({
    showScreen: mockShowScreen,
    openNewPatient: mockOpenNewPatient,
    editPatient: mockEditPatient
  })
}))

describe('PatientsScreen.vue', () => {
  beforeEach(() => {
    mockShowScreen.mockClear()
    mockOpenNewPatient.mockClear()
    mockEditPatient.mockClear()
    vi.restoreAllMocks()
  })

  const createWrapper = () => {
    return mount(PatientsScreen, {
      global: {
        directives: {
          motion: () => { }
        },
        stubs: {
          // Stub icons to avoid warnings
          Users: true,
          Search: true,
          Filter: true,
          Plus: true,
          LayoutGrid: true,
          List: true,
          Eye: true,
          Pencil: true,
          Phone: true,
          Mail: true,
          Trash2: true
        }
      }
    })
  }

  it('renders patients screen with a title', () => {
    const wrapper = createWrapper()
    expect(wrapper.text()).toContain('Patients')
    expect(wrapper.text()).toContain('Nouveau patient')
  })

  it('filters patients based on search input', async () => {
    const wrapper = createWrapper()
    const searchInput = wrapper.find('input[type="search"]')
    
    expect(wrapper.text()).toContain('Sophie Laurent')
    
    await searchInput.setValue('Amira')
    expect(wrapper.text()).toContain('Amira Benali')
    expect(wrapper.text()).not.toContain('Sophie Laurent')
  })

  it('calls openNewPatient when new patient button is clicked', async () => {
    const wrapper = createWrapper()
    const newPatientBtn = wrapper.findAll('button').find(btn => btn.text().includes('Nouveau patient'))
    
    await newPatientBtn.trigger('click')
    expect(mockOpenNewPatient).toHaveBeenCalled()
  })

  it('opens delete confirmation modal when trash icon is clicked', async () => {
    const wrapper = createWrapper()
    
    // Check that modal is not open initially
    expect(wrapper.text()).not.toContain('Supprimer le patient')

    // Find the delete button for the first patient (grid view by default)
    const deleteBtn = wrapper.find('button[title="Supprimer"]')
    expect(deleteBtn.exists()).toBe(true)

    // Click delete button
    await deleteBtn.trigger('click')

    // Modal should now be open
    expect(wrapper.text()).toContain('Supprimer le patient')
    expect(wrapper.text()).toContain('Êtes-vous sûr de vouloir supprimer ce patient')
  })

  it('switches between grid and list view', async () => {
    const wrapper = createWrapper()
    
    // Grid view is default
    expect(wrapper.find('.grid.grid-cols-1').exists()).toBe(true)
    
    // Switch to list view (second button in the layout group)
    const listBtns = wrapper.findAll('button').filter(b => b.html().toLowerCase().includes('list'))
    if (listBtns.length > 0) {
      await listBtns[0].trigger('click')
      // List view uses a table
      expect(wrapper.find('table').exists()).toBe(true)
    }
  })

  it('calls editPatient when pencil icon is clicked', async () => {
    const wrapper = createWrapper()
    
    // Grid view is default, pencil icon is now present here too
    const editBtn = wrapper.find('button[title="Modifier"]')
    await editBtn.trigger('click')
    
    expect(mockEditPatient).toHaveBeenCalled()
  })
})
