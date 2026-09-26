import { STREAMING_URL } from '../data';
import { ArrowRight } from './Icons';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="footer-top">
        <a href={STREAMING_URL} target="_blank" rel="noopener" className="footer-cta">
          <span>Tous les sons sur les plateformes</span>
          <ArrowRight />
        </a>
      </div>
      <div className="footer-wordmark" aria-hidden="true">MACEUN</div>
      <div className="footer-meta">
        <span>© {new Date().getFullYear()} DJ Maceun · Tous droits réservés</span>
        <a href="#top">Retour en haut ↑</a>
      </div>
    </footer>
  );
}
