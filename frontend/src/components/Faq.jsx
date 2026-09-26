import { useState } from 'react';
import { PHONE, SOCIALS } from '../data';

const [instagram, youtube, tiktok] = SOCIALS;

const ITEMS = [
  { q: 'Style de musique ?', a: <>Général, House et Techno. Le set s'adapte au public et au type d'événement.</> },
  { q: 'Comment booker Maceun ?', a: <>Via le formulaire ci-dessous ou par téléphone au <a href={PHONE.href}>{PHONE.label}</a>. Merci de préciser la date, le lieu et le type d'événement.</> },
  { q: 'Quels événements ?', a: <>Bals · Festivals · Soirées publiques &amp; privées · Anniversaires · Et bien plus encore. Toutes les demandes sont étudiées.</> },
  {
    q: 'Où le suivre ?',
    a: <>Instagram : <a href={instagram.url} target="_blank" rel="noopener">@maceunofficiel</a> · YouTube : <a href={youtube.url} target="_blank" rel="noopener">DJ Maceun</a> · TikTok : <a href={tiktok.url} target="_blank" rel="noopener">@maceunofficiel</a></>,
  },
];

export default function Faq() {
  const [open, setOpen] = useState(null);
  return (
    <section className="section section-light faq" id="faq">
      <div className="section-head">
        <h2 className="section-title reveal">FAQ</h2>
        <p className="section-sub reveal">Infos utiles</p>
      </div>
      <div className="faq-list">
        {ITEMS.map((item, i) => (
          <div className={`faq-item reveal${open === i ? ' is-open' : ''}`} key={item.q}>
            <button className="faq-q" aria-expanded={open === i} onClick={() => setOpen(open === i ? null : i)}>
              <span>{item.q}</span><i className="faq-icon" />
            </button>
            <div className="faq-a"><p>{item.a}</p></div>
          </div>
        ))}
      </div>
    </section>
  );
}
