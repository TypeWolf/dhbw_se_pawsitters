const API_BASE = window.PAWSITTERS_API_BASE || (
    window.location.hostname && window.location.port === '8080'
        ? '/api'
        : 'http://localhost:8080/api'
);

const state = {
    user: null,
    csrf: null,
    users: [],
    pets: [],
    requests: []
};

const elements = {};

document.addEventListener('DOMContentLoaded', () => {
    cacheElements();
    bindEvents();
    setupRevealAnimations();
    restoreSession();
});

function cacheElements() {
    [
        'adminLoginPanel',
        'adminLoginForm',
        'adminDashboard',
        'adminWelcome',
        'adminStatus',
        'adminLogoutButton',
        'adminUserCount',
        'adminPetCount',
        'adminRequestCount',
        'adminUsers',
        'adminPets',
        'adminRequests',
        'toastRegion'
    ].forEach(id => {
        elements[id] = document.getElementById(id);
    });
}

function bindEvents() {
    elements.adminLoginForm.addEventListener('submit', handleLogin);
    elements.adminLogoutButton.addEventListener('click', handleLogout);
}

function setupRevealAnimations() {
    const revealItems = document.querySelectorAll('.reveal');
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches || !('IntersectionObserver' in window)) {
        revealItems.forEach(item => item.classList.add('is-visible'));
        return;
    }

    const observer = new IntersectionObserver(entries => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('is-visible');
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.14 });

    revealItems.forEach(item => observer.observe(item));
}

async function restoreSession() {
    try {
        state.user = await api('/me');
        await ensureCsrf();
        await loadAdminData();
        showAdmin();
    } catch (error) {
        showLogin();
    }
}

async function handleLogin(event) {
    event.preventDefault();
    try {
        state.user = await api('/auth/login', {
            method: 'POST',
            body: formData(event.currentTarget),
            skipCsrf: true
        });
        await ensureCsrf();
        await loadAdminData();
        showAdmin();
        showToast('Admin session ready.');
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function handleLogout() {
    try {
        await api('/auth/logout', { method: 'POST' });
    } catch (error) {
        // The local UI still clears if the server already ended the session.
    }
    state.user = null;
    state.csrf = null;
    showLogin();
    showToast('Logged out.');
}

async function loadAdminData() {
    const [users, pets, requests] = await Promise.all([
        api('/admin/users'),
        api('/admin/pets'),
        api('/admin/requests')
    ]);
    state.users = users;
    state.pets = pets;
    state.requests = requests;
    renderAdmin();
}

function showAdmin() {
    elements.adminLoginPanel.classList.add('hidden');
    elements.adminDashboard.classList.remove('hidden');
    elements.adminWelcome.textContent = `Welcome, ${state.user.firstName}`;
    elements.adminStatus.textContent = `${state.user.email} has ${state.user.role} access.`;
}

function showLogin() {
    elements.adminLoginPanel.classList.remove('hidden');
    elements.adminDashboard.classList.add('hidden');
}

function renderAdmin() {
    elements.adminUserCount.textContent = String(state.users.length);
    elements.adminPetCount.textContent = String(state.pets.length);
    elements.adminRequestCount.textContent = String(state.requests.length);

    renderTable(elements.adminUsers, ['ID', 'Name', 'Email', 'Phone', 'Role'], state.users.map(user => [
        user.id,
        `${user.firstName} ${user.lastName}`,
        user.email,
        user.phoneNumber || '-',
        user.role
    ]));

    renderTable(elements.adminPets, ['ID', 'Name', 'Species', 'Breed', 'Owner'], state.pets.map(pet => [
        pet.id,
        pet.name,
        pet.species,
        pet.breed || '-',
        `${pet.owner.firstName} ${pet.owner.lastName}`
    ]));

    renderTable(elements.adminRequests, ['ID', 'Pet', 'Requester', 'Sitter', 'Window', 'Status'], state.requests.map(request => [
        request.id,
        request.pet.name,
        `${request.requester.firstName} ${request.requester.lastName}`,
        request.sitter ? `${request.sitter.firstName} ${request.sitter.lastName}` : '-',
        `${formatDate(request.startTime)} to ${formatDate(request.endTime)}`,
        request.status
    ]));
}

function renderTable(container, headers, rows) {
    container.replaceChildren();
    if (rows.length === 0) {
        container.append(node('p', { className: 'empty-state', text: 'No records yet.' }));
        return;
    }

    const table = node('table');
    const thead = node('thead');
    const headRow = node('tr');
    headers.forEach(header => headRow.append(node('th', { text: header })));
    thead.append(headRow);

    const tbody = node('tbody');
    rows.forEach(row => {
        const tr = node('tr');
        row.forEach(cell => tr.append(node('td', { text: String(cell) })));
        tbody.append(tr);
    });

    table.append(thead, tbody);
    container.append(table);
}

function node(tag, props = {}, children = []) {
    const element = document.createElement(tag);
    Object.entries(props).forEach(([key, value]) => {
        if (value === undefined || value === null) {
            return;
        }
        if (key === 'text') {
            element.textContent = value;
        } else if (key === 'className') {
            element.className = value;
        } else {
            element.setAttribute(key, value);
        }
    });
    children.forEach(child => element.append(child));
    return element;
}

function formData(form) {
    const data = Object.fromEntries(new FormData(form).entries());
    Object.keys(data).forEach(key => {
        if (typeof data[key] === 'string') {
            data[key] = data[key].trim();
        }
    });
    return data;
}

async function api(path, options = {}) {
    const method = options.method || 'GET';
    const headers = new Headers(options.headers || {});
    const init = {
        method,
        credentials: 'include',
        headers
    };

    if (options.body !== undefined) {
        headers.set('Content-Type', 'application/json');
        init.body = JSON.stringify(options.body);
    }

    if (!options.skipCsrf && method !== 'GET' && method !== 'HEAD') {
        await ensureCsrf();
        headers.set(state.csrf.headerName, state.csrf.token);
    }

    const response = await fetch(`${API_BASE}${path}`, init);
    const contentType = response.headers.get('content-type') || '';
    const payload = contentType.includes('application/json') ? await response.json() : null;

    if (!response.ok) {
        if (response.status === 403) {
            throw new Error('This account is not authorized for the admin workspace.');
        }
        throw new Error(payload?.message || `Request failed with status ${response.status}.`);
    }

    return payload;
}

async function ensureCsrf() {
    if (state.csrf) {
        return state.csrf;
    }
    const response = await fetch(`${API_BASE}/auth/csrf`, {
        method: 'GET',
        credentials: 'include'
    });
    if (!response.ok) {
        throw new Error('Could not prepare a secure request.');
    }
    state.csrf = await response.json();
    return state.csrf;
}

function showToast(message, type = 'success') {
    const toast = node('div', { className: type === 'error' ? 'toast error' : 'toast', text: message });
    elements.toastRegion.append(toast);
    window.setTimeout(() => toast.remove(), 4200);
}

function formatDate(value) {
    return new Intl.DateTimeFormat(undefined, {
        dateStyle: 'medium',
        timeStyle: 'short'
    }).format(new Date(value));
}
