const Session = {
    KEY: 'paws.user',

    get user() {
        const raw = localStorage.getItem(this.KEY);
        if (!raw) return null;
        try { return JSON.parse(raw); }
        catch { return null; }
    },

    set user(value) {
        if (value) localStorage.setItem(this.KEY, JSON.stringify(value));
        else localStorage.removeItem(this.KEY);
    },

    isLoggedIn() { return this.user !== null; },

    hasRole(role) {
        const u = this.user;
        return !!(u && u.roles && u.roles.includes(role));
    },

    logout() {
        localStorage.removeItem(this.KEY);
        window.location.href = 'index.html';
    },

    requireAuth(role) {
        if (!this.isLoggedIn()) {
            window.location.href = 'login.html';
            return false;
        }
        if (role && !this.hasRole(role)) {
            window.location.href = 'dashboard.html';
            return false;
        }
        return true;
    }
};
