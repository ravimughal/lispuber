document.addEventListener("DOMContentLoaded", () => {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");

  if ((!token && !role) || (role !== "CLIENTE")) {
    window.location.href = "index";
    return;
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






  const profileName = document.getElementById("profile-name");
  const profileEmail = document.getElementById("profile-email");
  const profileRides = document.getElementById("profile-rides");
  const profileSpent = document.getElementById("profile-spent");

  profileEmail.innerText = JSON.parse(localStorage.getItem("user")).email;
  profileName.innerText = JSON.parse(localStorage.getItem("user")).nome;


  loadRideHistory();
});

let rideHistory = [];



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




function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
  localStorage.removeItem("user");
  window.location.href = "index";
}




async function pedido() {
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



async function loadRideHistory() {
  const token = localStorage.getItem("token");

  const res = await fetch("https://lispuber.xyz/api/pedidos-taxi/byCliente", {
    headers: {
      "Authorization": "Bearer " + token
    }
  });

  const data = await res.json();

  rideHistory = data.map(p => ({
    from: p.ruaOrigem,
    to: p.ruaDestino,
    date: new Date(p.dataSolicitacao).toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit'
    }),
    type: mapTipo(p.conforto?.nivelConforto),
    km: calcKm(p),
    fare: p.preco,
    rating: 5.0
  }));

  console.log("Loaded ride history:", rideHistory);

  renderRidesList();
  calculateStats(); // 👈 move stats here
}

function mapTipo(nivel) {
  switch(nivel) {
    case "BASICO": return "economy";
    case "CONFORTO": return "comfort";
    case "LUXO": return "premium";
    default: return "economy";
  }
}

function calcKm(p) {
  // simple distance estimation (optional)
  if (!p.latitudeOrigem || !p.latitudeDestino) return 0;

  const dx = p.latitudeDestino - p.latitudeOrigem;
  const dy = p.longitudeDestino - p.longitudeOrigem;

  return Math.sqrt(dx*dx + dy*dy) * 111; // rough km
}


function calculateStats(){
  var totalRides = rideHistory.length;

  var totalSpent = rideHistory.reduce(function(s, r){
    return s + (r.fare || 0);
  }, 0);

  const profileRides = document.getElementById("profile-rides");
  const profileSpent = document.getElementById("profile-spent");
  
  profileRides.innerText = totalRides;
  profileSpent.innerText = "€" + totalSpent.toFixed(2);
  console.log("Rides:", totalRides);
  console.log("Total spent:", totalSpent);
}