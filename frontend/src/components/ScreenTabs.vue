<script setup>
import { screenButtons } from '../constants/medapp.js'
import { useMedAppState } from '../composables/useMedAppState.js'

const { currentScreen, showScreen } = useMedAppState()
</script>

<template>
  <section class="screen-tabs" aria-label="Navigation de maquette">
    <span class="screen-tabs-label">Maquettes :</span>
    <button
      v-for="screen in screenButtons"
      :key="screen.id"
      class="screen-tab-btn"
      :class="{ active: currentScreen === screen.id }"
      type="button"
      @click="showScreen(screen.id)"
    >
      {{ screen.label }}
    </button>
  </section>
</template>