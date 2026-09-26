import { useEffect, useRef, useState } from 'react';
import { PHOTOS } from '../data';
import Lightbox from './Lightbox';

export default function Gallery({ photos }) {
  const list = photos?.length ? photos : PHOTOS;
  const trackRef = useRef(null);
  const moved = useRef(0);
  const [dragging, setDragging] = useState(false);
  const [open, setOpen] = useState(null);

  useEffect(() => {
    const track = trackRef.current;
    let down = false, startX = 0, startScroll = 0, lastX = 0, velocity = 0, raf;

    const onDown = (e) => {
      if (e.pointerType !== 'mouse') return;       // sur mobile, le scroll natif suffit
      down = true; moved.current = 0;
      startX = lastX = e.clientX; startScroll = track.scrollLeft;
      cancelAnimationFrame(raf);
      setDragging(true);
    };
    const onMove = (e) => {
      if (!down) return;
      velocity = e.clientX - lastX; lastX = e.clientX;
      moved.current = Math.max(moved.current, Math.abs(e.clientX - startX));
      track.scrollLeft = startScroll - (e.clientX - startX);
    };
    const onUp = () => {
      if (!down) return;
      down = false; setDragging(false);
      const glide = () => {                         // inertie
        velocity *= 0.94;
        track.scrollLeft -= velocity;
        if (Math.abs(velocity) > 0.5) raf = requestAnimationFrame(glide);
      };
      glide();
    };
    // molette verticale → défilement horizontal tant qu'on n'est pas au bout
    const onWheel = (e) => {
      if (Math.abs(e.deltaX) > Math.abs(e.deltaY)) return;
      const max = track.scrollWidth - track.clientWidth;
      const atEdge = (e.deltaY < 0 && track.scrollLeft <= 0) || (e.deltaY > 0 && track.scrollLeft >= max - 1);
      if (atEdge) return;
      e.preventDefault(); e.stopPropagation();
      track.scrollLeft += e.deltaY;
    };

    track.addEventListener('pointerdown', onDown);
    window.addEventListener('pointermove', onMove);
    window.addEventListener('pointerup', onUp);
    track.addEventListener('wheel', onWheel, { passive: false });
    return () => {
      cancelAnimationFrame(raf);
      track.removeEventListener('pointerdown', onDown);
      window.removeEventListener('pointermove', onMove);
      window.removeEventListener('pointerup', onUp);
      track.removeEventListener('wheel', onWheel);
    };
  }, []);

  return (
    <section className="section gallery" id="gallery">
      <div className="section-head">
        <h2 className="section-title reveal">Photos</h2>
        <p className="section-sub reveal">Live &amp; backstage</p>
      </div>
      <div className={`gallery-track${dragging ? ' is-dragging' : ''}`} ref={trackRef} data-lenis-prevent-wheel>
        {list.map((p) => (
          <figure
            key={p.id ?? p.src}
            className={`g-item g-${p.shape}`}
            onClick={() => moved.current <= 6 && setOpen(p.src)}   // un drag n'ouvre pas la photo
          >
            <img src={p.src} alt="" loading="lazy" draggable="false" />
          </figure>
        ))}
      </div>
      <p className="gallery-hint">← Glisser →</p>
      <Lightbox src={open} onClose={() => setOpen(null)} />
    </section>
  );
}
