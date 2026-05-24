const API_BASE = window.PAWSITTERS_API_BASE || (
    window.location.hostname && window.location.port === '8080'
        ? '/api'
        : 'http://localhost:8080/api'
);

const state = {
    user: null,
    pets: [],
    mine: [],
    available: [],
    csrf: null,
    activeView: 'owner'
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
        'signedOutPanel',
        'loginForm',
        'registerForm',
        'loginTab',
        'registerTab',
        'dashboard',
        'welcomeTitle',
        'welcomeSubtitle',
        'logoutButton',
        'petForm',
        'requestForm',
        'requestPetSelect',
        'petList',
        'mineList',
        'availableList',
        'petCount',
        'mineCount',
        'availableCount',
        'refreshButton',
        'profileName',
        'profileEmail',
        'toastRegion',
        'confirmDialog',
        'confirmTitle',
        'confirmMessage'
    ].forEach(id => {
        elements[id] = document.getElementById(id);
    });
}

function bindEvents() {
    elements.loginTab.addEventListener('click', () => setAuthMode('login'));
    elements.registerTab.addEventListener('click', () => setAuthMode('register'));
    elements.loginForm.addEventListener('submit', handleLogin);
    elements.registerForm.addEventListener('submit', handleRegister);
    elements.logoutButton.addEventListener('click', handleLogout);
    elements.petForm.addEventListener('submit', handlePetCreate);
    elements.requestForm.addEventListener('submit', handleRequestCreate);
    elements.refreshButton.addEventListener('click', loadDashboardData);

    document.querySelectorAll('[data-view]').forEach(button => {
        button.addEventListener('click', () => setActiveView(button.dataset.view));
    });
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
        await loadDashboardData();
        showSignedIn();
    } catch (error) {
        showSignedOut();
    }
}

function setAuthMode(mode) {
    const login = mode === 'login';
    elements.loginForm.classList.toggle('hidden', !login);
    elements.registerForm.classList.toggle('hidden', login);
    elements.loginTab.classList.toggle('active', login);
    elements.registerTab.classList.toggle('active', !login);
}

