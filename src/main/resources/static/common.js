// common.js — shared helpers used across pages (auth state, toast, nav)

function money(n) { return '₦' + n.toLocaleString(); }

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, character => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  })[character]);
}

function getToken() { return localStorage.getItem('qc_token'); }
function getUser() {
  const raw = localStorage.getItem('qc_user');
  return raw ? JSON.parse(raw) : null;
}
function setSession(token, user) {
  localStorage.setItem('qc_token', token);
  localStorage.setItem('qc_user', JSON.stringify(user));
}
function clearSession() {
  localStorage.removeItem('qc_token');
  localStorage.removeItem('qc_user');
}

function showToast(msg, isError) {
  let t = document.getElementById('toast');
  if (!t) {
    t = document.createElement('div');
    t.id = 'toast';
    t.className = 'toast';
    document.body.appendChild(t);
  }
  t.textContent = msg;
  t.className = 'toast show' + (isError ? ' error' : '');
  setTimeout(() => { t.className = 'toast' + (isError ? ' error' : ''); }, 2200);
}

// Renders the account area of the nav (Sign in vs. Hi, Name)
function renderAccountNav(elId) {
  const el = document.getElementById(elId);
  if (!el) return;
  const user = getUser();
  if (user) {
    if (user.accountType === 'admin') {
      el.innerHTML = `
        <a href="admin-dashboard.html">Review applications</a>
        <button class="account-pill" onclick="logout()">Admin — Log out</button>
      `;
      return;
    }
    if (user.accountType === 'merchant') {
      el.innerHTML = `
        <a href="stores.html">Stores</a>
        <a href="help.html">Help</a>
        <a href="merchant-dashboard.html">My store</a>
        <button class="account-pill" onclick="logout()">Hi, ${escapeHtml(user.name.split(' ')[0])} — Log out</button>
      `;
      return;
    }
    el.innerHTML = `
      <a href="stores.html">Stores</a>
      <a href="help.html">Help</a>
      <a href="orders.html">My orders</a>
      <button class="account-pill" onclick="logout()">Hi, ${escapeHtml(user.name.split(' ')[0])} — Log out</button>
    `;
  } else {
    el.innerHTML = `
      <a href="stores.html">Stores</a>
      <a href="help.html">Help</a>
      <a href="login.html" class="account-pill">Sign in</a>
    `;
  }
}

function logout() {
  clearSession();
  showToast('Logged out');
  setTimeout(() => window.location.href = 'index.html', 600);
}

// Wrapper around fetch that attaches the auth token automatically
async function api(path, options = {}) {
  const token = getToken();
  const headers = Object.assign({ 'Content-Type': 'application/json' }, options.headers || {});
  if (token) headers['Authorization'] = 'Bearer ' + token;

  const res = await fetch(path, Object.assign({}, options, { headers }));
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(data.error || 'Something went wrong. Please try again.');
  }
  return data;
}

function getAnalyticsConsent() {
  return document.cookie.split('; ').find(cookie => cookie.startsWith('qc_analytics_consent='))?.split('=')[1] || '';
}

function setAnalyticsConsent(value) {
  const secure = window.location.protocol === 'https:' ? '; Secure' : '';
  document.cookie = `qc_analytics_consent=${value}; Max-Age=15552000; Path=/; SameSite=Lax${secure}`;
}

async function refreshVisitorCount() {
  const display = document.getElementById('visitorCount');
  try {
    const result = await api('/api/visits', { method: 'POST' });
    if (display) display.textContent = `${result.uniqueVisitors.toLocaleString()} unique browsers counted`;
  } catch {
    if (display) display.textContent = 'Visitor count unavailable';
  }
}

function initializeVisitorCounter(forcePrompt = false) {
  const consent = getAnalyticsConsent();
  const display = document.getElementById('visitorCount');

  if (consent === 'accepted' && !forcePrompt) {
    refreshVisitorCount();
    return;
  }
  if (consent === 'declined' && !forcePrompt) {
    if (display) display.textContent = 'Visitor counting declined';
    return;
  }

  document.getElementById('cookieNotice')?.remove();
  const notice = document.createElement('aside');
  notice.id = 'cookieNotice';
  notice.className = 'cookie-notice';
  notice.setAttribute('aria-label', 'Visitor count cookie consent');
  notice.setAttribute('aria-live', 'polite');
  notice.innerHTML = `
    <div>
      <strong>Allow cookies for the visitor count?</strong>
      <p>Accept to count unique browsers with a first-party cookie. We do not collect your name, IP address, or delivery address for this count.</p>
    </div>
    <div class="cookie-actions">
      <button type="button" class="cookie-accept">Accept</button>
      <button type="button" class="cookie-decline">Deny</button>
    </div>
  `;
  notice.querySelector('.cookie-accept').addEventListener('click', () => {
    setAnalyticsConsent('accepted');
    notice.remove();
    refreshVisitorCount();
  });
  notice.querySelector('.cookie-decline').addEventListener('click', () => {
    setAnalyticsConsent('declined');
    notice.remove();
    if (display) display.textContent = 'Visitor counting declined';
    api('/api/visits', { method: 'POST' }).catch(() => {});
  });
  document.body.appendChild(notice);
}

initializeVisitorCounter();

function openVisitorCookieSettings(event) {
  event.preventDefault();
  initializeVisitorCounter(true);
}
