import { useEffect, useState } from 'react';
import Logo from './Logo';

/** Sections à fond clair : le header passe en noir (logo noir) quand il est au-dessus. */
const LIGHT_SECTIONS = '.section-light, .footer';

export default function Header({ menuOpen, onToggleMenu }) {
  const [scrolled, setScrolled] = useState(false);
  const [hidden, setHidden] = useState(false);
  const [onLight, setOnLight] = useState(false);

  useEffect(() => {
    let lastY = window.scrollY;
    const onScroll = () => {
      const y = window.scrollY;
      setScrolled(y > 60);
      setHidden(y > lastY && y > window.innerHeight * 0.6);   // se cache en descendant, revient en remontant
      lastY = y;
      // fond sous le milieu du header
      const probe = document.querySelector('.header')?.getBoundingClientRect();
      const mid = probe ? probe.top + probe.height / 2 : 40;
      setOnLight([...document.querySelectorAll(LIGHT_SECTIONS)].some((el) => {
        const r = el.getBoundingClientRect();
        return r.top <= mid && r.bottom >= mid;
      }));
    };
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    window.addEventListener('resize', onScroll);
    return () => { window.removeEventListener('scroll', onScroll); window.removeEventListener('resize', onScroll); };
  }, []);

  const light = onLight && !menuOpen;            // le menu ouvert est toujours sur fond sombre
  const cls = ['header', scrolled && 'is-scrolled', hidden && !menuOpen && 'is-hidden', light && 'is-on-light'].filter(Boolean).join(' ');
  return (
    <header className={cls}>
      <a href="#top" className="logo" aria-label="Accueil">
        <Logo onLight={light} className="logo-mark" />
        <span>MACEUN</span>
      </a>
      <button className="menu-btn" aria-expanded={menuOpen} aria-controls="nav" onClick={onToggleMenu}>
        <span className="menu-btn-label">{menuOpen ? 'Fermer' : 'Menu'}</span>
        <span className="menu-btn-icon"><i /><i /></span>
      </button>
    </header>
  );
}
