// ─────────────────────────────────────────────
// AUTH GUARD
// ─────────────────────────────────────────────
const token = localStorage.getItem("token");
const role  = localStorage.getItem("role");
if (!token || role !== "MOTORISTA") { window.location.href = "index"; }

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
let routeLine = null;
let pickupMarker = null;
let destMarker = null;
let driverAnim = null;

function toast(msg) {
  const el = document.getElementById("toast");
  el.textContent = msg;
  el.classList.add("show");
  clearTimeout(_tt);
  _tt = setTimeout(() => el.classList.remove("show"), 2800);
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
document.addEventListener("DOMContentLoaded", () => {
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
});

// ─────────────────────────────────────────────
// PROFILE
// ─────────────────────────────────────────────
function initProfile() {
  document.getElementById("profile-name").textContent  = user.nome  || "—";
  document.getElementById("profile-name2").textContent = user.nome  || "Me";
  document.getElementById("profile-email").textContent = user.email || "—";
  document.getElementById("profile-license").textContent = user.numCarta || "—";
  document.getElementById("profile-license-lifetime").textContent = user.validadeCarta ? user.validadeCarta.split("T")[0] : "—";
  document.getElementById("profile-since").textContent = user.criadoEm ? "Driver since " + user.criadoEm.split("T")[0] : "";
}

// ─────────────────────────────────────────────
// SHEET
// ─────────────────────────────────────────────
let sheetExpanded = false;
function toggleSheet() {
  sheetExpanded = !sheetExpanded;
  document.getElementById("sheet").classList.toggle("expanded", sheetExpanded);
  setTimeout(() => map && map.invalidateSize(), 320);
}
function expandSheet() {
  if (!sheetExpanded) { sheetExpanded = true; document.getElementById("sheet").classList.add("expanded"); setTimeout(() => map && map.invalidateSize(), 320); }
}
function collapseSheet() {
  sheetExpanded = false;
  document.getElementById("sheet").classList.remove("expanded");
  setTimeout(() => map && map.invalidateSize(), 320);
}

// ─────────────────────────────────────────────
// PANELS
// ─────────────────────────────────────────────
const ALL_PANELS = ["panel-idle","panel-waiting","panel-request","panel-accepted","panel-active","panel-management","panel-profile","panel-history"];

function showPanel(id) {
  ALL_PANELS.forEach(p => {
    const el = document.getElementById(p);
    if (el) el.classList.toggle("hidden", p !== id);
  });
}

function showPanelInner(id) { showPanel(id); }

const TABS = ["idle","management","history","profile"];
function goPanel(name) {
  TABS.forEach(t => document.getElementById("nav-" + t).classList.toggle("active", t === name));
  if (name === "idle")       { showPanel(isOnline ? "panel-waiting" : "panel-idle"); collapseSheet(); return; }
  if (name === "history")    { loadHistory(); showPanel("panel-history"); expandSheet(); return; }
  if (name === "management") { loadFaturas(); loadTurnos(); showPanel("panel-management"); expandSheet(); return; }
  if (name === "profile")    { showPanel("panel-profile"); expandSheet(); return; }
}

function hidePanel() { goPanel("idle"); }

// ─────────────────────────────────────────────
// ONLINE / OFFLINE
// ─────────────────────────────────────────────
let isOnline = false;
let reqPollInterval = null;
let currentRequest  = null;   // pedido being shown
let activeViagemId  = null;   // viagem after driver accepted
let tripPollInterval = null;
let clientConfirmPollInterval = null;
let allTaxis = [];
let currentTurno = null;

async function getAllTaxis() {
  try {
    allTaxis = await api("GET", `/taxis`);
  } catch (e) {
    console.error("getAllTaxis:", e);
  }
}

function toggleOnline() {
  if (currentTurno == null) {
    toast("⚠️ You do not have a shift yet. Create one in the Management panel.");
    return;
  }

  isOnline = !isOnline;
  const tog = document.getElementById("online-toggle");
  tog.classList.toggle("online", isOnline);
  if (currentTurno) {
          const horasInicio = new Date(currentTurno.inicio).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
hour12: false
      });

            const horasFim = new Date(currentTurno.fim).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false
      });
    document.getElementById("online-label").innerHTML = isOnline ? `
      <div class="flex flex-col items-center justify-center text-center">
        <div>Online</div>
        <div class="text-xs text-slate-400">(${horasInicio} - ${horasFim})</div>
      </div>
    ` : `
      <div class="flex flex-col items-center justify-center text-center">
        <div>Go Online</div>
        <div class="text-xs text-slate-400">(${horasInicio} - ${horasFim})</div>
      </div>
    `;
  } else { 
    document.getElementById("online-label").innerHTML = isOnline ? `
      <div class="flex flex-col items-center justify-center text-center">
        <div>Online</div>
        <div class="text-xs text-slate-400">(Sem Turno)</div>
      </div>
    ` : `
      <div class="flex flex-col items-center justify-center text-center">
        <div>Go Online</div>
        <div class="text-xs text-slate-400">(Sem Turno)</div>
      </div>
    `;
  }
  console.log("Toggled online:", document.querySelector(".nav-tab.active"));
  if (isOnline) {
    toast("🟢 You are online — watching for rides");
    if (!document.getElementById("panel-idle").classList.contains("hidden")) {
      showPanel("panel-waiting");
    }
    collapseSheet();
    startRequestPolling();
  } else {
    stopRequestPolling();
    stopTripPolling();
    if (!document.getElementById("panel-waiting").classList.contains("hidden")) {
      showPanel("panel-idle");
    }
    toast("🔴 You are offline");
  }
}

