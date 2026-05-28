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
  const enterBtn = document.getElementById("enterBtn");
  const appBtns = document.querySelectorAll(".appBtn");
  //const appBtn = document.getElementById("appBtn");

  if (token && role && user) {
    enterBtn.classList.remove("hidden");
    signinBtn.classList.add("hidden");

    let path = "index";

    if (role === "GESTOR") path = "manager";
    else if (role === "MOTORISTA") path = "driver";
    else if (role === "CLIENTE") path = "user";

    enterBtn.href = path;
    appBtns.forEach(btn => {
      btn.href = path;
      if (path == "manager") {
        btn.textContent = "Start Managing and Analysing";
      } else if (path == "driver") {
        btn.textContent = "Start Driving";
      } else if (path == "user") {
        btn.textContent = "Book a Ride";
      }
    });

  } else {
    enterBtn.classList.add("hidden");
    signinBtn.classList.remove("hidden");

    appBtns.forEach(btn => {
      btn.href = "signin";
      btn.textContent = "Login or Register";
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

function isValidEmail(email) {
  const emailRegex =
    /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

  return emailRegex.test(email);
}


async function login() {
  const isPt = (localStorage.getItem("lang") || "en") === "pt";
  const email = document.getElementById("login-email").value;
  const emailError = document.getElementById("login-email-error");
  const password = document.getElementById("login-pass").value;
  const passwordError = document.getElementById("login-pass-error");
  const loginError = document.getElementById("login-error");

  emailError.textContent = "\u00A0";
  passwordError.textContent = "\u00A0";
  loginError.textContent = "\u00A0";

  let hasError = false;

  if (!email || !password) {
    emailError.textContent = !email ? (isPt ? "Email é obrigatório" : "Email is required") : "\u00A0";
    passwordError.textContent = !password ? (isPt ? "Senha é obrigatória" : "Password is required") : "\u00A0";
    hasError = true;
  }

  if (!isValidEmail(email)) {
    emailError.textContent = (isPt ? "Formato de email inválido!" : "Invalid email format");
    hasError = true;
  }

  if (password.length < 6) {
      passwordError.textContent = (isPt ?  "Senha muito curta (mínimo 6 caracteres)" : "Password too short (min 6 chars)");
    hasError = true;
  }

  if (hasError) return false;

  try {
    const response = await fetch("/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "accept": "*/*"
      },
      body: JSON.stringify({
        "email": email,
        "password": password
      })
    });

    if (!response.ok) {
      loginError.textContent = (isPt ? "Credenciais inválidas" : "Invalid credentials");
      throw new Error("Login failed");
      return false;
    }

    const data = await response.json();

    localStorage.setItem("token", data.token);
    localStorage.setItem("role", data.role);
    localStorage.setItem("user", JSON.stringify(data.user));

    showToast(isPt ? "Login bem-sucedido" : "Login successful");

    let page = "index";
    if (data.role === "GESTOR") page = "manager";
    else if (data.role === "MOTORISTA") page = "driver";
    else if (data.role === "CLIENTE") page = "user";

    const currentLang = localStorage.getItem("lang") || "en";
    window.location.href = `/${currentLang}/${page}`;

  } catch (error) {
    console.error(error);
    loginError.textContent = (isPt ? "Credenciais inválidas" : "Invalid credentials");
    return false;
  }
}




async function create() {
  const isPt = (localStorage.getItem("lang") || "en") === "pt";

  const name = document.getElementById("create-name").value.trim();
  const nameError = document.getElementById("create-name-error");

  const email = document.getElementById("create-email").value.trim();
  const emailError = document.getElementById("create-email-error");

  const dateBirth = document.getElementById("create-dateBirth").value;
  const dateBirthError = document.getElementById("create-dateBirth-error");

  const nif = document.getElementById("create-nif").value.trim();
  const nifError = document.getElementById("create-nif-error");

  const gender = document.getElementById("create-gender").value;
  const genderError = document.getElementById("create-gender-error");

  const password = document.getElementById("create-password").value;
  const passwordError = document.getElementById("create-password-error");

  const confirmPassword = document.getElementById("confirm-password").value;
  const confirmPasswordError = document.getElementById("confirm-password-error");

  const createError = document.getElementById("create-error");

  let hasError = false;

  if (!isValidEmail(email)) {
    emailError.textContent = (isPt ? "Formato de email inválido!" : "Invalid email format");
    hasError = true;
  }

  if (!/^\d{9}$/.test(nif)) {
    nifError.textContent = (isPt ? "NIF deve conter 9 dígitos" : "NIF must contain 9 digits");
    hasError = true;
  }

  if (name.length < 3) {
    nameError.textContent = (isPt ? "Nome muito curto (mínimo 3 caracteres)" : "Name too short (min 3 chars)");
    hasError = true;
  }

  if (!/^[A-Za-zÀ-ÖØ-öø-ÿ\s]+$/.test(name)) {
    nameError.textContent = (isPt ? "Nome deve conter apenas letras e espaços" : "Name must contain only letters and spaces");
    hasError = true;
  }

  if (new Date() - new Date(dateBirth) < 18 * 365.25 * 24 * 60 * 60 * 1000) {
    dateBirthError.textContent = (isPt ? "Tem de ter pelo menos 18 anos" : "You must be at least 18 years old");
    hasError = true;
  }

  if (password.length < 6) {
    passwordError.textContent = (isPt ?  "Senha muito curta (mínimo 6 caracteres)" : "Password too short (min 6 chars)");
    hasError = true;
  }

  if (password !== confirmPassword) {
    confirmPasswordError.textContent = (isPt ? "As senhas não coincidem" : "Passwords do not match");
    hasError = true;
  }

  if (!email || !password || !name || !dateBirth || !nif || !gender) {
    emailError.textContent = !email ? (isPt ? "Email é obrigatório" : "Email is required") : "\u00A0";
    passwordError.textContent = !password ? (isPt ? "Senha é obrigatória" : "Password is required") : "\u00A0";
    nameError.textContent = !name ? (isPt ? "Nome é obrigatório" : "Name is required") : "\u00A0";
    dateBirthError.textContent = !dateBirth ? (isPt ? "Data de nascimento é obrigatória" : "Date of birth is required") : "\u00A0";
    nifError.textContent = !nif ? (isPt ? "NIF é obrigatório" : "NIF is required") : "\u00A0";
    genderError.textContent = !gender ? (isPt ? "Por favor, selecione um gênero" : "Please select a gender") : "\u00A0";
    hasError = true;
  }

  if (hasError) return false;

  try {
    const response = await fetch("/api/auth/register/client", {
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
      throw new Error(response);
    }

    const data = await response.json();

    localStorage.setItem("token", data.token);
    localStorage.setItem("role", data.role);
    localStorage.setItem("user", JSON.stringify(data.cliente));

  } catch (error) {
    console.error(error);
    createError.textContent = (isPt ? "Falha no registro" : "Registration failed");
    return false;
  }
}

function togglePassword(e) {
  if (e) e.preventDefault(); 

  const passInput = document.getElementById("login-pass");
  const toggleBtn = document.getElementById("toggle-pass-btn");

  if (passInput && toggleBtn) {
    const currentLang = localStorage.getItem("lang") || "en";

    if (passInput.type === "password") {
      passInput.type = "text";
      toggleBtn.textContent = currentLang === "pt" ? "Ocultar" : "Hide";
    } else {
      passInput.type = "password";
      toggleBtn.textContent = currentLang === "pt" ? "Mostrar" : "Show";
    }
  }
}
function toggleCreatePassword(e, inputId, btnId) {
  if (e) e.preventDefault(); 

  const passInput = document.getElementById(inputId);
  const toggleBtn = document.getElementById(btnId);

  if (passInput && toggleBtn) {
    const currentLang = localStorage.getItem("lang") || "en";

    if (passInput.type === "password") {
      passInput.type = "text";
      toggleBtn.textContent = currentLang === "pt" ? "Ocultar" : "Hide";
    } else {
      passInput.type = "password";
      toggleBtn.textContent = currentLang === "pt" ? "Mostrar" : "Show";
    }
  }
}
function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
  localStorage.removeItem("user");
  window.location.href = "index";
}


