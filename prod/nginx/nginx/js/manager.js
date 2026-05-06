document.addEventListener("DOMContentLoaded", () => {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");

  if ((!token && !role) || (role !== "GESTOR")) {
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

});



function switchLang(lang) {
  const parts = window.location.pathname.split("/").filter(Boolean);

  // if URL is /en/about → ["en","about"]
  if (parts.length === 0) {
    window.location.href = `/${lang}/manager`;
    return;
  }

  const page = parts[1] || "manager";

  window.location.href = `/${lang}/${page}`;
}


let allMotoristas = [];
let allTaxis = [];
let confortos = [];
let taxaNoturna = 1.2; 
let lastViagens = [];
let allPedidos = [];
let allTurnos = [];


async function loadLastViagens() {
  const token = localStorage.getItem("token");

  try {
    const res = await fetch('/api/viagens', {
      headers: {
        'Accept': 'application/json',
        'Authorization': 'Bearer ' + token
      }
    });

    if (!res.ok) throw new Error('Failed to fetch viagens');

    const data = await res.json();
    console.log("Viagens:", data);
    lastViagens = data
      .sort((a, b) => new Date(b.inicio) - new Date(a.inicio))
      .slice(0, 3);

    renderLastViagens(lastViagens);

  } catch (err) {
    console.error(err);
  }
}

function renderFleetStatus(list) {
  const container = document.querySelector('#fleet-status');

  container.innerHTML = '';

  list.forEach(t => {

    const status = getTaxiStatus(t.id, allTurnos);

    let badgeClass = 'badge-slate';
    let dotColor = 'bg-slate-300';

    if (status === 'EM_TURNO') {
      badgeClass = 'badge-green';
      dotColor = 'bg-emerald-400';
    } else if (status === 'A_CARREGAR') {
      badgeClass = 'badge-amber';
      dotColor = 'bg-amber-400';
    }

    const label =
      status === 'EM_TURNO' ? 'Em turno' :
      status === 'DISPONIVEL' ? 'Disponível' :
      'A carregar';

    const row = `
      <div class="flex items-center justify-between text-sm">
        <div class="flex items-center gap-2">
          <span class="w-2 h-2 rounded-full ${dotColor}"></span>
          <span class="text-slate-600 dark:text-slate-300">
            ${t.matricula} · ${t.marca || ''} ${t.modelo || ''}
          </span>
        </div>
        <span class="badge ${badgeClass}">${label}</span>
      </div>
    `;

    container.innerHTML += row;
  });
}

function renderLastViagens(viagens) {
  const container = document.querySelector('#last-viagens');

  container.innerHTML = '';

  viagens.forEach(v => {
    const icon =
      v.kilometros > 20 ? '👑' :
      v.kilometros > 7 ? '🛋️' :
      '🚗';

    const row = `
      <div class="flex items-center gap-3 py-1">
        <div class="w-8 h-8 rounded-xl bg-sky-50 dark:bg-sky-950/40 border border-sky-100 dark:border-sky-900 flex items-center justify-center text-sm flex-shrink-0">
          ${icon}
        </div>

        <div class="flex-1 min-w-0">
          <div class="text-sm font-medium text-slate-800 dark:text-slate-200 truncate">
            ${v.pedido.ruaOrigem || '-'} → ${v.pedido.ruaDestino || '-'}
          </div>
          <div class="text-xs text-slate-400">
            ${formatTime(v.inicio)} · ${motoristaMap[v.pedido.motoristaId] || '-'} · ${v.kilometros ?? '-'} km
          </div>
        </div>

        <div class="text-sm font-semibold text-emerald-500 sora flex-shrink-0">
          €${v.preco?.toFixed(2) || '0.00'}
        </div>
      </div>
    `;

    container.innerHTML += row;
  });
}

async function loadTaxaNoturna() {
  const token = localStorage.getItem("token");

  try {
    const res = await fetch('/api/precos/taxa-noturna', {
      headers: {
        'Accept': 'application/json',
        'Authorization': 'Bearer ' + token
      }
    });

    if (!res.ok) throw new Error('Failed to fetch taxa noturna');
    taxaNoturna = await res.json();

  } catch (err) {
    console.error(err);
  }
}
async function loadConfortos() {
  const token = localStorage.getItem("token");

  try {
    const res = await fetch('/api/confortos/', {
      headers: {
        'Accept': 'application/json',
        'Authorization': 'Bearer ' + token
      }
    });

    if (!res.ok) throw new Error('Failed to fetch confortos');

    confortos = await res.json();
    console.log("Confortos:", confortos);
    renderConfortos(confortos);

  } catch (err) {
    console.error(err);
  }
}

async function getTotalTaxis() {
  const token = localStorage.getItem("token");

  try {
    const res = await fetch('/api/taxis', {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        "Authorization": "Bearer " + token
      }
    });

    if (!res.ok) throw new Error('Failed to fetch taxis');

    const taxis = await res.json();
    const total = taxis.length;
    document.querySelector('#total-taxis').textContent = total;

  } catch (err) {
    console.error(err);
  }
}

function renderTaxis(taxis) {
  const tbody = document.querySelector('#taxis-tbody');
  tbody.innerHTML = '';

  taxis.forEach(t => {

    const badgeConforto =
      t.conforto === 'LUXUOSO' ? 'badge-amber' :
      t.conforto === 'CONFORTO' ? 'badge-amber' :
      'badge-slate';

    const motorBadge =
      t.tipoMotor === 'ELETRICO' ? 'badge-green' : 'badge-blue';

    const status = getTaxiStatus(t.id, allTurnos);

    let badgeClass = 'badge-slate';
    let dotColor = 'bg-slate-300';

    if (status === 'EM_TURNO') {
      badgeClass = 'badge-green';
      dotColor = 'bg-emerald-400';
    } else if (status === 'A_CARREGAR') {
      badgeClass = 'badge-amber';
      dotColor = 'bg-amber-400';
    }

    const label =
      status === 'EM_TURNO' ? 'Em turno' :
      status === 'DISPONIVEL' ? 'Disponível' :
      'A carregar';

    tbody.innerHTML += `
      <tr>
        <td class="font-mono font-semibold text-slate-900 dark:text-white">${t.matricula || '-'}</td>
        <td>${t.marca || '-'} ${t.modelo || '-'}</td>
        <td>${t.anoCompra || '-'}</td>

        <td><span class="badge ${badgeConforto}">
          ${t.conforto.nivelConforto || '-'}
        </span></td>

        <td><span class="badge ${motorBadge}">
          ${t.tipoMotor === 'ELETRICO' ? '⚡ Elétrico' : '⛽ Combustão'}
        </span></td>

        <td><span class="badge ${badgeClass}">${label}</span></td>

        <td class="space-x-1">
          <button onclick='openEditTaxi(${JSON.stringify(t)})'
            class="btn btn-ghost btn-sm btn-icon">✏️</button>

          <button onclick="deleteTaxi(${t.id})"
            class="btn btn-danger btn-sm btn-icon">🗑️</button>
        </td>
      </tr>
    `;
  });
}



