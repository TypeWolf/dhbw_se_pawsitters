/**
 * Chat component handles real-time (polling) communication between Owners and Sitters.
 */
const Chat = {
    _userId: null,
    _isOpen: false,
    _selectedRequestId: null,
    _pollInterval: null,
    _lastReadIds: {}, // requestId -> last message id seen
    _unreadCounts: {}, // requestId -> count
    _initialized: false,

    init() {
        if (!Session.isLoggedIn()) return;
        this._userId = Session.user.id;
        
        // Load last read states from localStorage
        const saved = localStorage.getItem(`paws.chat.lastRead.${this._userId}`);
        if (saved) {
            try { this._lastReadIds = JSON.parse(saved); } catch(e) {}
        }

        this.injectHtml();
        this.attachEvents();
        this.loadContacts();
        this.startGlobalPolling();
        this._initialized = true;
    },

    saveReadState() {
        localStorage.setItem(`paws.chat.lastRead.${this._userId}`, JSON.stringify(this._lastReadIds));
    },

    injectHtml() {
        if (document.getElementById('chat-widget')) return;

        const widget = document.createElement('div');
        widget.id = 'chat-widget';
        widget.className = 'chat-widget';
        widget.innerHTML = `
            <div class="chat-container" id="chat-container">
                <div class="chat-header">
                    <h3 id="chat-title" data-i18n="chatTitle">Chat</h3>
                    <div class="chat-header-actions">
                        <button id="chat-back-btn" style="display:none;" title="Back to contacts">
                            <svg class="icon" style="width:20px; height:20px;"><use href="#i-arrow-l"></use></svg>
                        </button>
                        <button id="chat-close-btn" title="Close chat">
                            <svg class="icon" style="width:20px; height:20px;"><use href="#i-x"></use></svg>
                        </button>
                    </div>
                </div>
                <div class="chat-body">
                    <div class="chat-contacts" id="chat-contacts">
                        <!-- Contacts loaded here -->
                        <div class="text-mute" style="padding:20px; text-align:center;" data-i18n="chatNoContacts">No active chats</div>
                    </div>
                    <div class="chat-messages-area" id="chat-messages-area">
                        <div class="chat-messages-list" id="chat-messages-list">
                            <!-- Messages loaded here -->
                        </div>
                        <form class="chat-input-area" id="chat-form">
                            <input type="text" id="chat-input" placeholder="Type a message..." autocomplete="off">
                            <button type="submit" class="chat-send-btn">
                                <svg class="icon" style="width:18px; height:18px;"><use href="#i-arrow-r"></use></svg>
                            </button>
                        </form>
                    </div>
                </div>
            </div>
            <button class="chat-toggle" id="chat-toggle">
                <svg class="icon"><use href="#i-arrow-l"></use></svg>
                <span data-i18n="chatTitle">Chat</span>
                <span id="chat-global-badge" class="chat-badge" style="display:none;">0</span>
            </button>
        `;
        document.body.appendChild(widget);
        
        // Add CSS link if not present
        if (!document.querySelector('link[href="css/chat.css"]')) {
            const link = document.createElement('link');
            link.rel = 'stylesheet';
            link.href = 'css/chat.css';
            document.head.appendChild(link);
        }
    },

    attachEvents() {
        const toggle = document.getElementById('chat-toggle');
        const close = document.getElementById('chat-close-btn');
        const back = document.getElementById('chat-back-btn');
        const form = document.getElementById('chat-form');
        const container = document.getElementById('chat-container');

        toggle.addEventListener('click', () => {
            this._isOpen = !this._isOpen;
            container.classList.toggle('show', this._isOpen);
            toggle.classList.toggle('open', this._isOpen);
            if (this._isOpen) {
                if (!this._selectedRequestId) {
                    this.loadContacts();
                } else {
                    this.markAsRead(this._selectedRequestId);
                }
            }
        });

        close.addEventListener('click', () => {
            this._isOpen = false;
            container.classList.remove('show');
            toggle.classList.remove('open');
        });

        back.addEventListener('click', () => {
            this.showContacts();
        });

        form.addEventListener('submit', (e) => {
            e.preventDefault();
            this.sendMessage();
        });
    },

    async loadContacts() {
        try {
            const contacts = await Api.chat.contacts(this._userId);
            const list = document.getElementById('chat-contacts');
            
            if (!contacts || contacts.length === 0) {
                list.innerHTML = `<div class="text-mute" style="padding:20px; text-align:center;" data-i18n="chatNoContacts">${I18n.t('chatNoContacts') || 'No active chats'}</div>`;
                return;
            }

            list.innerHTML = contacts.map(c => {
                const isOwner = c.requester.id === this._userId;
                const otherUser = isOwner ? c.sitter : c.requester;
                const otherName = otherUser ? `${otherUser.firstName} ${otherUser.lastName}` : 'TBA';
                const roleLabel = isOwner ? 'Sitter' : 'Owner';
                const unread = this._unreadCounts[c.id] || 0;
                
                return `
                    <div class="chat-contact-item" onclick="Chat.selectContact(${c.id}, '${otherName}')">
                        <div class="avatar" style="width:40px; height:40px; font-size:0.9rem;">
                            ${otherName.charAt(0)}
                        </div>
                        <div class="chat-contact-info">
                            <div class="chat-contact-name">${otherName}</div>
                            <div class="chat-contact-sub">${roleLabel} • ${c.pet.name}</div>
                        </div>
                        ${unread > 0 ? `<span class="chat-contact-badge">${unread}</span>` : ''}
                    </div>
                `;
            }).join('');
        } catch (err) {
            console.error('Failed to load chat contacts', err);
        }
    },

    selectContact(requestId, name) {
        this._selectedRequestId = requestId;
        document.getElementById('chat-title').textContent = name;
        document.getElementById('chat-contacts').style.display = 'none';
        document.getElementById('chat-messages-area').classList.add('show');
        document.getElementById('chat-back-btn').style.display = 'block';
        
        this.markAsRead(requestId);
        this.loadMessages();
    },

    markAsRead(requestId) {
        if (!requestId) return;
        // In a real app we'd fetch messages first to get the max ID, 
        // but here we'll update it when rendering.
        this._unreadCounts[requestId] = 0;
        this.updateGlobalBadge();
        // If we have any messages in memory, update lastReadId
        const list = document.getElementById('chat-messages-list');
        const lastMsg = list.querySelector('.chat-message:last-child');
        if (lastMsg && lastMsg.dataset.id) {
            this._lastReadIds[requestId] = parseInt(lastMsg.dataset.id);
            this.saveReadState();
        }
    },

    showContacts() {
        this._selectedRequestId = null;
        document.getElementById('chat-title').textContent = I18n.t('chatTitle') || 'Chat';
        document.getElementById('chat-contacts').style.display = 'block';
        document.getElementById('chat-messages-area').classList.remove('show');
        document.getElementById('chat-back-btn').style.display = 'none';
        this.loadContacts();
    },

    async loadMessages() {
        if (!this._selectedRequestId) return;
        try {
            const messages = await Api.chat.messages(this._selectedRequestId, this._userId);
            this.renderMessages(messages);
            
            // If open and focused, update last read
            if (this._isOpen && this._selectedRequestId) {
                const maxId = messages.reduce((max, m) => Math.max(max, m.id), 0);
                if (maxId > (this._lastReadIds[this._selectedRequestId] || 0)) {
                    this._lastReadIds[this._selectedRequestId] = maxId;
                    this._unreadCounts[this._selectedRequestId] = 0;
                    this.saveReadState();
                    this.updateGlobalBadge();
                }
            }
        } catch (err) {
            console.error('Failed to load messages', err);
        }
    },

    renderMessages(messages) {
        const list = document.getElementById('chat-messages-list');
        const wasAtBottom = list.scrollHeight - list.scrollTop <= list.clientHeight + 50;

        list.innerHTML = messages.map(m => {
            const isSent = m.sender.id === this._userId;
            const time = new Date(m.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            return `
                <div class="chat-message ${isSent ? 'sent' : 'received'}" data-id="${m.id}">
                    <div class="chat-message-content">${this.escapeHtml(m.content)}</div>
                    <span class="chat-message-time">${time}</span>
                </div>
            `;
        }).join('');

        if (wasAtBottom) {
            list.scrollTop = list.scrollHeight;
        }
    },

    async sendMessage() {
        const input = document.getElementById('chat-input');
        const content = input.value.trim();
        if (!content || !this._selectedRequestId) return;

        input.value = '';
        try {
            const msg = await Api.chat.send(this._selectedRequestId, this._userId, content);
            // Mark own message as read
            this._lastReadIds[this._selectedRequestId] = msg.id;
            this.saveReadState();
            this.loadMessages(); 
        } catch (err) {
            alert('Failed to send message: ' + err.message);
        }
    },

    startGlobalPolling() {
        // Poll for all active contacts to check for new messages
        this._pollInterval = setInterval(async () => {
            try {
                const contacts = await Api.chat.contacts(this._userId);
                let changed = false;

                for (const c of contacts) {
                    const messages = await Api.chat.messages(c.id, this._userId);
                    const lastMsg = messages[messages.length - 1];
                    const lastRead = this._lastReadIds[c.id] || 0;
                    
                    if (lastMsg && lastMsg.id > lastRead) {
                        const newUnread = messages.filter(m => m.id > lastRead && m.sender.id !== this._userId).length;
                        if (this._unreadCounts[c.id] !== newUnread) {
                            this._unreadCounts[c.id] = newUnread;
                            changed = true;
                        }
                    } else if (this._unreadCounts[c.id] !== 0) {
                        this._unreadCounts[c.id] = 0;
                        changed = true;
                    }

                    // If currently viewing this contact, update messages
                    if (this._isOpen && this._selectedRequestId === c.id) {
                        this.renderMessages(messages);
                        const maxId = messages.reduce((max, m) => Math.max(max, m.id), 0);
                        if (maxId > lastRead) {
                            this._lastReadIds[c.id] = maxId;
                            this._unreadCounts[c.id] = 0;
                            this.saveReadState();
                            changed = true;
                        }
                    }
                }

                if (changed) {
                    if (this._isOpen && !this._selectedRequestId) {
                        this.loadContacts();
                    }
                    this.updateGlobalBadge();
                }
            } catch (e) {
                console.error('Polling error', e);
            }
        }, 4000); // Poll every 4 seconds to be gentler on the server
    },

    updateGlobalBadge() {
        const total = Object.values(this._unreadCounts).reduce((sum, n) => sum + n, 0);
        const badge = document.getElementById('chat-global-badge');
        if (total > 0) {
            badge.textContent = total;
            badge.style.display = 'flex';
        } else {
            badge.style.display = 'none';
        }
    },

    stopPolling() {
        if (this._pollInterval) {
            clearInterval(this._pollInterval);
            this._pollInterval = null;
        }
    },

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
};
