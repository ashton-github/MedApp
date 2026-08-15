import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../services/api.js'

// ─── Mapping helpers ──────────────────────────────────────────────────────────

const mapStatusToBackend = (status) => {
  if (status === 'ACTIVE') return 'ACTIVE'
  if (status === 'ARCHIVED') return 'ARCHIVEE'
  if (status === 'EXPIRED') return 'EXPIREE'
  return 'ACTIVE'
}

const mapStatusToFrontend = (status) => {
  if (status === 'ACTIVE') return 'ACTIVE'
  if (status === 'ARCHIVEE') return 'ARCHIVED'
  if (status === 'EXPIREE') return 'EXPIRED'
  return 'ACTIVE'
}

const toRequest = (o) => ({
  patientId: o.patientId,
  dateValidite: o.validityDate,
  remarques: o.notes ?? '',
  medicaments: (o.medications || []).map(m => ({
    nom: m.name,
    dosage: m.dosage,
    frequence: m.frequency,
    duree: m.duration
  }))
})

/**
 * Map a backend Ordonnance to a frontend object.
 */
const fromResponse = (r) => ({
  id: r.id || r._id,
  patientId: r.patientId,
  doctorId: r.medecinId,
  issueDate: r.dateEmission,
  validityDate: r.dateValidite,
  status: mapStatusToFrontend(r.statut),
  notes: r.remarques,
  medications: (r.medicaments || []).map(m => ({
    name: m.nom,
    dosage: m.dosage,
    frequency: m.frequence,
    duration: m.duree
  })),
  // Optionally, backend might return expanded patient and doctor objects,
  // but if it only returns IDs, we'll store IDs.
  // The frontend components can fetch details if needed.
  patientName: r.patientName || '', // If backend provides it in a DTO
  doctorName: r.doctorName || ''
})

// ─── Store ────────────────────────────────────────────────────────────────────

export const useOrdonnanceStore = defineStore('ordonnance', () => {
  // ─── State ──────────────────────────────────────────────────────────────────
  const ordonnances = ref([])
  const currentOrdonnance = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // ─── Helpers ─────────────────────────────────────────────────────────────────
  const _startLoading = () => { loading.value = true; error.value = null }
  const _stopLoading = () => { loading.value = false }
  const _setError = (err) => {
    error.value = err?.response?.data?.message ?? err?.message ?? 'Une erreur est survenue.'
  }

  // ─── Actions ─────────────────────────────────────────────────────────────────

  const fetchOrdonnances = async () => {
    _startLoading()
    try {
      const { data } = await api.get('/ordonnances')
      // If data is paginated (like patients), adjust this logic. 
      // Assuming array or data.content for now.
      const items = Array.isArray(data) ? data : (data.content || [])
      ordonnances.value = items.map(fromResponse)
    } catch (err) {
      _setError(err)
    } finally {
      _stopLoading()
    }
  }

  const fetchOrdonnancesByPatientId = async (patientId) => {
    _startLoading()
    try {
      const { data } = await api.get(`/ordonnances/patient/${patientId}`)
      const items = Array.isArray(data) ? data : (data.content || [])
      return items.map(fromResponse)
    } catch (err) {
      _setError(err)
      return []
    } finally {
      _stopLoading()
    }
  }

  const getOrdonnanceById = async (id) => {
    _startLoading()
    currentOrdonnance.value = null
    try {
      const { data } = await api.get(`/ordonnances/${id}`)
      currentOrdonnance.value = fromResponse(data)
    } catch (err) {
      _setError(err)
    } finally {
      _stopLoading()
    }
  }

  const createOrdonnance = async (ordonnanceData) => {
    _startLoading()
    try {
      const { data } = await api.post('/ordonnances', toRequest(ordonnanceData))
      const created = fromResponse(data)
      ordonnances.value = [created, ...ordonnances.value]
      return created
    } catch (err) {
      _setError(err)
      throw err
    } finally {
      _stopLoading()
    }
  }

  const updateOrdonnance = async (id, ordonnanceData) => {
    _startLoading()
    try {
      const { data } = await api.put(`/ordonnances/${id}`, toRequest(ordonnanceData))
      const updated = fromResponse(data)
      const idx = ordonnances.value.findIndex(o => o.id === id)
      if (idx !== -1) ordonnances.value[idx] = updated
      if (currentOrdonnance.value?.id === id) currentOrdonnance.value = updated
      return updated
    } catch (err) {
      _setError(err)
      throw err
    } finally {
      _stopLoading()
    }
  }

  const archiveOrdonnance = async (id) => {
    _startLoading()
    try {
      const { data } = await api.patch(`/ordonnances/${id}/archiver`)
      // Either remove it from the active list, or update its status to ARCHIVED
      const updated = fromResponse(data)
      const idx = ordonnances.value.findIndex(o => o.id === id)
      if (idx !== -1) ordonnances.value[idx] = updated
      if (currentOrdonnance.value?.id === id) currentOrdonnance.value = updated
      return updated
    } catch (err) {
      _setError(err)
      throw err
    } finally {
      _stopLoading()
    }
  }

  const downloadPdf = async (id) => {
    _startLoading()
    try {
      const response = await api.get(`/ordonnances/${id}/pdf`, { responseType: 'blob' })
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `ordonnance-${id}.pdf`)
      document.body.appendChild(link)
      link.click()
      link.remove()
    } catch (err) {
      _setError(err)
      throw err
    } finally {
      _stopLoading()
    }
  }

  return {
    ordonnances,
    currentOrdonnance,
    loading,
    error,
    fetchOrdonnances,
    fetchOrdonnancesByPatientId,
    getOrdonnanceById,
    createOrdonnance,
    updateOrdonnance,
    archiveOrdonnance,
    downloadPdf
  }
})
