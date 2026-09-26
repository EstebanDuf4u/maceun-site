/** Lit le jeton CSRF posé par Spring Security (cookie XSRF-TOKEN). */
function csrfToken() {
  return document.cookie.split('; ').find((c) => c.startsWith('XSRF-TOKEN='))?.split('=')[1];
}

async function request(path, { method = 'GET', json, form } = {}) {
  const headers = { Accept: 'application/json' };
  if (json !== undefined) headers['Content-Type'] = 'application/json';
  if (method !== 'GET') {
    const token = csrfToken();
    if (token) headers['X-XSRF-TOKEN'] = decodeURIComponent(token);
  }
  const res = await fetch(`/api${path}`, {
    method,
    headers,
    credentials: 'same-origin',
    body: json !== undefined ? JSON.stringify(json) : form,
  });
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    const err = new Error(data?.message || messageFor(res.status));
    err.status = res.status;
    err.fields = data?.fields || {};
    throw err;
  }
  return data;
}

function messageFor(status) {
  if (status === 401) return 'Session expirée, reconnectez-vous.';
  if (status === 403) return 'Action refusée, rechargez la page.';
  if (status === 413) return 'Fichier trop lourd (10 Mo maximum).';
  return `Erreur ${status}`;
}

const withFile = (file, extra = {}) => {
  const fd = new FormData();
  fd.append('file', file);
  Object.entries(extra).forEach(([k, v]) => v != null && fd.append(k, v));
  return fd;
};

export const api = {
  releases: () => request('/releases'),
  events: () => request('/events'),
  photos: () => request('/photos'),
  booking: (payload) => request('/booking', { method: 'POST', json: payload }),
};

export const adminApi = {
  me: () => request('/auth/me'),
  login: (username, password) => request('/auth/login', { method: 'POST', json: { username, password } }),
  logout: () => request('/auth/logout', { method: 'POST' }),

  releases: () => request('/admin/releases'),
  addRelease: (url, manualReleaseDate) => request('/admin/releases', { method: 'POST', json: { url, manualReleaseDate: manualReleaseDate || null } }),
  updateRelease: (id, visible, manualReleaseDate) => request(`/admin/releases/${id}`, { method: 'PUT', json: { visible, manualReleaseDate: manualReleaseDate || null } }),
  syncRelease: (id) => request(`/admin/releases/${id}/sync`, { method: 'POST' }),
  deleteRelease: (id) => request(`/admin/releases/${id}`, { method: 'DELETE' }),

  addPhoto: (file) => request('/admin/photos', { method: 'POST', form: withFile(file) }),
  setPhotoShape: (id, shape) => request(`/admin/photos/${id}`, { method: 'PATCH', json: { shape } }),
  reorderPhotos: (ids) => request('/admin/photos/order', { method: 'PUT', json: ids }),
  deletePhoto: (id) => request(`/admin/photos/${id}`, { method: 'DELETE' }),

};
