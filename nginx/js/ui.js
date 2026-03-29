// Theme
const Theme = {
  get() { return localStorage.getItem('dash-theme') || 'dark'; },
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



// Toast
let _toastTimer;
function showToast(msg, ms = 2600) {
  let t = document.getElementById('toast');
  if (!t) { t = document.createElement('div'); t.id = 'toast'; document.body.appendChild(t); }
  t.textContent = msg;
  t.classList.add('show');
  clearTimeout(_toastTimer);
  _toastTimer = setTimeout(() => t.classList.remove('show'), ms);
}



// Fare helpers
const FARES = {
  economy: { base: 2.00, km: 1.20, label: 'Economy',  emoji: '🚗' },
  comfort: { base: 3.00, km: 1.80, label: 'Comfort',  emoji: '🛋' },
  premium: { base: 5.00, km: 2.80, label: 'Premium',  emoji: '👑' },
};

function calcFare(type, distKm) {
  const f = FARES[type];
  return (f.base + f.km * distKm).toFixed(2);
}



//  Active nav-itme highlith
document.addEventListener('DOMContentLoaded', () => {
  const p = location.pathname.split('/').pop();
  document.querySelectorAll('[data-nav-href]').forEach(a => {
    if (a.dataset.navHref === p) a.classList.add('nav-active');
  });
});