// ─────────────────────────────────────────────
// PENDING REQUEST POLLING
// ─────────────────────────────────────────────
function startRequestPolling() {
  fetchPendingRequests();
  reqPollInterval = setInterval(fetchPendingRequests, 4000);
}

function stopRequestPolling() {
  clearInterval(reqPollInterval);
  reqPollInterval = null;
}

async function fetchPendingRequests() {
  // Don't override active ride panels
  const activeEl = document.getElementById("panel-accepted");
  const activeRideEl = document.getElementById("panel-active");
  if (activeEl && !activeEl.classList.contains("hidden")) return;
  if (activeRideEl && !activeRideEl.classList.contains("hidden")) return;

  try {
    const rides = await api("GET", "/pedidos-taxi/pendentes");
    if (!rides.length) {
      return;
    }
    console.log("Pending rides:", rides);
    showRideRequest(rides[0]);
  } catch (e) {
    console.error("fetchPendingRequests:", e);
  }
}

async function updateAllFares(latOrigem, lonOrigem, latDestino, lonDestino, tipo) {
  const dataIso = new Date().toISOString();

  const resultado = await api("POST", "/precos/calculate-preco-viagem", {
    latOrigem,
    lonOrigem,
    latDestino,
    lonDestino,
    conforto: tipo,
    data: dataIso
  });

  return resultado;
}

const formatCoord = (v) =>
  v != null ? Number(v).toFixed(7) : "0.0000000";

async function showRideRequest(r) {
  currentRequest = r;

  const origemLat = formatCoord(r.latitudeOrigem);
  const origemLon = formatCoord(r.longitudeOrigem);
  const destLat = formatCoord(r.latitudeDestino);
  const destLon = formatCoord(r.longitudeDestino);
  console.log("Calculating fare for coords:", { origemLat, origemLon, destLat, destLon, tipo: r.conforto });

  const fare = await updateAllFares(
    origemLat,
    origemLon,
    destLat,
    destLon,
    r.conforto.nivelConforto
  );

  document.getElementById("req-name").textContent    = r.cliente?.nome || "Client";
  document.getElementById("req-rating").textContent  = r.numPassageiros;
  document.getElementById("req-fare").textContent = "€" + fare;
  document.getElementById("req-pickup").textContent  = r.ruaOrigem  || "—";
  document.getElementById("req-dest").textContent    = r.ruaDestino || "—";

  // Calc rough distance if coords exist
  if (origemLat && driverLL) {
    const dx = parseFloat(origemLat) - driverLL[0];
    const dy = parseFloat(origemLon) - driverLL[1];
    const km = (Math.sqrt(dx*dx + dy*dy) * 111).toFixed(1);
    document.getElementById("req-distance").textContent = km + " km";
  }


  const url = `https://router.project-osrm.org/route/v1/driving/${origemLat},${origemLon};${destLat},${destLon}?overview=full&geometries=geojson`;
  fetch(url).then(r => r.json()).then(data => {
    if (!data.routes?.length) return;
    document.getElementById("req-trip-km").textContent = (data.routes[0].distance / 1000).toFixed(1) + " km";
    document.getElementById("req-eta").textContent = Math.round(data.routes[0].duration / 60) + " min";
  }).catch(err => {
    console.error("OSRM error:", err);
    document.getElementById("req-trip-km").textContent = "3.4 km";
    document.getElementById("req-eta").textContent = "5 min";
  });

  if (!document.getElementById("panel-waiting").classList.contains("hidden")) {
    showPanel("panel-request");
  }
  expandSheet();
}

