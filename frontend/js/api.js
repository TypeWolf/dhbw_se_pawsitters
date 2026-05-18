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
        complete: (id, userId) =>
            request(`/requests/${id}/complete?userId=${userId}`, { method: 'PUT' }),
        remove: (id) => request(`/requests/${id}`, { method: 'DELETE' })
    },
    wallet: {
        me: (userId) => request(`/wallet/me?userId=${userId}`),
        withdraw: (userId) => request(`/wallet/withdraw?userId=${userId}`, { method: 'POST' })
    },
    admin: {
        users: (requesterId) => request(`/admin/users?requesterId=${requesterId}`),
        pets: (requesterId) => request(`/admin/pets?requesterId=${requesterId}`),
        requests: (requesterId) => request(`/admin/requests?requesterId=${requesterId}`),
        payments: (requesterId) => request(`/admin/payments?requesterId=${requesterId}`),
        setRoles: (requesterId, userId, roles) =>
            request(`/admin/users/${userId}/roles?requesterId=${requesterId}`,
                { method: 'PUT', body: JSON.stringify(roles) })
    },
    ratings: {
        create: (requestId, stars, comment, raterId) =>
            request(`/ratings?requestId=${requestId}&stars=${stars}&comment=${encodeURIComponent(comment || '')}&raterId=${raterId}`, { method: 'POST' }),
        listForUser: (userId) => request(`/ratings/user/${userId}`),
        getAverage: (userId) => request(`/ratings/user/${userId}/average`)
    }
};
