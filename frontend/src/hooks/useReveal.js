import { useEffect } from 'react';

/**
 * Anime tous les éléments `.reveal` quand ils entrent à l'écran,
 * y compris ceux ajoutés plus tard (données chargées depuis l'API).
 */
export function useReveal() {
  useEffect(() => {
    const io = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return;
        const el = entry.target;
        const siblings = [...el.parentElement.children].filter((c) => c.classList.contains('reveal'));
        el.style.transitionDelay = `${Math.min(siblings.indexOf(el), 6) * 0.08}s`;
        el.dataset.revealed = '';   // attribut plutôt que classe : React ne l'écrase pas en re-rendant className
        io.unobserve(el);
      });
    }, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' });

    const observe = (root) => root.querySelectorAll?.('.reveal:not([data-revealed])').forEach((el) => io.observe(el));
    observe(document);
    const mo = new MutationObserver((muts) => muts.forEach((m) => m.addedNodes.forEach((n) => {
      if (n.nodeType !== 1) return;
      if (n.classList.contains('reveal')) io.observe(n);
      observe(n);
    })));
    mo.observe(document.body, { childList: true, subtree: true });
    return () => { io.disconnect(); mo.disconnect(); };
  }, []);
}
