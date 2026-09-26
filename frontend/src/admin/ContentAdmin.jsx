import { useEffect, useState } from 'react';
import { adminApi } from '../lib/api';
import { Field, ImageUpload, useLoad } from './ui';

export default function ContentAdmin({ notify, onError }) {
  const [{ data: saved, loading }, , setSaved] = useLoad(adminApi.content, onError);
  const [form, setForm] = useState(null);
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);

  useEffect(() => { if (saved) setForm(structuredClone(saved)); }, [saved]);

  const dirty = form && saved && JSON.stringify(form) !== JSON.stringify(saved);

  // prévient si on quitte la page avec des modifications non enregistrées
  useEffect(() => {
    if (!dirty) return;
    const warn = (e) => { e.preventDefault(); e.returnValue = ''; };
    window.addEventListener('beforeunload', warn);
    return () => window.removeEventListener('beforeunload', warn);
  }, [dirty]);

  if (loading || !form) return <p className="adm-muted">Chargement…</p>;

  /** Modifie une valeur à un chemin donné, ex. set('hero', 'name')(valeur) */
  const set = (section, key) => (value) =>
    setForm((f) => ({ ...f, [section]: { ...f[section], [key]: value?.target ? value.target.value : value } }));
  const setList = (key, list) => setForm((f) => ({ ...f, [key]: list }));
  const err = (path) => errors[path];

  const save = async () => {
    setSaving(true); setErrors({});
    try {
      const result = await adminApi.saveContent(form);
      setSaved(result);
      notify('Contenu du site enregistré');
    } catch (e) {
      setErrors(e.fields || {});
      if (Object.keys(e.fields || {}).length) notify('Certains champs sont à corriger', 'error');
      else onError(e);
    } finally {
      setSaving(false);
    }
  };

  const { hero, about, contact } = form;

  return (
    <section className="adm-content">
      <div className="adm-head">
        <div>
          <h1>Infos</h1>
          <p className="adm-muted">Tous les textes, liens et images du site. Astuce : dans les textes, entourez un mot d'*étoiles* pour l'afficher en <em>italique</em> ; les liens et numéros de téléphone deviennent cliquables tout seuls.</p>
        </div>
      </div>

      <Panel title="Accueil" desc="Le grand écran d'ouverture du site.">
        <div className="adm-split">
          <div>
            <ImageUpload value={hero.image} onChange={set('hero', 'image')} onError={onError} />
            {err('hero.image') && <small className="adm-field-error">{err('hero.image')}</small>}
            <small className="adm-hint">Photo plein écran, format paysage (1920 px de large conseillé).</small>
          </div>
          <div className="adm-form-fields">
            <Field label="Nom affiché en géant" hint="Court, en majuscules (24 caractères max)." error={err('hero.name')}>
              <input value={hero.name} onChange={set('hero', 'name')} maxLength={24} />
            </Field>
            <Field label="Accroche" error={err('hero.tagline')}>
              <input value={hero.tagline ?? ''} onChange={set('hero', 'tagline')} maxLength={80} />
            </Field>
            <Field label="Styles" error={err('hero.genres')}>
              <input value={hero.genres ?? ''} onChange={set('hero', 'genres')} maxLength={120} />
            </Field>
          </div>
        </div>
      </Panel>

      <Panel title="L'artiste" desc="La section de présentation.">
        <div className="adm-split">
          <div>
            <ImageUpload value={about.image} onChange={set('about', 'image')} onError={onError} />
            <small className="adm-hint">Format portrait conseillé.</small>
          </div>
          <div className="adm-form-fields">
            <Field label="Texte principal (en gros)" error={err('about.lead')}>
              <textarea rows={4} value={about.lead ?? ''} onChange={set('about', 'lead')} maxLength={800} />
            </Field>
            <Field label="Texte secondaire" error={err('about.text')}>
              <textarea rows={4} value={about.text ?? ''} onChange={set('about', 'text')} maxLength={2000} />
            </Field>
            <Field label="Étiquettes" hint="Entrée pour ajouter." error={err('about.tags')}>
              <TagsInput value={about.tags} onChange={set('about', 'tags')} />
            </Field>
            <div className="adm-field">
              <span>Chiffres clés (4 max)</span>
              <ListEditor
                items={about.stats}
                max={4}
                empty={{ value: '', label: '' }}
                addLabel="+ Ajouter un chiffre"
                onChange={set('about', 'stats')}
                render={(s, update, i) => (
                  <div className="adm-row adm-row-tight">
                    <input placeholder="Valeur (ex : 2018)" value={s.value} onChange={(e) => update({ ...s, value: e.target.value })} maxLength={12} className={err(`about.stats[${i}].value`) ? 'is-invalid' : ''} />
                    <input placeholder="Libellé" value={s.label} onChange={(e) => update({ ...s, label: e.target.value })} maxLength={40} className={err(`about.stats[${i}].label`) ? 'is-invalid' : ''} />
                  </div>
                )}
              />
            </div>
          </div>
        </div>
      </Panel>

      <Panel title="Contact & booking">
        <div className="adm-row">
          <Field label="Téléphone" error={err('contact.phone')}>
            <input value={contact.phone ?? ''} onChange={set('contact', 'phone')} maxLength={30} />
          </Field>
          <Field label="Lien « tous les sons » (Fanlink…)" error={err('contact.streamingUrl')}>
            <input type="url" value={contact.streamingUrl ?? ''} onChange={set('contact', 'streamingUrl')} placeholder="https://" />
          </Field>
        </div>
        <Field label="Texte de la section booking" error={err('contact.bookingText')}>
          <textarea rows={2} value={contact.bookingText ?? ''} onChange={set('contact', 'bookingText')} maxLength={500} />
        </Field>
      </Panel>

      <Panel title="Réseaux sociaux" desc="Affichés dans le menu, en grand en bas de page, et dans les infos Google.">
        <div className="adm-split">
          <div>
            <ImageUpload value={contact.socialImage} onChange={set('contact', 'socialImage')} onError={onError} />
            <small className="adm-hint">Image de fond du bloc « Follow me ».</small>
          </div>
          <ListEditor
            items={form.socials}
            max={12}
            empty={{ label: '', url: '' }}
            addLabel="+ Ajouter un réseau"
            onChange={(list) => setList('socials', list)}
            render={(s, update, i) => (
              <div className="adm-row adm-row-social">
                <input placeholder="Nom (Instagram…)" value={s.label} onChange={(e) => update({ ...s, label: e.target.value })} maxLength={30} className={err(`socials[${i}].label`) ? 'is-invalid' : ''} />
                <input type="url" placeholder="https://" value={s.url} onChange={(e) => update({ ...s, url: e.target.value })} className={err(`socials[${i}].url`) ? 'is-invalid' : ''} />
              </div>
            )}
          />
        </div>
      </Panel>

      <Panel title="FAQ">
        <ListEditor
          items={form.faq}
          max={30}
          empty={{ question: '', answer: '' }}
          addLabel="+ Ajouter une question"
          onChange={(list) => setList('faq', list)}
          render={(q, update, i) => (
            <div className="adm-faq-item">
              <input placeholder="Question" value={q.question} onChange={(e) => update({ ...q, question: e.target.value })} maxLength={200} className={err(`faq[${i}].question`) ? 'is-invalid' : ''} />
              <textarea rows={2} placeholder="Réponse" value={q.answer} onChange={(e) => update({ ...q, answer: e.target.value })} maxLength={1500} className={err(`faq[${i}].answer`) ? 'is-invalid' : ''} />
            </div>
          )}
        />
      </Panel>

      <div className={`adm-savebar${dirty ? ' is-dirty' : ''}`}>
        <span>{dirty ? 'Modifications non enregistrées' : 'Tout est enregistré'}</span>
        <div>
          <button className="adm-btn adm-btn-ghost" disabled={!dirty || saving} onClick={() => { setForm(structuredClone(saved)); setErrors({}); }}>Annuler</button>
          <button className="adm-btn adm-btn-primary" disabled={!dirty || saving} onClick={save}>{saving ? 'Enregistrement…' : 'Enregistrer'}</button>
        </div>
      </div>
    </section>
  );
}

