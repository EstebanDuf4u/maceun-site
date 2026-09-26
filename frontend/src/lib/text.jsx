import { Fragment } from 'react';

// *italique*, liens http(s) et numéros de téléphone français
const TOKEN = /(\*[^*\n]+\*|https?:\/\/[^\s<>()]+[^\s<>().,;:!?]|(?:\+33\s?|0)[1-9](?:[\s.-]?\d{2}){4})/g;

/** Numéro affiché → lien tel: international. */
export function telHref(phone) {
  const digits = (phone || '').replace(/[^\d+]/g, '');
  return `tel:${digits.startsWith('0') ? `+33${digits.slice(1)}` : digits}`;
}

/** Affichage lisible d'une URL : sans https:// ni www. */
const prettyUrl = (url) => url.replace(/^https?:\/\/(www\.)?/, '').replace(/\/$/, '');

/** Transforme un texte saisi dans l'admin en éléments React (jamais de HTML brut). */
export function rich(text) {
  if (!text) return null;
  return text.split(TOKEN).map((part, i) => {
    if (i % 2 === 0) return <Fragment key={i}>{part}</Fragment>;
    if (part.startsWith('*')) return <em key={i}>{part.slice(1, -1)}</em>;
    if (part.startsWith('http')) return <a key={i} href={part} target="_blank" rel="noopener">{prettyUrl(part)}</a>;
    return <a key={i} href={telHref(part)}>{part}</a>;
  });
}
