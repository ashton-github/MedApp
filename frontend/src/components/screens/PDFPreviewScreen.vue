<script setup>
import { computed } from 'vue'
import {
  ChevronRight,
  Download,
  Printer,
  Stethoscope,
  Pill,
  QrCode
} from 'lucide-vue-next'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { useOrdonnanceStore } from '../../stores/ordonnanceStore.js'
import { usePatientStore } from '../../stores/patientStore.js'
import { screens } from '../../constants/medapp.js'

const { showScreen, authUser } = useMedAppState()
const ordonnanceStore = useOrdonnanceStore()
const patientStore = usePatientStore()

const fallbackDoctorName = computed(() => {
  if (authUser.value?.email) {
    const namePart = authUser.value.email.split('@')[0]
    return `Dr. ${namePart.charAt(0).toUpperCase() + namePart.slice(1)}`
  }
  return 'Dr. Inconnu'
})

const rx = computed(() => {
  const o = ordonnanceStore.currentOrdonnance
  if (!o) {
    return {
      id: "---",
      patientId: "",
      issueDate: new Date().toISOString(),
      validityDate: new Date().toISOString(),
      doctorName: fallbackDoctorName.value,
      status: "ACTIVE",
      medications: [],
      notes: ""
    }
  }
  return {
    ...o,
    doctorName: o.doctorName || fallbackDoctorName.value
  }
})

const p = computed(() => {
  const patient = patientStore.patients.find(p => p.id === rx.value.patientId)
  return patient || {
    firstName: "Patient",
    lastName: "Inconnu",
    birthDate: "",
    phone: ""
  }
})

const fmt = (d) => d ? new Date(d).toLocaleDateString("fr-FR", { day: "2-digit", month: "short", year: "numeric" }) : ''

