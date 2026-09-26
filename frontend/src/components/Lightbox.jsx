import { useEffect } from 'react';
import { lockScroll, unlockScroll } from '../lib/scroll';
import { CloseIcon } from './Icons';

export default function Lightbox({ src, onClose }) {
  useEffect(() => {
    if (!src) return;
    lockScroll();
    const onKey = (e) => e.key === 'Escape' && onClose();
    document.addEventListener('keydown', onKey);
    return () => { unlockScroll(); document.removeEventListener('keydown', onKey); };
  }, [src, onClose]);

  return (
    <div
      className={`lightbox${src ? ' is-open' : ''}`}
      aria-hidden={!src}
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <button className="lightbox-close" aria-label="Fermer" onClick={onClose}><CloseIcon /></button>
      {src && <img src={src} alt="" />}
    </div>
  );
}
