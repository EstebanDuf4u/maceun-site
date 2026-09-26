export default function Hero({ hero }) {
  const letters = [...(hero?.name ?? '')];
  return (
    <section className="hero" id="hero">
      <div className="hero-media">
        {hero?.image && <img src={hero.image} alt={`${hero.name} sur scène`} className="hero-img" id="hero-img" />}
        <div className="hero-shade" />
      </div>
      <div className="hero-content">
        {hero?.tagline && <p className="eyebrow hero-eyebrow split-up">{hero.tagline}</p>}
        {/* la taille du titre s'adapte au nombre de lettres pour toujours remplir la largeur */}
        <h1 className="hero-title" aria-label={hero?.name} style={{ '--letters': Math.max(letters.length, 3) }}>
          {letters.map((l, i) => (
            <span className="hero-letter" key={i} aria-hidden="true" style={{ transitionDelay: `${0.1 + i * 0.06}s` }}>
              {l === ' ' ? ' ' : l}
            </span>
          ))}
        </h1>
        <div className="hero-bottom">
          <p className="hero-genres split-up">{hero?.genres}</p>
          <a href="#music" className="btn btn-light split-up"><span>Écouter</span></a>
        </div>
      </div>
      <a href="#music" className="scroll-hint" aria-label="Défiler"><span>Scroll</span><i /></a>
    </section>
  );
}
