import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { MotionPlugin } from '@vueuse/motion'

import './styles/medapp.css'
import App from './App.vue'

const app = createApp(App)

app.use(createPinia())
app.use(MotionPlugin)

app.mount('#app')
