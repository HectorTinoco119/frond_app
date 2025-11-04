const API_BASE = "http://192.168.0.108:4000";

/* --- Función para mostrar alertas Bootstrap --- */
function showAlert(message, type = "info") {
  const container = document.getElementById("alertContainer");
  if (!container) return;
  container.innerHTML = `
    <div class="alert alert-${type} alert-dismissible fade show" role="alert">
      ${message}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
  `;
}

/* --- Registro de usuario --- */
async function register() {
  const data = {
    email: document.getElementById("regEmail").value,
    password: document.getElementById("regPassword").value,
    firstname: document.getElementById("regFirstname").value,
    lastname: document.getElementById("regLastname").value,
    country: document.getElementById("regCountry").value,
    role: document.getElementById("regRole").value
  };

  try {
    const res = await fetch(`${API_BASE}/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data)
    });

    if (res.ok) {
      showAlert("Usuario registrado correctamente ✅", "success");
    } else {
      showAlert("Error al registrar el usuario ❌", "danger");
    }
  } catch (error) {
    showAlert("Error de conexión con el servidor", "danger");
  }
}

/* --- Login de usuario --- */
async function login() {
  const data = {
    email: document.getElementById("loginEmail").value,
    password: document.getElementById("loginPassword").value
  };

  try {
    const res = await fetch(`${API_BASE}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data)
    });

    if (res.ok) {
      const json = await res.json();
      localStorage.setItem("token", json.token);
      showAlert("Inicio de sesión exitoso ✅", "success");
      setTimeout(() => window.location.href = "profile.html", 1000);
    } else {
      showAlert("Credenciales incorrectas ❌", "danger");
    }
  } catch (error) {
    showAlert("Error de conexión con el servidor", "danger");
  }
}

/* --- Obtener perfil del usuario autenticado --- */
async function loadProfile() {
  const token = localStorage.getItem("token");
  if (!token) {
    window.location.href = "index.html";
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/admin/profile`, {
      headers: { "Authorization": `Bearer ${token}` }
    });

    if (res.ok) {
      const user = await res.json();
      renderProfile(user);
    } else {
      localStorage.removeItem("token");
      window.location.href = "index.html";
    }
  } catch (error) {
    console.error("Error al cargar el perfil", error);
  }
}

/* --- Renderizar los datos del perfil --- */
function renderProfile(user) {
  const container = document.getElementById("profileContainer");
  if (!container) return;

  container.innerHTML = `
    <div class="col-md-6">
      <div class="card shadow-sm">
        <div class="card-body">
          <h4 class="mb-3 text-center">${user.firstname} ${user.lastname}</h4>
          <ul class="list-group">
            <li class="list-group-item"><strong>ID:</strong> ${user.id}</li>
            <li class="list-group-item"><strong>Correo:</strong> ${user.email}</li>
            <li class="list-group-item"><strong>País:</strong> ${user.country}</li>
            <li class="list-group-item"><strong>Rol:</strong> ${user.role}</li>
            <li class="list-group-item"><strong>Estado:</strong> ${user.enabled ? "Activo" : "Inactivo"}</li>
          </ul>
        </div>
      </div>
    </div>
  `;
}

/* --- Cerrar sesión --- */
function logout() {
  localStorage.removeItem("token");
  window.location.href = "index.html";
}

/* --- Autoejecución: si estamos en profile.html, carga el perfil --- */
if (window.location.pathname.endsWith("profile.html")) {
  loadProfile();
}
