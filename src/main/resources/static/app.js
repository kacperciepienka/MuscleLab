const API_URL = "http://localhost:8080";

let authHeader = null;
let currentUsername = null;
let currentRole = null;
let currentUserData = null;

const routes = {
    "home": "homeView",
    "login": "loginView",
    "register-client": "clientRegisterView",
    "register-coach": "coachRegisterView",
    "coaches": "coachesView",
    "clients": "clientsView",
    "slots": "slotsView",
    "client-panel": "clientPanelView",
    "coach-panel": "coachPanelView",
    "account": "accountView",
    "debug": "responseView"
};

const viewToHash = {
    "homeView": "home",
    "loginView": "login",
    "clientRegisterView": "register-client",
    "coachRegisterView": "register-coach",
    "coachesView": "coaches",
    "clientsView": "clients",
    "slotsView": "slots",
    "clientPanelView": "client-panel",
    "coachPanelView": "coach-panel",
    "accountView": "account",
    "responseView": "debug"
};

/* =========================
   BASIC HELPERS
========================= */

function setOutput(data) {
    const output = document.getElementById("output");
    if (!output) return;

    output.textContent = JSON.stringify(data, null, 2);
}

function setLoggedUserText() {
    const text = document.getElementById("loggedUserText");
    if (!text) return;

    if (!currentUsername || !currentRole) {
        text.textContent = "Nie zalogowano";
        return;
    }

    text.textContent = `Zalogowano: ${currentUsername} (${currentRole})`;
}

function getInputValue(id) {
    const input = document.getElementById(id);
    if (!input) return "";

    return input.value.trim();
}

function getRawValue(id) {
    const input = document.getElementById(id);
    if (!input) return "";

    return input.value;
}

function getNumberValue(id) {
    const value = getInputValue(id);

    if (value === "") return null;

    const number = Number(value);

    if (Number.isNaN(number)) return null;

    return number;
}

function getDateTimeValue(id) {
    const value = getInputValue(id);

    if (!value) return "";

    return value;
}

function clearInputs(ids) {
    ids.forEach(id => {
        const element = document.getElementById(id);

        if (element) {
            element.value = "";
        }
    });
}

function clearContainer(containerId, emptyMessage) {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.innerHTML = `<p class="empty-text">${emptyMessage}</p>`;
}

function formatDateTime(value) {
    if (!value) return "-";

    return value.replace("T", " ").slice(0, 16);
}

function statusClass(status) {
    if (status === "AVAILABLE") return "status status-available";
    if (status === "BOOKED") return "status status-booked";
    if (status === "CANCELLED") return "status status-cancelled";
    if (status === "COMPLETED") return "status status-completed";
    if (status === "MISSED") return "status status-cancelled";

    return "status";
}

function addParam(params, name, value) {
    if (value === null || value === undefined || value === "") return;

    params.append(name, value);
}

function buildQuery(paramsObject) {
    const params = new URLSearchParams();

    Object.entries(paramsObject).forEach(([key, value]) => {
        addParam(params, key, value);
    });

    const queryString = params.toString();

    if (!queryString) return "";

    return `?${queryString}`;
}

function getAuthOnlyHeaders() {
    return {
        "Authorization": authHeader
    };
}

function getJsonAuthHeaders() {
    return {
        "Authorization": authHeader,
        "Content-Type": "application/json"
    };
}

async function parseResponse(response) {
    const text = await response.text();

    if (!text) {
        return {
            status: response.status,
            message: response.ok ? "Success" : "Empty response"
        };
    }

    try {
        return JSON.parse(text);
    } catch {
        return {
            status: response.status,
            message: text
        };
    }
}

async function request(url, options = {}) {
    try {
        const response = await fetch(API_URL + url, options);
        const data = await parseResponse(response);

        setOutput({
            request: url,
            status: response.status,
            response: data
        });

        function showToast(message, type = "info") {
            const toast = document.getElementById("toast");
            if (!toast) return;

            toast.textContent = message;
            toast.className = `toast toast-${type}`;

            setTimeout(() => {
                toast.classList.add("hidden");
            }, 3500);
        }

        if (!response.ok) {
            const message = data.message || data.error || `Błąd ${response.status}`;

            showToast(message, "error");

            console.warn("Request failed:", response.status, data);
        }

        return {
            ok: response.ok,
            status: response.status,
            data
        };
    } catch (error) {
        const errorData = {
            request: url,
            message: "Nie udało się połączyć z backendem",
            details: error.message
        };

        setOutput(errorData);

        showToast("Nie udało się połączyć z backendem", "error");

        return {
            ok: false,
            status: 0,
            data: errorData
        };
    }
}

function validateRegisterBody(body) {
    if (!body.username || !body.email || !body.password || !body.firstName || !body.lastName) {
        return "Uzupełnij wszystkie pola tekstowe.";
    }

    if (body.age === null || Number.isNaN(body.age)) {
        return "Podaj poprawny wiek.";
    }

    if (body.experience === null || Number.isNaN(body.experience)) {
        return "Podaj poprawne doświadczenie.";
    }

    return null;
}

/* =========================
   ROUTING / NAVIGATION
========================= */

