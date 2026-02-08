/* ==================== COMMON JS - Keep4Life ==================== */

// ==================== Global Variables ====================
const API_BASE = '/api';
let csrfToken = null;
let accessToken = null;
let currentUser = null;
let currentLang = localStorage.getItem('language') || 'ko';

// SSE related variables
let eventSource = null;
let reconnectTimer = null;
let isPageUnloading = false;
let notifications = [];
let unreadCount = 0;
let lastEventId = localStorage.getItem('lastEventId') || '';
let sseReconnectAttempts = 0;
const MAX_SSE_RECONNECT_ATTEMPTS = 5;

// ==================== CSRF Token Functions ====================
function getCsrfToken() {
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === 'XSRF-TOKEN') {
            return decodeURIComponent(value);
        }
    }
    return null;
}

async function initCsrfToken() {
    try {
        await fetch(`${API_BASE}/auth/csrf-token`, {
            credentials: 'include'
        });
        csrfToken = getCsrfToken();
    } catch (error) {
        console.error('CSRF token init failed:', error);
    }
}

// ==================== Access Token Functions ====================
function setAccessToken(token) {
    accessToken = token;
}

function getAccessToken() {
    return accessToken;
}

let _refreshPromise = null;

async function refreshAccessToken() {
    // 이미 refresh 중이면 기존 Promise를 재사용 (race condition 방지)
    if (_refreshPromise) {
        return _refreshPromise;
    }

    _refreshPromise = _doRefreshAccessToken();
    try {
        return await _refreshPromise;
    } finally {
        _refreshPromise = null;
    }
}

async function _doRefreshAccessToken() {
    try {
        if (!csrfToken) {
            csrfToken = getCsrfToken();
        }

        const headers = { 'Content-Type': 'application/json' };

        if (csrfToken) {
            headers['X-XSRF-TOKEN'] = csrfToken;
        }

        const response = await fetch(`${API_BASE}/auth/refresh`, {
            method: 'POST',
            headers: headers,
            credentials: 'include'
        });

        if (response.ok) {
            const data = await response.json();
            const newToken = data.accessToken || data.access_token || data.token;
            if (newToken && typeof newToken === 'string') {
                setAccessToken(newToken);
                return true;
            }
        }
        return false;
    } catch (error) {
        return false;
    }
}

// ==================== Auth Fetch ====================
async function authFetch(url, options = {}) {
    if (!csrfToken) {
        csrfToken = getCsrfToken();
    }

    // FormData인 경우 Content-Type을 설정하지 않음 (브라우저가 자동으로 boundary 설정)
    const isFormData = options.body instanceof FormData;

    const defaultOptions = {
        credentials: 'include',
        headers: {
            ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
            ...(options.headers || {})
        }
    };

    if (accessToken) {
        defaultOptions.headers['Authorization'] = `Bearer ${accessToken}`;
    }

    if (options.method && ['POST', 'PUT', 'DELETE', 'PATCH'].includes(options.method.toUpperCase())) {
        if (csrfToken) {
            defaultOptions.headers['X-XSRF-TOKEN'] = csrfToken;
        }
    }

    const finalOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers
        }
    };

    // FormData인 경우 Content-Type 헤더 제거 (혹시 options.headers에 있을 경우)
    if (isFormData) {
        delete finalOptions.headers['Content-Type'];
    }

    const response = await fetch(url, finalOptions);

    const responseCsrfToken = response.headers.get('X-CSRF-TOKEN');
    if (responseCsrfToken) {
        csrfToken = responseCsrfToken;
    } else {
        const cookieToken = getCsrfToken();
        if (cookieToken && cookieToken !== csrfToken) {
            csrfToken = cookieToken;
        }
    }

    if (response.status === 401) {
        const refreshed = await refreshAccessToken();
        if (refreshed && accessToken) {
            finalOptions.headers['Authorization'] = `Bearer ${accessToken}`;
            return await fetch(url, finalOptions);
        } else {
            currentUser = null;
            accessToken = null;
            // 로그인 페이지(index.html)로 직접 리다이렉트
            window.location.href = '/';
            throw new Error('Authentication required');
        }
    }

    if (response.status === 403) {
        // 애플리케이션 레벨 403인지 확인 (커스텀 에러 포맷에 code 필드가 있음)
        try {
            const cloned = response.clone();
            const body = await cloned.json();
            if (body.code) {
                // 애플리케이션 에러 (ForbiddenException 등) - CSRF 재시도 불필요
                return response;
            }
        } catch (e) {
            // JSON 파싱 실패 - CSRF 에러로 간주
        }
        // CSRF 토큰 만료로 인한 403 - 토큰 갱신 후 재시도
        await initCsrfToken();
        if (csrfToken) {
            finalOptions.headers['X-XSRF-TOKEN'] = csrfToken;
            return await fetch(url, finalOptions);
        }
    }

    return response;
}

