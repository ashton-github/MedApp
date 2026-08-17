import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePatientStore } from '../../src/stores/patientStore.js'
import api from '../../src/services/api.js'

vi.mock('../src/services/api.js', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  }
}))

describe('Patient Store', () => {
  let store

  const mockBackendPatient = {
    id: '1',
    nom: 'Doe',
    prenom: 'John',
    dateNaissance: '1990-01-01',
    sexe: 'M',
    telephone: '1234567890',
    adresse: '123 Main St',
    numeroSecuriteSociale: '123456789012345',
    antecedents: ['Allergy'],
    medecinReferent: 'Dr. Smith',
    dateCreation: '2023-01-01',
    dateMiseAJour: '2023-01-02'
  }

  const mockFrontendPatient = {
    id: '1',
    lastName: 'Doe',
    firstName: 'John',
    birthDate: '1990-01-01',
    gender: 'M',
    phone: '1234567890',
    address: '123 Main St',
    socialSecurityNumber: '123456789012345',
    medicalHistory: ['Allergy'],
    referringDoctor: 'Dr. Smith',
    createdAt: '2023-01-01',
    updatedAt: '2023-01-02'
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    store = usePatientStore()
    vi.clearAllMocks()
  })

  it('initializes with correct default state', () => {
    expect(store.patients).toEqual([])
    expect(store.currentPatient).toBeNull()
    expect(store.totalPages).toBe(0)
    expect(store.totalItems).toBe(0)
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  describe('fetchPatients', () => {
    it('fetches patients and maps them to frontend shape', async () => {
      api.get.mockResolvedValueOnce({
        data: {
          content: [mockBackendPatient],
          totalPages: 1,
          totalElements: 1
        }
      })

      await store.fetchPatients()

      expect(api.get).toHaveBeenCalledWith('/patients', { params: { page: 0, size: 20 } })
      expect(store.patients).toEqual([mockFrontendPatient])
      expect(store.totalPages).toBe(1)
      expect(store.totalItems).toBe(1)
      expect(store.error).toBeNull()
      expect(store.loading).toBe(false)
    })

    it('sets error when API fails', async () => {
      api.get.mockRejectedValueOnce(new Error('Network Error'))
      await store.fetchPatients()
      expect(store.error).toBe('Network Error')
    })
  })

  describe('searchPatients', () => {
    it('searches patients and updates store', async () => {
      api.get.mockResolvedValueOnce({
        data: [mockBackendPatient]
      })

      await store.searchPatients('John')

      expect(api.get).toHaveBeenCalledWith('/patients/search', { params: { query: 'John' } })
      expect(store.patients).toEqual([mockFrontendPatient])
    })
  })

  describe('getPatientById', () => {
    it('fetches a single patient and sets currentPatient', async () => {
      api.get.mockResolvedValueOnce({
        data: mockBackendPatient
      })

      await store.getPatientById('1')

      expect(api.get).toHaveBeenCalledWith('/patients/1')
      expect(store.currentPatient).toEqual(mockFrontendPatient)
    })
  })

  describe('createPatient', () => {
    it('creates a patient and adds it to the list', async () => {
      api.post.mockResolvedValueOnce({
        data: mockBackendPatient
      })

      const newPatientData = {
        lastName: 'Doe',
        firstName: 'John',
        birthDate: '1990-01-01',
        gender: 'M',
        phone: '1234567890',
        address: '123 Main St',
        socialSecurityNumber: '123456789012345',
        medicalHistory: ['Allergy'],
        referringDoctor: 'Dr. Smith'
      }

      const created = await store.createPatient(newPatientData)

      expect(api.post).toHaveBeenCalledWith('/patients', {
        nom: 'Doe',
        prenom: 'John',
        dateNaissance: '1990-01-01',
        sexe: 'M',
        telephone: '1234567890',
        adresse: '123 Main St',
        numeroSecuriteSociale: '123456789012345',
        antecedents: ['Allergy'],
        medecinReferent: 'Dr. Smith'
      })
      expect(created).toEqual(mockFrontendPatient)
      expect(store.patients).toEqual([mockFrontendPatient])
    })

    it('throws error and sets state when creation fails', async () => {
      api.post.mockRejectedValueOnce({ response: { data: { message: 'Creation failed' } } })
      await expect(store.createPatient({})).rejects.toThrow()
      expect(store.error).toBe('Creation failed')
    })
  })

  describe('updatePatient', () => {
    it('updates a patient and modifies state lists', async () => {
      store.patients = [mockFrontendPatient]
      store.currentPatient = mockFrontendPatient

      const updatedBackend = { ...mockBackendPatient, prenom: 'Johnny' }
      const updatedFrontend = { ...mockFrontendPatient, firstName: 'Johnny' }

      api.put.mockResolvedValueOnce({ data: updatedBackend })

      const result = await store.updatePatient('1', { ...mockFrontendPatient, firstName: 'Johnny' })

      expect(api.put).toHaveBeenCalled()
      expect(result).toEqual(updatedFrontend)
      expect(store.patients[0].firstName).toBe('Johnny')
      expect(store.currentPatient.firstName).toBe('Johnny')
    })
  })

  describe('deletePatient', () => {
    it('deletes a patient and removes from state', async () => {
      store.patients = [mockFrontendPatient]
      store.currentPatient = mockFrontendPatient

      api.delete.mockResolvedValueOnce({})

      await store.deletePatient('1')

      expect(api.delete).toHaveBeenCalledWith('/patients/1')
      expect(store.patients).toEqual([])
      expect(store.currentPatient).toBeNull()
    })
  })
})
