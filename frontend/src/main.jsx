import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

// Le site public et l'admin sont chargés séparément : les visiteurs ne téléchargent pas l'admin.
const isAdmin = window.location.pathname.startsWith('/admin');
const load = isAdmin ? import('./admin/AdminApp.jsx') : import('./App.jsx');

load.then(({ default: Root }) => {
  createRoot(document.getElementById('root')).render(
    <StrictMode>
      <Root />
    </StrictMode>,
  );
});