function renderMotoristas(motoristas) {

  const tbody = document.querySelector('#motoristas-tbody');
  tbody.innerHTML = '';

  motoristas.forEach(m => {

    const estado = getEstadoMotorista(m.id, allPedidos, allTurnos);

    let badge = 'badge-slate';

    if (estado === 'Ocupado') {
      badge = 'badge-amber';   // busy → warning color
    } else if (estado === 'Livre') {
      badge = 'badge-green';   // available → good
    } else {
      badge = 'badge-slate';   // not working → neutral
    }

    tbody.innerHTML += `
      <tr>
        <td>
          <div class="font-semibold text-slate-900 dark:text-white">
            ${m.nome || '-'}
          </div>
          <div class="text-xs text-slate-400">
            ${m.email || '-'}
          </div>
        </td>

        <td class="font-mono">${m.nif || '-'}</td>

        <td>${m.dataNascimento ? new Date(m.dataNascimento).getFullYear() : '-'}</td>

        <td class="font-mono text-xs">${m.numCarta || '-'}</td>

        <td class="text-slate-400 text-xs">
          ${m.rua || '-'} ${m.ruaNumero || ''}, ${m.codigoPostal || ''}
        </td>

        <td>
          <span class="badge ${badge}">
            ${estado}
          </span>
        </td>

        <td class="space-x-1">
          <button onclick='openEditMotorista(${JSON.stringify(m)})'
            class="btn btn-ghost btn-sm btn-icon">✏️</button>

          <button onclick="deleteMotorista(${m.id})"
            class="btn btn-danger btn-sm btn-icon">🗑️</button>
        </td>
      </tr>
    `;
  });
}

async function deleteMotorista(id) {
  const token = localStorage.getItem("token");

  if (!confirm("Tem a certeza que quer remover este motorista?")) return;

  try {
    const res = await fetch(`/api/motoristas/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': 'Bearer ' + token
      }
    });

    console.log("Delete response:", res);

    if (!res.ok) throw new Error('Erro ao remover motorista');

    toast('✅ Motorista removido com sucesso');

    // reload list after delete
    loadMotoristas(); 

  } catch (err) {
    console.error(err);
    toast('❌ Não foi possível remover o motorista');
  }
}

async function deleteTaxi(id) {
  const token = localStorage.getItem("token");

  try {
    const res = await fetch(`/api/taxis/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': 'Bearer ' + token
      }
    });

    if (!res.ok) throw new Error('Failed to delete taxi');

    toast("🚕 Táxi removido");

    loadTaxis(); // refresh

  } catch (err) {
    console.error(err);
  }
}

async function getTotalMotoristas() {
  const token = localStorage.getItem("token");

  try {
    const res = await fetch('/api/motoristas', {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        "Authorization": "Bearer " + token
      }
    });

    if (!res.ok) throw new Error('Failed to fetch motoristas');

    const motoristas = await res.json();
    const total = motoristas.length;
    document.querySelector('#total-motoristas').textContent = total;

  } catch (err) {
    console.error(err);
  }
}



async function getTotalViagens() {
  const token = localStorage.getItem("token");

  try {
    const res = await fetch('/api/viagens', {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        "Authorization": "Bearer " + token
      }
    });

    if (!res.ok) throw new Error('Failed to fetch viagens');

    const viagens = await res.json();
    const total = viagens.length;
    document.querySelector('#total-viagens').textContent = total;

  } catch (err) {
    console.error(err);
  }
}



async function getTotalReceitas() {
  const token = localStorage.getItem("token");

  try {
    const res = await fetch('/api/viagens', {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        "Authorization": "Bearer " + token
      }
    });

    if (!res.ok) throw new Error('Failed to fetch viagens');

    const viagens = await res.json();
    const total = viagens.reduce((sum, v) => sum + v.preco, 0).toFixed(2);
    document.querySelector('#total-receita').textContent = total + "€";
  } catch (err) {
    console.error(err);
  }
}


async function refreshDashboard() {
  await Promise.all([
    loadUsername(),
    getTotalMotoristas(),
    getTotalViagens(),
    getTotalReceitas(),
    getTotalTaxis(),
    getAllViagens(),

    loadMotoristas(),
    await loadTurnos(),
    await loadPedidos(),
    loadTaxis(),
    loadTaxaNoturna(),
    loadConfortos(),
    loadLastViagens(),
    renderTaxis(allTaxis),
    renderMotoristas(allMotoristas),
    renderConfortos(confortos),
    renderLastViagens(lastViagens),
    renderFleetStatus(allTaxis.slice(0, 4)),
    renderDriverActivity(allMotoristas, allViagens)
  ]);
}

refreshDashboard();
setInterval(refreshDashboard, 10000);


let motoristaMap = {};

async function loadMotoristas() {
  const token = localStorage.getItem("token");

  try {
    const res = await fetch('/api/motoristas', {
      headers: {
        'Accept': 'application/json',
        'Authorization': 'Bearer ' + token
      }
    });

    if (!res.ok) throw new Error('Failed to fetch motoristas');

    allMotoristas = await res.json();
    motoristaMap = {};
    allMotoristas.forEach(m => {
      motoristaMap[m.id] = m.nome;
    });

    renderMotoristas(allMotoristas);
    await getAllViagens();
    renderDriverActivity(allMotoristas, allViagens);

  } catch (err) {
    console.error(err);
  }
}

