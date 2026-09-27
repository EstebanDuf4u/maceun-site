import { useEffect, useState } from 'react';

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
