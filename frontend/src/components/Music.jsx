import { useRef } from 'react';
import Countdown from './Countdown';
import { ChevronLeft, ChevronRight } from './Icons';

export default function Music({ data, error }) {
  const sliderRef = useRef(null);

  const slide = (dir) => {
    const el = sliderRef.current;
    const card = el?.querySelector('.release-card');
    if (!card) return;
    const gap = parseFloat(getComputedStyle(el).columnGap) || 20;
    el.scrollBy({ left: dir * (card.offsetWidth + gap), behavior: 'smooth' });
  };

  return (
    <section className="section music" id="music">
      <div className="section-head">
        <h2 className="section-title reveal">Music</h2>
        <p className="section-sub reveal">Dernière sortie &amp; discographie</p>
      </div>

      {error && <p className="dates-empty">Impossible de charger la discographie pour le moment.</p>}
      {data ? <Feature release={data.latest} /> : !error && <div className="release-feature is-skeleton"><div className="release-cover" /></div>}

      {data?.previous?.length > 0 && (
        <>
          <div className="slider-head">
            <h3 className="slider-title reveal">Previous releases</h3>
            <div className="slider-arrows">
              <button className="arrow" aria-label="Précédent" onClick={() => slide(-1)}><ChevronLeft /></button>
              <button className="arrow" aria-label="Suivant" onClick={() => slide(1)}><ChevronRight /></button>
            </div>
          </div>
          <div className="releases" ref={sliderRef}>
            {data.previous.map((r) => (
              <a className="release-card reveal" href={r.url} target="_blank" rel="noopener" key={r.slug}>
                <div className="release-card-img">
                  <img src={r.cover} alt={`Pochette ${r.title}`} loading="lazy" />
                  <span className="play">▶</span>
                </div>
                <p className="release-card-meta">{pad(r.number)} · {new Date(r.releaseDate).getFullYear()} · {r.genre}</p>
                <h4>{r.title}{r.subtitle && <> <em>({r.subtitle})</em></>}</h4>
              </a>
            ))}
          </div>
        </>
      )}
    </section>
  );
}

function Feature({ release }) {
  const upcoming = new Date(`${release.releaseDate}T00:00:00+02:00`) > new Date();
  const dateLong = new Date(release.releaseDate).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long' });
  const dateShort = new Date(release.releaseDate).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit' }).replace('/', '.');

  const onTilt = (e) => {
    const r = e.currentTarget.getBoundingClientRect();
    const x = (e.clientX - r.left) / r.width - 0.5;
    const y = (e.clientY - r.top) / r.height - 0.5;
    e.currentTarget.firstChild.style.transform = `rotateY(${x * 12}deg) rotateX(${-y * 12}deg) scale(1.02)`;
  };
  const resetTilt = (e) => { e.currentTarget.firstChild.style.transform = ''; };

  return (
    <article className="release-feature reveal">
      <div className="release-cover tilt" onMouseMove={onTilt} onMouseLeave={resetTilt}>
        <img src={release.cover} alt={`Pochette ${release.title}`} />
        <span className="release-badge">New release</span>
      </div>
      <div className="release-info">
        <p className="eyebrow">Single · {new Date(release.releaseDate).getFullYear()}</p>
        <h3 className="release-title">{release.title}</h3>
        <p className="release-text">
          {upcoming
            ? `Retrouvez le dernier titre de DJ Maceun sur toutes les plateformes de streaming le ${dateLong}.`
            : 'Le dernier titre de DJ Maceun est disponible sur toutes les plateformes de streaming.'}
        </p>
        {upcoming && <Countdown date={release.releaseDate} />}
        <a href={release.url} target="_blank" rel="noopener" className="btn btn-light">
          <span>{upcoming ? `Pre-save · ${dateShort}` : 'Écouter maintenant'}</span>
        </a>
      </div>
    </article>
  );
}

const pad = (n) => String(n).padStart(2, '0');