function showView(viewId, hash = null) {
    const protectedClientView = viewId === "clientPanelView";
    const protectedCoachView = viewId === "coachPanelView";
    const protectedAccountView = viewId === "accountView";
    const protectedClientsView = viewId === "clientsView";

    if (protectedClientView && currentRole !== "CLIENT") {
        setOutput({ message: "Ten widok jest dostępny tylko dla klienta." });
        viewId = "loginView";
        hash = "login";
    }

    if (protectedCoachView && currentRole !== "COACH") {
        setOutput({ message: "Ten widok jest dostępny tylko dla trenera." });
        viewId = "loginView";
        hash = "login";
    }

    if (protectedClientsView && currentRole !== "COACH") {
        setOutput({ message: "Widok klientów jest dostępny tylko dla trenera." });
        viewId = "loginView";
        hash = "login";
    }

    if (protectedAccountView && !currentRole) {
        setOutput({ message: "Musisz być zalogowany, żeby wejść w ustawienia konta." });
        viewId = "loginView";
        hash = "login";
    }

    const views = document.querySelectorAll(".view");
    views.forEach(view => view.classList.remove("active-view"));

    const selectedView = document.getElementById(viewId);

    if (selectedView) {
        selectedView.classList.add("active-view");
    }

    const navButtons = document.querySelectorAll(".nav-button");
    navButtons.forEach(button => button.classList.remove("active-nav"));

    navButtons.forEach(button => {
        if (button.dataset.view === viewId) {
            button.classList.add("active-nav");
        }
    });

    const finalHash = hash || viewToHash[viewId] || "home";

    if (window.location.hash !== `#${finalHash}`) {
        history.pushState(null, "", `#${finalHash}`);
    }

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}

function handleRouteFromHash() {
    const hash = window.location.hash.replace("#", "") || "home";
    const viewId = routes[hash] || "homeView";

    showView(viewId, hash);

    if (viewId === "coachesView") {
        loadCoaches();
    }

    if (viewId === "clientsView" && currentRole === "COACH") {
        loadClients();
    }

    if (viewId === "slotsView") {
        loadAvailableSlots();
    }

    if (viewId === "clientPanelView" && currentRole === "CLIENT") {
        loadClientReservations();
        loadMyAccount(false);
    }

    if (viewId === "coachPanelView" && currentRole === "COACH") {
        loadCoachSlots();
        loadCoachReservations();
        loadMyAccount(false);
    }

    if (viewId === "accountView" && currentRole) {
        loadMyAccount();
    }
}

function updateNavigationByRole() {
    const clientNavButton = document.getElementById("clientNavButton");
    const coachNavButton = document.getElementById("coachNavButton");
    const accountNavButton = document.getElementById("accountNavButton");
    const clientsNavButton = document.getElementById("clientsNavButton");
    const logoutButton = document.getElementById("logoutButton");

    const guestButtons = document.querySelectorAll(".guest-only");
    const clientPublicButtons = document.querySelectorAll(".client-public-only");

    if (clientNavButton) clientNavButton.classList.add("hidden");
    if (coachNavButton) coachNavButton.classList.add("hidden");
    if (accountNavButton) accountNavButton.classList.add("hidden");
    if (clientsNavButton) clientsNavButton.classList.add("hidden");
    if (logoutButton) logoutButton.classList.add("hidden");

    guestButtons.forEach(button => button.classList.remove("hidden"));
    clientPublicButtons.forEach(button => button.classList.remove("hidden"));

    if (!currentRole) {
        return;
    }

    guestButtons.forEach(button => button.classList.add("hidden"));

    if (accountNavButton) accountNavButton.classList.remove("hidden");
    if (logoutButton) logoutButton.classList.remove("hidden");

    if (currentRole === "CLIENT") {
        if (clientNavButton) clientNavButton.classList.remove("hidden");
        clientPublicButtons.forEach(button => button.classList.remove("hidden"));
    }

    if (currentRole === "COACH") {
        if (coachNavButton) coachNavButton.classList.remove("hidden");
        if (clientsNavButton) clientsNavButton.classList.remove("hidden");

        clientPublicButtons.forEach(button => button.classList.add("hidden"));
    }
}

function logout() {
    authHeader = null;
    currentUsername = null;
    currentRole = null;
    currentUserData = null;

    setLoggedUserText();
    updateNavigationByRole();

    clearContainer("clientReservations", "Zaloguj się jako CLIENT.");
    clearContainer("coachSlots", "Zaloguj się jako COACH.");
    clearContainer("coachReservations", "Zaloguj się jako COACH.");
    clearContainer("clientsList", "Zaloguj się jako COACH, żeby zobaczyć klientów.");
    clearContainer("clientAccountSummary", "Zaloguj się, aby zobaczyć dane.");
    clearContainer("coachAccountSummary", "Zaloguj się, aby zobaczyć dane.");
    clearContainer("accountDetails", "Zaloguj się, aby zobaczyć dane konta.");

    setOutput({
        message: "Wylogowano"
    });

    showView("homeView", "home");
}

/* =========================
   AUTH
========================= */

async function login() {
    const username = getInputValue("loginUsername");
    const password = getRawValue("loginPassword");
    const role = getInputValue("loginRole");

    if (!username || !password) {
        setOutput({
            message: "Podaj username i password."
        });
        return;
    }

    const tempAuthHeader = "Basic " + btoa(username + ":" + password);

    const endpoint = role === "CLIENT"
        ? "/api/users/clients/me"
        : "/api/users/coaches/me";

    const result = await request(endpoint, {
        method: "GET",
        headers: {
            "Authorization": tempAuthHeader
        }
    });

    if (!result.ok) {
        authHeader = null;
        currentUsername = null;
        currentRole = null;
        currentUserData = null;

        setLoggedUserText();
        updateNavigationByRole();

        return;
    }

    authHeader = tempAuthHeader;
    currentUsername = result.data.username || username;
    currentRole = role;
    currentUserData = result.data;

    setLoggedUserText();
    updateNavigationByRole();
    renderAccountSummaries(result.data);

    if (role === "CLIENT") {
        await loadAvailableSlots();
        await loadClientReservations();
        showView("clientPanelView", "client-panel");
        return;
    }

    if (role === "COACH") {
        await loadCoachSlots();
        await loadCoachReservations();
        await loadClients();
        showView("coachPanelView", "coach-panel");
    }
}

