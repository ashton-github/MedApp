import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: true,
    port: 5173,
    watch: {
      usePolling: true // nécessaire pour le hot-reload dans un conteneur Docker
    }
  },
  test: {
    environment: 'jsdom',
    globals: true,

    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'dist/',
        'coverage/',
        'tests/',
        '**/*.config.*',
        '**/main.ts',
        '**/main.js'
      ]
    }
  }
})
