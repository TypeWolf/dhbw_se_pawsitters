const API_BASE = 'http://localhost:8080/api';

async function request(path, options = {}) {
    const res = await fetch(`${API_BASE}${path}`, {
        headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
        ...options
    });
    if (!res.ok) {
        const text = await res.text();
        throw new Error(text || `Request failed: ${res.status}`);
    }
    if (res.status === 204) return null;
    const ct = res.headers.get('content-type') || '';
    return ct.includes('application/json') ? res.json() : res.text();
}

const Api = {
    auth: {
        login: (email, password) =>
            request('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
        register: (user) =>
            request('/auth/register', { method: 'POST', body: JSON.stringify(user) })
    },
    users: {
        list: () => request('/users'),
        get: (id) => request(`/users/${id}`)
    },
    pets: {
        list: () => request('/pets'),
        create: (pet) => request('/pets', { method: 'POST', body: JSON.stringify(pet) })
    },
    requests: {
        listOpen: () => request('/requests'),
        listMine: (userId) => request(`/requests/mine?userId=${userId}`),
        listBooked: (sitterId) => request(`/requests/booked?sitterId=${sitterId}`),
        create: (req) => request('/requests', { method: 'POST', body: JSON.stringify(req) }),
        accept: (id, sitterId) =>
            request(`/requests/${id}/accept?sitterId=${sitterId}`, { method: 'PUT' }),
        cancel: (id, userId) =>
            request(`/requests/${id}/cancel?userId=${userId}`, { method: 'PUT' }),
        remove: (id) => request(`/requests/${id}`, { method: 'DELETE' })
    }
};
