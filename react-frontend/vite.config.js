import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  base: '/react-frontend/',
  server: {
    port: 5174,
    watch: {
      usePolling: true,
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080/jakarta-webapp',
        changeOrigin: true,
      },
      '/login': {
        target: 'http://localhost:8080/jakarta-webapp',
        changeOrigin: true,
      },
      '/logout': {
        target: 'http://localhost:8080/jakarta-webapp',
        changeOrigin: true,
      },
      '/home': {
        target: 'http://localhost:8080/jakarta-webapp',
        changeOrigin: true,
      },
    },
  },
})
