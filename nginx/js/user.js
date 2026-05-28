// ─────────────────────────────────────────────
// AUTH GUARD
// ─────────────────────────────────────────────
const token = localStorage.getItem("token");
const role  = localStorage.getItem("role");
if (!token || role !== "CLIENTE") { window.location.href = "index"; }

const user = JSON.parse(localStorage.getItem("user") || "{}");

// ─────────────────────────────────────────────
// API HELPER
// ─────────────────────────────────────────────
async function api(method, path, body) {
  const opts = {
    method,
    headers: { "Content-Type": "application/json", "Authorization": "Bearer " + token }
  };
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch("/api" + path, opts);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

// ─────────────────────────────────────────────
// TOAST
// ─────────────────────────────────────────────
let _tt;
function toast(msg) {
  const el = document.getElementById("toast");
  el.textContent = msg;
  el.classList.add("show");
  clearTimeout(_tt);
  _tt = setTimeout(() => el.classList.remove("show"), 2500);
}

// ─────────────────────────────────────────────
// THEME
// ─────────────────────────────────────────────
function applyTheme(t) {
  document.documentElement.classList.toggle("dark", t === "dark");
  document.getElementById("theme-btn").textContent = t === "dark" ? "☀️" : "🌙";
}
function toggleTheme() {
  const t = localStorage.getItem("dash-theme") === "dark" ? "light" : "dark";
  localStorage.setItem("dash-theme", t);
  applyTheme(t);
}
applyTheme(localStorage.getItem("dash-theme") || "light");

// ─────────────────────────────────────────────
// LANGUAGE
// ─────────────────────────────────────────────
function initLang() {
  const toggle = document.getElementById("langToggle");
  const menu   = document.getElementById("langMenu");
  const saved  = localStorage.getItem("lang") || "en";

  toggle.textContent = saved.toUpperCase() + " ▾";
  document.documentElement.lang = saved;

  toggle.addEventListener("click", () => menu.classList.toggle("hidden"));
  document.addEventListener("click", e => {
    if (!toggle.contains(e.target) && !menu.contains(e.target)) menu.classList.add("hidden");
  });
  document.querySelectorAll("[data-lang]").forEach(btn => {
    btn.addEventListener("click", () => {
      const lang = btn.dataset.lang;
      localStorage.setItem("lang", lang);
      const page = window.location.pathname.split("/").filter(Boolean)[1] || "index";
      window.location.href = `/${lang}/${page}`;
    });
  });
}

// ─────────────────────────────────────────────
// NAV
// ─────────────────────────────────────────────
const TABS = ["home", "activity", "profile"];

function goTab(tab) {
  const isOverlay = ["tracking", "rate"].includes(tab);
  document.querySelectorAll(".screen").forEach(s => s.classList.remove("active"));
  document.getElementById("screen-" + tab).classList.add("active");
  document.getElementById("main-nav").style.display = tab === "rate" ? "none" : "flex";

  if (!isOverlay) {
    TABS.forEach(t => document.getElementById("nav-" + t).classList.toggle("active", t === tab));
    if (tab === "home") setTimeout(() => map && map.invalidateSize(), 50);
    if (tab === "activity") { renderRidesList(); renderViagens(); }
  }
}

// ─────────────────────────────────────────────
// PROFILE
// ─────────────────────────────────────────────
const genderMap = {
  MASCULINO: "Male",
  FEMININO: "Female",
};

function initProfile() {
  document.getElementById("profile-name").textContent  = user.nome  || "—";
  document.getElementById("profile-name2").textContent = user.nome  || "Me";
  document.getElementById("profile-email").textContent = user.email || "—";
  document.getElementById("profile-nif").textContent   = user.nif   || "—";
  document.getElementById("profile-gender").textContent = genderMap[user.genero] || "Other";
  document.getElementById("profile-dob").textContent   = user.dataNascimento ? new Date(user.dataNascimento).toLocaleDateString() : "—";
}

// ─────────────────────────────────────────────
// RIDE HISTORY — pedidos-taxi
// ─────────────────────────────────────────────
let pedidos = [];

async function loadPedidos() {
  try {
    pedidos = await api("GET", "/pedidos-taxi/byCliente");
    renderRidesList();
    calcStats();
  } catch (e) {
    console.error("loadPedidos:", e);
  }
}

function renderRidesList() {
  const el = document.getElementById("rides-list");
  if (!el) return;
  el.innerHTML = "";
  pedidos.forEach(p => {
    const status =
    p.estadoPedido === "CONCLUIDO"
      ? "Concluded"
      : "Pending";

const fare = p.preco != null ? "€" + Number(p.preco).toFixed(2) : "";
const date = new Date(p.dataSolicitacao).toLocaleString([], {
  day: "2-digit",
  month: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
  hour12: false
});
const tipo = mapTipo(p.conforto?.nivelConforto);
const icon = { economy:"🚗", comfort:"🛋️", premium:"👑" }[tipo];
const btn = document.createElement("button");

btn.className = "w-full flex items-center gap-3 bg-white dark:bg-slate-800 rounded-xl border border-slate-100 dark:border-slate-700 p-3 text-left hover:border-sky-300 dark:hover:border-sky-700";

btn.innerHTML = `
  <div class="w-9 h-9 rounded-xl bg-sky-50 dark:bg-sky-950/50 border border-sky-100 dark:border-sky-800 flex items-center justify-center text-base flex-shrink-0">${icon}</div>
  <div class="flex-1 min-w-0">
    <div class="sora font-semibold text-sm text-slate-900 dark:text-white">
      ${p.ruaOrigem || "—"} → ${p.ruaDestino || "—"}
    </div>
    <div class="text-xs text-slate-400 mt-0.5">${date} · ${status}</div>
  </div>
  <div class="text-right flex-shrink-0">
    <div class="sora font-semibold text-sm text-slate-900 dark:text-white">${fare}</div>
  </div>`;

btn.onclick = () => openReceipt(p);
el.appendChild(btn);
});
if (!pedidos.length) el.innerHTML = '<p class="text-xs text-slate-400 text-center py-6">No rides yet.</p>';
}

function openReceipt(p) {
  const fare = p.preco != null ? "€" + Number(p.preco).toFixed(2) : "—";
  const date = new Date(p.dataSolicitacao).toLocaleString();
  
  const statusAtual = p.estadoPedido === "CONCLUIDO" ? "Concluded" : "Pending";

  document.getElementById("receipt-body").innerHTML = `
    <div class="flex justify-between text-slate-600 dark:text-slate-300 gap-4 mb-2">
      <span class="flex-shrink-0">From</span>
      <span class="font-medium text-slate-900 dark:text-white text-right">${p.ruaOrigem || "—"}</span>
    </div>
    <div class="flex justify-between text-slate-600 dark:text-slate-300 gap-4 mb-2">
      <span class="flex-shrink-0">To</span>
      <span class="font-medium text-slate-900 dark:text-white text-right">${p.ruaDestino || "—"}</span>
    </div>
    <div class="flex justify-between text-slate-600 dark:text-slate-300 mb-2">
      <span>Date</span>
      <span>${date}</span>
    </div>
    <div class="flex justify-between text-slate-600 dark:text-slate-300 mb-2">
      <span>Comfort</span>
      <span>${p.conforto?.nivelConforto || "—"}</span>
    </div>
    <div class="flex justify-between text-slate-600 dark:text-slate-300 mb-4">
      <span>Status</span>
      <span>${statusAtual}</span>
    </div>
    <div class="border-t border-slate-100 dark:border-slate-700 pt-2 flex justify-between font-bold text-slate-900 dark:text-white">
      <span>Total</span>
      <span class="text-sky-500">${fare}</span>
    </div>`;
    
  document.getElementById("receipt-modal").classList.remove("hidden");
}
// ─────────────────────────────────────────────
// TRIPS — viagens
// ─────────────────────────────────────────────
let viagens = [];

async function loadViagens() {
  try {
    viagens = await api("GET", "/viagens/byCliente");
    renderViagens();
    calcStats();
  } catch (e) {
    console.error("loadViagens:", e);
  }
}

function renderViagens() {
  const el = document.getElementById("viagens-list");
  if (!el) return;
  el.innerHTML = "";
  viagens.forEach(v => {
    const fare = v.preco != null ? "€" + Number(v.preco).toFixed(2) : "";
    const date = v.inicio ? new Date(v.inicio).toLocaleString([], { day:"2-digit", month:"2-digit", hour:"2-digit", minute:"2-digit" }) : "—";
    const div = document.createElement("div");
    div.className = "flex items-center gap-3 bg-white dark:bg-slate-800 rounded-xl border border-slate-100 dark:border-slate-700 p-3";
    
    div.innerHTML = `
      <div class="w-9 h-9 rounded-xl bg-emerald-50 dark:bg-emerald-950/50 border border-emerald-100 dark:border-emerald-800 flex items-center justify-center text-base flex-shrink-0">✅</div>
      <div class="flex-1 min-w-0">
        <div class="sora font-semibold text-sm text-slate-900 dark:text-white">
          ${v.pedido?.ruaOrigem || "—"} → ${v.pedido?.ruaDestino || "—"}
        </div>
        <div class="text-xs text-slate-400 mt-0.5">${date}</div>
      </div>
      <div class="sora font-semibold text-sm text-slate-900 dark:text-white flex-shrink-0">${fare}</div>`;
    el.appendChild(div);
  });
  if (!viagens.length) el.innerHTML = '<p class="text-xs text-slate-400 text-center py-6">No completed trips yet.</p>';
}

function calcStats() {
  const totalRides = pedidos.length;
  const totalSpent = viagens.reduce((s, v) => s + (v.preco || 0), 0);
  document.getElementById("profile-rides").textContent = totalRides;
  document.getElementById("profile-spent").textContent = "€" + totalSpent.toFixed(2);
}


// ─────────────────────────────────────────────
// FARE + RIDE TYPE
// ─────────────────────────────────────────────
const FARES = { economy:{base:2,km:1.2}, comfort:{base:3,km:1.8}, premium:{base:5,km:2.8} };
let selectedRide = "economy", routeKm = 3.4;

function mapTipo(nivel) {
  return { BASICO:"economy", CONFORTO:"comfort", LUXO:"premium", LUXUOSO:"premium" }[nivel] || "economy";
}

function mapToBackendConforto(type) {
  return { economy: "BASICO", comfort: "CONFORTO", premium: "LUXUOSO" }[type] || "BASICO";
}

let calculatedPrices = { economy: "0.00", comfort: "0.00", premium: "0.00" };


function calcFare(type, km) {
  return (FARES[type].base + FARES[type].km * km).toFixed(2);
}

async function updateAllFares() {
  if (!pickupLL || !destLL) return;

  console.log("Chamada à API de Preços:", {
    origem: pickupLL,
    destino: destLL,
    data: new Date().toISOString()
  });

  ["economy", "comfort", "premium"].forEach(t => {
    const el = document.getElementById("fare-" + t);
    if (el) el.textContent = "⏱️...";
  });

  const tipos = ["economy", "comfort", "premium"];
  const dataIso = new Date().toISOString();

  try {
    const promises = tipos.map(tipo => 
      api("POST", "/precos/calculate-preco-viagem", {
        latOrigem: pickupLL[0],
        lonOrigem: pickupLL[1],
        latDestino: destLL[0],
        lonDestino: destLL[1],
        conforto: mapToBackendConforto(tipo),
        data: dataIso
      })
    );

    const resultados = await Promise.all(promises);

    tipos.forEach((tipo, index) => {
      const valor = resultados[index].preco != null ? resultados[index].preco : resultados[index];
      calculatedPrices[tipo] = Number(valor).toFixed(2);
      const el = document.getElementById("fare-" + tipo);
      if (el) el.textContent = "€" + calculatedPrices[tipo];
    });

    const confirmBtn = document.getElementById("confirm-btn");
    if (confirmBtn) confirmBtn.textContent = "Confirm ride — €" + calculatedPrices[selectedRide];

  } catch (e) {
    console.error("Erro na API de preços:", e);


    if (e.message.includes("403") || e.message.includes("Forbidden") || token === null) {
      toast("🔒 Sessão expirada. Redirecionando...");
      setTimeout(() => {
        ["token","role","user"].forEach(k => localStorage.removeItem(k));
        window.location.href = "index";
      }, 1500);
      return;
    }
    
    // Fallback local caso seja apenas uma falha temporária do servidor
    tipos.forEach(t => {
      calculatedPrices[t] = calcFare(t, routeKm);
      const el = document.getElementById("fare-" + t);
      if (el) el.textContent = "€" + calculatedPrices[t];
    });
    const confirmBtn = document.getElementById("confirm-btn");
    if (confirmBtn) confirmBtn.textContent = "Confirm ride — €" + calculatedPrices[selectedRide];
  }
}

async function iniciarPagamento() {
  try {
    const res = await fetch(`/api/pagamentos/viagem/${activeViagemId}/iniciar`, {
      method: 'POST',
      headers: { "Content-Type": "application/json", "Authorization": "Bearer " + token }
    });

    console.log(res);

    if (!res.ok) {
      const err = await res.text();
      throw new Error(`HTTP ${res.status} — ${err}`);
    }

    const data = await res.json();

    // Init Stripe.js
    stripe = Stripe(data.publishableKey);
    
    const appearance =  {
      theme: 'night',
      variables: {
        colorText: '#ffffff',
      }
    }
    elements = stripe.elements({ clientSecret: data.clientSecret, appearance });
    
     const paymentEl = elements.create('payment', {
      paymentMethodOrder: ['card'],
      layout: {
        type: 'accordion',
        visibleAccordionItemsCount: 1
      }
    });
    paymentEl.mount('#payment-element');

  } catch (e) {
    console.error("Erro ao iniciar pagamento:", e);
  }
}

function selectRide(type) {
  selectedRide = type;
  ["economy","comfort","premium"].forEach(t => {
    const el = document.getElementById("card-" + t);
    if (el) el.classList.toggle("selected", t === type);
  });
  
  const currentPrice = calculatedPrices[type] !== "0.00" ? calculatedPrices[type] : calcFare(type, routeKm);
  if (!pickupLL || !destLL) {
    document.getElementById("confirm-btn").textContent = "Confirm ride";
  } else {
    document.getElementById("confirm-btn").textContent = "Confirm ride — €" + currentPrice;
  }
}

function setRouteKm(km, duration) {
  document.getElementById("km-economy").textContent = km.toFixed(1) + " km";
  document.getElementById("km-comfort").textContent = km.toFixed(1) + " km";
  document.getElementById("km-premium").textContent = km.toFixed(1) + " km";

  document.getElementById("economy-time").textContent = duration + " min";
  document.getElementById("comfort-time").textContent = Math.round(duration * 1.2) + " min";
  document.getElementById("premium-time").textContent = Math.round(duration * 0.8) + " min";

  // Atualiza os preços chamando a API do backend
  updateAllFares();
}

// ─────────────────────────────────────────────
// MAP
// ─────────────────────────────────────────────
let map, pickupMarker, destMarker, routeLine = null;
let pickupLL = [38.7223, -9.1393], destLL = null;
let pinMode = "pickup";
let pillTimer;

function pinIcon(color, label) {
  return L.divIcon({
    className: "",
    html: `<div style="width:26px;height:26px;border-radius:50% 50% 50% 0;background:${color};transform:rotate(-45deg);border:2px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,.3);display:flex;align-items:center;justify-content:center"><span style="transform:rotate(45deg);color:#fff;font-weight:700;font-size:9px">${label}</span></div>`,
    iconSize: [26,26], iconAnchor: [13,26]
  });
}

function setPinMode(mode) {
  pinMode = mode;
  document.getElementById("wrap-pickup").classList.toggle("pin-active", mode === "pickup");
  document.getElementById("wrap-dest").classList.toggle("pin-active", mode === "dest");
  const pill = document.getElementById("map-mode-pill");
  pill.textContent = mode === "pickup" ? "🟢 Tap map to set pickup" : "🔴 Tap map to set destination";
  pill.classList.add("visible");
  clearTimeout(pillTimer);
  pillTimer = setTimeout(() => pill.classList.remove("visible"), 2800);
  console.log("Pin mode set to:", mode);
}

let sheetOpen = false;

function rotateArrow() {
  const arrow = document.getElementById("sheet-arrow");
  sheetOpen = !sheetOpen;

  if (sheetOpen) {
    arrow.style.transform = "rotate(180deg)";
  } else {
    arrow.style.transform = "rotate(0deg)";
  }
}

function toggleSheet(id) {
  document.getElementById(id).classList.toggle("expanded");
  setTimeout(() => map && map.invalidateSize(), 320);
}

async function getAddress(lat, lng) {
  try {
    const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`, { headers:{"Accept":"application/json"} });
    const d = await res.json();
    return d.display_name || `${lat.toFixed(4)}, ${lng.toFixed(4)}`;
  } catch { return `${lat.toFixed(4)}, ${lng.toFixed(4)}`; }
}

function initBookingMap() {
  document.getElementById("dest-input").value = "";
  document.getElementById("pickup-input").value = "";
  map = L.map("map", { zoomControl:true, attributionControl:false });
  map.setView(pickupLL, 14);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", { maxZoom:19, crossOrigin:true }).addTo(map);

  map.on("click", async e => {
    const ll = [e.latlng.lat, e.latlng.lng];
    if (pinMode === "pickup") {
      pickupLL = ll;
      if (pickupMarker) map.removeLayer(pickupMarker);
      pickupMarker = L.marker(pickupLL, { icon:pinIcon("#22c55e","A") }).addTo(map);
  const address = await getAddress(ll[0], ll[1]);
  document.getElementById("pickup-input").value = address;
  setPinMode("dest");
    } else {
      destLL = ll;
      if (destMarker) map.removeLayer(destMarker);
      destMarker = L.marker(destLL, { icon:pinIcon("#ef4444","B") }).addTo(map);
      const address1 = await getAddress(ll[0], ll[1]);
  document.getElementById("dest-input").value = address1;
    }
    if (pickupLL && destLL) fetchRoute();
  });
  setPinMode("pickup");
}


let debounceTimer;

function setupAutocomplete(inputId, listId) {
  const input = document.getElementById(inputId);
  const list = document.getElementById(listId);

  input.addEventListener("input", () => {
    clearTimeout(debounceTimer);

    const query = input.value.trim();
    if (!query) {
      list.classList.add("hidden");
      list.innerHTML = "";
      return;
    }

    debounceTimer = setTimeout(async () => {
      const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=5`;

      try {
        const res = await fetch(url, {
          headers: {
            "Accept": "application/json"
          }
        });

        const data = await res.json();

        renderSuggestions(data, input, inputId, list);
      } catch (err) {
        console.error(err);
      }
    }, 300);
  });
}