// ─────────────────────────────────────────────
// ACCEPT RIDE → wait for client to see it → viagem
// ─────────────────────────────────────────────
async function acceptRide() {
  if (!currentRequest) return;
  try {
    await api("POST", `/pedidos-taxi/${currentRequest.id}/aceitar`);

    toast("✅ Ride accepted — waiting for client…");
    stopRequestPolling();

    // Show "accepted, waiting for client" panel
    document.getElementById("acc-client-name").textContent  = currentRequest.cliente?.nome || "Client";
    document.getElementById("acc-pickup").textContent       = currentRequest.ruaOrigem  || "—";
    document.getElementById("acc-dest").textContent         = currentRequest.ruaDestino || "—";
    // document.getElementById("acc-fare").textContent         = currentRequest.preco != null ? "€" + Number(currentRequest.preco).toFixed(2) : "—";
    showPanel("panel-accepted");
    expandSheet();
    fetchRoute();
    console.log("Ride accepted, waiting for client confirmation. Current request ID:", currentRequest);

    // Poll until a viagem is created (client's app confirmed the match)
    pollForViagem(currentRequest.id);

  } catch (e) {
    console.error("acceptRide:", e);
    toast("❌ Failed to accept — already taken?");
  }
}

function fetchRoute() {
  const url = `https://router.project-osrm.org/route/v1/driving/${currentRequest.longitudeOrigem},${currentRequest.latitudeOrigem};${currentRequest.longitudeDestino},${currentRequest.latitudeDestino}?overview=full&geometries=geojson`;
  fetch(url).then(r => r.json()).then(data => {
    console.log("OSRM route data:", data);
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
        pickupMarker = L.marker([currentRequest.latitudeOrigem, currentRequest.longitudeOrigem], {
          icon: pinIcon("#22c55e", "A")
        }).addTo(map);

        destMarker = L.marker([currentRequest.latitudeDestino, currentRequest.longitudeDestino], {
          icon: pinIcon("#ef4444", "B")
        }).addTo(map);
        const startLat = currentRequest.latitudeOrigem - 0.01;
const startLng = currentRequest.longitudeOrigem - 0.01;

const pickupLat = currentRequest.latitudeOrigem;
const pickupLng = currentRequest.longitudeOrigem;

const destLat = currentRequest.latitudeDestino;
const destLng = currentRequest.longitudeDestino;

if (driverMarker) {
  map.removeLayer(driverMarker);
}

driverMarker = L.marker([startLat, startLng], {
  icon: driverIcon()
}).addTo(map);


let step = 0;
const totalSteps = 20;

clearInterval(driverAnim);

driverAnim = setInterval(() => {
  step++;
  const t = step / totalSteps;

  let lat, lng;

  // Phase 1: start → pickup
  if (t < 0.5) {
    const r = t / 0.5;

    lat = startLat + (pickupLat - startLat) * r;
    lng = startLng + (pickupLng - startLng) * r;
  }

  // Phase 2: pickup → destination
  else {
    const r = (t - 0.5) / 0.5;

    lat = pickupLat + (destLat - pickupLat) * r;
    lng = pickupLng + (destLng - pickupLng) * r;
  }

  driverMarker.setLatLng([lat, lng]);

  // optional: keep map following driver
  map.panTo([lat, lng], { animate: true });

  if (step >= totalSteps) {
    clearInterval(driverAnim);
  }
      if (!document.getElementById("panel-waiting").classList.contains("hidden")) {
        clearInterval(driverAnim);
      }
}, 2500);
    console.log(data);
  }).catch(err => {
    console.error("Error fetching route:", err);
  });
}

function pinIcon(color, label) {
  return L.divIcon({
    className: "",
    html: `<div style="width:26px;height:26px;border-radius:50% 50% 50% 0;background:${color};transform:rotate(-45deg);border:2px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,.3);display:flex;align-items:center;justify-content:center"><span style="transform:rotate(45deg);color:#fff;font-weight:700;font-size:9px">${label}</span></div>`,
    iconSize: [26,26], iconAnchor: [13,26]
  });
}