async function registerClient() {
    const body = {
        username: getInputValue("clientUsername"),
        email: getInputValue("clientEmail"),
        password: getRawValue("clientPassword"),
        firstName: getInputValue("clientFirstName"),
        lastName: getInputValue("clientLastName"),
        age: getNumberValue("clientAge"),
        experience: getNumberValue("clientExperience")
    };

    const validationError = validateRegisterBody(body);

    if (validationError) {
        setOutput({ message: validationError });
        return;
    }

    const result = await request("/api/users/clients", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
    });

    if (result.ok) {
        document.getElementById("loginUsername").value = body.username;
        document.getElementById("loginPassword").value = body.password;
        document.getElementById("loginRole").value = "CLIENT";

        setOutput({
            message: "Klient został zarejestrowany. Dane logowania zostały wpisane automatycznie.",
            user: result.data
        });

        showView("loginView", "login");
    }
}

async function registerCoach() {
    const body = {
        username: getInputValue("coachUsername"),
        email: getInputValue("coachEmail"),
        password: getRawValue("coachPassword"),
        firstName: getInputValue("coachFirstName"),
        lastName: getInputValue("coachLastName"),
        age: getNumberValue("coachAge"),
        experience: getNumberValue("coachExperience")
    };

    const validationError = validateRegisterBody(body);

    if (validationError) {
        setOutput({ message: validationError });
        return;
    }

    const result = await request("/api/users/coaches", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
    });

    if (result.ok) {
        document.getElementById("loginUsername").value = body.username;
        document.getElementById("loginPassword").value = body.password;
        document.getElementById("loginRole").value = "COACH";

        setOutput({
            message: "Trener został zarejestrowany. Dane logowania zostały wpisane automatycznie.",
            user: result.data
        });

        showView("loginView", "login");
    }
}

/* =========================
   ACCOUNT
========================= */

async function loadMyAccount(showOutputAfterLoad = true) {
    if (!authHeader || !currentRole) {
        clearContainer("accountDetails", "Zaloguj się, aby zobaczyć dane konta.");
        return;
    }

    const endpoint = currentRole === "CLIENT"
        ? "/api/users/clients/me"
        : "/api/users/coaches/me";

    const result = await request(endpoint, {
        method: "GET",
        headers: getAuthOnlyHeaders()
    });

    if (!result.ok) {
        return;
    }

    currentUserData = result.data;
    currentUsername = result.data.username;

    setLoggedUserText();
    renderAccountDetails(result.data);
    renderAccountSummaries(result.data);

    const coachBox = document.getElementById("accountCoachSpecialisationBox");

    if (coachBox) {
        if (currentRole === "COACH") {
            coachBox.classList.remove("hidden");
        } else {
            coachBox.classList.add("hidden");
        }
    }

    if (!showOutputAfterLoad) {
        setOutput({
            message: "Dane konta odświeżone w tle."
        });
    }
}

function renderAccountDetails(user) {
    const container = document.getElementById("accountDetails");
    if (!container) return;

    container.innerHTML = `
        <div class="profile-big">
            <div class="profile-avatar">${getInitials(user.firstName, user.lastName)}</div>
            <div>
                <h3>${user.firstName} ${user.lastName}</h3>
                <p>${user.username} • ${currentRole}</p>
            </div>
        </div>

        <div class="account-data-grid">
            <div><span>User ID</span><strong>${user.userId ?? "-"}</strong></div>
            <div><span>Username</span><strong>${user.username ?? "-"}</strong></div>
            <div><span>Email</span><strong>${user.email ?? "-"}</strong></div>
            <div><span>Wiek</span><strong>${user.age ?? "-"}</strong></div>
            <div><span>Doświadczenie</span><strong>${user.experience ?? "-"} lat</strong></div>
            <div><span>Specjalizacja</span><strong>${user.specialisation ?? "brak"}</strong></div>
            <div class="wide-data"><span>Bio</span><strong>${user.bio ?? "brak"}</strong></div>
        </div>
    `;
}

function renderAccountSummaries(user) {
    const clientSummary = document.getElementById("clientAccountSummary");
    const coachSummary = document.getElementById("coachAccountSummary");

    const html = `
        <div class="profile-small">
            <div class="profile-avatar small-avatar">${getInitials(user.firstName, user.lastName)}</div>
            <div>
                <strong>${user.firstName} ${user.lastName}</strong>
                <span>${user.username}</span>
            </div>
        </div>

        <div class="mini-list">
            <div>Email: ${user.email ?? "-"}</div>
            <div>Wiek: ${user.age ?? "-"}</div>
            <div>Doświadczenie: ${user.experience ?? "-"} lat</div>
            <div>Specjalizacja: ${user.specialisation ?? "brak"}</div>
        </div>
    `;

    if (currentRole === "CLIENT" && clientSummary) {
        clientSummary.innerHTML = html;
    }

    if (currentRole === "COACH" && coachSummary) {
        coachSummary.innerHTML = html;
    }
}

function getInitials(firstName, lastName) {
    const first = firstName ? firstName.charAt(0).toUpperCase() : "";
    const last = lastName ? lastName.charAt(0).toUpperCase() : "";

    return `${first}${last}` || "ML";
}

function rebuildAuthAfterUsernameChange(newUsername) {
    const password = getRawValue("loginPassword");

    if (!password) {
        currentUsername = newUsername;
        authHeader = null;
        currentRole = null;
        currentUserData = null;

        setLoggedUserText();
        updateNavigationByRole();

        setOutput({
            message: "Username zmieniony. Zaloguj się ponownie nowym username, bo Basic Auth wymaga nowych danych."
        });

        showView("loginView", "login");
        return;
    }

    currentUsername = newUsername;
    authHeader = "Basic " + btoa(newUsername + ":" + password);

    setLoggedUserText();
    updateNavigationByRole();
}

