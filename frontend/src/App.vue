<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { screens } from './constants/medapp.js'
import AppHeader from './components/AppHeader.vue'
import Sidebar from './components/Sidebar.vue'

const route = useRoute()

const isSidebarCollapsed = ref(false)
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}
</script>

<template>
  <div class="flex h-screen overflow-hidden bg-background text-foreground">
    <template v-if="route.meta.screen === screens.login">
      <router-view />
    </template>

    <template v-else>
      <Sidebar :collapsed="isSidebarCollapsed" @toggle="toggleSidebar" />
      <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
        <AppHeader />
        <main class="flex-1 overflow-y-auto bg-background/50">
          <router-view />
        </main>
      </div>
    </template>
  </div>
</template>
