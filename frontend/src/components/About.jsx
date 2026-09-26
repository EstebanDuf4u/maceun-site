import { useEffect, useRef } from 'react';
import { TAGS } from '../data';

const STATS = [
  { value: 2018, label: 'Premiers sons' },
  { value: 4, label: 'Sorties Techno' },
  { value: 1, label: 'Nouveau single' },
];

export default function About() {
  return (
    <section className="about" id="about">
      <div className="about-media">
        <img src="/img/djm6.jpg" alt="Maceun aux platines" className="parallax" data-speed="0.12" loading="lazy" />
      </div>
      <div className="about-content">
        <p className="eyebrow reveal">L'artiste</p>
        <p className="about-lead reveal">
          Maceun a commencé la musique en <em>2018</em>, à <em>9 ans</em>. Depuis, il crée ses propres morceaux et
          s'est récemment tourné vers la Techno avec <em>« Feel Your Love »</em>, un vrai tournant dans sa carrière.
        </p>
        <p className="about-text reveal">
          Il a récemment joué à Paris, à La Boule Noire, avec le rappeur Rutra pour célébrer la sortie de son remix
          de <em>Salle Vide</em>. Il mixe aussi en festival et dans des événements privés : anniversaires, bals, soirées…
        </p>
        <ul className="tags reveal">{TAGS.map((t) => <li key={t}>{t}</li>)}</ul>
        <div className="stats reveal">
          {STATS.map((s) => (
            <div key={s.label}><CountUp value={s.value} /><span>{s.label}</span></div>
          ))}
        </div>
      </div>
    </section>
  );
}

/** Compte jusqu'à la valeur quand le chiffre apparaît à l'écran. */
function CountUp({ value }) {
  const ref = useRef(null);
  useEffect(() => {
    const el = ref.current;
    const io = new IntersectionObserver(([entry]) => {
      if (!entry.isIntersecting) return;
      io.disconnect();
      const from = value > 1000 ? value - 30 : 0;
      const t0 = performance.now();
      const step = (now) => {
        const p = Math.min((now - t0) / 1600, 1);
        el.textContent = Math.round(from + (value - from) * (1 - Math.pow(1 - p, 4)));
        if (p < 1) requestAnimationFrame(step);
      };
      requestAnimationFrame(step);
    }, { threshold: 0.6 });
    io.observe(el);
    return () => io.disconnect();
  }, [value]);
  return <strong ref={ref}>{value}</strong>;
}
