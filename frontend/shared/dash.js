// ── DASH shared JS ────────────────────────────────────────────────────────────

// ── Theme ─────────────────────────────────────────────────────────────────────
const Theme = {
  get() { return localStorage.getItem('dash-theme') || 'light'; },
  set(t) {
    localStorage.setItem('dash-theme', t);
    document.documentElement.classList.toggle('dark', t === 'dark');
    document.querySelectorAll('[data-theme-icon]').forEach(el => {
      el.textContent = t === 'dark' ? '☀️' : '🌙';
    });
  },
  toggle() { this.set(this.get() === 'dark' ? 'light' : 'dark'); },
  init()   { this.set(this.get()); },
};

document.addEventListener('DOMContentLoaded', () => Theme.init());

// ── Toast ─────────────────────────────────────────────────────────────────────
let _toastTimer;
function showToast(msg, ms = 2600) {
  let t = document.getElementById('toast');
  if (!t) { t = document.createElement('div'); t.id = 'toast'; document.body.appendChild(t); }
  t.textContent = msg;
  t.classList.add('show');
  clearTimeout(_toastTimer);
  _toastTimer = setTimeout(() => t.classList.remove('show'), ms);
}

// ── Pseudo API ────────────────────────────────────────────────────────────────
const DashAPI = {
  _delay: ms => new Promise(r => setTimeout(r, ms)),

  async bookRide(payload) {
    await this._delay(1400);
    return {
      rideId: 'D-' + Math.random().toString(36).slice(2,6).toUpperCase(),
      driver: { name: 'Marcus Webb', rating: 4.97, vehicle: 'Tesla Model 3', plate: 'AB-123-CD' },
      eta: { economy: 4, comfort: 6, premium: 3 }[payload.type] ?? 4,
    };
  },

  async cancelRide() { await this._delay(400); return { ok: true }; },
};

// ── Fare helpers ──────────────────────────────────────────────────────────────
const FARES = {
  economy: { base: 2.00, km: 1.20, label: 'Economy',  emoji: '🚗' },
  comfort: { base: 3.00, km: 1.80, label: 'Comfort',  emoji: '🛋' },
  premium: { base: 5.00, km: 2.80, label: 'Premium',  emoji: '👑' },
};

function calcFare(type, distKm) {
  const f = FARES[type];
  return (f.base + f.km * distKm).toFixed(2);
}

// ── Active nav ────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  const p = location.pathname.split('/').pop();
  document.querySelectorAll('[data-nav-href]').forEach(a => {
    if (a.dataset.navHref === p) a.classList.add('nav-active');
  });
});