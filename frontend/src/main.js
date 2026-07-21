import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { MotionPlugin } from '@vueuse/motion'

import './styles/medapp.css'
import App from './App.vue'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(MotionPlugin)

// Attempt to restore session from httpOnly refresh cookie before first render
import { useAuthStore } from './stores/authStore.js'
const authStore = useAuthStore()
authStore.restoreSession().finally(() => {
  app.mount('#app')
})