async function loadPedidos() {
  const token = localStorage.getItem("token");

  try {
    const res = await fetch('/api/pedidos-taxi/', {
      headers: {
        'Accept': 'application/json',
        'Authorization': 'Bearer ' + token
      }
    });

    if (!res.ok) throw new Error('Failed to fetch pedidos');

    allPedidos = await res.json();
    console.log("Pedidos:", allPedidos);

  } catch (err) {
    console.error(err);
  }
}


let taxiMap = {};

async function loadTaxis() {
  const token = localStorage.getItem("token");

  try {
    const res = await fetch('/api/taxis', {
      headers: {
        'Accept': 'application/json',
        'Authorization': 'Bearer ' + token
      }
    });

    if (!res.ok) throw new Error('Failed to fetch taxis');

    allTaxis = await res.json();

    // map for fast lookup
    taxiMap = {};
    allTaxis.forEach(t => {
      taxiMap[t.id] = t;
    });

    renderTaxis(allTaxis);
    await loadTurnos();
    renderFleetStatus(allTaxis.slice(0, 4));

  } catch (err) {
    console.error(err);
  }
}


let allViagens = [];

async function getAllViagens() {
  const token = localStorage.getItem("token");

  try {
    const res = await fetch('/api/viagens', {
      headers: {
        'Accept': 'application/json',
        'Authorization': 'Bearer ' + token
      }
    });

    if (!res.ok) throw new Error('Failed to fetch viagens');
    allViagens = await res.json();
  } catch (err) {
    console.error(err);
  }
}



function renderViagens(viagens) {
  const tbody = document.querySelector('#viagens-body');
  tbody.innerHTML = '';

  viagens.forEach(v => {
    const row = `
      <tr class="drill-row cursor-pointer">
        <td class="font-medium">${motoristaMap[v.pedido.motoristaId] || '-'}</td>
        <td class="font-mono text-xs">${taxiMap[v.pedido.taxiId] || '-'}</td>
        <td class="text-xs">${v.pedido.ruaOrigem || '-'}</td>
        <td class="text-xs">${v.pedido.ruaDestino || '-'}</td>
        <td class="text-xs">${formatTime(v.inicio)}</td>
        <td class="text-xs">${formatTime(v.fim)}</td>
        <td>${v.kilometros ?? '-'}</td>
        <td>${v.pedido.numPassageiros ?? '-'}</td>
        <td class="text-emerald-500 font-semibold">€${v.preco?.toFixed(2) || '0.00'}</td>
        <td><span class="badge ${v.faturaEmitida ? 'badge-green' : 'badge-slate'}">
          ${v.faturaEmitida ? '✓ Emitida' : 'Pendente'}
        </span></td>
      </tr>
    `;
    tbody.innerHTML += row;
  });
}


function applyFilters() {
  let filtered = [...allViagens];

  const start = document.getElementById('filter-start').value;
  const end = document.getElementById('filter-end').value;
  const minPrice = parseFloat(document.getElementById('filter-min-price').value);
  const maxPrice = parseFloat(document.getElementById('filter-max-price').value);
  const pax = parseInt(document.getElementById('filter-pax').value);
  const origin = document.getElementById('filter-origin').value.toLowerCase();
  const dest = document.getElementById('filter-dest').value.toLowerCase();

  filtered = filtered.filter(v => {

    if (start && new Date(v.inicio) < new Date(start)) return false;
    if (end && new Date(v.inicio) > new Date(end)) return false;

    if (!isNaN(minPrice) && v.preco < minPrice) return false;
    if (!isNaN(maxPrice) && v.preco > maxPrice) return false;

    if (!isNaN(pax) && v.pedido.numPassageiros !== pax) return false;

    if (origin && !v.pedido.ruaOrigem?.toLowerCase().includes(origin)) return false;
    if (dest && !v.pedido.ruaDestino?.toLowerCase().includes(dest)) return false;

    return true;
  });
  renderViagens(filtered);
}


function resetFilters() {
  document.querySelectorAll('#panel-viagens input').forEach(i => i.value = '');
  renderViagens(allViagens);
}

function formatTime(date) {
  if (!date) return '-';
  return new Date(date).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}








async function savePrices() {
  const token = localStorage.getItem("token");

  const basico = document.getElementById("price-basico").value;
  const luxo = document.getElementById("price-luxuoso").value;
  const conforto = document.getElementById("price-conforto").value;
  const noite = 1 + parseFloat(document.getElementById("price-noite").value) / 100;

  try {
    if (parseFloat(basico) > 0) {
      // BASICO
      await fetch(`/api/precos/precomin-conforto?preco=${basico}&conforto=BASICO`, {
        method: "PATCH",
        headers: {
          "Authorization": "Bearer " + token,
          "Accept": "application/json"
        }
      });
    }
    if (parseFloat(basico) <= 0) {
      toast("⚠️ Preço básico deve ser > 0");
      return;
    }

    if (parseFloat(luxo) > 0) {
      // LUXUOSO
      await fetch(`/api/precos/precomin-conforto?preco=${luxo}&conforto=LUXUOSO`, {
        method: "PATCH",
        headers: {
          "Authorization": "Bearer " + token,
          "Accept": "application/json"
        }
      });
    }
    if (parseFloat(luxo) <= 0) {
      toast("⚠️ Preço luxuoso deve ser > 0");
      return;
    }

    if (parseFloat(conforto) > 0) {
      // CONFORTO
      await fetch(`/api/precos/precomin-conforto?preco=${conforto}&conforto=CONFORTO`, {
        method: "PATCH",
        headers: {
          "Authorization": "Bearer " + token,
          "Accept": "application/json"
        }
      });
    }
    if (parseFloat(conforto) <= 0) {
      toast("⚠️ Preço conforto deve ser > 0");
      return;
    }

    if (parseFloat(noite) > 1 ) {
      // NOITE
      await fetch(`/api/precos/taxa-noturna?novoValor=${noite}`, {
        method: "PATCH",
        headers: {
          "Authorization": "Bearer " + token,
          "Accept": "application/json"
        }
      });
    }
    if (parseFloat(noite) <= 1) {
      toast("⚠️ Taxa noturna deve ser > 1");
      return;
    }

    toast("💰 Tarifas atualizadas com sucesso");

  } catch (err) {
    console.error(err);
    toast("❌ Erro ao atualizar tarifas");
  }
}