function renderSuggestions(data, input, inputId, list) {
  list.innerHTML = "";

  if (!data || data.length === 0) {
    const empty = document.createElement("div");
    empty.className =
      "px-3 py-2 text-sm text-slate-400 dark:text-slate-500 italic";

    empty.textContent = "No results found";

    list.appendChild(empty);
    list.classList.remove("hidden");
    return;
  }

  data.forEach(item => {
    const div = document.createElement("div");

    div.className =
      "px-3 py-2 text-sm cursor-pointer hover:bg-slate-100 dark:hover:bg-slate-700";

    div.textContent = item.display_name;

    div.onclick = () => {
      input.value = item.display_name;
      searchPlace(inputId);
      list.classList.add("hidden");
    };

    list.appendChild(div);
  });

  list.classList.remove("hidden");
}

// init both inputs
setupAutocomplete("pickup-input", "pickup-suggestions");
setupAutocomplete("dest-input", "dest-suggestions");



function fetchRoute() {
  if (!pickupLL || !destLL) return;
  toggleSheet('booking-sheet');
  const url = `https://router.project-osrm.org/route/v1/driving/${pickupLL[1]},${pickupLL[0]};${destLL[1]},${destLL[0]}?overview=full&geometries=geojson`;
  fetch(url).then(r => r.json()).then(data => {
    if (!data.routes?.length) return;
    const coords = data.routes[0].geometry.coordinates.map(c => [c[1],c[0]]);
      // remove old route
      if (routeLine) {
        map.removeLayer(routeLine);
      }

      // remove old markers
      if (pickupMarker) {
        map.removeLayer(pickupMarker);
      }

      if (destMarker) {
        map.removeLayer(destMarker);
      }
      routeLine = L.polyline(coords, { color:"#0ea5e9", weight:4, opacity:.85 }).addTo(map);
    map.fitBounds(routeLine.getBounds(), { padding:[70,200] });
        pickupMarker = L.marker(pickupLL, {
          icon: pinIcon("#22c55e", "A")
        }).addTo(map);

        destMarker = L.marker(destLL, {
          icon: pinIcon("#ef4444", "B")
        }).addTo(map);
        const startLat = currentRequest.latitudeOrigem - 0.01;
    console.log(data);
    setRouteKm(data.routes[0].distance / 1000, Math.round(data.routes[0].duration / 60));
  }).catch(() => setRouteKm(3.4, 5));
}

