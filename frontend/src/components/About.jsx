import { useEffect, useRef } from 'react';
import { rich } from '../lib/text';

export default function About({ about }) {
  if (!about) return null;
  return (
    <section className="about" id="about">
      {about.image && (
        <div className="about-media">
          <img src={about.image} alt="" className="parallax" data-speed="0.12" loading="lazy" />
        </div>
      )}
      <div className="about-content">
        <p className="eyebrow reveal">L'artiste</p>
        {about.lead && <p className="about-lead reveal">{rich(about.lead)}</p>}
        {about.text && <p className="about-text reveal">{rich(about.text)}</p>}
        {about.tags?.length > 0 && <ul className="tags reveal">{about.tags.map((t) => <li key={t}>{t}</li>)}</ul>}
        {about.stats?.length > 0 && (
          <div className="stats reveal">
            {about.stats.map((s, i) => (
              <div key={i}><CountUp value={s.value} /><span>{s.label}</span></div>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

/** Compte jusqu'à la valeur quand le chiffre apparaît à l'écran (texte non numérique : affiché tel quel). */
function CountUp({ value }) {
  const ref = useRef(null);
  const target = /^\d+$/.test(value) ? parseInt(value, 10) : null;
  useEffect(() => {
    if (target === null) return;
    const el = ref.current;
    const io = new IntersectionObserver(([entry]) => {
      if (!entry.isIntersecting) return;
      io.disconnect();
      const from = target > 1000 ? target - 30 : 0;
      const t0 = performance.now();
      const step = (now) => {
        const p = Math.min((now - t0) / 1600, 1);
        el.textContent = Math.round(from + (target - from) * (1 - Math.pow(1 - p, 4)));
        if (p < 1) requestAnimationFrame(step);
      };
      requestAnimationFrame(step);
    }, { threshold: 0.6 });
    io.observe(el);
    return () => io.disconnect();
  }, [target]);
  return <strong ref={ref}>{value}</strong>;
}