function renderConfortos(list) {
  const tbody = document.getElementById('tariff-table');
  tbody.innerHTML = '';

  list.forEach(c => {
    const nivel = c.nivelConforto.toLowerCase();

    const label =
      c.nivelConforto === 'BASICO' ? 'Básico' :
      c.nivelConforto === 'LUXUOSO' ? 'Luxuoso' :
      'Conforto';

    const night = (c.preco * taxaNoturna).toFixed(2); // same rule as your simulator
    const example = (c.preco * 30).toFixed(2);
    const exampleNight = (c.preco * taxaNoturna * 30).toFixed(2);

    const row = `
      <tr>
        <td><span class="badge">${label}</span></td>
        <td class="sora font-semibold">€${c.preco.toFixed(2)}</td>
        <td class="sora font-semibold">€${night}</td>
        <td class="text-slate-400">€${example} diurno / €${exampleNight} noturno</td>
      </tr>
    `;

    tbody.innerHTML += row;
  });
}

function renderDriverActivity(motoristas, viagens) {

  const tbody = document.querySelector('#driver-activity');
  tbody.innerHTML = '';

  const today = new Date().toISOString().split('T')[0];

  motoristas.slice(0, 5).forEach(m => {

    const driverTrips = viagens.filter(v => v.pedido.motoristaId === m.id);

    const trips = driverTrips.length;

    const km = driverTrips.reduce((sum, v) => sum + (v.kilometros || 0), 0);

    const revenue = driverTrips.reduce((sum, v) => sum + (v.preco || 0), 0);

    const now = new Date();

    const turnoAtual = allTurnos.find(t => {
      if (t.motoristaId !== m.id) return false;

      const inicio = new Date(t.inicio);
      const fim = new Date(t.fim);

      return now >= inicio && now <= fim;
    });

    const turno = turnoAtual
      ? `${new Date(turnoAtual.inicio).toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit',
          hour12: false
        })} - ${new Date(turnoAtual.fim).toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit',
          hour12: false
        })}`
      : '—';
      
    const estado = getEstadoMotorista(m.id, allPedidos, allTurnos);

    let badge = 'badge-slate';

    if (estado === 'Ocupado') {
      badge = 'badge-amber';   // busy → warning color
    } else if (estado === 'Livre') {
      badge = 'badge-green';   // available → good
    } else {
      badge = 'badge-slate';   // not working → neutral
    }

    const row = `
      <tr>
        <td>
          <div class="font-medium text-slate-800 dark:text-slate-200">
            ${m.nome}
          </div>
        </td>

        <td class="text-slate-400">${turno}</td>
        <td>${trips}</td>
        <td>${km.toFixed(1)}</td>

        <td class="text-emerald-500 font-semibold sora">
          €${revenue.toFixed(0)}
        </td>

        <td>
          <span class="badge ${badge}">
            ${estado}
          </span>
        </td>
      </tr>
    `;

    tbody.innerHTML += row;
  });
}




function getEstadoMotorista(motoristaId, pedidos, turnos) {
  const now = new Date();

  // 1. turno atual
  const emTurno = turnos.some(t => {
    console.log(
      "Motorista", motoristaId,
      "| Turno:", t.inicio, "-", t.fim,
      "| Agora:", now
    );

    return (
      t.motoristaId === motoristaId &&
      now >= new Date(t.inicio) &&
      now <= new Date(t.fim)
    );
  });

  if (emTurno) {
    // verificar pedidos ativos
    const hasAtivo = pedidos.some(p => {
      if (p.motoristaId === motoristaId) {
        return (p.estadoPedido !== 'CONCLUIDO' && p.estadoPedido !== 'CANCELADO');
      }
    });

    if (hasAtivo) {
      return 'Ocupado';
    }
  }

  // 2. turno futuro
  const temTurnoFuturo = turnos.some(t =>
    t.motoristaId === motoristaId &&
    new Date(t.fim) > now
  );

  if (temTurnoFuturo) return 'Livre com Turno';

  // 3. nenhum turno
  return 'Livre';
}


async function loadTurnos() {
  const token = localStorage.getItem("token");

  try {
    const res = await fetch('/api/turnos/', {
      headers: {
        'Accept': 'application/json',
        'Authorization': 'Bearer ' + token
      }
    });

    if (!res.ok) throw new Error('Failed to fetch turnos');

    allTurnos = await res.json();

  } catch (err) {
    console.error(err);
  }
}


function getTaxiStatus(taxiId, turnos) {
  const now = new Date();

  const active = turnos.find(t => {
    if (t.taxiId !== taxiId) return false;

    const start = new Date(t.inicio);
    const end = new Date(t.fim);

    return now >= start && now <= end;
  });

  if (active) return 'EM_TURNO';
  return 'DISPONIVEL';
}





