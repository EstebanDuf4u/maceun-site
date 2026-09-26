const LETTERS = 'MACEUN'.split('');

export default function Hero() {
  return (
    <section className="hero" id="hero">
      <div className="hero-media">
        <img src="/img/djm4.jpg" alt="Maceun sur scène" className="hero-img" id="hero-img" />
        <div className="hero-shade" />
      </div>
      <div className="hero-content">
        <p className="eyebrow hero-eyebrow split-up">DJ · Producer · Performer</p>
        <h1 className="hero-title" aria-label="Maceun">
          {LETTERS.map((l, i) => <span className="hero-letter" key={i} aria-hidden="true">{l}</span>)}
        </h1>
        <div className="hero-bottom">
          <p className="hero-genres split-up">House — Techno — Général</p>
          <a href="#music" className="btn btn-light split-up"><span>Écouter</span></a>
        </div>
      </div>
      <a href="#music" className="scroll-hint" aria-label="Défiler"><span>Scroll</span><i /></a>
    </section>
  );
}