function checkAuth() {
    return !!accessToken;
}

// ==================== Language Functions ====================
function applyLanguage() {
    console.log('[COMMON] Applying language:', currentLang);

    // 1. General text elements
    document.querySelectorAll('[data-lang-ko]').forEach(element => {
        const ko = element.getAttribute('data-lang-ko');
        const en = element.getAttribute('data-lang-en');
        if (ko && en) {
            if (element.tagName !== 'OPTION') {
                element.textContent = currentLang === 'ko' ? ko : en;
            }
        }
    });

    // 2. Placeholder attributes
    document.querySelectorAll('[data-placeholder-ko]').forEach(element => {
        const ko = element.getAttribute('data-placeholder-ko');
        const en = element.getAttribute('data-placeholder-en');
        if (ko && en) {
            element.placeholder = currentLang === 'ko' ? ko : en;
        }
    });

    // 3. Select option texts
    document.querySelectorAll('select').forEach(select => {
        select.querySelectorAll('option[data-lang-ko]').forEach(option => {
            const ko = option.getAttribute('data-lang-ko');
            const en = option.getAttribute('data-lang-en');
            if (ko && en) {
                option.textContent = currentLang === 'ko' ? ko : en;
            }
        });
    });

    // Show body after language is applied
    document.body.classList.add('lang-ready');

    console.log('[COMMON] Language applied successfully');
}

function t(ko, en) {
    return currentLang === 'ko' ? ko : en;
}

function getTypeText(type) {
    const typeMap = {
        'CHECK': { ko: '체크', en: 'Check' },
        'TIME': { ko: '시간', en: 'Time' },
        'TEXT': { ko: '텍스트', en: 'Text' },
        'NUMBER': { ko: '숫자', en: 'Number' },
        'CHECKLIST': { ko: '체크리스트', en: 'Checklist' }
    };
    return typeMap[type] ? typeMap[type][currentLang] : type;
}

function toggleLanguage() {
    currentLang = currentLang === 'ko' ? 'en' : 'ko';
    localStorage.setItem('language', currentLang);
    const toggleBtn = document.getElementById('languageToggle');
    if (toggleBtn) {
        toggleBtn.textContent = currentLang === 'ko' ? 'EN' : 'KO';
    }
    applyLanguage();

    // Call page-specific callback if defined
    if (typeof onLanguageChange === 'function') {
        onLanguageChange();
    }
}

