import { useRef, useState } from 'react';
import { adminApi, api } from '../lib/api';
import { DeleteButton, useLoad } from './ui';

export default function PhotosAdmin({ notify, onError }) {
  const [{ data: photos, loading }, reload, setPhotos] = useLoad(api.photos, onError);
  const [uploading, setUploading] = useState(null);   // { done, total }
  const [over, setOver] = useState(false);
  const input = useRef(null);

  const upload = async (files) => {
    const list = [...files].filter((f) => f.type.startsWith('image/'));
    if (!list.length) return;
    let ok = 0;
    for (let i = 0; i < list.length; i++) {
      setUploading({ done: i, total: list.length });
      try { await adminApi.addPhoto(list[i]); ok++; } catch (err) { onError(err); }
    }
    setUploading(null);
    if (ok) notify(ok > 1 ? `${ok} photos ajoutées` : 'Photo ajoutée');
    reload();
  };

  const move = async (index, dir) => {
    const next = [...photos];
    const [item] = next.splice(index, 1);
    next.splice(index + dir, 0, item);
    setPhotos(next);                      // affichage immédiat
    try { await adminApi.reorderPhotos(next.map((p) => p.id)); } catch (err) { onError(err); reload(); }
  };

  const toggleShape = async (p) => {
    try {
      const updated = await adminApi.setPhotoShape(p.id, p.shape === 'wide' ? 'tall' : 'wide');
      setPhotos(photos.map((x) => (x.id === p.id ? updated : x)));
    } catch (err) { onError(err); }
  };

  const remove = async (p) => {
    try { await adminApi.deletePhoto(p.id); notify('Photo supprimée'); reload(); } catch (err) { onError(err); }
  };

  return (
    <section>
      <div className="adm-head">
        <div>
          <h1>Photos</h1>
          <p className="adm-muted">La galerie du site, dans l'ordre d'affichage. Les photos sont en noir &amp; blanc sur le site et reprennent leurs couleurs au survol.</p>
        </div>
        <button className="adm-btn adm-btn-primary" onClick={() => input.current.click()} disabled={!!uploading}>+ Ajouter des photos</button>
        <input ref={input} type="file" accept="image/jpeg,image/png,image/webp" multiple hidden onChange={(e) => { upload(e.target.files); e.target.value = ''; }} />
      </div>

      <div
        className={`adm-drop${over ? ' is-over' : ''}`}
        onDragOver={(e) => { e.preventDefault(); setOver(true); }}
        onDragLeave={() => setOver(false)}
        onDrop={(e) => { e.preventDefault(); setOver(false); upload(e.dataTransfer.files); }}
      >
        {uploading ? `Envoi ${uploading.done + 1} / ${uploading.total}…` : 'Glissez des photos ici (JPG, PNG ou WebP, 10 Mo max chacune)'}
      </div>

      {loading && <p className="adm-muted">Chargement…</p>}
      {photos?.length === 0 && <p className="adm-empty">La galerie est vide.</p>}

      <div className="adm-photos">
        {photos?.map((p, i) => (
          <figure key={p.id} className={`adm-photo is-${p.shape}`}>
            <div className="adm-photo-img"><img src={p.src} alt="" loading="lazy" /></div>
            <figcaption>
              <span className="adm-photo-num">{i + 1}</span>
              <button className="adm-icon-btn" disabled={i === 0} onClick={() => move(i, -1)} aria-label="Avancer">←</button>
              <button className="adm-icon-btn" disabled={i === photos.length - 1} onClick={() => move(i, 1)} aria-label="Reculer">→</button>
              <button className="adm-btn adm-btn-small" onClick={() => toggleShape(p)} title="Format d'affichage dans la galerie">
                {p.shape === 'wide' ? 'Paysage' : 'Portrait'}
              </button>
              <DeleteButton onConfirm={() => remove(p)} label="✕" />
            </figcaption>
          </figure>
        ))}
      </div>
    </section>
  );
}
