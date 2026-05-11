const Layout = {
    renderHeader(activePage = '') {
        const header = document.getElementById('site-header');
        if (!header) return;

        const loggedIn = Session.isLoggedIn();
        const user = Session.user;

        const link = (href, key, page) =>
            `<a href="${href}" class="${activePage === page ? 'active' : ''}" data-i18n="${key}">${I18n.t(key)}</a>`;

        let nav;
        if (loggedIn) {
            nav = [
                link('dashboard.html', 'navDashboard', 'dashboard'),
                link('pets.html', 'navPets', 'pets'),
                link('my-requests.html', 'navMyRequests', 'requests'),
                link('sitter-jobs.html', 'navJobs', 'jobs'),
                link('calendar.html', 'navCalendar', 'calendar'),
                link('wallet.html', 'navWallet', 'wallet'),
                `<a href="#" id="logoutBtn" data-i18n="navLogout">${I18n.t('navLogout')}</a>`
            ].join('');
        } else {
            nav = [
                link('index.html', 'navHome', 'home'),
                `<a href="index.html#how" data-i18n="navHowItWorks">${I18n.t('navHowItWorks')}</a>`,
                link('login.html', 'navLogin', 'login'),
                `<a href="signup.html" class="nav-cta" data-i18n="navSignup">${I18n.t('navSignup')}</a>`
            ].join('');
        }

        header.innerHTML = `
            <div class="container">
                <a href="${loggedIn ? 'dashboard.html' : 'index.html'}" class="brand">
                    <span class="brand-mark">🐾</span>
                    <span data-i18n="brand">${I18n.t('brand')}</span>
                </a>
                <nav class="nav">${nav}</nav>
            </div>
        `;

        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', (e) => { e.preventDefault(); Session.logout(); });
        }
    },

    renderFooter() {
        const footer = document.getElementById('site-footer');
        if (!footer) return;
        const lang = I18n.lang;
        footer.innerHTML = `
            <div class="container">
                <span data-i18n="copyright">${I18n.t('copyright')}</span>
                <div class="lang-switcher">
                    <button class="${lang === 'en' ? 'active' : ''}" data-lang="en">EN</button>
                    <button class="${lang === 'de' ? 'active' : ''}" data-lang="de">DE</button>
                </div>
            </div>
        `;
        footer.querySelectorAll('[data-lang]').forEach(btn => {
            btn.addEventListener('click', () => {
                I18n.setLanguage(btn.dataset.lang);
                Layout.renderHeader(Layout._activePage);
                Layout.renderFooter();
            });
        });
    },

    init(activePage = '') {
        this._activePage = activePage;
        this.renderHeader(activePage);
        this.renderFooter();
        I18n.apply();
    }
};
