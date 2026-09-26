import { ArrowRight } from './Icons';

export default function Footer({ name, streamingUrl }) {
  return (
    <footer className="footer">
      {streamingUrl && (
        <div className="footer-top">
          <a href={streamingUrl} target="_blank" rel="noopener" className="footer-cta">
            <span>Tous les sons sur les plateformes</span>
            <ArrowRight />
          </a>
        </div>
      )}
      <div className="footer-wordmark" aria-hidden="true" style={{ '--letters': Math.max([...(name ?? '')].length, 3) }}>{name}</div>
      <div className="footer-meta">
        <span>© {new Date().getFullYear()} DJ {name && name.charAt(0) + name.slice(1).toLowerCase()} · Tous droits réservés</span>
        <a href="#top">Retour en haut ↑</a>
      </div>
    </footer>
  );
}