function Panel({ title, desc, children }) {
  return (
    <div className="adm-panel">
      <div className="adm-panel-head">
        <h2>{title}</h2>
        {desc && <p className="adm-muted">{desc}</p>}
      </div>
      <div className="adm-form">{children}</div>
    </div>
  );
}

/** Liste modifiable : monter / descendre / supprimer / ajouter. */
function ListEditor({ items, onChange, render, empty, max, addLabel }) {
  const update = (i) => (value) => onChange(items.map((x, j) => (j === i ? value : x)));
  const move = (i, dir) => {
    const next = [...items];
    [next[i], next[i + dir]] = [next[i + dir], next[i]];
    onChange(next);
  };
  return (
    <div className="adm-listedit">
      {items.map((item, i) => (
        <div className="adm-listedit-row" key={i}>
          <div className="adm-listedit-body">{render(item, update(i), i)}</div>
          <div className="adm-listedit-tools">
            <button type="button" className="adm-icon-btn" disabled={i === 0} onClick={() => move(i, -1)} aria-label="Monter">↑</button>
            <button type="button" className="adm-icon-btn" disabled={i === items.length - 1} onClick={() => move(i, 1)} aria-label="Descendre">↓</button>
            <button type="button" className="adm-icon-btn adm-icon-danger" onClick={() => onChange(items.filter((_, j) => j !== i))} aria-label="Retirer">✕</button>
          </div>
        </div>
      ))}
      {items.length < max && (
        <button type="button" className="adm-btn adm-btn-ghost adm-btn-add" onClick={() => onChange([...items, { ...empty }])}>{addLabel}</button>
      )}
    </div>
  );
}

function TagsInput({ value, onChange }) {
  const [draft, setDraft] = useState('');
  const add = () => {
    const tag = draft.trim();
    if (tag && !value.includes(tag) && value.length < 20) onChange([...value, tag]);
    setDraft('');
  };
  return (
    <div className="adm-tags">
      {value.map((t) => (
        <span key={t} className="adm-tag">
          {t}
          <button type="button" onClick={() => onChange(value.filter((x) => x !== t))} aria-label={`Retirer ${t}`}>✕</button>
        </span>
      ))}
      <input
        value={draft}
        onChange={(e) => setDraft(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ',') { e.preventDefault(); add(); }
          if (e.key === 'Backspace' && !draft && value.length) onChange(value.slice(0, -1));
        }}
        onBlur={add}
        placeholder="Ajouter…"
        maxLength={40}
      />
    </div>
  );
}
