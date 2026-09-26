import { NAV, PHONE, SOCIALS, STREAMING_URL } from '../data';

export default function Nav({ open }) {
  return (
    <nav className="nav" id="nav" aria-hidden={!open}>
      <div className="nav-bg" />
      <div className="nav-inner">
        <ul className="nav-list">
          {NAV.map((item, i) => (
            <li key={item.href}>
              <a href={item.href} tabIndex={open ? 0 : -1}>
                <span className="nav-num">{String(i + 1).padStart(2, '0')}</span>
                <span className="nav-txt">{item.label}</span>
              </a>
            </li>
          ))}
        </ul>
        <div className="nav-side">
          <p className="eyebrow">Follow me</p>
          <ul className="nav-socials">
            {[...SOCIALS, { label: 'Streaming', url: STREAMING_URL }].map((s) => (
              <li key={s.label}><a href={s.url} target="_blank" rel="noopener" tabIndex={open ? 0 : -1}>{s.label}</a></li>
            ))}
          </ul>
          <p className="nav-contact">Booking<br /><a href={PHONE.href} tabIndex={open ? 0 : -1}>{PHONE.label}</a></p>
        </div>
      </div>
    </nav>
  );
}