async function updateUsername() {
    const newUsername = getInputValue("accountNewUsername");

    if (!newUsername) {
        setOutput({ message: "Podaj nowy username." });
        return;
    }

    const result = await request(`/api/users/username?newUsername=${encodeURIComponent(newUsername)}`, {
        method: "PUT",
        headers: getAuthOnlyHeaders()
    });

    if (result.ok) {
        document.getElementById("loginUsername").value = newUsername;
        rebuildAuthAfterUsernameChange(result.data.username || newUsername);
        clearInputs(["accountNewUsername"]);
        await loadMyAccount();
    }
}

async function updateEmail() {
    const newEmail = getInputValue("accountNewEmail");

    if (!newEmail) {
        setOutput({ message: "Podaj nowy email." });
        return;
    }

    const result = await request(`/api/users/email?newEmail=${encodeURIComponent(newEmail)}`, {
        method: "PUT",
        headers: getAuthOnlyHeaders()
    });

    if (result.ok) {
        clearInputs(["accountNewEmail"]);
        await loadMyAccount();
    }
}

async function updateAge() {
    const age = getNumberValue("accountNewAge");

    if (age === null) {
        setOutput({ message: "Podaj poprawny wiek." });
        return;
    }

    const result = await request(`/api/users/age?age=${encodeURIComponent(age)}`, {
        method: "PUT",
        headers: getAuthOnlyHeaders()
    });

    if (result.ok) {
        clearInputs(["accountNewAge"]);
        await loadMyAccount();
    }
}

async function updateExperience() {
    const experience = getNumberValue("accountNewExperience");

    if (experience === null) {
        setOutput({ message: "Podaj poprawne doświadczenie." });
        return;
    }

    const result = await request(`/api/users/experience?experience=${encodeURIComponent(experience)}`, {
        method: "PUT",
        headers: getAuthOnlyHeaders()
    });

    if (result.ok) {
        clearInputs(["accountNewExperience"]);
        await loadMyAccount();
    }
}

async function updateBio() {
    const bio = getInputValue("accountNewBio");

    if (!bio) {
        setOutput({ message: "Podaj bio." });
        return;
    }

    const result = await request(`/api/users/bio?bio=${encodeURIComponent(bio)}`, {
        method: "PUT",
        headers: getAuthOnlyHeaders()
    });

    if (result.ok) {
        clearInputs(["accountNewBio"]);
        await loadMyAccount();
    }
}

async function updateAccountSpecialisation() {
    const specialisation = getInputValue("accountNewSpecialisation");

    if (!specialisation) {
        setOutput({ message: "Podaj specjalizację." });
        return;
    }

    const result = await request(`/api/users/specialisation?specialisation=${encodeURIComponent(specialisation)}`, {
        method: "PUT",
        headers: getAuthOnlyHeaders()
    });

    if (result.ok) {
        clearInputs(["accountNewSpecialisation"]);
        await loadMyAccount();

        if (currentRole === "COACH") {
            await loadClients();
        }
    }
}

async function updatePassword() {
    const oldPassword = getRawValue("accountOldPassword");
    const newPassword = getRawValue("accountNewPassword");

    if (!oldPassword || !newPassword) {
        setOutput({ message: "Podaj stare i nowe hasło." });
        return;
    }

    const query = `?oldPassword=${encodeURIComponent(oldPassword)}&newPassword=${encodeURIComponent(newPassword)}`;

    const result = await request(`/api/users/password${query}`, {
        method: "PUT",
        headers: getAuthOnlyHeaders()
    });

    if (result.ok) {
        clearInputs(["accountOldPassword", "accountNewPassword"]);

        document.getElementById("loginPassword").value = newPassword;
        authHeader = "Basic " + btoa(currentUsername + ":" + newPassword);

        setOutput({
            message: "Hasło zmienione. Basic Auth został odświeżony w frontendzie.",
            user: result.data
        });

        await loadMyAccount();
    }
}

async function deleteAccount() {
    const password = getRawValue("deleteAccountPassword");

    if (!password) {
        setOutput({ message: "Podaj hasło, aby usunąć konto." });
        return;
    }

    const confirmed = confirm("Na pewno chcesz usunąć konto? Tej operacji nie da się cofnąć w aktualnej bazie.");

    if (!confirmed) {
        return;
    }

    const result = await request(`/api/users/delete?password=${encodeURIComponent(password)}`, {
        method: "DELETE",
        headers: getAuthOnlyHeaders()
    });

    if (result.ok) {
        logout();

        setOutput({
            message: "Konto zostało usunięte."
        });
    }
}

/* =========================
   COACHES VIEW FOR CLIENT/GUEST
========================= */

function buildCoachFiltersQuery() {
    return buildQuery({
        username: getInputValue("coachFilterUsername"),
        firstName: getInputValue("coachFilterFirstName"),
        lastName: getInputValue("coachFilterLastName"),
        specialisation: getInputValue("coachFilterSpecialisation"),
        ageMin: getNumberValue("coachFilterAgeMin"),
        ageMax: getNumberValue("coachFilterAgeMax"),
        experienceMin: getNumberValue("coachFilterExperienceMin"),
        experienceMax: getNumberValue("coachFilterExperienceMax")
    });
}

function clearCoachFilters() {
    clearInputs([
        "coachFilterUsername",
        "coachFilterFirstName",
        "coachFilterLastName",
        "coachFilterSpecialisation",
        "coachFilterAgeMin",
        "coachFilterAgeMax",
        "coachFilterExperienceMin",
        "coachFilterExperienceMax"
    ]);
}

