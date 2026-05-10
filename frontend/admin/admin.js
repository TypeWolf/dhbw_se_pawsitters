const translations = {
    en: {
        title: "Pawsitters Platform",
        login: "Login",
        register: "Register",
        logout: "Logout",
        welcome: "Welcome",
        firstName: "First Name",
        lastName: "Last Name",
        email: "Email",
        password: "Password",
        petName: "Pet Name",
        species: "Species (e.g. Dog)",
        owner: "Owner",
        addPet: "Add Your Pet",
        createRequest: "Create Sitting Request",
        startTime: "Start Time",
        endTime: "End Time",
        postRequest: "Post Request",
        availableOffers: "Available Sitting Offers",
        pet: "Pet",
        time: "Time",
        status: "Status",
        action: "Action",
        pending: "Pending",
        acceptedBy: "Accepted by",
        yourRequest: "(Your Request)",
        accept: "Accept Offer",
        delete: "Delete",
        usersPetsInfo: "Users & Pets (Info)",
        type: "Type",
        name: "Name",
        select: "Select...",
        confirmDelete: "Are you sure you want to delete this request?",
        loginFailed: "Login failed: ",
        regSuccess: "Registration successful! Please login.",
        regFailed: "Registration failed: ",
        saveFailed: "Save failed: "
    },
    de: {
        title: "Pawsitters Plattform",
        login: "Anmelden",
        register: "Registrieren",
        logout: "Abmelden",
        welcome: "Willkommen",
        firstName: "Vorname",
        lastName: "Nachname",
        email: "E-Mail",
        password: "Passwort",
        petName: "Tiername",
        species: "Tierart (z.B. Hund)",
        owner: "Besitzer",
        addPet: "Haustier hinzufügen",
        createRequest: "Sitter-Anfrage erstellen",
        startTime: "Startzeit",
        endTime: "Endzeit",
        postRequest: "Anfrage senden",
        availableOffers: "Verfügbare Angebote",
        pet: "Tier",
        time: "Zeitraum",
        status: "Status",
        action: "Aktion",
        pending: "Ausstehend",
        acceptedBy: "Angenommen von",
        yourRequest: "(Deine Anfrage)",
        accept: "Annehmen",
        delete: "Löschen",
        usersPetsInfo: "Benutzer & Tiere (Info)",
        type: "Typ",
        name: "Name",
        select: "Auswählen...",
        confirmDelete: "Sind Sie sicher, dass Sie diese Anfrage löschen möchten?",
        loginFailed: "Anmeldung fehlgeschlagen: ",
        regSuccess: "Registrierung erfolgreich! Bitte anmelden.",
        regFailed: "Registrierung fehlgeschlagen: ",
        saveFailed: "Speichern fehlgeschlagen: "
    }
};

let currentLang = 'en';

function i18n(key) {
    return translations[currentLang][key] || key;
}

const API_BASE = 'http://localhost:8080/api';

// --- State Management ---
let currentUser = null;
let users = [];
let pets = [];
let requests = [];

// --- Selectors ---
const authContainer = document.getElementById('authContainer');
const userProfile = document.getElementById('userProfile');
const mainContent = document.getElementById('mainContent');
const loggedInUserName = document.getElementById('loggedInUserName');
const loggedInEmail = document.getElementById('loggedInEmail');

const petOwnerSelect = document.getElementById('petOwner');
const requestPetSelect = document.getElementById('requestPet');
const offerTableBody = document.querySelector('#offerTable tbody');
const infoTableBody = document.querySelector('#infoTable tbody');

// --- Language Switcher ---
function setLanguage(lang) {
    currentLang = lang;
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (el.tagName === 'INPUT' && el.type !== 'submit') {
            el.placeholder = i18n(key);
        } else if (el.tagName === 'OPTION') {
            el.textContent = i18n(key);
        } else {
            el.textContent = i18n(key);
        }
    });
    updateUI();
}

// --- Auth Functions ---
async function login(email, password) {
    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        if (!res.ok) throw new Error(await res.text());
        currentUser = await res.json();
        onLoginSuccess();
    } catch (err) {
        alert(i18n('loginFailed') + err.message);
    }
}

async function register(user) {
    try {
        const res = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(user)
        });
        if (!res.ok) throw new Error(await res.text());
        alert(i18n('regSuccess'));
    } catch (err) {
        alert(i18n('regFailed') + err.message);
    }
}

function onLoginSuccess() {
    authContainer.style.display = 'none';
    userProfile.style.display = 'flex';
    mainContent.style.display = 'block';
    loggedInUserName.textContent = `${currentUser.firstName} ${currentUser.lastName}`;
    loggedInEmail.textContent = currentUser.email;
    fetchData();
}

function logout() {
    currentUser = null;
    authContainer.style.display = 'flex';
    userProfile.style.display = 'none';
    mainContent.style.display = 'none';
}

