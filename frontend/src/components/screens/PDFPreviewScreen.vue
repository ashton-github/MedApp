<script setup>
import {
  ChevronRight,
  Download,
  Printer,
  Stethoscope,
  Pill,
  QrCode
} from 'lucide-vue-next'
import { useMedAppState } from '../../composables/useMedAppState.js'
import { screens } from '../../constants/medapp.js'

const { showScreen } = useMedAppState()

const rx = {
  id: "rx1",
  patientId: "p1",
  date: "2026-07-08",
  doctorName: "Dr. Martin",
  status: "ACTIVE",
  medications: [
    { name: "Amoxicilline 1g", dosage: "1 matin, 1 soir", duration: "7 jours" }
  ],
  notes: ""
}

const p = {
  id: "p1",
  firstName: "Sophie",
  lastName: "Laurent",
  dob: "1985-03-15",
  insurance: "CPAM Paris",
  phone: "+33 6 12 34 56 78"
}

const fmt = (d) => new Date(d).toLocaleDateString("fr-FR", { day: "2-digit", month: "short", year: "numeric" })
</script>

<template>
  <div class="p-6 space-y-4">
    <div class="flex items-center justify-between flex-wrap gap-3">
      <div class="flex items-center gap-2 text-sm text-muted-foreground">
        <button @click="showScreen(screens.ordonnances)" class="hover:text-foreground">Ordonnances</button>
        <ChevronRight class="w-3 h-3" />
        <span class="text-foreground">Aperçu PDF</span>
      </div>
      <div class="flex gap-2">
        <button class="border border-border text-foreground hover:bg-accent inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
          <Download class="w-4 h-4" /> Exporter PDF
        </button>
        <button onclick="window.print()" class="bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200/50 dark:shadow-blue-900/30 inline-flex items-center justify-center rounded-xl font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring px-3 py-1.5 text-sm gap-1.5">
          <Printer class="w-4 h-4" /> Imprimer
        </button>
      </div>
    </div>

    <div class="flex justify-center">
      <div v-motion :initial="{ opacity: 0, y: 16 }" :enter="{ opacity: 1, y: 0 }" class="w-full max-w-2xl">
        <div class="rounded-2xl border border-border overflow-hidden shadow-2xl bg-card">
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
                <p class="font-semibold">Dr. Jean Martin</p>
                <p class="text-blue-200">Médecin généraliste</p>
                <p class="text-blue-300 text-xs font-mono">RPPS : 10004589231</p>
              </div>
            </div>
          </div>

          <div class="p-8 space-y-6 bg-white dark:bg-card">
            <div class="flex justify-between text-sm border-b border-border pb-4">
              <div>
                <p class="text-muted-foreground text-xs font-semibold uppercase tracking-wider mb-1">N° Ordonnance</p>
                <p class="font-mono font-bold text-foreground text-base">{{ rx.id.toUpperCase() }}</p>
              </div>
              <div class="text-right">
                <p class="text-muted-foreground text-xs font-semibold uppercase tracking-wider mb-1">Date</p>
                <p class="font-bold text-foreground">{{ fmt(rx.date) }}</p>
              </div>
            </div>

            <div class="bg-muted/50 rounded-xl p-4">
              <p class="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-3">Informations patient</p>
              <div class="grid grid-cols-2 gap-3 text-sm">
                <div><p class="text-muted-foreground text-xs">Nom complet</p><p class="font-semibold text-foreground mt-0.5">{{ p.firstName }} {{ p.lastName }}</p></div>
                <div><p class="text-muted-foreground text-xs">Date de naissance</p><p class="font-semibold text-foreground mt-0.5">{{ fmt(p.dob) }}</p></div>
                <div><p class="text-muted-foreground text-xs">Assurance</p><p class="font-semibold text-foreground mt-0.5">{{ p.insurance }}</p></div>
                <div><p class="text-muted-foreground text-xs">Téléphone</p><p class="font-semibold text-foreground mt-0.5">{{ p.phone }}</p></div>
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
                    <p class="text-xs text-muted-foreground mt-0.5">Posologie : {{ m.dosage }} · Durée : {{ m.duration }}</p>
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
                <p class="text-xs text-muted-foreground font-medium">Dr. Jean Martin</p>
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