function pollForViagem(pedidoId) {
  clientConfirmPollInterval = setInterval(async () => {
    try {
      const pedido = await api("GET", `/pedidos-taxi/${pedidoId}`);

      if (pedido.estadoPedido === "EM_CURSO") {
        clearInterval(clientConfirmPollInterval);
        try {
          console.log("Creating viagem for pedidoId:", pedidoId); 
          console.log("Current turno:", currentTurno);
          console.log(new Date().toISOString());
          const viagem = await api("POST", `/viagens`, {
            clienteId: pedido.clienteId,
            turnoId: currentTurno.id,
            pedidoId: pedidoId,
            dataInicio: new Date().toISOString(),
            kilometros: 10
          });
          activeViagemId = viagem.id;

          startActiveRide(viagem);
        } catch (e) { 
          console.error("Error creating viagem:", e);

          // try to extract backend message if available
          const message =
            e?.message ||
            e?.response?.message ||
            e?.response?.data?.message ||
            JSON.stringify(e);

          console.error("Backend error message:", message);
          toast(message);
        }        
      }
    } catch (e) {
      console.error("pollForViagem:", e);
    }
  }, 3000);
}

function startActiveRide(viagem) {
  document.getElementById("active-name").textContent   = currentRequest.cliente?.nome || "Client";
  document.getElementById("active-pickup").textContent = currentRequest.ruaOrigem  || "—";
  document.getElementById("active-dest").textContent   = currentRequest.ruaDestino || "—";
  // document.getElementById("active-fare").textContent   = currentRequest.preco != null ? "€" + Number(currentRequest.preco).toFixed(2) : "—";
  // document.getElementById("active-km").textContent     = "—";
  // document.getElementById("active-eta").textContent    = "—";

  showPanel("panel-active");
  expandSheet();
  toast("🚗 Trip started — navigate to client!");
}

function declineRide() {
  currentRequest = null;
  showPanel("panel-waiting");
  collapseSheet();
  toast("⏭️ Request skipped");
}

// ─────────────────────────────────────────────
// COMPLETE RIDE
// ─────────────────────────────────────────────
async function completeRide() {
  if (!activeViagemId) {
    toast("⚠️ No active trip found");
    return;
  }
  try {
    await api("PATCH", `/viagens/${activeViagemId}/finalizar`);
    toast("💰 Trip completed!");
  } catch (e) {
    console.error("completeRide:", e);
    toast("⚠️ Could not finalize on server — marking done locally");
  }

  // Update today stats
  const fare = currentRequest?.preco || 0;
  updateTodayStats(fare);

  activeViagemId = null;
  currentRequest = null;
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
  if (isOnline) {
    showPanel("panel-waiting");
    startRequestPolling();
  } else {
    showPanel("panel-idle");
  }
  collapseSheet();
  loadHistory();
}

function stopTripPolling() {
  clearInterval(clientConfirmPollInterval);
  clearInterval(tripPollInterval);
  clientConfirmPollInterval = null;
  tripPollInterval = null;
}

// ─────────────────────────────────────────────
// TODAY STATS
// ─────────────────────────────────────────────
let todayEarnings = 0, todayRides = 0, todayKm = 0;

function updateTodayStats(fare) {
  todayEarnings += fare;
  todayRides++;
  // ["stat-rides","stat-rides2"].forEach(id => { const el = document.getElementById(id); if (el) el.textContent = todayRides; });
  // ["stat-earn","stat-earn2"].forEach(id => { const el = document.getElementById(id); if (el) el.textContent = "€" + todayEarnings.toFixed(2); });
}

