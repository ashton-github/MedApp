import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../services/api.js'

// ─── Mapping helpers ──────────────────────────────────────────────────────────
// Backend uses French field names, frontend uses English.
// These helpers keep the boundary clean.

/**
 * Map a frontend patient object to the backend PatientRequest shape.
 * @param {object} p - frontend patient
 */
const toRequest = (p) => ({
  nom:                  p.lastName,
  prenom:               p.firstName,
  dateNaissance:        p.birthDate,      // ISO date string "YYYY-MM-DD"
  sexe:                 p.gender,         // 'M' | 'F'
  telephone:            p.phone ?? null,
  adresse:              p.address ?? null,
  numeroSecuriteSociale: p.socialSecurityNumber,
  antecedents:          p.medicalHistory ?? [],  // List<String>
  medecinReferent:      p.referringDoctor ?? null
})

/**
 * Map a backend PatientResponse to a frontend patient object.
 * @param {object} r - backend response
 */
const fromResponse = (r) => ({
  id:                    r.id,
  lastName:              r.nom,
  firstName:             r.prenom,
  birthDate:             r.dateNaissance,    // "YYYY-MM-DD"
  gender:                r.sexe,             // 'M' | 'F'
  phone:                 r.telephone,
  address:               r.adresse,
  socialSecurityNumber:  r.numeroSecuriteSociale,
  medicalHistory:        r.antecedents ?? [],
  referringDoctor:       r.medecinReferent,
  createdAt:             r.dateCreation,
  updatedAt:             r.dateMiseAJour
})

// ─── Store ────────────────────────────────────────────────────────────────────

export const usePatientStore = defineStore('patient', () => {
  // ─── State ──────────────────────────────────────────────────────────────────
  const patients     = ref([])      // array of mapped frontend patient objects
  const currentPatient = ref(null)  // single patient for detail view
  const totalPages   = ref(0)
  const totalItems   = ref(0)
  const loading      = ref(false)
  const error        = ref(null)

  // ─── Helpers ─────────────────────────────────────────────────────────────────
  const _startLoading = () => { loading.value = true; error.value = null }
  const _stopLoading  = () => { loading.value = false }
  const _setError     = (err) => {
    // Use the backend message if available, otherwise fall back to a generic string
    error.value = err?.response?.data?.message ?? err?.message ?? 'Une erreur est survenue.'
  }

  // ─── Actions ─────────────────────────────────────────────────────────────────

  /**
   * Fetch a paginated list of patients.
   * GET /api/patients?page=0&size=20
   */
  const fetchPatients = async ({ page = 0, size = 20 } = {}) => {
    _startLoading()
    try {
      const { data } = await api.get('/patients', { params: { page, size } })
      patients.value   = data.content.map(fromResponse)
      totalPages.value = data.totalPages
      totalItems.value = data.totalElements
    } catch (err) {
      _setError(err)
    } finally {
      _stopLoading()
    }
  }

  /**
   * Search patients by query string.
   * GET /api/patients/search?query=...
   */
  const searchPatients = async (query) => {
    _startLoading()
    try {
      const { data } = await api.get('/patients/search', { params: { query } })
      patients.value = data.map(fromResponse)
    } catch (err) {
      _setError(err)
    } finally {
      _stopLoading()
    }
  }

  /**
   * Load a single patient by ID into currentPatient.
   * GET /api/patients/:id
   */
  const getPatientById = async (id) => {
    _startLoading()
    currentPatient.value = null
    try {
      const { data } = await api.get(`/patients/${id}`)
      currentPatient.value = fromResponse(data)
    } catch (err) {
      _setError(err)
    } finally {
      _stopLoading()
    }
  }

  /**
   * Create a new patient.
   * POST /api/patients
   * @param {object} patientData - frontend patient fields
   * @returns {object} the created patient (frontend shape)
   */
  const createPatient = async (patientData) => {
    _startLoading()
    try {
      const { data } = await api.post('/patients', toRequest(patientData))
      const created = fromResponse(data)
      patients.value = [created, ...patients.value]
      return created
    } catch (err) {
      _setError(err)
      throw err  // re-throw so the component can react (e.g. stay on form)
    } finally {
      _stopLoading()
    }
  }

  /**
   * Update an existing patient.
   * PUT /api/patients/:id
   * @param {string} id
   * @param {object} patientData - frontend patient fields
   * @returns {object} the updated patient (frontend shape)
   */
  const updatePatient = async (id, patientData) => {
    _startLoading()
    try {
      const { data } = await api.put(`/patients/${id}`, toRequest(patientData))
      const updated = fromResponse(data)
      // Replace in list if present
      const idx = patients.value.findIndex(p => p.id === id)
      if (idx !== -1) patients.value[idx] = updated
      if (currentPatient.value?.id === id) currentPatient.value = updated
      return updated
    } catch (err) {
      _setError(err)
      throw err
    } finally {
      _stopLoading()
    }
  }

  /**
   * Delete a patient.
   * DELETE /api/patients/:id  (requires SECRETAIRE role — backend enforced)
   * @param {string} id
   */
  const deletePatient = async (id) => {
    _startLoading()
    try {
      await api.delete(`/patients/${id}`)
      patients.value = patients.value.filter(p => p.id !== id)
      if (currentPatient.value?.id === id) currentPatient.value = null
    } catch (err) {
      _setError(err)
      throw err
    } finally {
      _stopLoading()
    }
  }

  return {
    // State
    patients,
    currentPatient,
    totalPages,
    totalItems,
    loading,
    error,
    // Actions
    fetchPatients,
    searchPatients,
    getPatientById,
    createPatient,
    updatePatient,
    deletePatient
  }
})
