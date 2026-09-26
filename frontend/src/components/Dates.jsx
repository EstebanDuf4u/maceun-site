export default function Dates({ data, loading, error, social }) {
  return (
    <section className="section section-light tour" id="tour">
      <div className="section-head">
        <h2 className="section-title reveal">Dates</h2>
        <p className="section-sub reveal">Prochains shows</p>
      </div>
      <div className="dates">
        {loading && <div className="dates-empty">Chargement des dates…</div>}
        {!loading && (error || !data?.length) && (
          <div className="dates-empty">
            {error ? 'Impossible de charger les dates.' : 'Aucune date annoncée pour le moment.'}
            {social && (<><br /><a href={social.url} target="_blank" rel="noopener">Suivez-le sur {social.label}</a> pour les prochaines annonces.</>)}
          </div>
        )}
        {data?.map((ev) => <DateRow key={ev.id ?? ev.datetime} ev={ev} />)}
      </div>
    </section>
  );
}

function DateRow({ ev }) {
  const d = new Date(ev.datetime);
  const day = d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short' }).replace('.', '');
  const time = d.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  return (
    <div className="date-row reveal">
      <div className="date-day">{day}<small>{d.getFullYear()} · {time}</small></div>
      <div className="date-venue">{ev.venue || 'TBA'}</div>
      <div className="date-city">{[ev.city, ev.country].filter(Boolean).join(', ')}</div>
      {ev.ticketUrl
        ? <a className="btn" href={ev.ticketUrl} target="_blank" rel="noopener"><span>Tickets</span></a>
        : <span className="eyebrow">Free entry</span>}
    </div>
  );
}
