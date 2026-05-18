// Layout — sticky header, footer, shared assets (fonts + SVG icon sprite).
// Every page calls Layout.init(activePage) once after the i18n / session / api scripts.

const Layout = {
    _activePage: null,

    /** Inject Google Fonts (Geist + Fraunces + Geist Mono) once. */
    injectFonts() {
        if (document.getElementById('paws-fonts')) return;
        const pre1 = document.createElement('link');
        pre1.rel = 'preconnect';
        pre1.href = 'https://fonts.googleapis.com';
        const pre2 = document.createElement('link');
        pre2.rel = 'preconnect';
        pre2.href = 'https://fonts.gstatic.com';
        pre2.crossOrigin = 'anonymous';
        const link = document.createElement('link');
        link.id = 'paws-fonts';
        link.rel = 'stylesheet';
        link.href = 'https://fonts.googleapis.com/css2?family=Geist:wght@300;400;500;600;700&family=Geist+Mono:wght@400;500&family=Fraunces:opsz,wght@9..144,400;9..144,500;9..144,600&display=swap';
        document.head.append(pre1, pre2, link);
    },

    /**
     * Inject a hidden SVG sprite with the icon set used across every page.
     * Use with <svg class="icon"><use href="#i-paw"/></svg>.
     */
    injectIconSprite() {
        if (document.getElementById('paws-icons')) return;
        const SPRITE = `
<svg id="paws-icons" xmlns="http://www.w3.org/2000/svg" style="display:none">
  <defs>
    <symbol id="i-paw" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">
      <ellipse cx="6" cy="11" rx="2" ry="2.5"/><ellipse cx="18" cy="11" rx="2" ry="2.5"/>
      <ellipse cx="9" cy="6.5" rx="1.6" ry="2.2"/><ellipse cx="15" cy="6.5" rx="1.6" ry="2.2"/>
      <path d="M12 13c-3 0-5 2-5 4.5 0 1.5 1.2 2.5 2.5 2.5.8 0 1.4-.4 2-.8.4-.3.7-.4 1-.4s.6.1 1 .4c.6.4 1.2.8 2 .8 1.3 0 2.5-1 2.5-2.5C17 15 15 13 12 13z"/>
    </symbol>
    <symbol id="i-check"  viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12.5l4.5 4.5L20 7"/></symbol>
    <symbol id="i-shield" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l8 3v6c0 4.5-3.2 8-8 9-4.8-1-8-4.5-8-9V6l8-3z"/><path d="M9 12l2 2 4-4"/></symbol>
    <symbol id="i-heart"  viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20s-7-4.5-7-10a4 4 0 017-2.6A4 4 0 0119 10c0 5.5-7 10-7 10z"/></symbol>
    <symbol id="i-star"   viewBox="0 0 24 24" fill="currentColor"><path d="M12 2.5l2.9 6 6.6.9-4.8 4.6 1.2 6.5L12 17.4 6.1 20.5l1.2-6.5L2.5 9.4l6.6-.9L12 2.5z"/></symbol>
    <symbol id="i-pin"    viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 21s-7-7.4-7-12a7 7 0 0114 0c0 4.6-7 12-7 12z"/><circle cx="12" cy="9" r="2.5"/></symbol>
    <symbol id="i-cal"    viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><rect x="3.5" y="5" width="17" height="15.5" rx="2"/><path d="M3.5 9.5h17M8 3v4M16 3v4"/></symbol>
    <symbol id="i-msg"    viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M4 5h16v11H8.5L4 19.5V5z"/></symbol>
    <symbol id="i-home"   viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M3 11l9-7 9 7v9a1.5 1.5 0 01-1.5 1.5H4.5A1.5 1.5 0 013 20v-9z"/></symbol>
    <symbol id="i-search" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="6.5"/><path d="M16 16l4 4"/></symbol>
    <symbol id="i-plus"   viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14M5 12h14"/></symbol>
    <symbol id="i-arrow-r" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M14 6l6 6-6 6"/></symbol>
    <symbol id="i-arrow-l" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5M10 6l-6 6 6 6"/></symbol>
    <symbol id="i-chev-r" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9 6l6 6-6 6"/></symbol>
    <symbol id="i-x"      viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M6 6l12 12M18 6L6 18"/></symbol>
    <symbol id="i-user"   viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="4"/><path d="M4 21c1.5-4 4.8-6 8-6s6.5 2 8 6"/></symbol>
    <symbol id="i-leash"  viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><circle cx="6" cy="6" r="2"/><path d="M8 7c2 1 4 4 4 7v8M12 14h6a3 3 0 003-3v-1"/></symbol>
    <symbol id="i-moon"   viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M20 14a8 8 0 01-10-10 8 8 0 1010 10z"/></symbol>
    <symbol id="i-sun"    viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 3v2M12 19v2M3 12h2M19 12h2M5.5 5.5l1.4 1.4M17.1 17.1l1.4 1.4M5.5 18.5l1.4-1.4M17.1 6.9l1.4-1.4"/></symbol>
    <symbol id="i-door"   viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M5 21V4a1 1 0 011-1h12a1 1 0 011 1v17"/><path d="M3 21h18"/><circle cx="15" cy="12" r=".7" fill="currentColor"/></symbol>
    <symbol id="i-house"  viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M3 11l9-7 9 7v9H3v-9z"/></symbol>
    <symbol id="i-card"   viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="6" width="18" height="13" rx="2"/><path d="M3 10h18M7 15h3"/></symbol>
    <symbol id="i-edit"   viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20h4L19 9l-4-4L4 16v4z"/></symbol>
    <symbol id="i-wallet" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M3 7a2 2 0 012-2h12a2 2 0 012 2v2H5a2 2 0 00-2 2v-4z"/><path d="M3 11h17a1 1 0 011 1v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-7z"/><circle cx="17" cy="14.5" r="1.2" fill="currentColor"/></symbol>
    <symbol id="i-clock"  viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3.5 2.5"/></symbol>
    <symbol id="i-cam"    viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M3 7h4l2-2h6l2 2h4v12H3V7z"/><circle cx="12" cy="13" r="4"/></symbol>
    <symbol id="i-badge"  viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2l2.5 2.5L18 4l1 3.5L21.5 9 20 12l1.5 3-2.5 1.5L18 20l-3.5-.5L12 22l-2.5-2.5L6 20l-1-3.5L2.5 15 4 12 2.5 9 5 7.5 6 4l3.5.5L12 2z"/><path d="M9 12l2 2 4-4"/></symbol>
  </defs>
</svg>`;
        const wrap = document.createElement('div');
        wrap.innerHTML = SPRITE;
        document.body.prepend(wrap.firstElementChild);
    },

    /** Render the sticky header with a sage paw wordmark + role-aware nav. */
    renderHeader(activePage = '') {
        const header = document.getElementById('site-header');
        if (!header) return;
        const loggedIn = Session.isLoggedIn();

        const navLink = (href, key, page) =>
            `<a href="${href}" class="${activePage === page ? 'active' : ''}" data-i18n="${key}">${I18n.t(key)}</a>`;

        let nav;
        if (loggedIn) {
            const items = [
                navLink('dashboard.html',   'navDashboard',  'dashboard'),
                navLink('pets.html',        'navPets',       'pets'),
                navLink('my-requests.html', 'navMyRequests', 'requests'),
                navLink('sitter-jobs.html', 'navJobs',       'jobs'),
                navLink('calendar.html',    'navCalendar',   'calendar'),
                navLink('wallet.html',      'navWallet',     'wallet')
            ];
            if (Session.hasRole && Session.hasRole('ADMIN')) {
                items.push(navLink('admin.html', 'navAdmin', 'admin'));
            }
            items.push(`<a href="#" id="logoutBtn" data-i18n="navLogout">${I18n.t('navLogout')}</a>`);
            nav = items.join('');
        } else {
            nav = [
                navLink('index.html', 'navHome', 'home'),
                `<a href="index.html#how" data-i18n="navHowItWorks">${I18n.t('navHowItWorks')}</a>`,
                navLink('login.html', 'navLogin', 'login'),
                `<a href="signup.html" class="nav-cta" data-i18n="navSignup">${I18n.t('navSignup')}</a>`
            ].join('');
        }

        header.innerHTML = `
            <div class="container">
                <a href="${loggedIn ? 'dashboard.html' : 'index.html'}" class="brand">
                    <span class="brand-mark"><svg class="icon"><use href="#i-paw"></use></svg></span>
                    <span data-i18n="brand">${I18n.t('brand')}</span>
                </a>
                <nav class="nav">${nav}</nav>
            </div>
        `;

        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) logoutBtn.addEventListener('click', e => { e.preventDefault(); Session.logout(); });
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

    /** One-call init for every page: fonts, sprite, header, footer, i18n. */
    init(activePage = '') {
        this._activePage = activePage;
        this.injectFonts();
        this.injectIconSprite();
        this.renderHeader(activePage);
        this.renderFooter();
        I18n.apply();
    }
};