const printOrdonnance = () => {
  const el = document.querySelector('[data-print-target]')
  if (!el) return

  const win = window.open('', '_blank', 'width=900,height=1200')
  win.document.write(`
    <!DOCTYPE html>
    <html lang="fr">
    <head>
      <meta charset="UTF-8" />
      <title>Ordonnance - ${rx.value.id}</title>
      <style>
        @page { size: A4; margin: 12mm; }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Inter', system-ui, sans-serif; background: white; color: #0F172A; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
        .card { width: 100%; }
        .header { background: linear-gradient(to right, #2563eb, #1d4ed8); padding: 24px; color: white; display: flex; justify-content: space-between; align-items: flex-start; }
        .header-left { display: flex; flex-direction: column; gap: 2px; }
        .header-logo { display: flex; align-items: center; gap: 8px; font-weight: 700; font-size: 18px; }
        .header-sub { color: #bfdbfe; font-size: 13px; }
        .header-right { text-align: right; font-size: 13px; }
        .header-right p { color: #bfdbfe; font-size: 12px; }
        .body { padding: 32px; }
        .row-between { display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 1px solid #e2e8f0; padding-bottom: 16px; margin-bottom: 24px; }
        .label { font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #64748b; margin-bottom: 4px; }
        .value { font-weight: 700; font-size: 15px; }
        .patient-box { background: #f8fafc; border-radius: 12px; padding: 16px; margin-bottom: 24px; }
        .grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-top: 12px; }
        .grid2 .label { font-size: 11px; color: #64748b; margin-bottom: 2px; }
        .grid2 .value { font-size: 13px; font-weight: 600; }
        .section-title { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; color: #64748b; margin-bottom: 12px; }
        .med-item { display: flex; gap: 12px; align-items: flex-start; border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px 14px; margin-bottom: 8px; }
        .med-icon { width: 28px; height: 28px; background: #dbeafe; border-radius: 8px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; color: #2563eb; font-size: 14px; }
        .med-name { font-weight: 600; font-size: 13px; }
        .med-detail { font-size: 12px; color: #64748b; margin-top: 2px; }
        .notes-box { border: 1px solid #fcd34d; background: #fffbeb; border-radius: 10px; padding: 14px; margin-top: 16px; }
        .notes-label { font-size: 11px; font-weight: 700; text-transform: uppercase; color: #92400e; margin-bottom: 4px; }
        .notes-text { font-size: 13px; color: #78350f; }
        .footer { display: flex; justify-content: space-between; align-items: flex-end; border-top: 1px solid #e2e8f0; margin-top: 24px; padding-top: 16px; }
        .sig-box { width: 140px; height: 64px; border: 2px dashed #e2e8f0; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 11px; color: #94a3b8; margin-bottom: 6px; }
        .sig-name { font-size: 11px; font-weight: 600; color: #64748b; }
        .legal { font-size: 11px; text-align: center; color: #94a3b8; border-top: 1px solid #e2e8f0; margin-top: 16px; padding-top: 12px; }
      </style>
    </head>
    <body>
      <div class="card">
        <div class="header">
          <div class="header-left">
            <div class="header-logo">✚ MedApp</div>
            <span class="header-sub">Ordonnance médicale officielle</span>
          </div>
          <div class="header-right">
            <strong>${rx.value.doctorName}</strong>
            <p>Médecin</p>
          </div>
        </div>
        <div class="body">
          <div class="row-between">
            <div><div class="label">N° Ordonnance</div><div class="value">${rx.value.id.toUpperCase()}</div></div>
            <div style="text-align:center"><div class="label">Date d'émission</div><div class="value">${fmt(rx.value.issueDate)}</div></div>
            <div style="text-align:right"><div class="label">Valide jusqu'au</div><div class="value" style="color:#16a34a">${fmt(rx.value.validityDate)}</div></div>
          </div>
          <div class="patient-box">
            <div class="section-title">Informations patient</div>
            <div class="grid2">
              <div><div class="label">Nom complet</div><div class="value">${p.value.firstName} ${p.value.lastName}</div></div>
              <div><div class="label">Date de naissance</div><div class="value">${fmt(p.value.birthDate)}</div></div>
              <div><div class="label">Téléphone</div><div class="value">${p.value.phone || 'Non renseigné'}</div></div>
            </div>
          </div>
          <div class="section-title">Prescriptions</div>
          ${(rx.value.medications || []).map(m => `
            <div class="med-item">
              <div class="med-icon">💊</div>
              <div>
                <div class="med-name">${m.name}</div>
                <div class="med-detail">${[m.dosage, m.frequency, m.duration].filter(Boolean).join(' · ')}</div>
              </div>
            </div>
          `).join('')}
          ${rx.value.notes ? `
            <div class="notes-box">
              <div class="notes-label">Instructions</div>
              <div class="notes-text">${rx.value.notes}</div>
            </div>
          ` : ''}
          <div class="footer">
            <div>
              <div class="sig-box">Signature du médecin</div>
              <div class="sig-name">${rx.value.doctorName}</div>
            </div>
          </div>
          <div class="legal">Document généré par MedApp · Conforme à la réglementation française en matière de prescription médicale électronique</div>
        </div>
      </div>
    </body>
    </html>
  `)
  win.document.close()
  win.focus()
  setTimeout(() => {
    win.print()
    win.close()
  }, 400)
}
</script>

