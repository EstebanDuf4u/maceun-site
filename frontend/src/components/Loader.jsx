import { useEffect, useRef, useState } from 'react';
import { reduceMotion } from '../lib/scroll';

/** Croix qui tourne par crans + compteur, puis le cercle s'ouvre sur le site. */
/** @param contentReady le contenu du site est chargé (sinon le hero serait vide à l'ouverture) */
export default function Loader({ onDone, contentReady }) {
  const [count, setCount] = useState(0);
  const [done, setDone] = useState(false);
  const onDoneRef = useRef(onDone);
  const readyRef = useRef(contentReady);
  readyRef.current = contentReady;

  useEffect(() => {
    const start = performance.now();
    const minDuration = reduceMotion ? 0 : 1600;
    let loaded = document.readyState === 'complete';
    const onLoad = () => (loaded = true);
    window.addEventListener('load', onLoad);
    let raf;
    const tick = (now) => {
      const t = Math.min((now - start) / minDuration, 1);
      const shown = loaded && readyRef.current ? t : Math.min(t, 0.9);     // bloque à 90 % tant que la page ou le contenu charge
      setCount(Math.round(shown * 100));
      if (shown < 1) { raf = requestAnimationFrame(tick); return; }
      setDone(true);
      onDoneRef.current();
    };
    raf = requestAnimationFrame(tick);
    return () => { cancelAnimationFrame(raf); window.removeEventListener('load', onLoad); };
  }, []);

  return (
    <div className={`loader${done ? ' is-done' : ''}`} aria-hidden="true">
      <svg className="loader-cross" viewBox="0 0 100 100"><path d="M42 10h16v32h32v16H58v32H42V58H10V42h32z" /></svg>
      <div className="loader-count">{String(count).padStart(3, '0')}</div>
    </div>
  );
}