function searchPlace(inputId) {
  const input = document.getElementById(inputId);
  let q = input.value.trim();

  if (!q) return;

  fetch(
    `https://nominatim.openstreetmap.org/search?format=json&limit=1&q=${encodeURIComponent(q)}`,
    {
      headers: {
        "Accept-Language": "en"
      }
    }
  )
    .then(r => r.json())
    .then(data => {
      if (!data || !data.length) {
        toast("⚠️ Location not found");
        return;
      }

      const place = data[0];
      const ll = [parseFloat(place.lat), parseFloat(place.lon)];

      // update correct field
      if (inputId === "pickup-input") {
        pickupLL = ll;

        if (pickupMarker) map.removeLayer(pickupMarker);
        pickupMarker = L.marker(ll, {
          icon: pinIcon("#22c55e", "A")
        }).addTo(map);
      } else {
        destLL = ll;

        if (destMarker) map.removeLayer(destMarker);
        destMarker = L.marker(ll, {
          icon: pinIcon("#ef4444", "B")
        }).addTo(map);
      }

      // shorten label
      input.value = place.display_name.split(",").slice(0, 2).join(",");

      // route if both exist
      if (pickupLL && destLL) {
        fetchRoute();
      }
    })
    .catch(() => toast("⚠️ Search unavailable — tap map"));
}
function swapLocations() {
  const tmpLL  = pickupLL ? [...pickupLL] : null;
  const tmpTxt = document.getElementById("pickup-input").value;
  if (destLL) {
    pickupLL = [...destLL];
    if (pickupMarker) map.removeLayer(pickupMarker);
    pickupMarker = L.marker(pickupLL, { icon:pinIcon("#22c55e","A") }).addTo(map);
    document.getElementById("pickup-input").value = document.getElementById("dest-input").value;
  } else { document.getElementById("pickup-input").value = ""; }
  if (tmpLL) {
    destLL = tmpLL;
    if (destMarker) map.removeLayer(destMarker);
    destMarker = L.marker(destLL, { icon:pinIcon("#ef4444","B") }).addTo(map);
    document.getElementById("dest-input").value = tmpTxt;
  } else {
    destLL = null;
    if (destMarker) { map.removeLayer(destMarker); destMarker = null; }
    document.getElementById("dest-input").value = "";
  }
  if (pickupLL && destLL) fetchRoute();
  toast("⇅ Locations swapped");
}

