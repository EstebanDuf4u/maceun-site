import { useState } from 'react';
import { adminApi } from '../lib/api';
import { DeleteButton, Field, Modal, useLoad } from './ui';

const EMPTY = { startsAt: '', venue: '', city: '', country: 'France', ticketUrl: '' };

const formatDateTime = (iso) => new Date(iso).toLocaleString('fr-FR', {
  weekday: 'short', day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit',
});

export default function EventsAdmin({ notify, onError }) {
  const [{ data: events, loading }, reload] = useLoad(adminApi.events, onError);
  const [{ data: bit }] = useLoad(adminApi.bandsintownEvents, onError);
  const [editing, setEditing] = useState(null);
  const now = new Date();

  const remove = async (ev) => {
    try {
      await adminApi.deleteEvent(ev.id.replace('m-', ''));
      notify('Date supprimée');
      reload();
    } catch (err) { onError(err); }
  };

  return (
    <section>
      <div className="adm-head">
        <div>
          <h1>Dates</h1>
          <p className="adm-muted">Les dates ajoutées ici s'affichent sur le site avec celles de Bandsintown, triées par date. Les dates passées disparaissent toutes seules.</p>
        </div>
        <button className="adm-btn adm-btn-primary" onClick={() => setEditing('new')}>+ Ajouter une date</button>
      </div>

      <h2 className="adm-h2">Ajoutées depuis l'admin</h2>
      {loading && <p className="adm-muted">Chargement…</p>}
      {events?.length === 0 && <p className="adm-empty">Aucune date ajoutée manuellement.</p>}
      <ul className="adm-list">
        {events?.map((ev) => (
          <li key={ev.id} className={new Date(ev.datetime) < now ? 'is-past' : ''}>
            <div className="adm-list-main">
              <strong>{ev.venue}</strong>
              <span className="adm-muted">{[ev.city, ev.country].filter(Boolean).join(', ')}</span>
            </div>
            <div className="adm-list-date">
              {formatDateTime(ev.datetime)}
              {new Date(ev.datetime) < now && <span className="adm-badge adm-badge-muted">Passée</span>}
            </div>
            <div className="adm-list-actions">
              <button className="adm-btn" onClick={() => setEditing(ev)}>Modifier</button>
              <DeleteButton onConfirm={() => remove(ev)} />
            </div>
          </li>
        ))}
      </ul>

      <h2 className="adm-h2">Bandsintown <span className="adm-badge">automatique</span></h2>
      <p className="adm-muted">Ces dates viennent de Bandsintown : pour les modifier, passez par <a href="https://artists.bandsintown.com/" target="_blank" rel="noopener">Bandsintown for Artists</a>. Le site se met à jour en 10 minutes.</p>
      {bit?.length === 0 && <p className="adm-empty">Aucune date à venir sur Bandsintown.</p>}
      <ul className="adm-list is-readonly">
        {bit?.map((ev) => (
          <li key={ev.id}>
            <div className="adm-list-main">
              <strong>{ev.venue}</strong>
              <span className="adm-muted">{[ev.city, ev.country].filter(Boolean).join(', ')}</span>
            </div>
            <div className="adm-list-date">{formatDateTime(ev.datetime)}</div>
          </li>
        ))}
      </ul>

      {editing && (
        <EventForm
          initial={editing === 'new' ? EMPTY : editing}
          onClose={() => setEditing(null)}
          onSaved={(isNew) => { setEditing(null); notify(isNew ? 'Date ajoutée' : 'Date modifiée'); reload(); }}
          onError={onError}
        />
      )}
    </section>
  );
}

function EventForm({ initial, onClose, onSaved, onError }) {
  const isNew = !initial.id;
  const [form, setForm] = useState({
    startsAt: initial.datetime?.slice(0, 16) ?? initial.startsAt,
    venue: initial.venue ?? '', city: initial.city ?? '', country: initial.country ?? '', ticketUrl: initial.ticketUrl ?? '',
  });
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true); setErrors({});
    try {
      if (isNew) await adminApi.createEvent(form);
      else await adminApi.updateEvent(initial.id.replace('m-', ''), form);
      onSaved(isNew);
    } catch (err) {
      setErrors(err.fields || {});
      if (!Object.keys(err.fields || {}).length) onError(err);
      setSaving(false);
    }
  };

  return (
    <Modal title={isNew ? 'Nouvelle date' : 'Modifier la date'} onClose={onClose}>
      <form className="adm-form" onSubmit={submit}>
        <Field label="Date et heure" error={errors.startsAt}>
          <input type="datetime-local" value={form.startsAt} onChange={set('startsAt')} required />
        </Field>
        <Field label="Lieu / événement" hint="Ex : La Boule Noire, Festival de…" error={errors.venue}>
          <input value={form.venue} onChange={set('venue')} required maxLength={200} />
        </Field>
        <div className="adm-row">
          <Field label="Ville" error={errors.city}>
            <input value={form.city} onChange={set('city')} required maxLength={120} />
          </Field>
          <Field label="Pays" error={errors.country}>
            <input value={form.country} onChange={set('country')} maxLength={120} />
          </Field>
        </div>
        <Field label="Lien billetterie (optionnel)" hint="Laisser vide pour afficher « Free entry »" error={errors.ticketUrl}>
          <input type="url" value={form.ticketUrl} onChange={set('ticketUrl')} placeholder="https://" />
        </Field>
        <div className="adm-form-actions">
          <button type="button" className="adm-btn adm-btn-ghost" onClick={onClose}>Annuler</button>
          <button className="adm-btn adm-btn-primary" disabled={saving}>{saving ? 'Enregistrement…' : 'Enregistrer'}</button>
        </div>
      </form>
    </Modal>
  );
}