// ── Theme ──────────────────────────────────────────────────────────────────
    function applyTheme(t){
      document.documentElement.classList.toggle('dark',t==='dark');
      document.getElementById('theme-btn').textContent=t==='dark'?'☀️':'🌙';
    }
    function toggleTheme(){
      var t=localStorage.getItem('dash-theme')==='dark'?'light':'dark';
      localStorage.setItem('dash-theme',t);applyTheme(t);
    }
    applyTheme(localStorage.getItem('dash-theme')||'light');

    // ── Toast ──────────────────────────────────────────────────────────────────
    var _tt;
    function toast(msg){
      var el=document.getElementById('toast');
      el.textContent=msg;el.classList.add('show');
      clearTimeout(_tt);_tt=setTimeout(function(){el.classList.remove('show');},2700);
    }

    // ── Today date ─────────────────────────────────────────────────────────────
    // document.getElementById('today-date').textContent=new Date().toLocaleDateString('pt-PT',{weekday:'long',year:'numeric',month:'long',day:'numeric'});

    // ── Turno filter default ───────────────────────────────────────────────────
    var today=new Date().toISOString().split('T')[0];
    document.querySelectorAll('input[type=date]').forEach(function(el){el.value=today;});

    // ── Panel navigation ───────────────────────────────────────────────────────
    var PANELS=['dashboard','taxis','motoristas','turnos','reabast','viagens','faturas','precos','rel-frota','rel-fat'];
    function showPanel(id){
      PANELS.forEach(function(p){
        var el=document.getElementById('panel-'+p);
        var nav=document.getElementById('nav-'+p);
        if(el) el.classList.toggle('active',p===id);
        if(nav) nav.classList.toggle('active',p===id);
      });
      // close mobile menu
      document.getElementById('sidebar').classList.remove('open');
      window.scrollTo(0,0);
    }

    // ── Mobile menu ─────────────────────────────────────────────────────────────
    function toggleMobileMenu(){document.getElementById('sidebar').classList.toggle('open');}

    // ── Modal ──────────────────────────────────────────────────────────────────
    var MODAL_MAP={
      'modal-criarTaxi':'tpl-criarTaxi',
      'modal-editarTaxi':'tpl-editarTaxi',
      'modal-criarMotorista':'tpl-criarMotorista',
      'modal-editarMotorista':'tpl-editarMotorista',
      'modal-viagem-detail':'tpl-viagem-detail',
      'modal-confirm':'tpl-confirm',
    };
    function openModal(id){
      var tpl=MODAL_MAP[id];
      if(!tpl) return;
      var t=document.getElementById(tpl);
      if(!t) return;
      document.getElementById('modal-content').innerHTML=t.innerHTML;
      document.getElementById('modal-overlay').classList.add('open');
    }
    function closeModal(){document.getElementById('modal-overlay').classList.remove('open');}
    function handleOverlayClick(e){if(e.target===document.getElementById('modal-overlay'))closeModal();}

    // ── Taxi form ─────────────────────────────────────────────────────────────
    var MODELOS={'Toyota':['Corolla','Yaris','Camry','Prius'],'Mercedes':['E-Class','C-Class','S-Class','GLE'],'Tesla':['Model 3','Model Y','Model S','Model X'],'BMW':['3 Series','5 Series','7 Series','X5'],'Volkswagen':['Passat','Golf','Tiguan','Arteon'],'Renault':['Megane','Talisman','Espace']};

    function updateModelos(){
      var marca=document.getElementById('f-marca').value;
      var sel=document.getElementById('f-modelo');
      sel.innerHTML='<option value="">Selecionar…</option>';
      (MODELOS[marca]||[]).forEach(function(m){
        var o=document.createElement('option');o.value=m;o.textContent=m;sel.appendChild(o);
      });
    }

    var MAT_RE=/^([A-Z]{2}-\d{2}-\d{2}|[A-Z]{2}-\d{2}-[A-Z]{2}|\d{2}-\d{2}-[A-Z]{2}|\d{2}-[A-Z]{2}-\d{2}|\d{2}-[A-Z]{2}-[A-Z]{2}|[A-Z]{2}-[A-Z]{2}-\d{2})$/i;
    function validateMat(el){
      var ok=MAT_RE.test(el.value.toUpperCase());
      document.getElementById('mat-err').classList.toggle('hidden',ok||!el.value);
      return ok||!el.value;
    }

    function criarTaxi(){
      const matricula = document.getElementById('f-taxi-mat').value.trim();
      const ano = parseInt(document.getElementById('f-taxi-ano').value);
      const marca = document.getElementById('f-taxi-marca').value;
      const modelo = document.getElementById('f-taxi-modelo').value;
      const conforto = document.getElementById('f-taxi-conforto').value;
      const motor = document.getElementById('f-taxi-motor').value;
      const km = parseInt(document.getElementById('f-taxi-km').value);

      // VALIDATION (ALL REQUIRED)
      if (!matricula) return toast('⚠️ Matrícula obrigatória');
      if (!ano) return toast('⚠️ Ano obrigatório');
      if (!marca) return toast('⚠️ Marca obrigatória');
      if (!modelo) return toast('⚠️ Modelo obrigatório');
      if (!conforto) return toast('⚠️ Conforto obrigatório');
      if (!motor) return toast('⚠️ Tipo de motor obrigatório');
      if (isNaN(km)) return toast('⚠️ Kilómetros obrigatórios');

      const body = {
        matricula,
        anoCompra: ano,
        marca,
        modelo,
        nivelConforto: conforto,
        tipoMotor: motor,
        kilometros: km
      };

      const token = localStorage.getItem("token");

      fetch('/api/taxis', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(body)
      })
      .then(res => {
        if (!res.ok) throw new Error('Erro ao criar táxi');
        return res.json();
      })
      .then(() => {
        closeModal();
        toast('🚖 Táxi criado com sucesso!');
        loadTaxis();
      })
      .catch(err => {
        console.error(err);
        toast('❌ Erro ao criar táxi');
      });
    }


    function updateRelatorio(){
      const from = document.getElementById('rel-from').value;
      const to = document.getElementById('rel-to').value;

      const fromDate = from ? new Date(from) : null;
      const toDate = to ? new Date(to) : null;

      let filtered = allViagens.filter(v => {
        const d = new Date(v.inicio);
        if (fromDate && d < fromDate) return false;
        if (toDate && d > toDate) return false;
        return true;
      });

      // ===== TOTALS =====
      const totalViagens = filtered.length;

      let totalKm = 0;
      let totalMinutes = 0;

      const perDriver = {};

      filtered.forEach(v => {
        totalKm += v.kilometros || 0;

        const start = new Date(v.inicio);
        const end = new Date(v.fim);
        totalMinutes += (end - start) / 60000;

        const id = v.pedido.motoristaId;
        if (!perDriver[id]) {
          perDriver[id] = { nome: motoristaMap[id], viagens: 0, km: 0, minutes: 0, preco:0 };
        }

        perDriver[id].viagens++;
        perDriver[id].km += v.kilometros || 0;
        perDriver[id].minutes += (end - start) / 60000;
        perDriver[id].preco += v.preco;
      });

      // ===== UPDATE TOTAL CARDS =====
      document.getElementById('stat-viagens').textContent = totalViagens;

      const hours = Math.floor(totalMinutes / 60);
      const minutes = Math.round(totalMinutes % 60);
      document.getElementById('stat-horas').textContent = `${hours}h ${minutes}m`;

      document.getElementById('stat-km').textContent = totalKm.toFixed(1) + ' km';

      document.querySelector('[onclick="drillTotal(\'viagens\')"] .sora').textContent = totalViagens;

      const h = Math.floor(totalMinutes / 60);
      const m = Math.round(totalMinutes % 60);
      document.querySelector('[onclick="drillTotal(\'horas\')"] .sora').textContent = `${h}h ${m}m`;

      document.querySelector('[onclick="drillTotal(\'km\')"] .sora').textContent =
        totalKm.toFixed(1) + ' km';

      // ===== UPDATE TABLE =====
      const tbody = document.querySelector('#rel-frota-body');
      if (tbody) {
        tbody.innerHTML = '';

        Object.values(perDriver).forEach(d => {
          const h = Math.floor(d.minutes / 60);
          const m = Math.round(d.minutes % 60);

          const percent = totalKm ? (d.km / totalKm) * 100 : 0;

          tbody.innerHTML += `
            <tr class="hover:bg-slate-50 dark:hover:bg-slate-700/30 transition">
              <td class="font-medium text-slate-800 dark:text-slate-200">${d.nome}</td>

              <td class="text-slate-600 dark:text-slate-300 font-semibold">
                ${d.viagens}
              </td>

              <td class="text-slate-600 dark:text-slate-300">
                ${h}h ${m}m
              </td>

              <td class="text-slate-600 dark:text-slate-300 font-semibold">
                ${d.km.toFixed(1)} km
              </td>

              <td class="text-emerald-500 font-bold sora">
                €${d.preco.toFixed(2)}
              </td>
            </tr>
          `;
        });
      }
    }



    function saveTaxi(){
      const id = document.getElementById('f-taxi-id').value;

      const matricula = document.getElementById('f-taxi-mat').value.trim();
      const ano = parseInt(document.getElementById('f-taxi-ano').value);
      const marca = document.getElementById('f-taxi-marca').value;
      const modelo = document.getElementById('f-taxi-modelo').value;
      const conforto = document.getElementById('f-taxi-conforto').value;
      const motor = document.getElementById('f-taxi-motor').value;
      const km = parseInt(document.getElementById('f-taxi-km').value);

      if(!id){ toast('⚠️ ID inválido'); return; }

      const body = {
        matricula: matricula || null,
        anoCompra: isNaN(ano) ? null : ano,
        marca: marca || null,
        modelo: modelo || null,
        nivelConforto: conforto || null,
        tipoMotor: motor || null,
        kilometros: isNaN(km) ? null : km
      };

      const token = localStorage.getItem("token");

      fetch(`/api/taxis/${id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(body)
      })
      .then(res => {
        console.log("Update taxi response:", res);
        if(!res.ok) throw new Error('Erro ao atualizar táxi');
        return res.json();
      })
      .then(() => {
        closeModal();
        toast('🚖 Táxi atualizado!');
        loadTaxis();
      })
      .catch(err => {
        console.error(err);
        toast('❌ Erro ao atualizar táxi');
      });
    }

    function openEditTaxi(data){
      openModal('modal-editarTaxi');
      setTimeout(function(){
        document.getElementById('modal-taxi-title').textContent='Editar táxi';
        document.getElementById('f-taxi-id').value=data.id;
        document.getElementById('f-taxi-mat').value=data.matricula;
        document.getElementById('f-taxi-ano').value=data.anoCompra;
        document.getElementById('f-taxi-marca').value=data.marca;
        document.getElementById('f-taxi-modelo').value=data.modelo;
        document.getElementById('f-taxi-conforto').value=data.conforto.nivelConforto;
        document.getElementById('f-taxi-motor').value=data.tipoMotor;
        document.getElementById('f-taxi-km').value=data.kilometros;
      },50);
    }

    // ── Motorista form ─────────────────────────────────────────────────────────
    function validateNIF(el){
      var ok=/^\d{9}$/.test(el.value)&&parseInt(el.value)>0;
      document.getElementById('nif-err').classList.toggle('hidden',ok||!el.value);
    }
    function validateSenha(el){
      var v=el.value;
      var ok=v.length>=6&&/[a-zA-Z]/.test(v)&&/\d/.test(v);
      document.getElementById('senha-err').classList.toggle('hidden',ok||!v);
    }

    // Simulate postal code lookup
    var POSTAL_MAP={'1000':'Lisboa','2700':'Amadora','2710':'Sintra','4000':'Porto','8000':'Faro','3000':'Coimbra'};
    function lookupPostal(el){
      var prefix=el.value.split('-')[0];
      var loc=POSTAL_MAP[prefix];
      var locEl=document.getElementById('f-localidade');
      if(loc){locEl.value=loc;}else{locEl.value='';}
    }

    function saveMotorista(){
      var id = document.getElementById('f-id').value.trim();
      var nome = document.getElementById('f-nome').value.trim();
      var nasc = document.getElementById('f-nasc').value;
      var carta = document.getElementById('f-carta').value.trim();
      var senha = document.getElementById('f-senha').value;

      var rua = document.getElementById('f-rua').value.trim();
      var numero = parseInt(document.getElementById('f-num').value);
      var cp = document.getElementById('f-cp').value.trim();
      var genero = document.getElementById('f-genero').value;
      var validadeCarta = document.getElementById('f-validade-carta')?.value || null;

      if (senha && (senha.length < 6 || !/[a-zA-Z]/.test(senha) || !/\d/.test(senha))) {
        toast('⚠️ Senha não cumpre requisitos');
        return;
      }

      const body = {
        nif: null,
        email: null,
        senha: null,
        nome: nome || null,
        genero: genero || null,
        dataNascimento: nasc,
        rua: rua || null,
        ruaNumero: numero || null,
        codigoPostal: cp || null,
        numCarta: carta || null,
        validadeCarta: null
      };

      const token = localStorage.getItem("token");

      fetch(`/api/motoristas/${id}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + token
        },
        body: JSON.stringify(body)
      })
      .then(res => {
        if (!res.ok) throw new Error("Erro ao atualizar motorista");
        return res.json();
      })
      .then(() => {
        closeModal();
        toast('✅ Motorista atualizado!');
        loadMotoristas();
      })
      .catch(err => {
        console.error(err);
        toast('❌ Erro ao atualizar motorista');
      });
    }

    function createMotorista(){
      var nome = document.getElementById('f-nome').value.trim();
      var nif = document.getElementById('f-nif').value.trim();
      var email = document.getElementById('f-email').value.trim();
      var nasc = document.getElementById('f-nasc').value;
      var carta = document.getElementById('f-carta').value.trim();
      var senha = document.getElementById('f-senha').value;
      var senha2 = document.getElementById('f-senha2').value;

      var rua = document.getElementById('f-rua').value.trim();
      var numero = parseInt(document.getElementById('f-num').value);
      var cp = document.getElementById('f-cp').value.trim();
      var genero = document.getElementById('f-genero').value;
      var validadeCarta = document.getElementById('f-validade-carta').value;

      // VALIDATIONS (all mandatory)
      if (!nome) { toast('⚠️ Nome obrigatório');         console.log(nasc);
 return; }
      if (!/^\d{9}$/.test(nif)) { toast('⚠️ NIF inválido'); return; }
      if (!email) { toast('⚠️ Email obrigatório'); return; }
      if (!nasc) { toast('⚠️ Data de nascimento obrigatória'); return; }
      if (!carta) { toast('⚠️ Carta de condução obrigatória'); return; }
      if (!rua) { toast('⚠️ Rua obrigatória'); return; }
      if (isNaN(numero)) { toast('⚠️ Número obrigatório'); return; }
      if (!cp) { toast('⚠️ Código postal obrigatório'); return; }
      if (!genero) { toast('⚠️ Género obrigatório'); return; }
      if (!validadeCarta) { toast('⚠️ Validade da carta obrigatória'); return; }

      if (senha.length < 6 || !/[a-zA-Z]/.test(senha) || !/\d/.test(senha)) {
        toast('⚠️ Senha não cumpre requisitos');
        return;
      }

      if (senha !== senha2) {
        toast('⚠️ Senhas não coincidem');
        return;
      }

      const body = {
        nif: nif,
        email: email,
        senha: senha,
        nome: nome,
        genero: genero,
        dataNascimento: nasc,
        rua: rua,
        ruaNumero: numero,
        codigoPostal: cp,
        numCarta: carta,
        validadeCarta: validadeCarta
      };

      const token = localStorage.getItem("token");

      fetch(`/api/motoristas`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + token
        },
        body: JSON.stringify(body)
      })
      .then(res => {
        console.log("Create motorista response:", res);
        if (!res.ok) throw new Error("Erro ao criar motorista");
        return res.json();
      })
      .then(() => {
        closeModal();
        toast('✅ Motorista criado!');
        loadMotoristas();
      })
      .catch(err => {
        console.error(err);
        toast('❌ Erro ao criar motorista');
      });
    }

    function openEditMotorista(data){
      openModal('modal-editarMotorista');
      setTimeout(function(){
        document.getElementById('modal-mot-title').textContent='Editar motorista';
        document.getElementById('f-id').value=data.id;
        document.getElementById('f-nome').value=data.nome;
        document.getElementById('f-nasc').value = data.dataNascimento ? new Date(data.dataNascimento).toISOString().split('T')[0] : '';
        document.getElementById('f-num').value=data.ruaNumero;
        document.getElementById('f-carta').value=data.numCarta;
        document.getElementById('f-rua').value=data.rua;
        document.getElementById('f-cp').value=data.codigoPostal;
        document.getElementById('f-genero').value=data.genero;
      },50);
    }

    // ── Confirm delete ─────────────────────────────────────────────────────────
    function confirmDelete(label){
      MODAL_MAP['modal-confirm']='tpl-confirm';
      openModal('modal-confirm');
      setTimeout(function(){
        var el=document.getElementById('confirm-msg');
        if(el) el.textContent='Tem a certeza que quer remover o '+label+'? Esta ação é irreversível.';
      },30);
    }

    async function runSimulator() {
      const token = localStorage.getItem("token");

      const conforto = document.getElementById('sim-conforto').value;
      const inicio = document.getElementById('sim-inicio').value;
      const fim = document.getElementById('sim-fim').value;

      if (!inicio || !fim) {
        toast('⚠️ Preencha início e fim');
        return;
      }

      try {
        const url = `/api/precos/calculate-preco-viagem?conforto=${conforto}&inicio=${inicio}&fim=${fim}`;

        const res = await fetch(url, {
          method: "GET",
          headers: {
            "Authorization": "Bearer " + token,
            "Accept": "application/json"
          }
        });

        if (!res.ok) throw new Error("Erro no cálculo");

        const data = await res.json();

        document.getElementById('sim-total').textContent = `€${data.preco.toFixed(2)}`;
        document.getElementById('sim-breakdown').textContent = data.detalhe || '';
        document.getElementById('sim-result').classList.remove('hidden');

      } catch (err) {
        console.error(err);
        toast("❌ Erro ao calcular preço");
      }
    }