async function loadCoaches() {
    const query = buildCoachFiltersQuery();

    const result = await request(`/api/users/coaches${query}`, {
        method: "GET"
    });

    const container = document.getElementById("coachesList");

    if (!container) return;

    container.innerHTML = "";

    if (!result.ok || !result.data.content || result.data.content.length === 0) {
        clearContainer("coachesList", "Brak trenerów dla podanych filtrów.");
        return;
    }

    result.data.content.forEach(coach => {
        const item = document.createElement("div");
        item.className = "item";

        item.innerHTML = `
            <div class="item-title">${coach.firstName} ${coach.lastName}</div>
            <div class="item-row">Username: ${coach.username}</div>
            <div class="item-row">Email: ${coach.email}</div>
            <div class="item-row">Wiek: ${coach.age}</div>
            <div class="item-row">Doświadczenie: ${coach.experience} lat</div>
            <div class="item-row">Specjalizacja: ${coach.specialisation ?? "brak"}</div>
            <div class="item-row">Bio: ${coach.bio ?? "brak"}</div>

            <div class="item-actions">
                <button onclick="showCoachSlots('${coach.username}')">Pokaż terminy</button>
            </div>
        `;

        container.appendChild(item);
    });
}

async function showCoachSlots(coachUsername) {
    clearSlotFilters();

    const input = document.getElementById("slotFilterCoachUsername");

    if (input) {
        input.value = coachUsername;
    }

    showView("slotsView", "slots");
    await loadAvailableSlots();
}

/* =========================
   CLIENTS VIEW FOR COACH
========================= */

function buildClientFiltersQuery() {
    return buildQuery({
        username: getInputValue("clientFilterUsername"),
        firstName: getInputValue("clientFilterFirstName"),
        lastName: getInputValue("clientFilterLastName"),
        ageMin: getNumberValue("clientFilterAgeMin"),
        ageMax: getNumberValue("clientFilterAgeMax"),
        experienceMin: getNumberValue("clientFilterExperienceMin"),
        experienceMax: getNumberValue("clientFilterExperienceMax")
    });
}

function clearClientFilters() {
    clearInputs([
        "clientFilterUsername",
        "clientFilterFirstName",
        "clientFilterLastName",
        "clientFilterAgeMin",
        "clientFilterAgeMax",
        "clientFilterExperienceMin",
        "clientFilterExperienceMax"
    ]);
}

async function loadClients() {
    if (!authHeader || currentRole !== "COACH") {
        clearContainer("clientsList", "Zaloguj się jako COACH, żeby zobaczyć klientów.");
        return;
    }

    const query = buildClientFiltersQuery();

    const result = await request(`/api/users/clients${query}`, {
        method: "GET",
        headers: getAuthOnlyHeaders()
    });

    const container = document.getElementById("clientsList");

    if (!container) return;

    container.innerHTML = "";

    if (!result.ok || !result.data.content || result.data.content.length === 0) {
        clearContainer("clientsList", "Brak klientów dla podanych filtrów.");
        return;
    }

    result.data.content.forEach(client => {
        const item = document.createElement("div");
        item.className = "item";

        item.innerHTML = `
            <div class="item-title">${client.firstName} ${client.lastName}</div>
            <div class="item-row">Username: ${client.username}</div>
            <div class="item-row">Email: ${client.email}</div>
            <div class="item-row">Wiek: ${client.age}</div>
            <div class="item-row">Doświadczenie: ${client.experience} lat</div>
            <div class="item-row">Bio: ${client.bio ?? "brak"}</div>

            <div class="item-actions">
                <button onclick="openCoachReservationsForClient('${client.username}')">
                    Pokaż rezerwacje klienta
                </button>
            </div>
        `;

        container.appendChild(item);
    });
}

async function openCoachReservationsForClient(clientUsername) {
    showView("coachPanelView", "coach-panel");

    const input = document.getElementById("coachResFilterClientUsername");

    if (input) {
        input.value = clientUsername;
    }

    await loadCoachReservations();
}

/* =========================
   AVAILABLE SLOTS
========================= */

function buildSlotFiltersQuery() {
    return buildQuery({
        coachUsername: getInputValue("slotFilterCoachUsername"),
        coachSpecialisation: getInputValue("slotFilterSpecialisation"),
        minAge: getNumberValue("slotFilterMinAge"),
        maxAge: getNumberValue("slotFilterMaxAge"),
        minExperience: getNumberValue("slotFilterMinExperience"),
        maxExperience: getNumberValue("slotFilterMaxExperience"),
        startFrom: getDateTimeValue("slotFilterStartFrom"),
        startTo: getDateTimeValue("slotFilterStartTo")
    });
}

function clearSlotFilters() {
    clearInputs([
        "slotFilterCoachUsername",
        "slotFilterSpecialisation",
        "slotFilterMinAge",
        "slotFilterMaxAge",
        "slotFilterMinExperience",
        "slotFilterMaxExperience",
        "slotFilterStartFrom",
        "slotFilterStartTo"
    ]);
}