// ─────────────────────────────────────────────
// BOOKING → DRIVER WAIT → TRACKING → VIAGEM → RATING
// ─────────────────────────────────────────────
let activePedidoId = null, activeViagemId = null;
let driverPollInterval = null, tripPollInterval = null, trackAnimInterval = null;
let trackMap = null, trackDriverMarker = null;

async function bookRide() {
  const dest   = document.getElementById("dest-input").value.trim();
  const pickup = document.getElementById("pickup-input").value.trim();
  if (!dest || !destLL)   { toast("⚠️ Set a destination first"); return; }
  if (!pickupLL)          { toast("⚠️ Set a pickup location first"); return; }

  document.getElementById("booking-form").style.display = "none";
  document.getElementById("searching-state").classList.remove("hidden");

  const confortoMap = { economy:1, comfort:2, premium:3 };
  console.log("Booking ride with data:", pickup, pickupLL, dest, destLL, selectedRide);
  try {
    const pedido = await api("POST", "/pedidos-taxi", {
      ruaOrigem: pickup,
      latitudeOrigem: pickupLL[0],
      longitudeOrigem: pickupLL[1],
      ruaDestino: dest,
      latitudeDestino: destLL[0],
      longitudeDestino: destLL[1],
      numPassageiros: 1,
      confortoId: confortoMap[selectedRide] || 1,
      clienteId: 0
    });
    activePedidoId = pedido.id;
    waitForDriver();
  } catch (e) {
    console.error(e);
    toast("❌ Booking failed");
    resetBookingUI();
  }
}

