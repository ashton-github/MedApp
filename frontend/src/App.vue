<script setup>
import { ref, onMounted } from 'vue'
import api from './services/api.js'

const backendStatus = ref('en cours de vérification...')
const mongoStatus = ref('-')
const hasError = ref(false)

onMounted(async () => {
  try {
    const { data } = await api.get('/health')
    backendStatus.value = data.status
    mongoStatus.value = data.mongodb
  } catch (e) {
    hasError.value = true
    backendStatus.value = 'injoignable'
  }
})
</script>

<template>
  <main class="container">
    <h1>MedApp — Environnement de développement</h1>
    <p>Squelette minimal de la stack VueJS / Spring Boot / MongoDB.</p>

    <section class="status" :class="{ error: hasError }">
      <h2>Statut de la stack</h2>
      <ul>
        <li>Backend (Spring Boot) : <strong>{{ backendStatus }}</strong></li>
        <li>Base de données (MongoDB) : <strong>{{ mongoStatus }}</strong></li>
      </ul>
    </section>

    <section>
      <h2>Prochaine étape</h2>
      <p>
        Commencer par écrire un test qui échoue (Red) pour la première
        fonctionnalité d'authentification, avant d'implémenter le code
        correspondant.
      </p>
    </section>
  </main>
</template>

<style scoped>
.container {
  max-width: 640px;
  margin: 3rem auto;
  font-family: system-ui, sans-serif;
  color: #222;
}
.status {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 1rem 1.5rem;
  background: #f7fbff;
}
.status.error {
  background: #fff4f4;
  border-color: #f0b4b4;
}
</style>