// ─────────────────────────────────────────────
// HISTORY (pedidos by motorista)
// ─────────────────────────────────────────────
async function loadHistory() {
  try {
    const pedidos = await api("GET", "/pedidos-taxi/byMotorista");
    const viagens = await api("GET", "/viagens/byMotorista");

    const container = document.getElementById("history-list");
    document.getElementById("profile-rides").textContent = pedidos.length + " rides";

    if (!pedidos.length) {
      container.innerHTML = '<p class="text-sm text-slate-400 text-center py-6">No rides yet</p>';
      return;
    }

const viagemByPedido = {};
viagens.forEach(v => {
  if (v.pedido?.id) {
    viagemByPedido[v.pedido.id] = v;
  }
});

container.innerHTML = pedidos.map(p => {
  console.log(viagens);
  console.log(pedidos);
  const viagem = viagemByPedido[p.id];

  // 💰 preço
  const random = Math.random() * (18 - 3) + 3;
  let fare;
  if (viagem) {
    fare = "€" + Number(viagem.preco).toFixed(2); // real
  } else {
    fare = "€" + Number(random).toFixed(2); // estimativa
  }

  const date = p.dataSolicitacao
    ? new Date(p.dataSolicitacao).toLocaleString("pt-PT", {
        day: "2-digit",
        month: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
        hour12: false
      })
    : "—";

  // 🧾 botão fatura
  const faturaBtn = viagem
    ? `<button onclick="criarFatura(${viagem.id})"
         class="text-xs bg-emerald-500 text-white px-2 py-1 rounded-lg mt-1">
         Fatura
       </button>`
    : `<span class="text-xs text-slate-400">Estimativa</span>`;

  const status =
    p.estadoPedido === "CONCLUIDO"
      ? "Concluded"
      : "Pending";

  return `
    <div class="flex justify-between items-center p-3 rounded-xl bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700">
      <div>
        <div class="font-semibold text-slate-700 dark:text-slate-200 text-sm">
          ${p.ruaOrigem?.split(",")[0] || "—"} → ${p.ruaDestino?.split(",")[0] || "—"}
        </div>
        <div class="text-xs text-slate-400 mt-0.5">
          ${date} · ${status}
        </div>
        <div class="text-xs text-slate-400">
          Client: ${p.cliente?.nome || ""}
        </div>
      </div>

      <div class="text-right">
        <div class="font-bold ${viagem ? "text-emerald-500" : "text-yellow-500"}">
          ${fare}
        </div>
        ${faturaBtn}
      </div>
    </div>`;
}).join("");
  } catch (e) {
    console.error("loadHistory:", e);
  }
}

function openInvoiceModal(data) {
  document.getElementById("inv-num").textContent = data.numSequencial;
  document.getElementById("inv-ride").textContent = data.viagemId;
  document.getElementById("inv-price").textContent = (data.preco).toFixed(2);

  document.getElementById("invoice-modal").classList.remove("hidden");
  document.getElementById("invoice-modal").classList.add("flex");
}

function closeInvoiceModal() {
  document.getElementById("invoice-modal").classList.add("hidden");
  document.getElementById("invoice-modal").classList.remove("flex");
}

