import { useState } from 'react';
import { adminApi } from '../lib/api';
import { DeleteButton, Field, ImageUpload, Modal, formatDate, useLoad } from './ui';

const EMPTY = { title: '', subtitle: '', genre: 'Techno', releaseDate: '', cover: '', url: '', visible: true };
const GENRES = ['Techno', 'Techno / Hard Techno', 'Hard Techno', 'House', 'Remix', 'Général'];

export default function ReleasesAdmin({ notify, onError }) {
  const [{ data: releases, loading }, reload] = useLoad(adminApi.releases, onError);
  const [editing, setEditing] = useState(null);   // null | 'new' | release

  const today = new Date().toISOString().slice(0, 10);
  const latestVisible = releases?.find((r) => r.visible);

  const remove = async (r) => {
    try {
      await adminApi.deleteRelease(r.id);
      notify(`« ${r.title} » supprimée`);
      reload();
    } catch (err) { onError(err); }
  };

  return (
    <section>
      <div className="adm-head">
        <div>
          <h1>Sorties</h1>
          <p className="adm-muted">La sortie visible la plus récente est mise en avant sur le site (compte à rebours si elle n'est pas encore sortie).</p>
        </div>
        <button className="adm-btn adm-btn-primary" onClick={() => setEditing('new')}>+ Nouvelle sortie</button>
      </div>

      {loading && <p className="adm-muted">Chargement…</p>}
      {releases?.length === 0 && <p className="adm-empty">Aucune sortie pour l'instant.</p>}

      <div className="adm-cards">
        {releases?.map((r) => (
          <article key={r.id} className={`adm-card${r.visible ? '' : ' is-hidden'}`}>
            <img src={r.cover} alt="" className="adm-card-cover" />
            <div className="adm-card-body">
              <div className="adm-badges">
                {r.id === latestVisible?.id && <span className="adm-badge adm-badge-accent">Mise en avant</span>}
                {r.releaseDate > today && <span className="adm-badge">À venir</span>}
                {!r.visible && <span className="adm-badge adm-badge-muted">Masquée</span>}
              </div>
              <h3>{r.title}{r.subtitle && <em> ({r.subtitle})</em>}</h3>
              <p className="adm-muted">{String(r.number).padStart(2, '0')} · {formatDate(r.releaseDate)} · {r.genre}</p>
              <div className="adm-card-actions">
                <button className="adm-btn" onClick={() => setEditing(r)}>Modifier</button>
                <DeleteButton onConfirm={() => remove(r)} />
              </div>
            </div>
          </article>
        ))}
      </div>

      {editing && (
        <ReleaseForm
          initial={editing === 'new' ? EMPTY : editing}
          onClose={() => setEditing(null)}
          onSaved={(saved, isNew) => {
            setEditing(null);
            notify(isNew ? `« ${saved.title} » ajoutée` : 'Modifications enregistrées');
            reload();
          }}
          onError={onError}
        />
      )}
    </section>
  );
}

function ReleaseForm({ initial, onClose, onSaved, onError }) {
  const [form, setForm] = useState({ ...EMPTY, ...initial, subtitle: initial.subtitle ?? '' });
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);
  const isNew = !initial.id;
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true); setErrors({});
    const payload = { title: form.title, subtitle: form.subtitle, genre: form.genre, releaseDate: form.releaseDate,
      cover: form.cover, url: form.url, visible: form.visible };
    try {
      const saved = isNew ? await adminApi.createRelease(payload) : await adminApi.updateRelease(initial.id, payload);
      onSaved(saved, isNew);
    } catch (err) {
      setErrors(err.fields || {});
      if (!Object.keys(err.fields || {}).length) onError(err);
      setSaving(false);
    }
  };

  return (
    <Modal title={isNew ? 'Nouvelle sortie' : `Modifier « ${initial.title} »`} onClose={onClose}>
      <form className="adm-form adm-form-release" onSubmit={submit}>
        <div className="adm-form-cover">
          <ImageUpload value={form.cover} onChange={(cover) => setForm((f) => ({ ...f, cover }))} onError={onError} />
          {errors.cover && <small className="adm-field-error">{errors.cover}</small>}
          <small className="adm-hint">Carré, idéalement 1000 × 1000 px (JPG, PNG ou WebP, 10 Mo max).</small>
        </div>
        <div className="adm-form-fields">
          <Field label="Titre" error={errors.title}>
            <input value={form.title} onChange={set('title')} required maxLength={200} />
          </Field>
          <Field label="Précision (optionnel)" hint="Ex : Remix Techno, Extended Mix…" error={errors.subtitle}>
            <input value={form.subtitle} onChange={set('subtitle')} maxLength={200} />
          </Field>
          <div className="adm-row">
            <Field label="Genre" error={errors.genre}>
              <input value={form.genre} onChange={set('genre')} list="genres" required maxLength={120} />
              <datalist id="genres">{GENRES.map((g) => <option key={g} value={g} />)}</datalist>
            </Field>
            <Field label="Date de sortie" error={errors.releaseDate}>
              <input type="date" value={form.releaseDate} onChange={set('releaseDate')} required />
            </Field>
          </div>
          <Field label="Lien d'écoute / pre-save" hint="Fanlink, Hypeddit, Spotify…" error={errors.url}>
            <input type="url" value={form.url} onChange={set('url')} placeholder="https://" required />
          </Field>
          <label className="adm-check">
            <input type="checkbox" checked={form.visible} onChange={set('visible')} />
            <span>Visible sur le site</span>
          </label>
          <div className="adm-form-actions">
            <button type="button" className="adm-btn adm-btn-ghost" onClick={onClose}>Annuler</button>
            <button className="adm-btn adm-btn-primary" disabled={saving}>{saving ? 'Enregistrement…' : 'Enregistrer'}</button>
          </div>
        </div>
      </form>
    </Modal>
  );
}