function waitForDriver() {
  driverPollInterval = setInterval(async () => {
    try {
      const ride = await api("GET", `/pedidos-taxi/${activePedidoId}`);
      if (ride.motoristaId != null && ride.estadoPedido !== "PENDENTE") {
        await confirmarPedido(activePedidoId);
        clearInterval(driverPollInterval);
        document.getElementById("searching-state").classList.add("hidden");
        toast("🚗 Driver found!");
        startTracking(ride);
      }
    } catch (e) { console.error("poll driver:", e); }
  }, 2000);
}

async function confirmarPedido(id) {
  const res = await api("POST", `/pedidos-taxi/${id}/confirmar`);
  return res;
}

async function startTracking(ride) {
  // Populate driver info
  try {
    const drv = await api("GET", `/motoristas/${ride.motoristaId}`);
    document.getElementById("drv-name").textContent = drv.nome || "Driver";
  } catch { document.getElementById("drv-name").textContent = "Driver"; }

  // document.getElementById("drv-car").textContent       = "—";
  // document.getElementById("drv-rating").textContent    = "—";
  document.getElementById("dest-label-track").textContent = ride.ruaDestino?.split(",")[0] || "—";
  // document.getElementById("fare-label-track").textContent = ride.preco != null ? "€" + Number(ride.preco).toFixed(2) : "—";

  goTab("tracking");
  initTrackMap(ride);
  console.log("Started tracking for pedido", activePedidoId);

  // Poll for viagem creation (trip starts)
  tripPollInterval = setInterval(async () => {
    try {
      const viagens = await api("GET", "/viagens/byCliente");
      console.log("Polling viagens for active pedido:", activePedidoId, viagens);
      const match = viagens.find(v => v.pedido.id === activePedidoId);
      if (match) {
        activeViagemId = match.id;
        clearInterval(tripPollInterval);
        pollTripEnd();
      }
    } catch (e) { console.error("poll viagem:", e); }
  }, 3000);
}

