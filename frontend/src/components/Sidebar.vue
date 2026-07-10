<script setup>
import { addressMap, navMap, screens } from '../constants/medapp.js'
import { useMedAppState } from '../composables/useMedAppState.js'

const { currentScreen, showScreen } = useMedAppState()
</script>

<template>
  <section class="mockup-window">
    <div class="window-bar">
      <div class="dots">
        <span></span>
        <span></span>
        <span></span>
      </div>
      <span class="address">{{ addressMap[currentScreen] }}</span>
    </div>

    <div class="workspace">
      <aside v-if="currentScreen !== screens.login" class="sidebar">
        <div class="brand">MedApp</div>
        <button
          class="nav-item"
          :class="{ active: navMap[currentScreen] === 'patients' }"
          type="button"
          @click="showScreen(screens.patients)"
        >
          Patients
        </button>
        <button
          class="nav-item"
          :class="{ active: navMap[currentScreen] === 'ordonnances' }"
          type="button"
          @click="showScreen(screens.ordonnances)"
        >
          Ordonnances
        </button>
        <div class="sidebar-separator"></div>
        <button class="nav-item muted" type="button" @click="showScreen(screens.login)">Déconnexion</button>
      </aside>

      <div class="content">
        <slot />
      </div>
    </div>
  </section>
</template>