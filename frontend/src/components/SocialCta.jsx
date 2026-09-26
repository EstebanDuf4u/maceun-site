export default function SocialCta({ socials, image }) {
  if (!socials?.length) return null;
  return (
    <section className="social-cta">
      {image && <img src={image} alt="" className="social-cta-bg parallax" data-speed="0.15" loading="lazy" />}
      <div className="social-cta-inner">
        <p className="eyebrow reveal">Follow me</p>
        <ul className="social-big">
          {socials.map((s) => (
            <li className="reveal" key={s.label}><a href={s.url} target="_blank" rel="noopener">{s.label}</a></li>
          ))}
        </ul>
      </div>
    </section>
  );
}
