import { useEffect, useState } from 'react';

const pad = (n) => String(n).padStart(2, '0');

function remaining(target) {
  let diff = Math.max(0, target - Date.now());
  const d = Math.floor(diff / 864e5); diff -= d * 864e5;
  const h = Math.floor(diff / 36e5); diff -= h * 36e5;
  const m = Math.floor(diff / 6e4); diff -= m * 6e4;
  return { d, h, m, s: Math.floor(diff / 1e3), over: target <= Date.now() };
}

export default function Countdown({ date }) {
  // la sortie est à minuit heure de Paris
  const target = new Date(`${date}T00:00:00+02:00`).getTime();
  const [t, setT] = useState(() => remaining(target));
  useEffect(() => {
    const id = setInterval(() => setT(remaining(target)), 1000);
    return () => clearInterval(id);
  }, [target]);
  if (t.over) return null;
  return (
    <div className="countdown">
      <div><strong>{pad(t.d)}</strong><span>Jours</span></div>
      <div><strong>{pad(t.h)}</strong><span>Heures</span></div>
      <div><strong>{pad(t.m)}</strong><span>Min</span></div>
      <div><strong>{pad(t.s)}</strong><span>Sec</span></div>
    </div>
  );
}