// ==================== Toast Notifications ====================
// NOTE: This function may be overridden by page-specific implementations in HTML files
function showToast(notification) {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    // Handle both uppercase (from SSE) and lowercase types
    const type = (notification.type || 'default').toLowerCase();

    const toast = document.createElement('div');
    toast.className = `toast-notification ${type}`;

    const typeLabels = {
        'comment': currentLang === 'ko' ? '새 댓글' : 'New Comment',
        'reply': currentLang === 'ko' ? '새 답글' : 'New Reply',
        'like': currentLang === 'ko' ? '새 좋아요' : 'New Like',
        'friend_request': currentLang === 'ko' ? '친구 요청' : 'Friend Request',
        'friend_accept': currentLang === 'ko' ? '친구 수락' : 'Friend Accepted',
        'chat_message': currentLang === 'ko' ? '새 메시지' : 'New Message'
    };

    // Support both notification.data structure (from SSE) and flat structure
    let title = typeLabels[type] || notification.title || '';
    let message = '';

    if (notification.data) {
        // SSE notification format
        const categoryTitle = notification.data.categoryTitle || '';
        const categoryPrefix = categoryTitle ? `[${categoryTitle}] ` : '';

        if (type === 'comment') {
            message = `${categoryPrefix}${notification.data.senderEmail}: ${notification.data.comment}`;
        } else if (type === 'reply') {
            message = `${categoryPrefix}${notification.data.senderEmail}${currentLang === 'ko' ? '님이 답글을 남겼습니다' : ' replied'}`;
        } else if (type === 'friend_request') {
            message = `${notification.data.senderNickname || notification.data.senderEmail || ''}${currentLang === 'ko' ? '님이 친구 요청을 보냈습니다' : ' sent you a friend request'}`;
        } else if (type === 'friend_accept') {
            message = `${notification.data.senderNickname || notification.data.senderEmail || ''}${currentLang === 'ko' ? '님이 친구 요청을 수락했습니다' : ' accepted your friend request'}`;
        } else if (type === 'chat_message') {
            message = `${notification.data.senderNickname || ''}${currentLang === 'ko' ? ': ' : ': '}${notification.data.messagePreview || ''}`;
        } else {
            message = `${categoryPrefix}${notification.data.senderEmail}${currentLang === 'ko' ? '님이 좋아요를 눌렀습니다' : ' liked your category'}`;
        }
    } else {
        message = notification.message || '';
    }

    toast.innerHTML = `
        <div class="toast-title">${title}</div>
        <div class="toast-message">${message}</div>
        <div class="toast-time">${formatTimeAgo(notification.createdAt || new Date())}</div>
    `;

    toast.onclick = () => {
        toast.remove();
        if (notification.onClick) {
            notification.onClick();
        } else {
            toggleNotificationModal();
        }
    };

    container.appendChild(toast);

    setTimeout(() => {
        if (toast.parentNode) {
            toast.remove();
        }
    }, 5000);
}

function formatTimeAgo(date) {
    const now = new Date();
    const past = new Date(date);
    const diffMs = now - past;
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHour = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHour / 24);

    if (diffSec < 60) {
        return currentLang === 'ko' ? '방금 전' : 'Just now';
    } else if (diffMin < 60) {
        return currentLang === 'ko' ? `${diffMin}분 전` : `${diffMin}m ago`;
    } else if (diffHour < 24) {
        return currentLang === 'ko' ? `${diffHour}시간 전` : `${diffHour}h ago`;
    } else {
        return currentLang === 'ko' ? `${diffDay}일 전` : `${diffDay}d ago`;
    }
}

// ==================== Notification Functions ====================
// NOTE: These functions may be overridden by page-specific implementations in HTML files

function updateNotificationBadge() {
    // Try both ID and class selectors for compatibility
    const badge = document.getElementById('notificationBadge') || document.querySelector('.notification-badge');
    if (badge) {
        if (isNaN(unreadCount)) {
            unreadCount = 0;
        }
        if (unreadCount > 0) {
            const displayCount = unreadCount > 99 ? '99+' : unreadCount;
            badge.textContent = displayCount;
            badge.style.display = 'block';
        } else {
            badge.style.display = 'none';
        }
    }
}

function toggleNotificationModal() {
    const modal = document.getElementById('notificationModal');
    if (modal) {
        modal.classList.toggle('active');
        if (modal.classList.contains('active')) {
            renderNotificationList();
            updateMarkAllReadButton();
        }
    }
}

function updateMarkAllReadButton() {
    const btn = document.querySelector('.mark-all-read-btn');
    if (btn) {
        btn.disabled = notifications.length === 0;
    }
}

function renderNotificationList() {
    const listElement = document.getElementById('notificationList');
    if (!listElement) return;

    if (notifications.length === 0) {
        listElement.innerHTML = `
            <div class="notification-empty" data-lang-en="No notifications" data-lang-ko="알림이 없습니다">
                ${currentLang === 'ko' ? '알림이 없습니다' : 'No notifications'}
            </div>
        `;
        updateMarkAllReadButton();
        return;
    }

    const notificationItems = notifications.map(notif => {
        const typeLabels = {
            'comment': currentLang === 'ko' ? '댓글' : 'Comment',
            'reply': currentLang === 'ko' ? '답글' : 'Reply',
            'like': currentLang === 'ko' ? '좋아요' : 'Like',
            'friend_request': currentLang === 'ko' ? '친구 요청' : 'Friend Request',
            'friend_accept': currentLang === 'ko' ? '친구 수락' : 'Friend Accepted',
            'chat_message': currentLang === 'ko' ? '메시지' : 'Message'
        };

        return `
            <div class="notification-item ${notif.read ? '' : 'unread'}" data-id="${notif.id}">
                <div class="notification-item-header">
                    <span class="notification-item-type ${notif.type}">${typeLabels[notif.type] || notif.type}</span>
                    <span class="notification-item-time">${formatTimeAgo(notif.createdAt)}</span>
                </div>
                <div class="notification-item-content">
                    <span class="notification-item-sender">${notif.senderNickname || ''}</span>
                    ${notif.message || ''}
                </div>
            </div>
        `;
    }).join('');

    listElement.innerHTML = notificationItems;

    // Click event to mark as read
    listElement.querySelectorAll('.notification-item').forEach(item => {
        item.addEventListener('click', async () => {
            const id = item.getAttribute('data-id');
            await markAsRead(id);
        });
    });
}