// --- API Calls ---
async function fetchData() {
    if (!currentUser) return;
    try {
        const [uRes, pRes, rRes] = await Promise.all([
            fetch(`${API_BASE}/users`),
            fetch(`${API_BASE}/pets`),
            fetch(`${API_BASE}/requests`)
        ]);
        users = await uRes.json();
        pets = await pRes.json();
        requests = await rRes.json();
        
        updateUI();
    } catch (err) {
        console.error("Failed to fetch data", err);
    }
}

// --- UI Updates ---
function updateUI() {
    if (!currentUser) return;

    // Update Dropdowns
    const updateSelect = (select, items, labelFn) => {
        const currentVal = select.value;
        select.innerHTML = `<option value="" data-i18n="select">${i18n('select')}</option>`;
        items.forEach(item => {
            const opt = document.createElement('option');
            opt.value = item.id;
            opt.textContent = labelFn(item);
            select.appendChild(opt);
        });
        select.value = currentVal;
    };

    updateSelect(petOwnerSelect, users, u => `${u.firstName} ${u.lastName}`);
    petOwnerSelect.value = currentUser.id;

    const myPets = pets.filter(p => p.owner.id === currentUser.id);
    updateSelect(requestPetSelect, myPets, p => `${p.name} (${p.species})`);

    // Update Info Table
    infoTableBody.innerHTML = '';
    users.forEach(u => infoTableBody.insertAdjacentHTML('beforeend', `<tr><td>User</td><td>${u.firstName} ${u.lastName}</td><td>${u.id}</td></tr>`));
    pets.forEach(p => infoTableBody.insertAdjacentHTML('beforeend', `<tr><td>Pet</td><td>${p.name} (${p.species})</td><td>${p.id}</td></tr>`));

    // Update Offers Table
    offerTableBody.innerHTML = '';
    requests.forEach(req => {
        const statusBadge = req.status === 'PENDING' 
            ? `<span class="badge badge-pending">${i18n('pending')}</span>`
            : `<span class="badge badge-accepted">${i18n('acceptedBy')} ${req.sitter.firstName}</span>`;
        
        let actions = '';
        if (req.status === 'PENDING') {
            if (req.requester.id !== currentUser.id) {
                actions += `<button class="accept" onclick="acceptOffer(${req.id})">${i18n('accept')}</button> `;
            } else {
                actions += `<span style="color: #666; font-style: italic;">${i18n('yourRequest')}</span> `;
            }
        }

        if (req.requester.id === currentUser.id) {
            actions += `<button class="delete" onclick="deleteOffer(${req.id})">${i18n('delete')}</button>`;
        }

        const row = `<tr>
            <td>${req.pet.name}</td>
            <td>${req.requester.firstName}</td>
            <td>${new Date(req.startTime).toLocaleString()} - <br>${new Date(req.endTime).toLocaleString()}</td>
            <td>${statusBadge}</td>
            <td>${actions || '---'}</td>
        </tr>`;
        offerTableBody.insertAdjacentHTML('beforeend', row);
    });
}

// --- Event Handlers ---
document.getElementById('loginForm').addEventListener('submit', (e) => {
    e.preventDefault();
    login(document.getElementById('loginEmail').value, document.getElementById('loginPassword').value);
});

document.getElementById('registerForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const user = {
        firstName: document.getElementById('regFirstName').value,
        lastName: document.getElementById('regLastName').value,
        email: document.getElementById('regEmail').value,
        password: document.getElementById('regPassword').value
    };
    register(user);
    e.target.reset();
});

document.getElementById('petForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const pet = {
        name: document.getElementById('petName').value,
        species: document.getElementById('petSpecies').value,
        owner: { id: parseInt(petOwnerSelect.value) }
    };
    try {
        const res = await fetch(`${API_BASE}/pets`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(pet)
        });
        if (!res.ok) throw new Error(await res.text());
        e.target.reset();
        fetchData();
    } catch (err) {
        alert(i18n('saveFailed') + err.message);
    }
});

document.getElementById('requestForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const petId = parseInt(requestPetSelect.value);
    const pet = pets.find(p => p.id === petId);
    
    const request = {
        pet: { id: petId },
        requester: { id: currentUser.id },
        startTime: document.getElementById('startTime').value,
        endTime: document.getElementById('endTime').value
    };

    await fetch(`${API_BASE}/requests`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request)
    });
    e.target.reset();
    fetchData();
});

async function acceptOffer(requestId) {
    try {
        const response = await fetch(`${API_BASE}/requests/${requestId}/accept?sitterId=${currentUser.id}`, {
            method: 'PUT'
        });
        if (!response.ok) throw new Error(await response.text());
        fetchData();
    } catch (err) {
        alert(err.message);
    }
}

async function deleteOffer(requestId) {
    if (!confirm(i18n('confirmDelete'))) return;
    await fetch(`${API_BASE}/requests/${requestId}`, { method: 'DELETE' });
    fetchData();
}

// Poll every 5 seconds if logged in
setInterval(() => { if (currentUser) fetchData(); }, 5000);

// Initialize language
document.addEventListener('DOMContentLoaded', () => setLanguage('en'));
