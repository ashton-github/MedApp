import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import DashboardScreen from '../../../src/components/screens/DashboardScreen.vue'

const mockDashboardState = {
    loading: false,
    statistics: {
        patientsToday: 2,
        activePrescriptions: 3,
        appointmentsThisWeek: 4,
        activePatients: 12
    },
    recentPatients: [
        { id: '1', firstName: 'Sara', lastName: 'Trabelsi', createdAt: '2026-08-18' }
    ],
    prescriptionStats: [
        { name: 'Actives', v: 3, c: '#10B981' },
        { name: 'Expirées', v: 1, c: '#EF4444' },
        { name: 'Archivées', v: 0, c: '#8B5CF6' }
    ],
    prescriptionSegments: [
        { name: 'Actives', v: 3, c: '#10B981', dash: '80 251', offset: 0 }
    ],
    prescriptionTotal: 4,
    activityChart: {
        consultations: Array.from({ length: 12 }, (_, i) => ({ x: i * 50, y: i, value: i })),
        prescriptions: Array.from({ length: 12 }, (_, i) => ({ x: i * 50, y: 20 - i, value: i })),
        months: ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Juin', 'Juil', 'Août', 'Sep', 'Oct', 'Nov', 'Déc']
    },
    activity: [
        { id: 'a1', type: 'patient', text: 'Nouveau patient · Sara Trabelsi', time: new Date() }
    ],
    fetchDashboard: vi.fn()
}

vi.mock('../../../src/stores/dashboardStore.js', () => ({
    useDashboardStore: () => mockDashboardState
}))

vi.mock('../../../src/composables/useMedAppState.js', () => ({
    useMedAppState: () => ({ showScreen: vi.fn() })
}))

vi.mock('../../../src/constants/medapp.js', () => ({
    screens: { patients: 'patients', agenda: 'agenda', patientForm: 'patientForm' }
}))

describe('DashboardScreen', () => {
    beforeEach(() => setActivePinia(createPinia()))

    const mountDashboard = () => mount(DashboardScreen, {
        global: {
            stubs: {
                Calendar: true,
                Users: true,
                FileText: true,
                Activity: true,
                ArrowRight: true,
                UserPlus: true,
                Pencil: true
            },
            directives: { motion: {} }
        }
    })

    it('renders the welcome message', () => {
        const wrapper = mountDashboard()
        expect(wrapper.text()).toContain('Bonjour')
        expect(wrapper.text()).not.toContain('@')
    })

    it('renders KPIs, activity chart, and valid patient dates', () => {
        const wrapper = mountDashboard()
        expect(wrapper.text()).toContain("Patients aujourd'hui")
        expect(wrapper.text()).toContain('2')
        expect(wrapper.text()).toContain('Activité médicale')
        expect(wrapper.text()).toContain('Sara Trabelsi')
        expect(wrapper.text()).toContain('18 août 2026')
        expect(wrapper.text()).not.toContain('Invalid Date')
    })
})