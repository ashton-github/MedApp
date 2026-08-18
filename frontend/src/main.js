import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { MotionPlugin } from '@vueuse/motion'

import './styles/medapp.css'
import App from './App.vue'
import router from './router/index.js'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(MotionPlugin)

// Attempt to restore session from httpOnly refresh cookie before first render.
// This must resolve before the router's first navigation guard runs, so the
// guard sees the correct isAuthenticated state instead of a false negative.
import { useAuthStore } from './stores/authStore.js'
const authStore = useAuthStore()
authStore.restoreSession().finally(() => {
  app.mount('#app')
})
