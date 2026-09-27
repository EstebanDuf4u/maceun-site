/**
 * Logo DJM : version blanche sur fond sombre, noire sur fond clair.
 * Les deux sont empilées et on bascule en fondu (pas de saut d'image au changement de fond).
 */
export default function Logo({ onLight = false, className = '' }) {
  return (
    <span className={`djm ${onLight ? 'is-on-light' : ''} ${className}`} aria-hidden="true">
      <img src="/img/logo-blanc.png" alt="" className="djm-blanc" width="40" height="40" />
      <img src="/img/logo-noir.png" alt="" className="djm-noir" width="40" height="40" />
    </span>
  );
}