async function loadAvailableSlots() {
    const query = buildSlotFiltersQuery();

    const result = await request(`/api/training-slots/client${query}`, {
        method: "GET"
    });

    const container = document.getElementById("availableSlots");

    if (!container) return;

    container.innerHTML = "";

    if (!result.ok || !result.data.content || result.data.content.length === 0) {
        clearContainer("availableSlots", "Brak dostępnych treningów dla podanych filtrów.");
        return;
    }

    result.data.content.forEach(slot => {
        const item = document.createElement("div");
        item.className = "item";

        const reservationButton = currentRole === "CLIENT"
            ? `<button onclick="makeReservation('${slot.slotCode}')">Zarezerwuj</button>`
            : `<button class="secondary-button" onclick="showView('loginView', 'login')">Zaloguj jako klient</button>`;

        item.innerHTML = `
            <div class="item-title">${slot.coachFirstName} (${slot.coachUsername})</div>
            <div class="item-row">Kod slotu: ${slot.slotCode}</div>
            <div class="item-row">Specjalizacja: ${slot.coachSpecialisation ?? "brak"}</div>
            <div class="item-row">Email: ${slot.coachEmail}</div>
            <div class="item-row">Start: ${formatDateTime(slot.startTime)}</div>
            <div class="item-row">Koniec: ${formatDateTime(slot.endTime)}</div>
            <span class="${statusClass(slot.status)}">${slot.status}</span>

            <div class="item-actions">
                ${reservationButton}
            </div>
        `;

        container.appendChild(item);
    });
}

async function makeReservation(slotCode) {
    if (!authHeader || currentRole !== "CLIENT") {
        setOutput({
            message: "Musisz być zalogowany jako CLIENT, żeby zrobić rezerwację."
        });

        showView("loginView", "login");
        return;
    }

    const result = await request("/api/reservations", {
        method: "POST",
        headers: getJsonAuthHeaders(),
        body: JSON.stringify({
            trainingSlotCode: slotCode
        })
    });

    if (result.ok) {
        await loadAvailableSlots();
        await loadClientReservations();
        showView("clientPanelView", "client-panel");
    }
}

/* =========================
   CLIENT RESERVATIONS
========================= */

function buildClientReservationFiltersQuery() {
    return buildQuery({
        trainingSlotCode: getInputValue("clientResFilterTrainingSlotCode"),
        coachUsername: getInputValue("clientResFilterCoachUsername"),
        coachExperienceFrom: getNumberValue("clientResFilterCoachExperienceFrom"),
        reservationStatus: getInputValue("clientResFilterStatus"),
        trainingStartFrom: getDateTimeValue("clientResFilterTrainingStartFrom"),
        trainingStartTo: getDateTimeValue("clientResFilterTrainingStartTo"),
        createdAtFrom: getDateTimeValue("clientResFilterCreatedAtFrom"),
        createdAtTo: getDateTimeValue("clientResFilterCreatedAtTo")
    });
}

function clearClientReservationFilters() {
    clearInputs([
        "clientResFilterTrainingSlotCode",
        "clientResFilterCoachUsername",
        "clientResFilterCoachExperienceFrom",
        "clientResFilterStatus",
        "clientResFilterTrainingStartFrom",
        "clientResFilterTrainingStartTo",
        "clientResFilterCreatedAtFrom",
        "clientResFilterCreatedAtTo"
    ]);
}

async function loadClientReservations() {
    if (!authHeader || currentRole !== "CLIENT") {
        clearContainer("clientReservations", "Zaloguj się jako CLIENT.");
        return;
    }

    const query = buildClientReservationFiltersQuery();

    const result = await request(`/api/reservations/client${query}`, {
        method: "GET",
        headers: getAuthOnlyHeaders()
    });

    const container = document.getElementById("clientReservations");

    if (!container) return;

    container.innerHTML = "";

    if (!result.ok || !result.data.content || result.data.content.length === 0) {
        clearContainer("clientReservations", "Brak rezerwacji dla podanych filtrów.");
        return;
    }

    result.data.content.forEach(reservation => {
        const item = document.createElement("div");
        item.className = "item";

        const cancelButton = reservation.status === "BOOKED"
            ? `<button class="danger-button" onclick="cancelClientReservation('${reservation.reservationCode}')">Anuluj rezerwację</button>`
            : "";

        item.innerHTML = `
            <div class="item-title">${reservation.reservationTrainingSlotCoachFirstName}</div>
            <div class="item-row">Kod rezerwacji: ${reservation.reservationCode}</div>
            <div class="item-row">Kod slotu: ${reservation.reservationTrainingSlotCode}</div>
            <div class="item-row">Trener: ${reservation.reservationTrainingSlotCoachUsername}</div>
            <div class="item-row">Email trenera: ${reservation.reservationTrainingSlotCoachEmail}</div>
            <div class="item-row">Specjalizacja: ${reservation.reservationTrainingSlotCoachSpecialisation ?? "brak"}</div>
            <div class="item-row">Doświadczenie trenera: ${reservation.reservationTrainingSlotCoachExperience}</div>
            <div class="item-row">Start: ${formatDateTime(reservation.reservationTrainingSlotStartTime)}</div>
            <div class="item-row">Koniec: ${formatDateTime(reservation.reservationTrainingSlotEndTime)}</div>
            <div class="item-row">Utworzono: ${formatDateTime(reservation.createdAt)}</div>
            <span class="${statusClass(reservation.status)}">${reservation.status}</span>

            <div class="item-actions">
                ${cancelButton}
            </div>
        `;

        container.appendChild(item);
    });
}

async function cancelClientReservation(reservationCode) {
    if (!authHeader || currentRole !== "CLIENT") {
        setOutput({
            message: "Musisz być zalogowany jako CLIENT."
        });
        return;
    }

    const result = await request(`/api/reservations/client/cancel?reservationCode=${encodeURIComponent(reservationCode)}`, {
        method: "PUT",
        headers: getAuthOnlyHeaders()
    });

    if (result.ok) {
        await loadClientReservations();
        await loadAvailableSlots();
    }
}

/* =========================
   COACH PANEL
========================= */

