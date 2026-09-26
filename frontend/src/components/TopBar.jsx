import { CloseIcon } from './Icons';

export default function TopBar({ release, onClose }) {
  return (
    <div className="top-bar">
      <a href="#music">
        Nouveau son <em>« {release?.title ?? 'I Lose Myself'} »</em>, dispo le{' '}
        {release ? formatLong(release.releaseDate) : '2 octobre'} sur toutes les plateformes →
      </a>
      <button className="top-bar-close" aria-label="Fermer" onClick={onClose}><CloseIcon /></button>
    </div>
  );
}

const formatLong = (iso) => new Date(iso).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long' });