function drillTotal(type){
  const from = document.getElementById('rel-from').value;
  const to = document.getElementById('rel-to').value;

  const fromDate = from ? new Date(from) : null;
  const toDate = to ? new Date(to) : null;

  const filtered = allViagens.filter(v => {
    const d = new Date(v.inicio);
    if (fromDate && d < fromDate) return false;
    if (toDate && d > toDate) return false;
    return true;
  });

  const area = document.getElementById('drill-area');
  const title = document.getElementById('drill-title');
  const content = document.getElementById('drill-content');

  area.classList.remove('hidden');

  let html = '';

  if(type === 'viagens'){
    title.textContent = 'Lista de viagens';

    filtered.forEach(v => {
      html += buildCard(
        `${v.pedido.ruaOrigem} → ${v.pedido.ruaDestino}`,
        `${motoristaMap[v.pedido.motoristaId]} · ${v.kilometros} km`,
        ''
      );
    });
  }

  if(type === 'horas'){
    title.textContent = 'Tempo por viagem';

    filtered.forEach(v => {
const diffMs = new Date(v.fim) - new Date(v.inicio);

const totalMinutes = Math.floor(diffMs / 60000);
const hours = Math.floor(totalMinutes / 60);
const minutes = totalMinutes % 60;

const formattedTime = `${hours}h ${minutes}m`;

html += buildCard(
        motoristaMap[v.pedido.motoristaId],
        `${v.pedido.ruaOrigem} → ${v.pedido.ruaDestino}`,
        `${formattedTime}`
      );
    });
  }

  if(type === 'km'){
    title.textContent = 'Quilómetros por viagem';

    filtered.forEach(v => {
      html += buildCard(
        motoristaMap[v.pedido.motoristaId],
        `${v.pedido.ruaOrigem} → ${v.pedido.ruaDestino}`,
        `${v.kilometros} km`
      );
    });
  }

  content.innerHTML = html || `<div class="text-xs text-slate-400">Sem dados</div>`;
}