async function markAsRead(notificationId) {
    try {
        const response = await authFetch(`${API_BASE}/notification/${notificationId}/read`, {
            method: 'PUT'
        });
        if (response.ok) {
            const notif = notifications.find(n => n.id === notificationId);
            if (notif && !notif.read) {
                notif.read = true;
                unreadCount = Math.max(0, unreadCount - 1);
                updateNotificationBadge();
                renderNotificationList();
            }
        }
    } catch (error) {
        console.error('Error marking notification as read:', error);
    }
}

async function markAllAsRead() {
    try {
        const response = await authFetch(`${API_BASE}/notification/read-all`, {
            method: 'PUT'
        });
        if (response.ok) {
            notifications.forEach(n => n.read = true);
            unreadCount = 0;
            updateNotificationBadge();
            renderNotificationList();
            updateMarkAllReadButton();
        }
    } catch (error) {
        console.error('Error marking all as read:', error);
    }
}

async function loadNotifications() {
    try {
        const response = await authFetch(`${API_BASE}/notification/unread`);
        if (response.ok) {
            notifications = await response.json();
            unreadCount = notifications.filter(n => !n.read).length;
            updateNotificationBadge();
        }
    } catch (error) {
        console.error('Error loading notifications:', error);
    }
}

// ==================== SSE Functions ====================
function connectSSE() {
    if (!accessToken) {
        console.log('[SSE] No access token, skipping SSE connection');
        return;
    }

    if (eventSource) {
        console.log('[SSE] Closing existing connection');
        eventSource.close();
    }

    console.log('[SSE] Connecting...');

    const EventSourceClass = window.EventSourcePolyfill || window.EventSource;

    eventSource = new EventSourceClass(`${API_BASE}/sse/subscribe`, {
        headers: {
            'Authorization': `Bearer ${accessToken}`
        },
        withCredentials: true,
        lastEventId: lastEventId
    });

    eventSource.onopen = () => {
        console.log('[SSE] Connected');
        sseReconnectAttempts = 0;
    };

    eventSource.onmessage = (event) => {
        console.log('[SSE] Message:', event.data);
        if (event.lastEventId) {
            lastEventId = event.lastEventId;
            localStorage.setItem('lastEventId', lastEventId);
        }

        try {
            const notification = JSON.parse(event.data);
            if (notification.type !== 'ping') {
                notifications.unshift(notification);
                unreadCount++;
                updateNotificationBadge();
                showToast(notification);
            }
        } catch (e) {
            console.log('[SSE] Non-JSON message:', event.data);
        }
    };

    eventSource.onerror = (error) => {
        console.error('[SSE] Error:', error);
        eventSource.close();

        if (!isPageUnloading && sseReconnectAttempts < MAX_SSE_RECONNECT_ATTEMPTS) {
            sseReconnectAttempts++;
            const delay = Math.min(1000 * Math.pow(2, sseReconnectAttempts), 30000);
            console.log(`[SSE] Reconnecting in ${delay}ms (attempt ${sseReconnectAttempts})`);
            reconnectTimer = setTimeout(connectSSE, delay);
        }
    };
}

function disconnectSSE() {
    if (reconnectTimer) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
    }
    if (eventSource) {
        eventSource.close();
        eventSource = null;
    }
}

// Cleanup on page unload
window.addEventListener('beforeunload', () => {
    isPageUnloading = true;
    disconnectSSE();
});

// ==================== Utility Functions ====================
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}
