import { api } from '../lib/api';
import { useLoad } from './ui';

const formatDateTime = (iso) => new Date(iso).toLocaleString('fr-FR', {
  weekday: 'short', day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit',
});

/** Lecture seule : les dates se gèrent sur Bandsintown, le site les reprend automatiquement. */
export default function EventsAdmin({ onError }) {
  const [{ data: events, loading }] = useLoad(api.events, onError);
  return (
    <section>
      <div className="adm-head">
        <div>
          <h1>Dates</h1>
          <p className="adm-muted">
            Les dates viennent de Bandsintown. Pour en ajouter ou en modifier une, passez par{' '}
            <a href="https://artists.bandsintown.com/" target="_blank" rel="noopener">Bandsintown for Artists</a> :
            le site se met à jour tout seul en 10 minutes maximum.
          </p>
        </div>
        <a className="adm-btn adm-btn-primary" href="https://artists.bandsintown.com/" target="_blank" rel="noopener">Ouvrir Bandsintown ↗</a>
      </div>
      {loading && <p className="adm-muted">Chargement…</p>}
      {events?.length === 0 && <p className="adm-empty">Aucune date à venir sur Bandsintown.</p>}
      <ul className="adm-list is-readonly">
        {events?.map((ev) => (
          <li key={ev.id}>
            <div className="adm-list-main">
              <strong>{ev.venue}</strong>
              <span className="adm-muted">{[ev.city, ev.country].filter(Boolean).join(', ')}</span>
            </div>
            <div className="adm-list-date">{formatDateTime(ev.datetime)}</div>
          </li>
        ))}
      </ul>
    </section>
  );
}
