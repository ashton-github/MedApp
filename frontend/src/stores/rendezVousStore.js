import { defineStore } from 'pinia'
import api from '../services/api.js'

// Normalize "HH:MM:SS" (LocalTime serialized by Jackson) to "HH:MM"
const normalizeTime = (t) => t ? t.substring(0, 5) : ''

const mapRv = (rv) => ({
  id: rv.id,
  patientId: rv.patientId,
  patientName: rv.patientName,
  medecinId: rv.medecinId,
  day: rv.date,            // "YYYY-MM-DD"
  time: normalizeTime(rv.heure), // "HH:MM"
  duration: rv.duree,
  type: rv.type,
  status: rv.statut,
  notes: rv.remarques
})

export const useRendezVousStore = defineStore('rendezVous', {
  state: () => ({
    rendezVous: [],
    loading: false,
    error: null
  }),

  actions: {
    async fetchRendezVous() {
      this.loading = true
      this.error = null
      try {
        const response = await api.get('/rendezvous')
        this.rendezVous = response.data.map(mapRv)
        return this.rendezVous
      } catch (err) {
        this.error = err.response?.data?.message || 'Erreur lors du chargement des rendez-vous'
        throw err
      } finally {
        this.loading = false
      }
    },

    async createRendezVous(payload) {
      this.loading = true
      this.error = null
      try {
        const response = await api.post('/rendezvous', {
          patientId: payload.patientId,
          date: payload.day,
          heure: payload.time,
          duree: payload.duration,
          type: payload.type,
          remarques: payload.notes
        })
        const newRv = mapRv(response.data)
        this.rendezVous.push(newRv)
        return newRv
      } catch (err) {
        this.error = err.response?.data?.message || 'Erreur lors de la création du rendez-vous'
        throw err
      } finally {
        this.loading = false
      }
    },

    async updateRendezVous(id, payload) {
      this.loading = true
      this.error = null
      try {
        const response = await api.put(`/rendezvous/${id}`, {
          patientId: payload.patientId,
          date: payload.day,
          heure: payload.time,
          duree: payload.duration,
          type: payload.type,
          remarques: payload.notes
        })
        const updated = mapRv(response.data)
        
        const index = this.rendezVous.findIndex(r => r.id === id)
        if (index !== -1) {
          this.rendezVous.splice(index, 1, updated)
        }
        return updated
      } catch (err) {
        this.error = err.response?.data?.message || 'Erreur lors de la mise à jour du rendez-vous'
        throw err
      } finally {
        this.loading = false
      }
    },

    async changerStatutRendezVous(id, statut) {
      this.loading = true
      this.error = null
      try {
        const response = await api.patch(`/rendezvous/${id}/statut`, { statut })
        const updated = mapRv(response.data)
        
        const index = this.rendezVous.findIndex(r => r.id === id)
        if (index !== -1) {
          this.rendezVous.splice(index, 1, updated)
        }
        return updated
      } catch (err) {
        this.error = err.response?.data?.message || 'Erreur lors de la modification du statut'
        throw err
      } finally {
        this.loading = false
      }
    },

    async deleteRendezVous(id) {
      this.loading = true
      this.error = null
      try {
        await api.delete(`/rendezvous/${id}`)
        this.rendezVous = this.rendezVous.filter(r => r.id !== id)
      } catch (err) {
        this.error = err.response?.data?.message || 'Erreur lors de la suppression du rendez-vous'
        throw err
      } finally {
        this.loading = false
      }
    }
  }
})
