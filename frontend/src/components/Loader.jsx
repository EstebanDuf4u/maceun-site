import { useEffect, useRef, useState } from 'react';
import { reduceMotion } from '../lib/scroll';

/** Logo qui tourne par crans + compteur, puis le cercle s'ouvre sur le site. */
export default function Loader({ onDone }) {
  const [count, setCount] = useState(0);
  const [done, setDone] = useState(false);
  const onDoneRef = useRef(onDone);

  useEffect(() => {
    const start = performance.now();
    const minDuration = reduceMotion ? 0 : 1600;
    let loaded = document.readyState === 'complete';
    const onLoad = () => (loaded = true);
    window.addEventListener('load', onLoad);
    let raf;
    const tick = (now) => {
      const t = Math.min((now - start) / minDuration, 1);
      const shown = loaded ? t : Math.min(t, 0.9);     // bloque à 90 % tant que la page charge
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
      <img src="/img/logo-blanc.png" alt="" className="loader-cross" width="96" height="96" />
      <div className="loader-count">{String(count).padStart(3, '0')}</div>
    </div>
  );
}
