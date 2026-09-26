async function request(path, options) {
  const res = await fetch(`/api${path}`, {
    ...options,
    headers: { Accept: 'application/json', ...(options?.body ? { 'Content-Type': 'application/json' } : {}) },
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    const err = new Error(data?.message || `Erreur ${res.status}`);
    err.status = res.status;
    err.fields = data?.fields || {};
    throw err;
  }
  return data;
}

export const api = {
  releases: () => request('/releases'),
  events: () => request('/events'),
  booking: (payload) => request('/booking', { method: 'POST', body: JSON.stringify(payload) }),
};
