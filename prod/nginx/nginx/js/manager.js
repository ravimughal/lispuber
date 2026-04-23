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
    window.location.href = `/${lang}/index`;
    return;
  }

  const page = parts[1] || "index";

  window.location.href = `/${lang}/${page}`;
}