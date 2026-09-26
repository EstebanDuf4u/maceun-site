import { SOCIALS } from '../data';

export default function SocialCta() {
  return (
    <section className="social-cta">
      <img src="/img/djm2.jpg" alt="" className="social-cta-bg parallax" data-speed="0.15" loading="lazy" />
      <div className="social-cta-inner">
        <p className="eyebrow reveal">Follow me</p>
        <ul className="social-big">
          {SOCIALS.map((s) => (
            <li className="reveal" key={s.label}><a href={s.url} target="_blank" rel="noopener">{s.label}</a></li>
          ))}
        </ul>
      </div>
    </section>
  );
}
