import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { useAuthStore } from './authStore.js'
import { usePatientStore } from './patientStore.js'
import { useRendezVousStore } from './rendezVousStore.js'
import { useOrdonnanceStore } from './ordonnanceStore.js'
import { useDoctorStore } from './doctorStore.js'

export const useDashboardStore = defineStore('dashboard', () => {
    const authStore = useAuthStore()
    const patientStore = usePatientStore()
    const rendezVousStore = useRendezVousStore()
    const ordonnanceStore = useOrdonnanceStore()
    const doctorStore = useDoctorStore()

    const loading = ref(false)
    const error = ref(null)

    const patients = computed(() => patientStore.patients ?? [])
    const rendezVous = computed(() => rendezVousStore.rendezVous ?? [])
    const ordonnances = computed(() => ordonnanceStore.ordonnances ?? [])

    const dateKey = (value) => {
        if (!value) return null
        if (typeof value === 'string') {
            const iso = value.match(/^(\d{4}-\d{2}-\d{2})/)
            if (iso) return iso[1]

            const french = value.match(/^(\d{2})\/(\d{2})\/(\d{4})$/)
            if (french) return `${french[3]}-${french[2]}-${french[1]}`
        }

        const date = value instanceof Date ? value : new Date(value)
        if (Number.isNaN(date.getTime())) return null

        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    }

    const parseDate = (value) => {
        if (!value) return null
        if (value instanceof Date) return Number.isNaN(value.getTime()) ? null : value

        // Date-only values are parsed locally to avoid timezone shifts.
        if (typeof value === 'string') {
            const match = value.match(/^(\d{4})-(\d{2})-(\d{2})$/)
            if (match) {
                const date = new Date(
                    Number(match[1]),
                    Number(match[2]) - 1,
                    Number(match[3])
                )
                return Number.isNaN(date.getTime()) ? null : date
            }
        }

        const date = new Date(value)
        return Number.isNaN(date.getTime()) ? null : date
    }

    const parseDateTime = (day, time = '') => {
        if (!day) return null
        if (!time) return parseDate(day)

        const normalizedTime = String(time).length === 5
            ? `${time}:00`
            : String(time)

        return parseDate(`${day}T${normalizedTime}`)
    }

    const todayKey = () => dateKey(new Date())

    const startOfWeek = () => {
        const date = new Date()
        date.setHours(0, 0, 0, 0)
        const day = date.getDay()
        date.setDate(date.getDate() + (day === 0 ? -6 : 1 - day))
        return date
    }

    const endOfWeek = () => {
        const date = startOfWeek()
        date.setDate(date.getDate() + 7)
        return date
    }

    const currentDoctor = computed(() => {
        const email = authStore.user?.email
        if (!email) return null

        return doctorStore.doctors?.find(doctor => doctor.email === email) ?? null
    })

    const doctorFullName = computed(() => {
        const doctor = currentDoctor.value
        if (!doctor) return 'Utilisateur'

        return [doctor.prenom, doctor.nom].filter(Boolean).join(' ') || 'Utilisateur'
    })

    const recentPatients = computed(() =>
        [...patients.value]
            .filter(patient => dateKey(patient.createdAt))
            .sort((a, b) => dateKey(b.createdAt).localeCompare(dateKey(a.createdAt)))
            .slice(0, 4)
    )

    const patientsToday = computed(() => {
        const today = todayKey()
        return patients.value.filter(patient => dateKey(patient.createdAt) === today).length
    })

    const appointmentsToday = computed(() => {
        const today = todayKey()
        return rendezVous.value.filter(rdv => dateKey(rdv.day) === today).length
    })

    const appointmentsThisWeek = computed(() => {
        const start = startOfWeek()
        const end = endOfWeek()

        return rendezVous.value.filter(rdv => {
            const date = parseDate(rdv.day)
            return date && date >= start && date < end
        }).length
    })

    const activePrescriptions = computed(() =>
        ordonnances.value.filter(ordonnance => ordonnance.status === 'ACTIVE').length
    )

    const totalPatients = computed(() =>
        patientStore.totalItems ?? patients.value.length
    )

    const statistics = computed(() => ({
        patientsToday: patientsToday.value,
        appointmentsToday: appointmentsToday.value,
        activePrescriptions: activePrescriptions.value,
        appointmentsThisWeek: appointmentsThisWeek.value,
        activePatients: totalPatients.value
    }))

    const prescriptionStats = computed(() => [
        { name: 'Actives', v: ordonnances.value.filter(o => o.status === 'ACTIVE').length, c: '#10B981' },
        { name: 'Expirées', v: ordonnances.value.filter(o => o.status === 'EXPIRED').length, c: '#EF4444' },
        { name: 'Archivées', v: ordonnances.value.filter(o => o.status === 'ARCHIVED').length, c: '#8B5CF6' }
    ])

    const prescriptionTotal = computed(() =>
        prescriptionStats.value.reduce((sum, item) => sum + item.v, 0)
    )

    const prescriptionSegments = computed(() => {
        const total = prescriptionTotal.value
        const circumference = 2 * Math.PI * 40

        if (total === 0) {
            return prescriptionStats.value.map(item => ({
                ...item,
                percentage: 0,
                dash: `0 ${circumference}`,
                offset: 0
            }))
        }

        let accumulated = 0

        return prescriptionStats.value.map(item => {
            const percentage = (item.v / total) * 100

            const offset = accumulated === 0
                ? 0
                : -(accumulated / 100) * circumference

            const segment = {
                ...item,
                percentage,
                dash: `${(percentage / 100) * circumference} ${circumference}`,
                offset
            }

            accumulated += percentage

            return segment
        })
    })

    const monthlyActivity = computed(() => {
        const currentYear = new Date().getFullYear()
        const months = Array.from({ length: 12 }, (_, month) => ({
            month,
            consultations: 0,
            prescriptions: 0
        }))

        patients.value.forEach(patient => {
            const date = parseDate(patient.createdAt)
            if (date?.getFullYear() === currentYear) months[date.getMonth()].consultations++
        })

        ordonnances.value.forEach(ordonnance => {
            const date = parseDate(ordonnance.issueDate)
            if (date?.getFullYear() === currentYear) months[date.getMonth()].prescriptions++
        })

        return months
    })

    const buildChartPoints = (values, width = 600, height = 180) => {
        const max = Math.max(...values, 1)
        return values.map((value, index) => ({
            x: values.length === 1 ? width / 2 : (index / (values.length - 1)) * width,
            y: height - (value / max) * height,
            value
        }))
    }

    const activityChart = computed(() => ({
        consultations: buildChartPoints(monthlyActivity.value.map(item => item.consultations)),
        prescriptions: buildChartPoints(monthlyActivity.value.map(item => item.prescriptions)),
        months: ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Juin', 'Juil', 'Août', 'Sep', 'Oct', 'Nov', 'Déc']
    }))

    const activity = computed(() => {
        const events = []

        patients.value.forEach(patient => {
            const created = parseDate(patient.createdAt)
            if (created) {
                const name = [patient.firstName, patient.lastName].filter(Boolean).join(' ')
                events.push({
                    id: `patient-created-${patient.id}`,
                    title: 'Nouveau patient',
                    text: `Nouveau patient · ${name || 'Patient'}`,
                    detail: name || 'Patient',
                    date: created,
                    time: created,
                    type: 'patient'
                })
            }

            const updated = parseDate(patient.updatedAt)
            if (updated && (!created || updated.getTime() !== created.getTime())) {
                const name = [patient.firstName, patient.lastName].filter(Boolean).join(' ')
                events.push({
                    id: `patient-updated-${patient.id}`,
                    title: 'Patient mis à jour',
                    text: `Patient mis à jour · ${name || 'Patient'}`,
                    detail: name || 'Patient',
                    date: updated,
                    time: updated,
                    type: 'patient-update'
                })
            }
        })

        ordonnances.value.forEach(ordonnance => {
            const date = parseDate(ordonnance.issueDate)
            if (!date) return
            const patientLabel = ordonnance.patientName
                ? `Pour ${ordonnance.patientName}`
                : ordonnance.patientId
                    ? `Patient #${ordonnance.patientId}`
                    : `Ordonnance #${ordonnance.id}`

            events.push({
                id: `prescription-${ordonnance.id}`,
                title: 'Nouvelle ordonnance',
                text: `Nouvelle ordonnance · ${patientLabel}`,
                detail: ordonnance.status ? `${patientLabel} · ${ordonnance.status}` : patientLabel,
                date,
                time: date,
                type: 'prescription'
            })
        })

        rendezVous.value.forEach(rdv => {
            const date = parseDateTime(rdv.day, rdv.time)
            if (!date) return

            const details = [
                rdv.patientName || (rdv.patientId ? `Patient #${rdv.patientId}` : null),
                rdv.type,
                rdv.time ? `${rdv.time}${rdv.duration ? ` · ${rdv.duration} min` : ''}` : null,
                rdv.status
            ].filter(Boolean)

            events.push({
                id: `appointment-${rdv.id}`,
                title: 'Rendez-vous',
                text: `Rendez-vous · ${details.join(' · ') || 'Rendez-vous'}`,
                detail: details.join(' · ') || 'Rendez-vous',
                date,
                time: date,
                type: 'appointment'
            })
        })

        return events.sort((a, b) => b.date.getTime() - a.date.getTime()).slice(0, 6)
    })

    const fetchDashboard = async () => {
        loading.value = true
        error.value = null

        try {
            // size=1000 prevents the first-page limitation from hiding today's patients.
            // Sorting/filtering is still performed entirely in the frontend.
            await Promise.all([
                patientStore.fetchPatients({ page: 0, size: 1000 }),
                rendezVousStore.fetchRendezVous(),
                ordonnanceStore.fetchOrdonnances(),
                doctorStore.fetchDoctors()
            ])
        } catch (err) {
            error.value = err?.response?.data?.message ?? err?.message ?? 'Impossible de charger le tableau de bord.'
        } finally {
            loading.value = false
        }
    }

    return {
        loading, error, patients, rendezVous, ordonnances,
        currentDoctor, doctorFullName,
        recentPatients, patientsToday, statistics,
        prescriptionStats, prescriptionTotal, prescriptionSegments,
        monthlyActivity, activityChart, activity,
        fetchDashboard
    }
})