import { useEffect, useRef, useState } from 'react';
import { adminApi } from '../lib/api';

export function Modal({ title, onClose, children }) {
  useEffect(() => {
    const onKey = (e) => e.key === 'Escape' && onClose();
    document.addEventListener('keydown', onKey);
    document.body.style.overflow = 'hidden';
    return () => { document.removeEventListener('keydown', onKey); document.body.style.overflow = ''; };
  }, [onClose]);
  return (
    <div className="adm-modal" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className="adm-modal-card" role="dialog" aria-modal="true" aria-label={title}>
        <div className="adm-modal-head">
          <h2>{title}</h2>
          <button className="adm-icon-btn" onClick={onClose} aria-label="Fermer">✕</button>
        </div>
        {children}
      </div>
    </div>
  );
}

export function Field({ label, error, hint, children }) {
  return (
    <label className={`adm-field${error ? ' has-error' : ''}`}>
      <span>{label}</span>
      {children}
      {hint && !error && <small className="adm-hint">{hint}</small>}
      {error && <small className="adm-field-error">{error}</small>}
    </label>
  );
}

/** Bouton de suppression en deux clics, pour éviter les erreurs. */
export function DeleteButton({ onConfirm, label = 'Supprimer' }) {
  const [armed, setArmed] = useState(false);
  useEffect(() => {
    if (!armed) return;
    const id = setTimeout(() => setArmed(false), 3000);
    return () => clearTimeout(id);
  }, [armed]);
  return (
    <button
      type="button"
      className={`adm-btn adm-btn-danger${armed ? ' is-armed' : ''}`}
      onClick={() => (armed ? onConfirm() : setArmed(true))}
    >
      {armed ? 'Confirmer ?' : label}
    </button>
  );
}

/** Choix d'une image : envoi immédiat au serveur, renvoie le chemin public. */
export function ImageUpload({ value, onChange, onError }) {
  const input = useRef(null);
  const [busy, setBusy] = useState(false);
  const [over, setOver] = useState(false);

  const send = async (file) => {
    if (!file) return;
    setBusy(true);
    try {
      const res = await adminApi.upload(file);
      onChange(res.src);
    } catch (err) {
      onError(err);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div
      className={`adm-upload${over ? ' is-over' : ''}`}
      onClick={() => input.current.click()}
      onDragOver={(e) => { e.preventDefault(); setOver(true); }}
      onDragLeave={() => setOver(false)}
      onDrop={(e) => { e.preventDefault(); setOver(false); send(e.dataTransfer.files[0]); }}
    >
      {value ? <img src={value} alt="" /> : <span className="adm-upload-empty">+</span>}
      <div className="adm-upload-label">{busy ? 'Envoi…' : value ? 'Changer l\'image' : 'Glisser une image ou cliquer'}</div>
      <input ref={input} type="file" accept="image/jpeg,image/png,image/webp" hidden onChange={(e) => send(e.target.files[0])} />
    </div>
  );
}

export function useLoad(loader, onError) {
  const [state, setState] = useState({ data: null, loading: true });
  const reload = async () => {
    try {
      setState({ data: await loader(), loading: false });
    } catch (err) {
      setState((s) => ({ ...s, loading: false }));
      onError(err);
    }
  };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { reload(); }, []);
  return [state, reload, (data) => setState({ data, loading: false })];
}

export const formatDate = (iso, opts = { day: 'numeric', month: 'long', year: 'numeric' }) =>
  new Date(iso).toLocaleDateString('fr-FR', opts);