function buildCard(title, subtitle, right){
  return `
    <div class="flex items-center justify-between p-2 rounded-lg border border-slate-100 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700/30 transition">
      <div>
        <div class="text-sm font-medium text-slate-800 dark:text-slate-200">${title}</div>
        <div class="text-xs text-slate-400">${subtitle}</div>
      </div>
      <div class="text-sm font-semibold text-sky-500">${right}</div>
    </div>
  `;
}

let relatorioCache = {
  filtered: [],
  perClient: {}
};

function updateRelatorioClientes() {

  const filtered = getViagensFiltradas();
  console.log("Relatório clientes - viagens filtradas1:", filtered);

  let total = 0;
  const perClient = {};

  filtered.forEach(v => {
    const clientId = v.cliente.id;
    const price = v.preco || 0;

    total += price;

    if (!perClient[clientId]) {
      perClient[clientId] = {
        id: clientId,
        nome: v.cliente.nome,
        total: 0,
        viagens: [],
        count: 0
      };
    }

    perClient[clientId].total += price;
    perClient[clientId].viagens.push(v);
    perClient[clientId].count++;
  });

  relatorioCache.filtered = filtered;
  console.log("Relatório clientes - viagens filtradas:", filtered);
  relatorioCache.perClient = perClient;
  console.log("Relatório clientes - por cliente:", perClient);

  // ===== TOP STATS =====
  document.getElementById('fat-total').textContent = `€${total.toFixed(2)}`;
  document.getElementById('fat-clients').textContent = Object.keys(perClient).length;
  document.getElementById('fat-trips').textContent = filtered.length;

  // ===== TABLE (LEVEL 2 ENTRY) =====
  const tbody = document.getElementById('fat-table-body');
  tbody.innerHTML = '';

Object.values(perClient)
  .sort((a, b) => b.total - a.total)
  .forEach(c => {

    tbody.innerHTML += `
        <tr onclick="drillClienteSubtotal('${c.id}')"
            class="cursor-pointer hover:bg-slate-100 dark:hover:bg-slate-700">
        
        <td>${c.nome}</td>
        <td>${c.count}</td>
        <td class="text-emerald-500 font-semibold">
          €${c.total.toFixed(2)}
        </td>
      </tr>
    `;
  });
}