async function createTrainingSlot() {
    if (!authHeader || currentRole !== "COACH") {
        setOutput({
            message: "Musisz być zalogowany jako COACH, żeby dodać slot."
        });
        return;
    }

    const startTime = getDateTimeValue("slotStartTime");
    const endTime = getDateTimeValue("slotEndTime");

    if (!startTime || !endTime) {
        setOutput({
            message: "Podaj start i koniec treningu."
        });
        return;
    }

    const result = await request("/api/training-slots", {
        method: "POST",
        headers: getJsonAuthHeaders(),
        body: JSON.stringify({
            startTime,
            endTime
        })
    });

    if (result.ok) {
        clearInputs(["slotStartTime", "slotEndTime"]);

        await loadCoachSlots();
        showView("coachPanelView", "coach-panel");
    }
}

async function updateSpecialisation() {
    if (!authHeader || currentRole !== "COACH") {
        setOutput({
            message: "Musisz być zalogowany jako COACH."
        });
        return;
    }

    const specialisation = getInputValue("specialisationInput");

    if (!specialisation) {
        setOutput({
            message: "Podaj specjalizację."
        });
        return;
    }

    const result = await request(`/api/users/specialisation?specialisation=${encodeURIComponent(specialisation)}`, {
        method: "PUT",
        headers: getAuthOnlyHeaders()
    });

    if (result.ok) {
        clearInputs(["specialisationInput"]);
        await loadMyAccount(false);
    }
}

function buildCoachSlotFiltersQuery() {
    return buildQuery({
        status: getInputValue("coachSlotFilterStatus"),
        startFrom: getDateTimeValue("coachSlotFilterStartFrom"),
        startTo: getDateTimeValue("coachSlotFilterStartTo")
    });
}

function clearCoachSlotFilters() {
    clearInputs([
        "coachSlotFilterStatus",
        "coachSlotFilterStartFrom",
        "coachSlotFilterStartTo"
    ]);
}

async function loadCoachSlots() {
    if (!authHeader || currentRole !== "COACH") {
        clearContainer("coachSlots", "Zaloguj się jako COACH.");
        return;
    }

    const query = buildCoachSlotFiltersQuery();

    const result = await request(`/api/training-slots/coach${query}`, {
        method: "GET",
        headers: getAuthOnlyHeaders()
    });

    const container = document.getElementById("coachSlots");

    if (!container) return;

    container.innerHTML = "";

    if (!result.ok || !result.data.content || result.data.content.length === 0) {
        clearContainer("coachSlots", "Brak slotów dla podanych filtrów.");
        return;
    }

    result.data.content.forEach(slot => {
        const item = document.createElement("div");
        item.className = "item";

        const editButton = slot.status === "AVAILABLE" || slot.status === "BOOKED"
            ? `<button class="secondary-button" onclick="toggleSlotEdit('${slot.slotCode}')">Edytuj godzinę</button>`
            : "";

        const cancelAvailableSlotButton = slot.status === "AVAILABLE"
            ? `<button class="danger-button" onclick="cancelCoachSlot('${slot.slotCode}')">Anuluj wolny slot</button>`
            : "";

        const cancelBookedSlotButton = slot.status === "BOOKED"
            ? `<button class="danger-button" onclick="cancelReservationBySlotCode('${slot.slotCode}')">Anuluj rezerwację i zablokuj slot</button>`
            : "";

        item.innerHTML = `
            <div class="item-title">${slot.slotCode}</div>
            <div class="item-row">Trener: ${slot.coachUsername}</div>
            <div class="item-row">Start: ${formatDateTime(slot.startTime)}</div>
            <div class="item-row">Koniec: ${formatDateTime(slot.endTime)}</div>
            <span class="${statusClass(slot.status)}">${slot.status}</span>

            <div class="item-actions">
                ${editButton}
                ${cancelAvailableSlotButton}
                ${cancelBookedSlotButton}
            </div>

            <div id="edit-${slot.slotCode}" class="slot-edit-box hidden">
                <label>Nowy start</label>
                <input id="edit-start-${slot.slotCode}" type="datetime-local">

                <label>Nowy koniec</label>
                <input id="edit-end-${slot.slotCode}" type="datetime-local">

                <button onclick="updateTrainingSlotTime('${slot.slotCode}')">Zapisz zmianę</button>
            </div>
        `;

        container.appendChild(item);
    });
}

function toggleSlotEdit(slotCode) {
    const box = document.getElementById(`edit-${slotCode}`);
    if (!box) return;

    box.classList.toggle("hidden");
}

async function updateTrainingSlotTime(slotCode) {
    const startTime = getDateTimeValue(`edit-start-${slotCode}`);
    const endTime = getDateTimeValue(`edit-end-${slotCode}`);

    if (!startTime || !endTime) {
        setOutput({
            message: "Podaj nowy start i nowy koniec."
        });
        return;
    }

    const query = `?slotCode=${encodeURIComponent(slotCode)}&startTime=${encodeURIComponent(startTime)}&endTime=${encodeURIComponent(endTime)}`;

    const result = await request(`/api/training-slots/date${query}`, {
        method: "PUT",
        headers: getAuthOnlyHeaders()
    });

    if (result.ok) {
        await loadCoachSlots();
    }
}

async function cancelCoachSlot(slotCode) {
    if (!authHeader || currentRole !== "COACH") {
        setOutput({
            message: "Musisz być zalogowany jako COACH."
        });
        return;
    }

    const confirmed = confirm(`Na pewno chcesz anulować wolny slot ${slotCode}?`);

    if (!confirmed) {
        return;
    }

    const result = await request(`/api/training-slots/cancel?slotCode=${encodeURIComponent(slotCode)}`, {
        method: "PUT",
        headers: getAuthOnlyHeaders()
    });

    if (result.ok) {
        await loadCoachSlots();
        await loadCoachReservations();
    }
}

