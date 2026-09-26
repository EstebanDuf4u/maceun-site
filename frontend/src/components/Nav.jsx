import { NAV } from '../data';
import { telHref } from '../lib/text';

export default function Nav({ open, content }) {
  const socials = content?.socials ?? [];
  const streaming = content?.contact?.streamingUrl;
  const phone = content?.contact?.phone;
  const tab = open ? 0 : -1;
  return (
    <nav className="nav" id="nav" aria-hidden={!open}>
      <div className="nav-bg" />
      <div className="nav-inner">
        <ul className="nav-list">
          {NAV.map((item, i) => (
            <li key={item.href}>
              <a href={item.href} tabIndex={tab}>
                <span className="nav-num">{String(i + 1).padStart(2, '0')}</span>
                <span className="nav-txt">{item.label}</span>
              </a>
            </li>
          ))}
        </ul>
        <div className="nav-side">
          <p className="eyebrow">Follow me</p>
          <ul className="nav-socials">
            {[...socials, ...(streaming ? [{ label: 'Streaming', url: streaming }] : [])].map((s) => (
              <li key={s.label}><a href={s.url} target="_blank" rel="noopener" tabIndex={tab}>{s.label}</a></li>
            ))}
          </ul>
          {phone && <p className="nav-contact">Booking<br /><a href={telHref(phone)} tabIndex={tab}>{phone}</a></p>}
        </div>
      </div>
    </nav>
  );
}
