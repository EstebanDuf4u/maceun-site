import { useCallback, useEffect, useState } from 'react';
import Loader from './components/Loader';
import TopBar from './components/TopBar';
import Header from './components/Header';
import Nav from './components/Nav';
import Hero from './components/Hero';
import Marquee from './components/Marquee';
import Music from './components/Music';
import Dates from './components/Dates';
import About from './components/About';
import Gallery from './components/Gallery';
import Faq from './components/Faq';
import Booking from './components/Booking';
import SocialCta from './components/SocialCta';
import Footer from './components/Footer';
import { useFetch } from './hooks/useFetch';
import { useParallax } from './hooks/useParallax';
import { useReveal } from './hooks/useReveal';
import { api } from './lib/api';
import { lockScroll, scrollToTarget, unlockScroll } from './lib/scroll';
import './styles/style.css';

const TOPBAR_KEY = 'topbar-closed';

export default function App() {
  const [ready, setReady] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [topBar, setTopBar] = useState(false);

  const releases = useFetch(api.releases);
  const events = useFetch(api.events);
  const photos = useFetch(api.photos);

  useReveal();
  useParallax(ready);

  // Les états globaux passent par des classes sur <body>, comme le CSS d'origine
  useEffect(() => {
    const b = document.body.classList;
    b.toggle('is-loading', !ready);
    b.toggle('is-ready', ready);
    b.toggle('menu-open', menuOpen);
    b.toggle('has-top-bar', topBar);
  }, [ready, menuOpen, topBar]);

  useEffect(() => {
    if (!ready) return;
    menuOpen ? lockScroll() : unlockScroll();
  }, [ready, menuOpen]);

  const onLoaded = useCallback(() => {
    setReady(true);
    let closed = false;
    try { closed = !!sessionStorage.getItem(TOPBAR_KEY); } catch { /* stockage indisponible */ }
    if (!closed) setTimeout(() => setTopBar(true), 1400);
  }, []);

  const closeTopBar = () => {
    setTopBar(false);
    try { sessionStorage.setItem(TOPBAR_KEY, '1'); } catch { /* stockage indisponible */ }
  };

  // Tous les liens d'ancre : ferme le menu puis scroll fluide
  useEffect(() => {
    const onClick = (e) => {
      const a = e.target.closest('a[href^="#"]');
      if (!a) return;
      const href = a.getAttribute('href');
      e.preventDefault();
      const wasOpen = document.body.classList.contains('menu-open');
      setMenuOpen(false);
      setTimeout(() => scrollToTarget(href === '#top' || href === '#' ? 0 : href), wasOpen ? 500 : 0);
    };
    const onKey = (e) => e.key === 'Escape' && setMenuOpen(false);
    document.addEventListener('click', onClick);
    document.addEventListener('keydown', onKey);
    return () => { document.removeEventListener('click', onClick); document.removeEventListener('keydown', onKey); };
  }, []);

  return (
    <>
      <Loader onDone={onLoaded} />
      <TopBar release={releases.data?.latest} onClose={closeTopBar} />
      <Header menuOpen={menuOpen} onToggleMenu={() => setMenuOpen((o) => !o)} />
      <Nav open={menuOpen} />
      <main id="top">
        <Hero />
        <Marquee release={releases.data?.latest} />
        <Music data={releases.data} error={releases.error} />
        <Dates data={events.data} loading={events.loading} error={events.error} />
        <About />
        <Gallery photos={photos.data} />
        <Faq />
        <Booking />
        <SocialCta />
      </main>
      <Footer />
    </>
  );
}
