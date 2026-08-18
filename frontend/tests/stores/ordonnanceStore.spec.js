import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useOrdonnanceStore } from '../../src/stores/ordonnanceStore.js'
import api from '../../src/services/api.js'

vi.mock('../../src/services/api.js', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn()
  }
}))

describe('Ordonnance Store', () => {
  let store

  const backendOrdonnance = {
    id: 'o1',
    patientId: 'p1',
    medecinId: 'm1',
    dateEmission: '2026-08-01',
    dateValidite: '2026-09-01',
    statut: 'ARCHIVEE',
    remarques: 'Hydratation conseillée',
    medicaments: [
      {
        nom: 'Doliprane',
        dosage: '1000mg',
        frequence: '3x/jour',
        duree: '5 jours'
      }
    ]
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useOrdonnanceStore()
    vi.clearAllMocks()
  })

  it('initializes with expected default state', () => {
    expect(store.ordonnances).toEqual([])
    expect(store.currentOrdonnance).toBeNull()
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('fetches ordonnances by patient and maps backend fields', async () => {
    api.get.mockResolvedValueOnce({ data: [backendOrdonnance] })

    const result = await store.fetchOrdonnancesByPatientId('p1')

    expect(api.get).toHaveBeenCalledWith('/ordonnances/patient/p1')
    expect(result).toEqual([
      {
        id: 'o1',
        patientId: 'p1',
        doctorId: 'm1',
        issueDate: '2026-08-01',
        validityDate: '2026-09-01',
        status: 'ARCHIVED',
        notes: 'Hydratation conseillée',
        medications: [
          {
            name: 'Doliprane',
            dosage: '1000mg',
            frequency: '3x/jour',
            duration: '5 jours'
          }
        ],
        patientName: '',
        doctorName: ''
      }
    ])
  })

  it('creates ordonnance with mapped request payload', async () => {
    api.post.mockResolvedValueOnce({ data: backendOrdonnance })

    await store.createOrdonnance({
      patientId: 'p1',
      validityDate: '2026-09-01',
      notes: 'Hydratation conseillée',
      medications: [
        {
          name: 'Doliprane',
          dosage: '1000mg',
          frequency: '3x/jour',
          duration: '5 jours'
        }
      ]
    })

    expect(api.post).toHaveBeenCalledWith('/ordonnances', {
      patientId: 'p1',
      dateValidite: '2026-09-01',
      remarques: 'Hydratation conseillée',
      medicaments: [
        {
          nom: 'Doliprane',
          dosage: '1000mg',
          frequence: '3x/jour',
          duree: '5 jours'
        }
      ]
    })
    expect(store.ordonnances[0].id).toBe('o1')
  })

  it('updates ordonnance and syncs ordonnances/currentOrdonnance', async () => {
    store.ordonnances = [
      {
        id: 'o1',
        patientId: 'p1',
        doctorId: 'm1',
        issueDate: '2026-08-01',
        validityDate: '2026-09-01',
        status: 'ACTIVE',
        notes: '',
        medications: [],
        patientName: '',
        doctorName: ''
      }
    ]
    store.currentOrdonnance = { ...store.ordonnances[0] }

    const updatedBackend = {
      ...backendOrdonnance,
      statut: 'ACTIVE',
      remarques: 'Dosage modifié'
    }

    api.put.mockResolvedValueOnce({ data: updatedBackend })

    const updated = await store.updateOrdonnance('o1', {
      patientId: 'p1',
      validityDate: '2026-10-01',
      notes: 'Dosage modifié',
      medications: [
        {
          name: 'Doliprane',
          dosage: '500mg',
          frequency: '2x/jour',
          duration: '3 jours'
        }
      ]
    })

    expect(api.put).toHaveBeenCalledWith('/ordonnances/o1', {
      patientId: 'p1',
      dateValidite: '2026-10-01',
      remarques: 'Dosage modifié',
      medicaments: [
        {
          nom: 'Doliprane',
          dosage: '500mg',
          frequence: '2x/jour',
          duree: '3 jours'
        }
      ]
    })
    expect(updated.notes).toBe('Dosage modifié')
    expect(store.ordonnances[0].notes).toBe('Dosage modifié')
    expect(store.currentOrdonnance.notes).toBe('Dosage modifié')
  })

  it('archives ordonnance and maps ARCHIVEE to ARCHIVED', async () => {
    store.ordonnances = [
      {
        id: 'o1',
        patientId: 'p1',
        doctorId: 'm1',
        issueDate: '2026-08-01',
        validityDate: '2026-09-01',
        status: 'ACTIVE',
        notes: '',
        medications: [],
        patientName: '',
        doctorName: ''
      }
    ]

    api.patch.mockResolvedValueOnce({
      data: {
        ...backendOrdonnance,
        statut: 'ARCHIVEE'
      }
    })

    const archived = await store.archiveOrdonnance('o1')

    expect(api.patch).toHaveBeenCalledWith('/ordonnances/o1/archiver')
    expect(archived.status).toBe('ARCHIVED')
    expect(store.ordonnances[0].status).toBe('ARCHIVED')
  })
})
