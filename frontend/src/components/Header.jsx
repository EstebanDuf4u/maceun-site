import { useEffect, useState } from 'react';
import { LogoMark } from './Icons';

export default function Header({ menuOpen, onToggleMenu }) {
  const [scrolled, setScrolled] = useState(false);
  const [hidden, setHidden] = useState(false);

  useEffect(() => {
    let lastY = window.scrollY;
    const onScroll = () => {
      const y = window.scrollY;
      setScrolled(y > 60);
      setHidden(y > lastY && y > window.innerHeight * 0.6);   // se cache en descendant, revient en remontant
      lastY = y;
    };
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  const cls = ['header', scrolled && 'is-scrolled', hidden && !menuOpen && 'is-hidden'].filter(Boolean).join(' ');
  return (
    <header className={cls}>
      <a href="#top" className="logo" aria-label="Accueil">
        <LogoMark />
        <span>MACEUN</span>
      </a>
      <button className="menu-btn" aria-expanded={menuOpen} aria-controls="nav" onClick={onToggleMenu}>
        <span className="menu-btn-label">{menuOpen ? 'Fermer' : 'Menu'}</span>
        <span className="menu-btn-icon"><i /><i /></span>
      </button>
    </header>
  );
}