function initTrackMap(ride) {
  console.log("Initializing track map with ride data:", ride);
  console.log("Pickup LL:", pickupLL, "Dest LL:", destLL);
  const pLat = ride.latitudeOrigem  || pickupLL[0];
  const pLng = ride.longitudeOrigem || pickupLL[1];
  const dLat = ride.latitudeDestino  || (destLL ? destLL[0] : pLat + 0.03);
  const dLng = ride.longitudeDestino || (destLL ? destLL[1] : pLng + 0.03);

  if (trackMap)   trackMap.remove(); trackMap = null;
  
  if (!trackMap) {
    trackMap = L.map("track-map", { zoomControl:true, attributionControl:false });
  }
  trackMap.setView([pLat, pLng], 13);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", { maxZoom:19, crossOrigin:true }).addTo(trackMap);
  L.marker([pLat, pLng], { icon:pinIcon("#22c55e","A") }).addTo(trackMap);
  L.marker([dLat, dLng], { icon:pinIcon("#ef4444","B") }).addTo(trackMap);

  const url = `https://router.project-osrm.org/route/v1/driving/${pLng},${pLat};${dLng},${dLat}?overview=full&geometries=geojson`;
  fetch(url).then(r => r.json()).then(data => {
    if (data.routes?.length) {
      const coords = data.routes[0].geometry.coordinates.map(c => [c[1],c[0]]);
      L.polyline(coords, { color:"#0ea5e9", weight:4, opacity:.8 }).addTo(trackMap);
      trackMap.fitBounds(L.polyline(coords).getBounds(), { padding:[80,80] });
      document.getElementById("progress-label").textContent = (data.routes[0].distance/1000).toFixed(1) + " km";
    }
  }).catch(() => {});

  const carIcon = L.divIcon({ className:"", html:'<div style="font-size:1.4rem;filter:drop-shadow(0 2px 4px rgba(0,0,0,.3))">🚗</div>', iconSize:[28,28], iconAnchor:[14,14] });
  let drvLat = pLat - 0.012, drvLng = pLng - 0.01;
  if (trackDriverMarker) trackMap.removeLayer(trackDriverMarker);
  trackDriverMarker = L.marker([drvLat, drvLng], { icon:carIcon }).addTo(trackMap);

  // Animate car toward pickup, then to destination
  let step = 0, total = 20;
  clearInterval(trackAnimInterval);
  trackAnimInterval = setInterval(() => {
    if (step >= total) { clearInterval(trackAnimInterval); return; }
    step++;
    const t = step / total;
    let lat, lng;
    if (t < 0.45) { const r = t/0.45; lat = drvLat+(pLat-drvLat)*r; lng = drvLng+(pLng-drvLng)*r; }
    else           { const r = (t-0.45)/0.55; lat = pLat+(dLat-pLat)*r; lng = pLng+(dLng-pLng)*r; }
    trackDriverMarker.setLatLng([lat, lng]);
    const rem = Math.max(0, Math.round((1-t)*14));
    document.getElementById("eta-text").textContent = rem > 0 ? rem+" min away" : "Arriving…";
    document.getElementById("pickup-eta").textContent = t < 0.45 ? Math.round((1-t/0.45)*4)+" min" : "Here!";
    document.getElementById("progress-phase").textContent = t < 0.45 ? "Driver is driving" : "In progress";
    document.getElementById("ride-progress").style.width = Math.round(t*100) + "%";
  }, 2500);
}

