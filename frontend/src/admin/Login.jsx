import { useState } from 'react';
import { adminApi } from '../lib/api';

export default function Login({ onLogged }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true); setError('');
    try {
      onLogged(await adminApi.login(username, password));
    } catch (err) {
      setError(err.message);
      setLoading(false);
    }
  };

  return (
    <div className="adm-login">
      <form className="adm-login-card" onSubmit={submit}>
        <svg viewBox="0 0 100 100" className="adm-login-logo"><circle cx="50" cy="50" r="46" /><path d="M44 22h12v22h22v12H56v22H44V56H22V44h22z" /></svg>
        <h1>Espace admin</h1>
        <p className="adm-muted">Gérer les sorties, les dates et les photos du site.</p>
        <label className="adm-field">
          <span>Identifiant</span>
          <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" autoFocus required />
        </label>
        <label className="adm-field">
          <span>Mot de passe</span>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" required />
        </label>
        {error && <p className="adm-error" role="alert">{error}</p>}
        <button className="adm-btn adm-btn-primary adm-btn-block" disabled={loading}>
          {loading ? 'Connexion…' : 'Se connecter'}
        </button>
      </form>
    </div>
  );
}
