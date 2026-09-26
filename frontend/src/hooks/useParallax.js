import { useEffect } from 'react';
import { reduceMotion } from '../lib/scroll';

/** Parallaxe des images `.parallax[data-speed]` et de l'image du hero. */
export function useParallax(ready) {
  useEffect(() => {
    if (reduceMotion || !ready) return;
    const hero = document.getElementById('hero-img');
    let started = false;
    const onScroll = () => {
      const vh = window.innerHeight;
      document.querySelectorAll('.parallax').forEach((el) => {
        const r = el.parentElement.getBoundingClientRect();
        if (r.bottom < 0 || r.top > vh) return;
        const offset = (r.top + r.height / 2 - vh / 2) * parseFloat(el.dataset.speed || 0.1);
        el.style.transform = `translate3d(0, ${-offset}px, 0)`;
      });
      const y = window.scrollY;
      if (hero && y < vh && (y > 0 || started)) {
        started = true;
        hero.style.transition = 'none';
        hero.style.transform = `translate3d(0, ${y * 0.35}px, 0) scale(${1.05 + (y / vh) * 0.1})`;
      }
    };
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, [ready]);
}