<template>
  <div class="p-6 space-y-4 print:p-0 print:m-0">
    <div class="flex items-center justify-between flex-wrap gap-3 print:hidden">
      <div class="flex items-center gap-2 text-sm text-muted-foreground">
        <button @click="showScreen(screens.ordonnances)" class="hover:text-foreground">Ordonnances</button>
        <ChevronRight class="w-3 h-3" />
        <span class="text-foreground">Aperçu PDF</span>
      </div>
      <div class="flex gap-2">
        <button @click="ordonnanceStore.downloadPdf(rx.id)" class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
          <Download class="w-4 h-4" /> Exporter PDF
        </button>
        <button @click="printOrdonnance" class="bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
          <Printer class="w-4 h-4" /> Imprimer
        </button>
      </div>
    </div>

    <div class="flex justify-center print:block print:w-full">
      <div v-motion :initial="{ opacity: 0, y: 16 }" :enter="{ opacity: 1, y: 0 }" class="w-full max-w-2xl print:max-w-none print:w-full">
        <div data-print-target class="rounded-2xl border border-border overflow-hidden shadow-2xl bg-card print:border-none print:shadow-none print:rounded-none">
          <div class="bg-gradient-to-r from-blue-600 to-blue-700 p-6 text-white">
            <div class="flex items-start justify-between">
              <div>
                <div class="flex items-center gap-2 mb-0.5">
                  <Stethoscope class="w-5 h-5" />
                  <span class="font-bold text-lg">MedApp</span>
                </div>
                <p class="text-blue-200 text-sm">Ordonnance médicale officielle</p>
              </div>
              <div class="text-right text-sm">
                <p class="font-semibold">{{ rx.doctorName }}</p>
                <p class="text-blue-200">Médecin</p>
                <!-- Optional: add RPPS or other info if available in the future -->
              </div>
            </div>
          </div>

          <div class="p-8 space-y-6 bg-white dark:bg-card">
            <div class="flex justify-between text-sm border-b border-border pb-4">
              <div>
                <p class="text-muted-foreground text-xs font-semibold uppercase tracking-wider mb-1">N° Ordonnance</p>
                <p class="font-mono font-bold text-foreground text-base">{{ rx.id.toUpperCase() }}</p>
              </div>
              <div class="text-center">
                <p class="text-muted-foreground text-xs font-semibold uppercase tracking-wider mb-1">Date d'émission</p>
                <p class="font-bold text-foreground">{{ fmt(rx.issueDate) }}</p>
              </div>
              <div class="text-right">
                <p class="text-muted-foreground text-xs font-semibold uppercase tracking-wider mb-1">Valide jusqu'au</p>
                <p class="font-bold text-emerald-600">{{ fmt(rx.validityDate) }}</p>
              </div>
            </div>

            <div class="bg-muted/50 rounded-xl p-4">
              <p class="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-3">Informations patient</p>
              <div class="grid grid-cols-2 gap-3 text-sm">
                <div><p class="text-muted-foreground text-xs">Nom complet</p><p class="font-semibold text-foreground mt-0.5">{{ p.firstName }} {{ p.lastName }}</p></div>
                <div><p class="text-muted-foreground text-xs">Date de naissance</p><p class="font-semibold text-foreground mt-0.5">{{ fmt(p.birthDate) }}</p></div>
                <div><p class="text-muted-foreground text-xs">Téléphone</p><p class="font-semibold text-foreground mt-0.5">{{ p.phone || 'Non renseigné' }}</p></div>
              </div>
            </div>

            <div>
              <p class="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-3">Prescriptions</p>
              <div class="space-y-2">
                <div v-for="(m, i) in rx.medications" :key="i" class="flex items-start gap-3 p-3.5 border border-border rounded-xl">
                  <div class="w-7 h-7 bg-blue-100 dark:bg-blue-900/30 rounded-lg flex items-center justify-center shrink-0 mt-0.5">
                    <Pill class="w-3.5 h-3.5 text-blue-600" />
                  </div>
                  <div>
                    <p class="font-semibold text-foreground">{{ m.name }}</p>
                    <p class="text-xs text-muted-foreground mt-0.5">{{ m.dosage }}{{ m.dosage && m.frequency ? ' · ' : '' }}{{ m.frequency }}{{ (m.dosage || m.frequency) && m.duration ? ' · ' : '' }}{{ m.duration }}</p>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="rx.notes" class="border border-amber-200 dark:border-amber-800 bg-amber-50 dark:bg-amber-900/20 rounded-xl p-4">
              <p class="text-xs font-bold text-amber-700 dark:text-amber-400 uppercase tracking-wider mb-1">Instructions</p>
              <p class="text-sm text-amber-900 dark:text-amber-300">{{ rx.notes }}</p>
            </div>

            <div class="flex justify-between items-end pt-4 border-t border-border">
              <div>
                <div class="w-36 h-16 border-2 border-dashed border-border rounded-xl flex items-center justify-center mb-2">
                  <p class="text-xs text-muted-foreground">Signature du médecin</p>
                </div>
                <p class="text-xs text-muted-foreground font-medium">{{ rx.doctorName }}</p>
              </div>
              <div class="flex flex-col items-center gap-1.5">
                <div class="w-16 h-16 bg-muted rounded-xl flex items-center justify-center border border-border">
                  <QrCode class="w-9 h-9 text-muted-foreground" />
                </div>
                <p class="text-xs text-muted-foreground">Vérification</p>
              </div>
            </div>

            <p class="text-xs text-center text-muted-foreground border-t border-border pt-4 leading-relaxed">
              Document généré par MedApp · Conforme à la réglementation française en matière de prescription médicale électronique
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
