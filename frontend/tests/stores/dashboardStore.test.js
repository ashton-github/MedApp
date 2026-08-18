import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

import { useDashboardStore } from '../../src/stores/dashboardStore.js'

// -----------------------------------------------------------------------------
// Helpers
// -----------------------------------------------------------------------------

const getLocalDateString = (offsetDays = 0) => {
    const d = new Date()
    d.setDate(d.getDate() + offsetDays)

    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')

    return `${year}-${month}-${day}`
}

const todayStr = getLocalDateString(0)
const yesterdayStr = getLocalDateString(-1)

// -----------------------------------------------------------------------------
// Mock backend responses
//
// IMPORTANT:
// These objects intentionally use the BACKEND field names because the real
// patientStore / ordonnanceStore / rendezVousStore perform the mapping.
// -----------------------------------------------------------------------------

const apiMocks = {
    patients: [
        {
            id: '1',
            nom: 'Ben Ali',
            prenom: 'Ali',
            dateNaissance: '1990-01-01',
            sexe: 'M',
            telephone: '20000000',
            adresse: 'Tunis',
            numeroSecuriteSociale: '123456',
            antecedents: [],
            medecinReferent: null,
            dateCreation: todayStr,
            dateMiseAJour: todayStr
        },
        {
            id: '2',
            nom: 'Trabelsi',
            prenom: 'Sara',
            dateNaissance: '1992-05-10',
            sexe: 'F',
            telephone: '21000000',
            adresse: 'Ariana',
            numeroSecuriteSociale: '654321',
            antecedents: [],
            medecinReferent: null,
            dateCreation: yesterdayStr,
            dateMiseAJour: todayStr
        }
    ],

    ordonnances: [
        {
            id: 'o1',
            patientId: '1',
            medecinId: 'd1',
            dateEmission: todayStr,
            dateValidite: todayStr,
            statut: 'ACTIVE',
            remarques: '',
            medicaments: []
        },
        {
            id: 'o2',
            patientId: '2',
            medecinId: 'd1',
            dateEmission: yesterdayStr,
            dateValidite: todayStr,
            statut: 'EXPIREE',
            remarques: '',
            medicaments: []
        }
    ],

    rendezvous: [
        {
            id: 'r1',
            patientId: '1',
            patientName: 'Ali Ben Ali',
            medecinId: 'd1',
            date: todayStr,
            heure: '10:00:00',
            duree: 30,
            type: 'Consultation',
            statut: 'PLANIFIE',
            remarques: ''
        }
    ]
}

// -----------------------------------------------------------------------------
// API mock
// -----------------------------------------------------------------------------

vi.mock('../../src/services/api.js', () => ({
    default: {
        get: vi.fn(async (url, options) => {
            if (url === '/patients') {
                return {
                    data: {
                        content: apiMocks.patients,
                        totalPages: 1,
                        totalElements: apiMocks.patients.length,
                        number: options?.params?.page ?? 0,
                        size: options?.params?.size ?? 20
                    }
                }
            }

            if (url === '/ordonnances') {
                return {
                    data: apiMocks.ordonnances
                }
            }

            if (url === '/rendezvous') {
                return {
                    data: apiMocks.rendezvous
                }
            }

            if (url === '/doctors') {
                return {
                    data: []
                }
            }

            throw new Error(`Unexpected GET ${url}`)
        })
    }
}))

// -----------------------------------------------------------------------------
// Auth store mock
// -----------------------------------------------------------------------------

vi.mock('../../src/stores/authStore.js', () => ({
    useAuthStore: () => ({
        user: {
            email: 'doctor@test.tn'
        }
    })
}))

// -----------------------------------------------------------------------------
// Doctor store mock
// -----------------------------------------------------------------------------

vi.mock('../../src/stores/doctorStore.js', () => ({
    useDoctorStore: () => ({
        doctors: [],
        fetchDoctors: vi.fn(async () => [])
    })
}))

// -----------------------------------------------------------------------------
// Tests
// -----------------------------------------------------------------------------