// Poll until viagem is finalized
function pollTripEnd() {
  const check = setInterval(async () => {
    try {
      const v = await api("GET", `/viagens/${activeViagemId}`);
      console.log("Polling viagem for completion:", activeViagemId, v);
      if (v.dataFim || v.pedido.estadoPedido === "CONCLUIDO") {
        clearInterval(check);
        clearInterval(trackAnimInterval);
        document.getElementById("eta-text").textContent = "Arrived!";
        document.getElementById("progress-phase").textContent = "Ride complete";
        document.getElementById("ride-progress").style.width = "100%";
        setTimeout(showRating, 1500);
      }
    } catch (e) { console.error("poll trip end:", e); }
  }, 4000);
}

async function cancelRide() {
  clearInterval(driverPollInterval);
  clearInterval(tripPollInterval);
  clearInterval(trackAnimInterval);
  if (activePedidoId) {
    try { await api("POST", `/pedidos-taxi/${activePedidoId}/cancelar`); } catch {}
  }
  toast("❌ Ride cancelled");
  resetBookingUI();
  goTab("home");
}

function resetBookingUI() {
  document.getElementById("booking-form").style.display = "";
  document.getElementById("searching-state").classList.add("hidden");
  activePedidoId = null;
  activeViagemId = null;
  document.getElementById("fare-economy" ).textContent = "";
  document.getElementById("fare-comfort" ).textContent = "";
  document.getElementById("fare-premium" ).textContent = "";

  document.getElementById("km-economy" ).textContent = "";
  document.getElementById("km-comfort" ).textContent = "";
  document.getElementById("km-premium" ).textContent = "";

  document.getElementById("economy-time" ).textContent = "";
  document.getElementById("comfort-time" ).textContent = "";
  document.getElementById("premium-time" ).textContent = "";

  document.getElementById("confirm-btn" ).textContent = "Confirm ride";
}

