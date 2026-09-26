import { useState } from 'react';
import { api } from '../lib/api';
import { PHONE } from '../data';

const EMPTY = { nom: '', email: '', message: '', website: '' };

export default function Booking() {
  const [form, setForm] = useState(EMPTY);
  const [status, setStatus] = useState('idle');        // idle | sending | sent | error
  const [errors, setErrors] = useState({});
  const [errorMsg, setErrorMsg] = useState('');

  const update = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setStatus('sending'); setErrors({}); setErrorMsg('');
    try {
      await api.booking(form);
      setStatus('sent');
      setForm(EMPTY);
    } catch (err) {
      setStatus('error');
      setErrors(err.fields || {});
      setErrorMsg(err.message);
    }
  };

  return (
    <section className="section booking" id="booking">
      <div className="booking-grid">
        <div>
          <h2 className="section-title reveal">Booking</h2>
          <p className="booking-lead reveal">Travaillons <em>ensemble.</em></p>
          <p className="booking-text reveal">
            Vous voulez booker Maceun pour votre événement ? Remplissez le formulaire, vous aurez une réponse au plus vite.
          </p>
          <a href={PHONE.href} className="booking-phone reveal">{PHONE.label}</a>
        </div>

        <form className={`form reveal${status === 'sent' ? ' is-sent' : ''}`} onSubmit={submit} noValidate>
          <Field name="nom" label="Prénom + Nom" value={form.nom} onChange={update} error={errors.nom} />
          <Field name="email" type="email" label="Email" value={form.email} onChange={update} error={errors.email} />
          <Field name="message" label="Message (date, lieu, type d'événement…)" value={form.message} onChange={update} error={errors.message} textarea />
          {/* piège à bots, invisible pour les humains */}
          <label className="form-hp" aria-hidden="true">
            Site web <input name="website" tabIndex={-1} autoComplete="off" value={form.website} onChange={update} />
          </label>
          <button type="submit" className="btn btn-light btn-block" disabled={status === 'sending'}>
            <span>{status === 'sending' ? 'Envoi…' : status === 'error' ? 'Réessayer' : 'Envoyer la demande'}</span>
          </button>
          {status === 'error' && <p className="form-error" role="alert">{errorMsg}</p>}
          <p className="form-success" role="status">✓ Message envoyé. Réponse sous 48h.</p>
        </form>
      </div>
    </section>
  );
}

function Field({ name, label, error, textarea, ...props }) {
  const Tag = textarea ? 'textarea' : 'input';
  return (
    <label className="field">
      <Tag name={name} placeholder=" " required rows={textarea ? 4 : undefined} aria-invalid={!!error} {...props} />
      <span>{label}</span>
      {error && <small className="field-error">{error}</small>}
    </label>
  );
}
