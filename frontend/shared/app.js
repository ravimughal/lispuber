// ── DASH App JS ──────────────────────────────────────────────────────────────

// ── MAP ───────────────────────────────────────────────────────────────────────
let _map = null, _pickupMarker = null, _destMarker = null,
    _routeLayer = null, _driverMarker = null,
    _pickupLatLng = null, _destLatLng = null, _routeKm = 0;

function icon(color, label) {
  return L.divIcon({
    className: '',
    html: `<div style="width:28px;height:28px;border-radius:50% 50% 50% 0;background:${color};transform:rotate(-45deg);border:2px solid white;box-shadow:0 2px 8px rgba(0,0,0,.2);display:flex;align-items:center;justify-content:center">
             <span style="transform:rotate(45deg);font-size:10px;color:white;font-weight:700">${label}</span></div>`,
    iconSize:[28,28], iconAnchor:[14,28],
  });
}

function carIcon() {
  return L.divIcon({
    className:'',
    html:`<div style="font-size:1.3rem;filter:drop-shadow(0 2px 4px rgba(0,0,0,.25))">🚗</div>`,
    iconSize:[26,26], iconAnchor:[13,13],
  });
}

function initMap(id, lat=38.7223, lng=-9.1393, zoom=14) {
  if (_map) { _map.remove(); _map=null; }
  _map = L.map(id, { zoomControl:true, attributionControl:false }).setView([lat,lng], zoom);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom:19 }).addTo(_map);
  return _map;
}

async function geocode(q) {
  const r = await fetch(`https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(q)}&format=json&limit=1`,
    { headers:{ 'Accept-Language':'en' } });
  const d = await r.json();
  return d.length ? { lat:+d[0].lat, lng:+d[0].lon, name:d[0].display_name } : null;
}

async function reverseGeocode(lat, lng) {
  try {
    const r = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`);
    const d = await r.json();
    return d.display_name || `${lat.toFixed(4)}, ${lng.toFixed(4)}`;
  } catch { return `${lat.toFixed(4)}, ${lng.toFixed(4)}`; }
}

async function drawRoute(f, t) {
  if (_routeLayer) { _map.removeLayer(_routeLayer); _routeLayer=null; }
  try {
    const r = await fetch(`https://router.project-osrm.org/route/v1/driving/${f[1]},${f[0]};${t[1]},${t[0]}?overview=full&geometries=geojson`);
    const d = await r.json();
    if (!d.routes?.length) return 3.4;
    const coords = d.routes[0].geometry.coordinates.map(([lng,lat]) => [lat,lng]);
    _routeKm = d.routes[0].distance / 1000;
    _routeLayer = L.polyline(coords, { color:'#0ea5e9', weight:4, opacity:.75 }).addTo(_map);
    _map.fitBounds(_routeLayer.getBounds(), { padding:[80,80] });
    return _routeKm;
  } catch { return 3.4; }
}

// ── TRACKING ──────────────────────────────────────────────────────────────────
let _trackInterval = null;

function startTracking() {
  if (!_map||!_pickupLatLng||!_destLatLng) return;
  const [sLat,sLng] = _pickupLatLng, [dLat,dLng] = _destLatLng;
  let drvLat = sLat-0.01, drvLng = sLng-0.008;
  if (_driverMarker) _map.removeLayer(_driverMarker);
  _driverMarker = L.marker([drvLat,drvLng],{icon:carIcon()}).addTo(_map);
  let step=0; const total=14;
  _trackInterval = setInterval(() => {
    if (step>=total) { clearInterval(_trackInterval); return; }
    step++;
    const t = step/total;
    let lat, lng;
    if (t<0.4) { const tt=t/0.4; lat=drvLat+(sLat-drvLat)*tt; lng=drvLng+(sLng-drvLng)*tt; }
    else        { const tt=(t-0.4)/0.6; lat=sLat+(dLat-sLat)*tt; lng=sLng+(dLng-sLng)*tt; }
    _driverMarker.setLatLng([lat,lng]);
    const etaEl=document.getElementById('eta-text');
    if (etaEl) { const rem=Math.max(0,Math.round((1-t)*14)); etaEl.textContent=rem>0?`${rem} min away`:'Arriving…'; }
    const prog=document.getElementById('ride-progress');
    if (prog) prog.style.width=Math.round(t*100)+'%';
  }, 2400);
}

function stopTracking() {
  clearInterval(_trackInterval);
  if (_driverMarker) { _map.removeLayer(_driverMarker); _driverMarker=null; }
}

// ── FARES ─────────────────────────────────────────────────────────────────────
const FARE_CFG = {
  economy: { base:2.00, km:1.20 },
  comfort: { base:3.00, km:1.80 },
  premium: { base:5.00, km:2.80 },
};

function calcFare(type, km) {
  const f=FARE_CFG[type]; return (f.base + f.km*km).toFixed(2);
}

let selectedRide = 'economy';

function selectRide(type) {
  selectedRide = type;
  document.querySelectorAll('[data-ride]').forEach(el => {
    const active = el.dataset.ride===type;
    el.classList.toggle('border-sky-400',  active);
    el.classList.toggle('bg-sky-50',       active);
    el.classList.toggle('dark:bg-sky-950/30', active);
    el.classList.toggle('border-slate-200', !active);
    el.classList.toggle('dark:border-slate-700', !active);
  });
  updateConfirmBtn();
}

function updateFares(km) {
  for (const t of ['economy','comfort','premium']) {
    const el=document.getElementById('fare-'+t);
    if (el) el.textContent='€'+calcFare(t,km);
  }
  const kl=document.getElementById('km-label');
  if (kl) kl.textContent=km.toFixed(1)+' km';
  updateConfirmBtn();
}

function updateConfirmBtn() {
  const btn=document.getElementById('confirm-btn');
  if (btn) btn.textContent = _routeKm>0
    ? `Confirm ride — €${calcFare(selectedRide,_routeKm)}`
    : 'Confirm ride';
}