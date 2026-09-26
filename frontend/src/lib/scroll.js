import Lenis from 'lenis';

export const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

/** Instance unique du smooth scroll, partagée par toute l'app. */
export const lenis = reduceMotion ? null : new Lenis({ lerp: 0.1, smoothWheel: true, autoRaf: true });
lenis?.stop();

export function scrollToTarget(target) {
  if (lenis) return lenis.scrollTo(target, { duration: 1.6 });
  if (typeof target === 'number') window.scrollTo({ top: target, behavior: 'smooth' });
  else document.querySelector(target)?.scrollIntoView({ behavior: 'smooth' });
}

export const lockScroll = () => lenis?.stop();
export const unlockScroll = () => lenis?.start();