// ─────────────────────────────────────────────
// FATURAS
// ─────────────────────────────────────────────
async function criarFatura(viagemId) {
  try {
    const token = localStorage.getItem("token");

    const fatura = await fetch(`/api/faturas/viagem/${viagemId}/emitir`,{
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`
        }
      }
    ).then(r => r.json());
    console.log("Fatura criada:", fatura);

    const precoEuro = (fatura.preco / 100).toFixed(2);

    toast(`Emitida agora!`);
    openInvoiceModal(fatura);


  } catch (e) {
    console.error(e);
    toast("Emitida anteriormente!");
    const faturas = await api("GET", "/faturas/motorista/" + user.id);
    const fatura = faturas.find(f => f.viagemId === viagemId);
    openInvoiceModal(fatura);
  }
}

async function loadFaturas() {
  try {
    const faturas = await api("GET", "/faturas/motorista/" + user.id);
    const container = document.getElementById("faturas-list");

    if (!faturas.length) {
      container.innerHTML = '<p class="text-xs text-center text-slate-400 py-3">No invoices yet</p>';
      return;
    }
    console.log("Loaded faturas:", faturas);

    faturas.sort((a,b) => new Date(b.data) - new Date(a.data));
    container.innerHTML = faturas.map(f => `
      <div class="flex justify-between p-3 bg-white dark:bg-slate-800 rounded-xl border border-slate-100 dark:border-slate-700">
        <div>
          <div class="text-sm font-semibold">#${f.numSequencial}</div>
          <div class="text-xs text-slate-400">${new Date(f.data).toLocaleDateString("pt-PT")}</div>
        </div>
        <div class="text-right">
          <div class="font-bold text-emerald-500">€${f.preco}</div>
        </div>
      </div>`).join("");
  } catch (e) {
    console.error("loadFaturas:", e);
  }
}

// ─────────────────────────────────────────────
// TURNOS
// ─────────────────────────────────────────────
const currentTurnoId = user.turnoIdAtual;
let selectedTaxiId = null, selectedTurnoId = null;

async function loadTurnos() {
  try {
    const turnos = await api("GET", `/turnos/byMotorista/${user.id}`);
    const container = document.getElementById("turnos-list");

    if (!turnos.length) {
      container.innerHTML = '<p class="text-xs text-center text-slate-400 py-3">No shifts yet</p>';
      return;
    }

    const now = new Date();

    turnos.sort((a, b) => new Date(a.inicio) - new Date(b.inicio));

    currentTurno = turnos.find(t => isTurnoAtivo(t));

    if (!currentTurno) {
    document.getElementById("online-label").innerHTML = `
      <div class="flex flex-col items-center justify-center text-center">
        <div>Go Online</div>
        <div class="text-xs text-slate-400">(Sem Turno)</div>
      </div>
    `;
    } else {
      const horasInicio = new Date(currentTurno.inicio).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false
      });

            const horasFim = new Date(currentTurno.fim).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false
      });

      document.getElementById("online-label").innerHTML = `
        <div class="flex flex-col items-center justify-center text-center">
          <div>Go Online</div>
          <div class="text-xs text-slate-400">(${horasInicio} - ${horasFim})</div>
        </div>
      `;
    }
  

container.innerHTML = turnos.map(t => {
  const ativo = isTurnoAtivo(t);
  console.log("Turno", t.id, "ativo?", ativo);
  const taxi = allTaxis.find(tx => tx.id === t.taxiId);
  const matricula = taxi ? taxi.matricula : "—";
  return `
    <div class="p-3 rounded-xl border 
      ${ativo 
        ? "bg-green-100 dark:bg-green-900 border-green-400" 
        : "bg-white dark:bg-slate-800 border-slate-100 dark:border-slate-700"}">

      <div class="flex justify-between items-center">
        <div class="text-sm font-semibold">
          🚕 Taxi ${matricula}
        </div>

        ${ativo ? `
          <button onclick="toggleMenu(${t.id})"
            class="text-xs px-2 py-1 rounded bg-slate-200 dark:bg-slate-700">
            ⚙️
          </button>
        ` : ""}
      </div>

      <div class="text-xs text-slate-400 mt-1">
        ${t.inicio?.split("T")[0]} ${t.inicio?.split("T")[1]?.slice(0, 5)}
        →
        ${t.fim?.split("T")[0] || "—"} ${t.fim?.split("T")[1]?.slice(0, 5) || ""}
      </div>

      <!-- 👇 MENU (ESTAVA EM FALTA) -->
      <div id="menu-${t.id}" class="hidden mt-2 space-y-1">

        <button onclick="abrirReabastecimento(${t.id})"
          class="w-full text-left text-xs px-2 py-1 rounded bg-slate-100 dark:bg-slate-700">
          ⛽ Reabastecer
        </button>

        <button onclick="abrirCarregar(${t.id})"
          class="w-full text-left text-xs px-2 py-1 rounded bg-slate-100 dark:bg-slate-700">
          🔋 Carregar eletricidade
        </button>

      </div>
    </div>`;
}).join("");

  } catch (e) {
    console.error("loadTurnos:", e);
  }
}


async function loadAvailableTaxis() {
  const inicio = document.getElementById("turno-start").value;
  const fim    = document.getElementById("turno-end").value;
  if (!inicio || !fim) { toast("⚠️ Select start and end first"); return; }

  try {
    const turno = await api("POST", "/turnos", { motoristaId: user.id, inicio, fim });
    selectedTurnoId = turno.id;

    const taxis = await api("GET", `/turnos/${turno.id}/taxis-disponiveis`);
    renderTaxiList(taxis);
  } catch (e) {
    toast("❌ The selected time overlaps with an existing shift or is bigger than 8 hours");
    console.error(e);
  }
}

function toggleMenu(id) {
  const el = document.getElementById(`menu-${id}`);
  if (!el) return;
  el.classList.toggle("hidden");
}

async function abrirReabastecimento(turnoId) {
  try {
    await api("POST", `/reabastecimento/${turnoId}`, {
      dataInicio: new Date().toISOString()
    });

    toast("⛽ Reabastecimento iniciado");
  } catch (e) {
    console.error(e);
    toast("❌ Erro ao iniciar reabastecimento");
  }
}

async function abrirCarregar(turnoId) {
  try {
    await api("POST", `/carregamento/${turnoId}`, {
      dataInicio: new Date().toISOString()
    });

    toast("🔋 Carregamento iniciado");
  } catch (e) {
    console.error(e);
    toast("❌ Erro ao iniciar carregamento");
  }
}

function isTurnoAtivo(t) {
  const now = new Date();
  const inicio = new Date(t.inicio);
  const fim = new Date(t.fim);

  return inicio <= now && (!fim || now <= fim);
}

function renderTaxiList(taxis) {
  const container = document.getElementById("taxi-list");
  if (!taxis.length) {
    container.innerHTML = '<p class="text-xs text-center text-slate-400">No taxis available</p>';
    return;
  }
  container.innerHTML = taxis.map(t => `
    <div onclick="selectTaxi(${t.id})" id="taxi-${t.id}"
      class="p-2 rounded-lg border cursor-pointer bg-white dark:bg-slate-900 hover:border-emerald-500 transition-colors">
      <div class="text-sm font-semibold">${t.matricula}</div>
      <div class="text-xs text-slate-400">${t.modelo || "Taxi"}</div>
    </div>`).join("");
}

function selectTaxi(id) {
  selectedTaxiId = id;
  document.querySelectorAll("#taxi-list > div").forEach(el => el.classList.remove("border-emerald-500"));
  const el = document.getElementById("taxi-" + id);
  if (el) el.classList.add("border-emerald-500");
}

async function createTurno() {
  if (!selectedTaxiId)  { toast("🚕 Select a taxi first"); return; }
  if (!selectedTurnoId) { toast("🚕 Load taxis first"); return; }
  try {
    await api("POST", `/turnos/${selectedTurnoId}/selecionar-taxi`, { taxiId: selectedTaxiId });
    toast("✅ Shift created!");
    loadTurnos();
  } catch (e) {
    toast("❌ Failed to create shift");
  }
}

function switchManagementTab(tab) {
  const isFaturas = tab === "faturas";
  document.getElementById("content-faturas").classList.toggle("hidden", !isFaturas);
  document.getElementById("content-turnos").classList.toggle("hidden", isFaturas);

  const activeClass = ["bg-white","dark:bg-slate-900","shadow"];
  const inactiveClass = ["text-slate-400"];

  const tabF = document.getElementById("tab-faturas");
  const tabT = document.getElementById("tab-turnos");

  if (isFaturas) {
    activeClass.forEach(c => tabF.classList.add(c));
    inactiveClass.forEach(c => tabF.classList.remove(c));
    activeClass.forEach(c => tabT.classList.remove(c));
    inactiveClass.forEach(c => tabT.classList.add(c));
  } else {
    activeClass.forEach(c => tabT.classList.add(c));
    inactiveClass.forEach(c => tabT.classList.remove(c));
    activeClass.forEach(c => tabF.classList.remove(c));
    inactiveClass.forEach(c => tabF.classList.add(c));
  }
}

function setDefaultTurnoTimes() {
  const pad = n => String(n).padStart(2, "0");
  const fmt = d => `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  const now = new Date();
  document.getElementById("turno-start").value = fmt(new Date(now.getTime() + 2*3600*1000));
  document.getElementById("turno-end").value   = fmt(new Date(now.getTime() + 10*3600*1000));
}

// ─────────────────────────────────────────────
// MAP
// ─────────────────────────────────────────────
let map, driverMarker;
let driverLL = [38.7223, -9.1393];

function driverIcon() {
  return L.divIcon({
    className: "",
    html: '<div style="width:34px;height:34px;border-radius:50%;background:#22c55e;border:3px solid #fff;box-shadow:0 3px 10px rgba(0,0,0,.25);display:flex;align-items:center;justify-content:center;font-size:16px;">🚗</div>',
    iconSize: [34,34], iconAnchor: [17,17]
  });
}

function initMap() {
  map = L.map("map", { zoomControl:true, attributionControl:false });
  map.setView(driverLL, 15);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", { maxZoom:19, crossOrigin:true }).addTo(map);
  driverMarker = L.marker(driverLL, { icon:driverIcon() }).addTo(map);

  if (navigator.geolocation) {
    navigator.geolocation.watchPosition(p => {
      driverLL = [p.coords.latitude, p.coords.longitude];
      driverMarker.setLatLng(driverLL);
      map.setView(driverLL, 15);
    }, () => {}, { enableHighAccuracy:true });
  }
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
window.addEventListener("load", () => {
  initMap();
  initProfile();
  setDefaultTurnoTimes();
  loadHistory();
  getAllTaxis();
  loadTurnos();
});