async function handleLogin(event) {
    event.preventDefault();
    const data = formData(event.currentTarget);
    try {
        state.user = await api('/auth/login', {
            method: 'POST',
            body: data,
            skipCsrf: true
        });
        await ensureCsrf();
        await loadDashboardData();
        showSignedIn();
        showToast('Welcome back.');
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = formData(form);
    try {
        await api('/auth/register', {
            method: 'POST',
            body: data,
            skipCsrf: true
        });
        form.reset();
        setAuthMode('login');
        showToast('Account created. You can log in now.');
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function handleLogout() {
    try {
        await api('/auth/logout', { method: 'POST' });
    } catch (error) {
        // The local UI still clears the session state if the server already ended it.
    }
    state.user = null;
    state.pets = [];
    state.mine = [];
    state.available = [];
    state.csrf = null;
    showSignedOut();
    showToast('Logged out.');
}

async function handlePetCreate(event) {
    event.preventDefault();
    const form = event.currentTarget;
    try {
        await api('/me/pets', {
            method: 'POST',
            body: formData(form)
        });
        form.reset();
        await loadDashboardData();
        showToast('Pet saved.');
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function handleRequestCreate(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = formData(form);
    data.petId = Number(data.petId);

    try {
        await api('/requests', {
            method: 'POST',
            body: data
        });
        form.reset();
        await loadDashboardData();
        showToast('Sitting request posted.');
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function loadDashboardData() {
    const [pets, mine, available] = await Promise.all([
        api('/me/pets'),
        api('/requests/mine'),
        api('/requests/available')
    ]);

    state.pets = pets;
    state.mine = mine;
    state.available = available;
    renderDashboard();
}

async function acceptRequest(requestId) {
    try {
        await api(`/requests/${requestId}/accept`, { method: 'PUT' });
        await loadDashboardData();
        showToast('Request accepted.');
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function deleteRequest(requestId) {
    const confirmed = await confirmAction('Delete request', 'This removes the request from the marketplace.');
    if (!confirmed) {
        return;
    }

    try {
        await api(`/requests/${requestId}`, { method: 'DELETE' });
        await loadDashboardData();
        showToast('Request deleted.');
    } catch (error) {
        showToast(error.message, 'error');
    }
}

function showSignedIn() {
    elements.signedOutPanel.classList.add('hidden');
    elements.dashboard.classList.remove('hidden');
    elements.welcomeTitle.textContent = `Welcome, ${state.user.firstName}`;
    elements.welcomeSubtitle.textContent = `${state.user.email} is protected by a secure session.`;
    elements.profileName.textContent = `${state.user.firstName} ${state.user.lastName}`;
    elements.profileEmail.textContent = state.user.email;
    document.getElementById('dashboard').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function showSignedOut() {
    elements.signedOutPanel.classList.remove('hidden');
    elements.dashboard.classList.add('hidden');
}

function setActiveView(view) {
    state.activeView = view;
    document.querySelectorAll('[data-view]').forEach(button => {
        button.classList.toggle('active', button.dataset.view === view);
    });
    document.querySelectorAll('[data-panel]').forEach(panel => {
        panel.classList.toggle('hidden', panel.dataset.panel !== view);
    });
}

function renderDashboard() {
    elements.petCount.textContent = String(state.pets.length);
    elements.mineCount.textContent = String(state.mine.length);
    elements.availableCount.textContent = String(state.available.length);
    renderPetSelect();
    renderPets();
    renderMine();
    renderAvailable();
}

function renderPetSelect() {
    elements.requestPetSelect.replaceChildren();
    if (state.pets.length === 0) {
        elements.requestPetSelect.append(option('', 'Add a pet first'));
        elements.requestPetSelect.disabled = true;
        return;
    }

    elements.requestPetSelect.disabled = false;
    elements.requestPetSelect.append(option('', 'Choose a pet'));
    state.pets.forEach(pet => {
        elements.requestPetSelect.append(option(String(pet.id), `${pet.name} (${pet.species})`));
    });
}

function renderPets() {
    elements.petList.replaceChildren();
    if (state.pets.length === 0) {
        elements.petList.append(emptyState('No pets yet. Add your first pet to start a care request.'));
        return;
    }

    state.pets.forEach(pet => {
        const subtitle = [pet.species, pet.breed, pet.age === null || pet.age === undefined ? null : `${pet.age} years`]
            .filter(Boolean)
            .join(' - ');
        elements.petList.append(node('article', { className: 'pet-card' }, [
            node('h4', { text: pet.name }),
            node('p', { className: 'card-subtitle', text: subtitle || 'Pet profile' })
        ]));
    });
}

function renderMine() {
    elements.mineList.replaceChildren();
    if (state.mine.length === 0) {
        elements.mineList.append(emptyState('No active requests yet. Create one when care is needed.'));
        return;
    }

    state.mine.forEach(request => {
        elements.mineList.append(requestItem(request, { showDelete: request.requester.id === state.user.id }));
    });
}

function renderAvailable() {
    elements.availableList.replaceChildren();
    if (state.available.length === 0) {
        elements.availableList.append(emptyState('No open requests right now. Check back soon.'));
        return;
    }

    state.available.forEach(request => {
        elements.availableList.append(requestItem(request, { showAccept: true }));
    });
}

function requestItem(request, actions = {}) {
    const status = node('span', {
        className: request.status === 'ACCEPTED' ? 'status accepted' : 'status',
        text: request.status
    });

    const actionButtons = [];
    if (actions.showAccept) {
        actionButtons.push(node('button', {
            className: 'button primary compact',
            type: 'button',
            text: 'Accept',
            onClick: () => acceptRequest(request.id)
        }));
    }
    if (actions.showDelete) {
        actionButtons.push(node('button', {
            className: 'button danger compact',
            type: 'button',
            text: 'Delete',
            onClick: () => deleteRequest(request.id)
        }));
    }

    const sitter = request.sitter ? `Accepted by ${request.sitter.firstName}` : 'Open for sitters';
    return node('article', { className: 'timeline-item' }, [
        node('div', {}, [
            node('h4', { text: `${request.pet.name} with ${request.requester.firstName}` }),
            node('p', { className: 'timeline-meta', text: `${formatDate(request.startTime)} to ${formatDate(request.endTime)}` }),
            node('p', { className: 'timeline-meta', text: sitter })
        ]),
        node('div', { className: 'timeline-actions' }, [status, ...actionButtons])
    ]);
}

function emptyState(message) {
    return node('p', { className: 'empty-state', text: message });
}

function option(value, text) {
    return node('option', { value, text });
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
        } else if (key === 'onClick') {
            element.addEventListener('click', value);
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
        if (data[key] === '') {
            delete data[key];
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
        if (response.status === 401) {
            state.user = null;
            showSignedOut();
        }
        throw new Error(errorMessage(payload, response.status));
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

function errorMessage(payload, status) {
    if (payload?.fields) {
        return Object.values(payload.fields)[0] || payload.message;
    }
    return payload?.message || `Request failed with status ${status}.`;
}

function showToast(message, type = 'success') {
    const toast = node('div', { className: type === 'error' ? 'toast error' : 'toast', text: message });
    elements.toastRegion.append(toast);
    window.setTimeout(() => toast.remove(), 4200);
}

function confirmAction(title, message) {
    if (!elements.confirmDialog.showModal) {
        showToast('This browser cannot show the confirmation dialog.', 'error');
        return Promise.resolve(false);
    }

    elements.confirmTitle.textContent = title;
    elements.confirmMessage.textContent = message;
    document.body.classList.add('modal-open');
    elements.confirmDialog.showModal();

    return new Promise(resolve => {
        elements.confirmDialog.addEventListener('close', () => {
            document.body.classList.remove('modal-open');
            resolve(elements.confirmDialog.returnValue === 'confirm');
        }, { once: true });
    });
}

function formatDate(value) {
    return new Intl.DateTimeFormat(undefined, {
        dateStyle: 'medium',
        timeStyle: 'short'
    }).format(new Date(value));
}
