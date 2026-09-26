import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    // en dev, l'API Spring Boot tourne sur :8080
    proxy: { '/api': 'http://localhost:8080' },
  },
});
