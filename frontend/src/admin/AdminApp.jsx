import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '../lib/api';
import Login from './Login';
import ReleasesAdmin from './ReleasesAdmin';
import EventsAdmin from './EventsAdmin';
import PhotosAdmin from './PhotosAdmin';
import './admin.css';

const TABS = [
  { id: 'releases', label: 'Sorties', component: ReleasesAdmin },
  { id: 'events', label: 'Dates', component: EventsAdmin },
  { id: 'photos', label: 'Photos', component: PhotosAdmin },
];

const tabFromHash = () => TABS.find((t) => `#${t.id}` === window.location.hash)?.id ?? 'releases';

export default function AdminApp() {
  const [user, setUser] = useState(undefined);        // undefined = vérification en cours
  const [tab, setTab] = useState(tabFromHash);
  const [toast, setToast] = useState(null);

  useEffect(() => {
    document.body.className = 'admin';
    document.title = 'Admin · DJ Maceun';
    adminApi.me().then(setUser).catch(() => setUser(null));
    const onHash = () => setTab(tabFromHash());
    window.addEventListener('hashchange', onHash);
    return () => window.removeEventListener('hashchange', onHash);
  }, []);

  const notify = useCallback((message, type = 'ok') => {
    setToast({ message, type, key: Date.now() });
  }, []);

  useEffect(() => {
    if (!toast) return;
    const id = setTimeout(() => setToast(null), 3500);
    return () => clearTimeout(id);
  }, [toast]);

  /** Toute erreur 401 renvoie à l'écran de connexion. */
  const onError = useCallback((err) => {
    if (err.status === 401) { setUser(null); notify('Session expirée, reconnectez-vous.', 'error'); return; }
    notify(err.message || 'Une erreur est survenue.', 'error');
  }, [notify]);

  const logout = async () => {
    await adminApi.logout().catch(() => {});
    setUser(null);
  };

  if (user === undefined) return <div className="adm-splash"><span className="adm-spinner" /></div>;
  if (!user) return <Login onLogged={setUser} />;

  const Current = TABS.find((t) => t.id === tab).component;
  return (
    <div className="adm">
      <header className="adm-top">
        <a href="/admin" className="adm-brand">
          <svg viewBox="0 0 100 100"><circle cx="50" cy="50" r="46" /><path d="M44 22h12v22h22v12H56v22H44V56H22V44h22z" /></svg>
          <span>MACEUN</span><small>admin</small>
        </a>
        <nav className="adm-tabs">
          {TABS.map((t) => (
            <a key={t.id} href={`#${t.id}`} className={t.id === tab ? 'is-active' : ''}>{t.label}</a>
          ))}
        </nav>
        <div className="adm-user">
          <a href="/" target="_blank" rel="noopener" className="adm-link">Voir le site ↗</a>
          <button className="adm-btn adm-btn-ghost" onClick={logout}>Déconnexion</button>
        </div>
      </header>

      <main className="adm-main">
        <Current notify={notify} onError={onError} />
      </main>

      {toast && (
        <div key={toast.key} className={`adm-toast adm-toast-${toast.type}`} role="status">{toast.message}</div>
      )}
    </div>
  );
}
