import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../services/api.js'

export const useDoctorStore = defineStore('doctor', () => {
  const doctors = ref([])
  const loading = ref(false)
  const error = ref(null)

  const fetchDoctors = async () => {
    // Éviter de refetcher si on a déjà les données
    if (doctors.value.length > 0) return

    loading.value = true
    error.value = null
    try {
      const { data } = await api.get('/users', { params: { role: 'MEDECIN' } })
      doctors.value = data
    } catch (err) {
      error.value = err?.response?.data?.message || err?.message || 'Erreur de chargement des médecins'
      console.error('Failed to fetch doctors', err)
    } finally {
      loading.value = false
    }
  }

  const getDoctorFullName = (id) => {
    if (!id) return 'Non renseigné'
    const doc = doctors.value.find(d => d.id === id)
    return doc ? `Dr. ${doc.prenom} ${doc.nom}` : 'Non renseigné'
  }

  return {
    doctors,
    loading,
    error,
    fetchDoctors,
    getDoctorFullName
  }
})