// ─────────────────────────────────────────────
// RATING
// ─────────────────────────────────────────────
let ratingStars = 0;

function showRating() { goTab("rate"); ratingStars = 0; document.querySelectorAll(".star-btn").forEach(s => s.style.color = "#e2e8f0"); iniciarPagamento(); }
function setStar(n) {
  ratingStars = n;
  document.querySelectorAll(".star-btn").forEach((s,i) => s.style.color = i < n ? "#f59e0b" : "#e2e8f0");
}
function toggleCompliment(btn) {
  ["border-sky-400","text-sky-500","bg-sky-50"].forEach(c => btn.classList.toggle(c));
}
function submitRating() {
  toast("✅ Payment valid!");
  resetBookingUI();
  if (destMarker) { map.removeLayer(destMarker); destMarker = null; }
  if (pickupMarker) { map.removeLayer(pickupMarker); pickupMarker = null; }
  if (routeLine)  { map.removeLayer(routeLine);  routeLine  = null; }
  document.getElementById("dest-input").value = "";
  document.getElementById("pickup-input").value = "";
  document.getElementById("booking-sheet").classList.remove("expanded");
  destLL = null;
  pickupLL = null;
  setTimeout(() => map && map.invalidateSize(), 100);
  setPinMode("pickup");
  loadPedidos();
  loadViagens();
  goTab("home");
}

// ─────────────────────────────────────────────
// ACTIVITY TABS
// ─────────────────────────────────────────────
function activityTab(t) {
  document.getElementById("activity-rides").classList.toggle("hidden", t !== "rides");
  document.getElementById("activity-viagens").classList.toggle("hidden", t !== "viagens");
  ["rides","viagens"].forEach(id => {
    const btn = document.getElementById("tab-" + id);
    const active = t === id;
    btn.className = active
      ? "px-4 py-1.5 rounded-full bg-sky-500 text-white text-xs font-semibold sora"
      : "px-4 py-1.5 rounded-full border border-slate-200 dark:border-slate-700 text-xs text-slate-500 dark:text-slate-400 hover:border-sky-400 hover:text-sky-500 sora";
  });
}

// ─────────────────────────────────────────────
// LOGOUT
// ─────────────────────────────────────────────
function logout() {
  ["token","role","user"].forEach(k => localStorage.removeItem(k));
  window.location.href = "index";
}

// ─────────────────────────────────────────────
// BOOT
// ─────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
  initLang();
  initProfile();
  loadPedidos();
  loadViagens();
});

window.addEventListener("load", () => {
  initBookingMap();
});