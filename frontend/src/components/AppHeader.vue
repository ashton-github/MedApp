<script setup>
defineProps({
  backendStatus: {
    type: String,
    required: true
  },
  mongoStatus: {
    type: String,
    required: true
  },
  backendUnavailable: {
    type: Boolean,
    default: false
  }
})
</script>

<template>
  <header class="hero">
    <div>
      <p class="eyebrow">MedApp</p>
      <h1>Prototype fonctionnel des écrans principaux</h1>
      <p class="lead">
        Connexion, patients et ordonnances sont navigables dans une seule interface Vue alignée sur la maquette.
      </p>
    </div>

    <section class="status-card" :class="{ error: backendUnavailable }" aria-label="Statut technique">
      <div>
        <span class="status-label">Backend</span>
        <strong>{{ backendStatus }}</strong>
      </div>
      <div>
        <span class="status-label">MongoDB</span>
        <strong>{{ mongoStatus }}</strong>
      </div>
    </section>
  </header>
</template>