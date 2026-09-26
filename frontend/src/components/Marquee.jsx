import { Fragment } from 'react';

export default function Marquee({ release }) {
  const title = release?.title ?? 'I Lose Myself';
  const date = release
    ? new Date(release.releaseDate).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit' }).replace('/', '.')
    : '02.10';
  const upcoming = !release || new Date(`${release.releaseDate}T00:00:00+02:00`) > new Date();
  const label = upcoming ? `Out ${date}` : 'Out now';
  return (
    <div className="marquee" aria-hidden="true">
      <div className="marquee-track">
        {Array.from({ length: 8 }, (_, i) => (
          <Fragment key={i}><span>{i % 2 ? label : title}</span><b>+</b></Fragment>
        ))}
      </div>
    </div>
  );
}
