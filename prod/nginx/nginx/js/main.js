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



document.addEventListener('DOMContentLoaded', () => {
  //  Active nav-itme highlith
  const p = location.pathname.split('/').pop();
  document.querySelectorAll('[data-nav-href]').forEach(a => {
    if (a.dataset.navHref === p) a.classList.add('nav-active');
  });



  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");
  const user = JSON.parse(localStorage.getItem("user"));

  const signinBtn = document.getElementById("signinBtn");
  const appBtns = document.querySelectorAll(".appBtn");
  //const appBtn = document.getElementById("appBtn");

  if (token && role && user) {
    signinBtn.classList.add("hidden");

    let path = "index";

    if (role === "GESTOR") path = "manager";
    else if (role === "MOTORISTA") path = "driver";
    else if (role === "CLIENTE") path = "user";

    appBtns.forEach(btn => {
      btn.classList.remove("hidden");
      btn.href = path;
    });

  } else {
    signinBtn.classList.remove("hidden");

    appBtns.forEach(btn => {
      btn.classList.add("hidden");
    });

    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("user");
  }




  const toggle = document.getElementById("langToggle");
  const menu = document.getElementById("langMenu");

  const savedLang = localStorage.getItem("lang") || "en";
  setLang(savedLang);

  toggle.addEventListener("click", () => {
    menu.classList.toggle("hidden");
  });

  document.querySelectorAll("[data-lang]").forEach(btn => {
    btn.addEventListener("click", () => {
      const lang = btn.dataset.lang;

      localStorage.setItem("lang", lang);

      setLang(lang);
      switchLang(lang);

      menu.classList.add("hidden");
    });
  });

  function setLang(lang) {
    toggle.innerText = lang.toUpperCase() + " ▾";
    document.documentElement.lang = lang;
  }

  document.addEventListener("click", (e) => {
    if (!toggle.contains(e.target) && !menu.contains(e.target)) {
      menu.classList.add("hidden");
    }
  });
});





function switchLang(lang) {
  const parts = window.location.pathname.split("/").filter(Boolean);

  // if URL is /en/about → ["en","about"]
  if (parts.length === 0) {
    window.location.href = `/${lang}/index`;
    return;
  }

  const page = parts[1] || "index";

  window.location.href = `/${lang}/${page}`;
}




async function login() {
  const email = document.getElementById("login-email").value;
  const password = document.getElementById("login-pass").value;

  try {
    const response = await fetch("https://lispuber.xyz/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "accept": "*/*"
      },
      body: JSON.stringify({
        email: email,
        password: password
      })
    });

    if (!response.ok) {
      throw new Error("Login failed");
    }

    const data = await response.json();

    localStorage.setItem("token", data.token);
    localStorage.setItem("role", data.role);
    localStorage.setItem("user", JSON.stringify(data.user));

    showToast("Login successful");
    window.location.href = "index";

  } catch (error) {
    console.error(error);
    showToast("Login failed");
  }
}




async function create() {
  const nif = document.getElementById("create-nif").value;
  const email = document.getElementById("create-email").value;
  const password = document.getElementById("create-password").value;
  const name = document.getElementById("create-name").value;
  const gender = document.getElementById("create-gender").value;
  const dateBirth = document.getElementById("create-dateBirth").value;

  try {
    const response = await fetch("https://lispuber.xyz/api/auth/register/client", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "accept": "*/*"
      },
      body: JSON.stringify({
        nif: nif,
        email: email,
        senha: password,
        nome: name,
        genero: gender,
        dataNascimento: dateBirth
      })
    });

    if (!response.ok) {
      throw new Error("Login failed");
    }

    const data = await response.json();

    localStorage.setItem("token", data.token);
    localStorage.setItem("role", data.role);
    localStorage.setItem("user", JSON.stringify(data.cliente));

    showToast("Login successful");
    // window.location.href = "index";

  } catch (error) {
    console.error(error);
    showToast("Login failed");
  }
}



function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
  localStorage.removeItem("user");
  window.location.href = "index";
}


