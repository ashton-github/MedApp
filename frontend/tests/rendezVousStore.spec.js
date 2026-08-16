import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useRendezVousStore } from '../src/stores/rendezVousStore.js'
import api from '../src/services/api.js'

vi.mock('../src/services/api.js', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn()
  }
}))

describe('RendezVous Store', () => {
  let store

  const backendRendezVous = {
    id: 'rv1',
    patientId: 'p1',
    patientName: 'Dupont Marie',
    medecinId: 'm1',
    date: '2026-08-20',
    heure: '10:00:00', // Backend sends HH:MM:SS
    duree: 30,
    type: 'CONSULTATION',
    statut: 'PLANIFIE',
    remarques: 'Première consultation'
  }

  const expectedMappedRv = {
    id: 'rv1',
    patientId: 'p1',
    patientName: 'Dupont Marie',
    medecinId: 'm1',
    day: '2026-08-20',
    time: '10:00', // Normalized to HH:MM
    duration: 30,
    type: 'CONSULTATION',
    status: 'PLANIFIE',
    notes: 'Première consultation'
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useRendezVousStore()
    vi.clearAllMocks()
  })

  it('initializes with expected default state', () => {
    expect(store.rendezVous).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('fetches rendezvous and maps backend fields', async () => {
    api.get.mockResolvedValueOnce({ data: [backendRendezVous] })

    const result = await store.fetchRendezVous()

    expect(api.get).toHaveBeenCalledWith('/rendezvous')
    expect(result).toEqual([expectedMappedRv])
    expect(store.rendezVous).toEqual([expectedMappedRv])
  })

  it('creates rendezvous with mapped request payload', async () => {
    api.post.mockResolvedValueOnce({ data: backendRendezVous })

    await store.createRendezVous({
      patientId: 'p1',
      day: '2026-08-20',
      time: '10:00',
      duration: 30,
      type: 'CONSULTATION',
      notes: 'Première consultation'
    })

    expect(api.post).toHaveBeenCalledWith('/rendezvous', {
      patientId: 'p1',
      date: '2026-08-20',
      heure: '10:00',
      duree: 30,
      type: 'CONSULTATION',
      remarques: 'Première consultation'
    })
    expect(store.rendezVous[0]).toEqual(expectedMappedRv)
  })

  it('updates rendezvous and syncs state', async () => {
    store.rendezVous = [expectedMappedRv]

    const updatedBackend = {
      ...backendRendezVous,
      type: 'SUIVI',
      remarques: 'Notes modifiées'
    }

    api.put.mockResolvedValueOnce({ data: updatedBackend })

    const updated = await store.updateRendezVous('rv1', {
      patientId: 'p1',
      day: '2026-08-20',
      time: '10:00',
      duration: 30,
      type: 'SUIVI',
      notes: 'Notes modifiées'
    })

    expect(api.put).toHaveBeenCalledWith('/rendezvous/rv1', {
      patientId: 'p1',
      date: '2026-08-20',
      heure: '10:00',
      duree: 30,
      type: 'SUIVI',
      remarques: 'Notes modifiées'
    })
    expect(updated.type).toBe('SUIVI')
    expect(updated.notes).toBe('Notes modifiées')
    expect(store.rendezVous[0].type).toBe('SUIVI')
    expect(store.rendezVous[0].notes).toBe('Notes modifiées')
  })

  it('changes status and syncs state', async () => {
    store.rendezVous = [expectedMappedRv]

    const updatedBackend = {
      ...backendRendezVous,
      statut: 'TERMINE'
    }

    api.patch.mockResolvedValueOnce({ data: updatedBackend })

    const updated = await store.changerStatutRendezVous('rv1', 'TERMINE')

    expect(api.patch).toHaveBeenCalledWith('/rendezvous/rv1/statut', { statut: 'TERMINE' })
    expect(updated.status).toBe('TERMINE')
    expect(store.rendezVous[0].status).toBe('TERMINE')
  })

  it('deletes rendezvous and removes from state', async () => {
    store.rendezVous = [expectedMappedRv]

    api.delete.mockResolvedValueOnce({})

    await store.deleteRendezVous('rv1')

    expect(api.delete).toHaveBeenCalledWith('/rendezvous/rv1')
    expect(store.rendezVous).toEqual([])
  })
})
