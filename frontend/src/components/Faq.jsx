import { useState } from 'react';
import { rich } from '../lib/text';

export default function Faq({ faq }) {
  const [open, setOpen] = useState(null);
  if (!faq?.length) return null;
  return (
    <section className="section section-light faq" id="faq">
      <div className="section-head">
        <h2 className="section-title reveal">FAQ</h2>
        <p className="section-sub reveal">Infos utiles</p>
      </div>
      <div className="faq-list">
        {faq.map((item, i) => (
          <div className={`faq-item reveal${open === i ? ' is-open' : ''}`} key={i}>
            <button className="faq-q" aria-expanded={open === i} onClick={() => setOpen(open === i ? null : i)}>
              <span>{item.question}</span><i className="faq-icon" />
            </button>
            <div className="faq-a"><p>{rich(item.answer)}</p></div>
          </div>
        ))}
      </div>
    </section>
  );
}