async function cancelReservationBySlotCode(slotCode) {
    if (!authHeader || currentRole !== "COACH") {
        setOutput({
            message: "Musisz być zalogowany jako COACH."
        });
        return;
    }

    const result = await request(`/api/reservations/coach?trainingSlotCode=${encodeURIComponent(slotCode)}&reservationStatus=BOOKED`, {
        method: "GET",
        headers: getAuthOnlyHeaders()
    });

    if (!result.ok || !result.data.content || result.data.content.length === 0) {
        setOutput({
            message: "Nie znaleziono aktywnej rezerwacji dla tego slota.",
            slotCode: slotCode
        });
        return;
    }

    const reservation = result.data.content[0];

    const confirmed = confirm(
        `Na pewno chcesz anulować rezerwację ${reservation.reservationCode}? Slot zostanie ustawiony jako CANCELLED.`
    );

    if (!confirmed) {
        return;
    }

    await cancelCoachReservation(reservation.reservationCode);
}

function buildCoachReservationFiltersQuery() {
    return buildQuery({
        clientUsername: getInputValue("coachResFilterClientUsername"),
        trainingSlotCode: getInputValue("coachResFilterTrainingSlotCode"),
        clientAgeFrom: getNumberValue("coachResFilterClientAgeFrom"),
        clientAgeTo: getNumberValue("coachResFilterClientAgeTo"),
        clientExperienceFrom: getNumberValue("coachResFilterClientExperienceFrom"),
        clientExperienceTo: getNumberValue("coachResFilterClientExperienceTo"),
        reservationStatus: getInputValue("coachResFilterStatus"),
        startFrom: getDateTimeValue("coachResFilterStartFrom"),
        startTo: getDateTimeValue("coachResFilterStartTo"),
        createdAtFrom: getDateTimeValue("coachResFilterCreatedAtFrom"),
        createdAtTo: getDateTimeValue("coachResFilterCreatedAtTo")
    });
}

function clearCoachReservationFilters() {
    clearInputs([
        "coachResFilterClientUsername",
        "coachResFilterTrainingSlotCode",
        "coachResFilterClientAgeFrom",
        "coachResFilterClientAgeTo",
        "coachResFilterClientExperienceFrom",
        "coachResFilterClientExperienceTo",
        "coachResFilterStatus",
        "coachResFilterStartFrom",
        "coachResFilterStartTo",
        "coachResFilterCreatedAtFrom",
        "coachResFilterCreatedAtTo"
    ]);
}

async function loadCoachReservations() {
    if (!authHeader || currentRole !== "COACH") {
        clearContainer("coachReservations", "Zaloguj się jako COACH.");
        return;
    }

    const query = buildCoachReservationFiltersQuery();

    const result = await request(`/api/reservations/coach${query}`, {
        method: "GET",
        headers: getAuthOnlyHeaders()
    });

    const container = document.getElementById("coachReservations");

    if (!container) return;

    container.innerHTML = "";

    if (!result.ok || !result.data.content || result.data.content.length === 0) {
        clearContainer("coachReservations", "Brak rezerwacji dla podanych filtrów.");
        return;
    }

    result.data.content.forEach(reservation => {
        const item = document.createElement("div");
        item.className = "item";

        const actions = reservation.status === "BOOKED"
            ? `
                <button class="success-button" onclick="completeReservation('${reservation.reservationCode}')">
                    Oznacz completed
                </button>
                <button class="danger-button" onclick="cancelCoachReservation('${reservation.reservationCode}')">
                    Anuluj jako trener
                </button>
              `
            : "";

        item.innerHTML = `
            <div class="item-title">Klient: ${reservation.reservationClientFirstName}</div>
            <div class="item-row">Kod rezerwacji: ${reservation.reservationCode}</div>
            <div class="item-row">Kod slotu: ${reservation.reservationTrainingSlotCode}</div>
            <div class="item-row">Username klienta: ${reservation.reservationClientUsername}</div>
            <div class="item-row">Email klienta: ${reservation.reservationClientEmail}</div>
            <div class="item-row">Doświadczenie klienta: ${reservation.reservationClientExperience}</div>
            <div class="item-row">Start: ${formatDateTime(reservation.reservationTrainingSlotStartTime)}</div>
            <div class="item-row">Koniec: ${formatDateTime(reservation.reservationTrainingSlotEndTime)}</div>
            <div class="item-row">Utworzono: ${formatDateTime(reservation.createdAt)}</div>
            <span class="${statusClass(reservation.status)}">${reservation.status}</span>

            <div class="item-actions">
                ${actions}
            </div>
        `;

        container.appendChild(item);
    });
}

async function completeReservation(reservationCode) {
    if (!authHeader || currentRole !== "COACH") {
        setOutput({
            message: "Musisz być zalogowany jako COACH."
        });
        return;
    }

    const result = await request(`/api/reservations/coach/complete?reservationCode=${encodeURIComponent(reservationCode)}`, {
        method: "PUT",
        headers: getAuthOnlyHeaders()
    });

    if (result.ok) {
        await loadCoachReservations();
        await loadCoachSlots();
    }
}

async function cancelCoachReservation(reservationCode) {
    if (!authHeader || currentRole !== "COACH") {
        setOutput({
            message: "Musisz być zalogowany jako COACH."
        });
        return;
    }

    const result = await request(`/api/reservations/coach/cancel?reservationCode=${encodeURIComponent(reservationCode)}`, {
        method: "PUT",
        headers: getAuthOnlyHeaders()
    });

    if (result.ok) {
        await loadCoachReservations();
        await loadCoachSlots();
    }
}

/* =========================
   INIT
========================= */

window.addEventListener("load", async () => {
    setLoggedUserText();
    updateNavigationByRole();

    if (!window.location.hash) {
        history.replaceState(null, "", "#home");
    }

    handleRouteFromHash();

    await loadAvailableSlots();
});

window.addEventListener("hashchange", () => {
    handleRouteFromHash();
});