function drillClientes(clientId) {

  const client = relatorioCache.perClient[clientId];
  if (!client) return;

  const area = document.getElementById('drill2-area');
  const title = document.getElementById('drill2-title');
  const content = document.getElementById('drill2-content');

  area.classList.remove('hidden');
  title.textContent = `Viagens de ${client.nome}`;

  content.innerHTML = client.viagens
    .sort((a, b) => (b.preco || 0) - (a.preco || 0))
    .map(v => `
      <div class="p-2 border-b cursor-pointer"
           onclick="drillViagem(${v.id})">
        ${v.pedido.ruaOrigem} → ${v.pedido.ruaDestino}
        <span class="float-right text-emerald-500">
          €${(v.preco || 0).toFixed(2)}
        </span>
      </div>
    `).join('');
}


function drillViagem(id) {
  const v = relatorioCache.filtered.find(x => x.id === id);
  if (!v) return;

  const h = (new Date(v.fim) - new Date(v.inicio)) / 60000;
  const hours = Math.floor(h / 60);
  const minutes = Math.round(h % 60);

  alert(
    `Cliente: ${v.cliente.nome}\n` +
    `Viagem: ${v.pedido.ruaOrigem} → ${v.pedido.ruaDestino}\n` +
    `Preço: €${v.preco}\n` +
    `Duração: ${hours}h ${minutes}m`
  );
}

function closeDrill() {
  document.getElementById('drill2-area').classList.add('hidden');
}

function getViagensFiltradas() {
  const from = document.getElementById('rel2-from').value;
  const to = document.getElementById('rel2-to').value;

  const fromDate = from ? new Date(from).getTime() : null;
  const toDate = to ? new Date(to).getTime() : null;

  return allViagens.filter(v => {
    const d = new Date(v.inicio).getTime();

    if (fromDate && d < fromDate) return false;
    if (toDate && d > toDate) return false;
    return true;
  });
}


function drillClienteSubtotal(clientId) {
  console.log("CLICKED CLIENT:", clientId);
  const client = relatorioCache.perClient[clientId];
  if (!client) return;

  const area = document.getElementById('drill2-area');
  const title = document.getElementById('drill2-title');
  const content = document.getElementById('drill2-content');

  area.classList.remove('hidden');
  title.textContent = `Subtotal: ${client.nome}`;

  // SORT TRIPS DESC by price (IMPORTANT REQUIREMENT c)
  const sortedTrips = [...client.viagens]
    .sort((a, b) => (b.preco || 0) - (a.preco || 0));

  content.innerHTML = sortedTrips.map(v => `
    <div class="p-2 border-b cursor-pointer"
         onclick="drillViagem(${v.id})">

      <div class="flex justify-between">
        <span>${v.pedido.ruaOrigem} → ${v.pedido.ruaDestino}</span>
        <span class="text-emerald-500 font-semibold">
          €${(v.preco || 0).toFixed(2)}
        </span>
      </div>

      <div class="text-xs text-slate-400">
        ${new Date(v.inicio).toLocaleString('pt-PT', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
          })}
      </div>
    </div>
  `).join('');
}

function closeDrill() {
  document.getElementById('drill2-area').classList.add('hidden');
}



async function loadUsername() {
  const userData = localStorage.getItem("user");

  if (userData) {
    // Convert the string back into a Javascript Object
    const user = JSON.parse(userData);
    
    const el = document.getElementById("username");
    if (el) {
      el.textContent = user.nome;
    }
  }
}


function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
  localStorage.removeItem("user");
  window.location.href = "index";
}