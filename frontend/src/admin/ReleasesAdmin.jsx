import { useState } from 'react';
import { adminApi } from '../lib/api';
import { DeleteButton, Field, Modal, formatDate, useLoad } from './ui';

export default function ReleasesAdmin({ notify, onError }) {
  const [{ data: releases, loading }, reload] = useLoad(adminApi.releases, onError);
  const [url, setUrl] = useState('');
  const [date, setDate] = useState('');
  const [adding, setAdding] = useState(false);
  const [addError, setAddError] = useState('');
  const [editing, setEditing] = useState(null);
  const [busy, setBusy] = useState(null);         // id en cours de mise à jour

  const today = new Date().toISOString().slice(0, 10);
  const featured = releases?.find((r) => r.visible && r.title && r.releaseDate);

  const add = async (e) => {
    e.preventDefault();
    setAdding(true); setAddError('');
    try {
      const r = await adminApi.addRelease(url, date);
      notify(`« ${r.title} » ajouté`);
      setUrl(''); setDate('');
      reload();
    } catch (err) {
      if (err.status === 401) onError(err); else setAddError(err.message);
    } finally {
      setAdding(false);
    }
  };

  const sync = async (r) => {
    setBusy(r.id);
    try {
      const updated = await adminApi.syncRelease(r.id);
      notify(updated.syncError ? updated.syncError : `« ${updated.title} » mis à jour`, updated.syncError ? 'error' : 'ok');
      reload();
    } catch (err) { onError(err); } finally { setBusy(null); }
  };

  const toggle = async (r) => {
    setBusy(r.id);
    try { await adminApi.updateRelease(r.id, !r.visible, r.manualReleaseDate); reload(); }
    catch (err) { onError(err); } finally { setBusy(null); }
  };

  const remove = async (r) => {
    try { await adminApi.deleteRelease(r.id); notify(`« ${r.title ?? r.url} » retiré`); reload(); }
    catch (err) { onError(err); }
  };

  return (
    <section>
      <div className="adm-head">
        <div>
          <h1>Sorties</h1>
          <p className="adm-muted">
            Collez le lien Fanlink d'un son : le titre, la pochette et les plateformes sont récupérés sur Fanlink,
            la date de sortie et le genre sur Deezer. Tout est remis à jour automatiquement toutes les 6 h.
          </p>
        </div>
      </div>

      <form className="adm-panel adm-add" onSubmit={add}>
        <Field label="Lien Fanlink" error={addError}>
          <input type="url" value={url} onChange={(e) => setUrl(e.target.value)} placeholder="https://maceun.fanlink.tv/…" required />
        </Field>
        <Field label="Date de sortie (optionnel)" hint="Seulement si le son n'est pas encore sorti.">
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        </Field>
        <button className="adm-btn adm-btn-primary" disabled={adding}>{adding ? 'Récupération…' : 'Ajouter'}</button>
      </form>

      {loading && <p className="adm-muted">Chargement…</p>}
      {releases?.length === 0 && <p className="adm-empty">Aucun son pour l'instant.</p>}

      <div className="adm-cards">
        {releases?.map((r) => (
          <article key={r.id} className={`adm-card${r.visible ? '' : ' is-hidden'}`}>
            {r.cover ? <img src={r.cover} alt="" className="adm-card-cover" /> : <div className="adm-card-cover" />}
            <div className="adm-card-body">
              <div className="adm-badges">
                {r.id === featured?.id && <span className="adm-badge adm-badge-accent">Mise en avant</span>}
                {r.releaseDate > today && <span className="adm-badge">À venir</span>}
                {!r.visible && <span className="adm-badge adm-badge-muted">Masqué</span>}
                {r.syncError && <span className="adm-badge adm-badge-error">Erreur</span>}
              </div>
              <h3>{r.title ?? 'Sans titre'}</h3>
              <p className="adm-muted">
                {[r.releaseDate && formatDate(r.releaseDate), r.genre].filter(Boolean).join(' · ') || 'Date inconnue'}
                {r.manualReleaseDate && ' (date saisie)'}
              </p>
              {r.platforms?.length > 0 && <p className="adm-platforms">{r.platforms.map((p) => p.name).join(' · ')}</p>}
              {r.syncError && <p className="adm-error">{r.syncError}</p>}
              <a href={r.url} target="_blank" rel="noopener" className="adm-card-link">{r.url.replace(/^https:\/\//, '')}</a>
              <p className="adm-hint">{r.syncedAt ? `Mis à jour ${formatDate(r.syncedAt, { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })}` : 'Jamais mis à jour'}</p>
              <div className="adm-card-actions">
                <button className="adm-btn adm-btn-small" disabled={busy === r.id} onClick={() => sync(r)}>{busy === r.id ? '…' : 'Actualiser'}</button>
                <button className="adm-btn adm-btn-small" disabled={busy === r.id} onClick={() => toggle(r)}>{r.visible ? 'Masquer' : 'Afficher'}</button>
                <button className="adm-btn adm-btn-small" onClick={() => setEditing(r)}>Date</button>
                <DeleteButton onConfirm={() => remove(r)} label="Retirer" />
              </div>
            </div>
          </article>
        ))}
      </div>

      {editing && (
        <DateForm
          release={editing}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); notify('Date enregistrée'); reload(); }}
          onError={onError}
        />
      )}
    </section>
  );
}

function DateForm({ release, onClose, onSaved, onError }) {
  const [date, setDate] = useState(release.manualReleaseDate ?? '');
  const [saving, setSaving] = useState(false);
  const save = async (e) => {
    e.preventDefault();
    setSaving(true);
    try { await adminApi.updateRelease(release.id, release.visible, date); onSaved(); }
    catch (err) { onError(err); setSaving(false); }
  };
  return (
    <Modal title={`Date de sortie · ${release.title ?? ''}`} onClose={onClose}>
      <form className="adm-form" onSubmit={save}>
        <p className="adm-muted">
          Utile uniquement pour un son pas encore sorti (compte à rebours sur le site).
          Dès que Deezer connaît le son, c'est sa date qui est utilisée.
        </p>
        <Field label="Date de sortie">
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        </Field>
        <div className="adm-form-actions">
          {release.manualReleaseDate && <button type="button" className="adm-btn adm-btn-ghost" onClick={() => setDate('')}>Effacer</button>}
          <button type="button" className="adm-btn adm-btn-ghost" onClick={onClose}>Annuler</button>
          <button className="adm-btn adm-btn-primary" disabled={saving}>{saving ? 'Enregistrement…' : 'Enregistrer'}</button>
        </div>
      </form>
    </Modal>
  );
}