describe('dashboardStore', () => {

    beforeEach(() => {
        setActivePinia(createPinia())
    })

    // -------------------------------------------------------------------------
    // Patients today
    // -------------------------------------------------------------------------

    it('correctly counts patients added today', async () => {
        const store = useDashboardStore()

        await store.fetchDashboard()

        expect(store.patientsToday).toBe(1)
        expect(store.statistics.patientsToday).toBe(1)
    })

    // -------------------------------------------------------------------------
    // Patient sorting
    // -------------------------------------------------------------------------

    it('requests enough patients and sorts recent patients by creation date', async () => {
        const store = useDashboardStore()

        await store.fetchDashboard()

        expect(store.recentPatients).toHaveLength(2)

        expect(store.recentPatients[0].id).toBe('1')
        expect(store.recentPatients[0].firstName).toBe('Ali')
        expect(store.recentPatients[0].lastName).toBe('Ben Ali')

        expect(store.recentPatients[1].id).toBe('2')
        expect(store.recentPatients[1].firstName).toBe('Sara')
        expect(store.recentPatients[1].lastName).toBe('Trabelsi')
    })

    // -------------------------------------------------------------------------
    // Verify pagination size
    // -------------------------------------------------------------------------

    it('requests up to 1000 patients from the backend', async () => {
        const store = useDashboardStore()

        await store.fetchDashboard()

        const api = (await import('../../src/services/api.js')).default

        expect(api.get).toHaveBeenCalledWith(
            '/patients',
            {
                params: {
                    page: 0,
                    size: 1000
                }
            }
        )
    })

    // -------------------------------------------------------------------------
    // Prescription statistics
    // -------------------------------------------------------------------------

    it('calculates prescription statistics correctly', async () => {
        const store = useDashboardStore()

        await store.fetchDashboard()

        expect(store.prescriptionStats).toEqual([
            {
                name: 'Actives',
                v: 1,
                c: '#10B981'
            },
            {
                name: 'Expirées',
                v: 1,
                c: '#EF4444'
            },
            {
                name: 'Archivées',
                v: 0,
                c: '#8B5CF6'
            }
        ])

        expect(store.prescriptionTotal).toBe(2)
    })

    // -------------------------------------------------------------------------
    // Prescription donut
    // -------------------------------------------------------------------------

    it('builds valid prescription donut segments', async () => {
        const store = useDashboardStore()

        await store.fetchDashboard()

        expect(store.prescriptionSegments).toHaveLength(3)

        const active = store.prescriptionSegments.find(
            segment => segment.name === 'Actives'
        )

        const expired = store.prescriptionSegments.find(
            segment => segment.name === 'Expirées'
        )

        expect(active.percentage).toBe(50)
        expect(expired.percentage).toBe(50)

        expect(active.dash).toBeDefined()
        expect(expired.dash).toBeDefined()

        expect(active.offset).toBe(0)
        expect(expired.offset).toBeLessThan(0)
    })

    // -------------------------------------------------------------------------
    // Medical activity chart
    // -------------------------------------------------------------------------

    it('builds medical activity data for the current year', async () => {
        const store = useDashboardStore()

        await store.fetchDashboard()

        expect(store.monthlyActivity).toHaveLength(12)

        expect(
            store.monthlyActivity.reduce(
                (sum, month) => sum + month.consultations,
                0
            )
        ).toBe(2)

        expect(
            store.monthlyActivity.reduce(
                (sum, month) => sum + month.prescriptions,
                0
            )
        ).toBe(2)
    })

    // -------------------------------------------------------------------------
    // Activity chart points
    // -------------------------------------------------------------------------

    it('builds chart points for consultations and prescriptions', async () => {
        const store = useDashboardStore()

        await store.fetchDashboard()

        expect(store.activityChart.consultations).toHaveLength(12)
        expect(store.activityChart.prescriptions).toHaveLength(12)

        expect(store.activityChart.months).toHaveLength(12)

        expect(store.activityChart.months).toEqual([
            'Jan',
            'Fév',
            'Mar',
            'Avr',
            'Mai',
            'Juin',
            'Juil',
            'Août',
            'Sep',
            'Oct',
            'Nov',
            'Déc'
        ])

        store.activityChart.consultations.forEach(point => {
            expect(point).toHaveProperty('x')
            expect(point).toHaveProperty('y')
            expect(point).toHaveProperty('value')
        })
    })

    // -------------------------------------------------------------------------
    // Today's appointments
    // -------------------------------------------------------------------------

    it('correctly counts appointments today', async () => {
        const store = useDashboardStore()

        await store.fetchDashboard()

        expect(store.statistics.appointmentsToday).toBe(1)
    })

    // -------------------------------------------------------------------------
    // Active prescriptions
    // -------------------------------------------------------------------------

    it('correctly counts active prescriptions', async () => {
        const store = useDashboardStore()

        await store.fetchDashboard()

        expect(store.statistics.activePrescriptions).toBe(1)
    })

    // -------------------------------------------------------------------------
    // Total patients
    // -------------------------------------------------------------------------

    it('uses totalItems for the active patients KPI', async () => {
        const store = useDashboardStore()

        await store.fetchDashboard()

        expect(store.statistics.activePatients).toBe(2)
    })

    // -------------------------------------------------------------------------
    // Recent activity
    // -------------------------------------------------------------------------

    it('creates detailed recent activity events', async () => {
        const store = useDashboardStore()

        await store.fetchDashboard()

        expect(store.activity.length).toBeGreaterThan(0)

        expect(
            store.activity.some(
                item =>
                    item.type === 'patient' &&
                    item.text.includes('Ali Ben Ali')
            )
        ).toBe(true)

        expect(
            store.activity.some(
                item =>
                    item.type === 'prescription' &&
                    item.text.includes('Patient #1')
            )
        ).toBe(true)

        expect(
            store.activity.some(
                item =>
                    item.type === 'appointment' &&
                    item.text.includes('Ali Ben Ali')
            )
        ).toBe(true)
    })

    // -------------------------------------------------------------------------
    // Patient update activity
    // -------------------------------------------------------------------------

    it('creates an activity event when a patient is updated', async () => {
        const store = useDashboardStore()

        await store.fetchDashboard()

        expect(
            store.activity.some(
                item =>
                    item.type === 'patient-update' &&
                    item.text.includes('Sara Trabelsi')
            )
        ).toBe(true)
    })

    // -------------------------------------------------------------------------
    // Dashboard loading
    // -------------------------------------------------------------------------

    it('finishes loading after fetching dashboard data', async () => {
        const store = useDashboardStore()

        expect(store.loading).toBe(false)

        const promise = store.fetchDashboard()

        await promise

        expect(store.loading).toBe(false)
        expect(store.error).toBe(null)
    })
})