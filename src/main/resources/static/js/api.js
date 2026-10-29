function saveAuth(email, password) {
    sessionStorage.setItem('auth', btoa(email + ':' + password));
    sessionStorage.setItem('email', email);
}

function authHeader() {
    const a = sessionStorage.getItem('auth');
    return a ? { 'Authorization': 'Basic ' + a } : {};
}

async function api(path, options = {}) {
    return fetch(path, {
        ...options,
        headers: {
            'X-Requested-With': 'XMLHttpRequest',
            'Content-Type': 'application/json',
            ...authHeader(),
            ...(options.headers || {})
        }
    });
}

function logout() {
    sessionStorage.clear();
    location.href = 'index.html';
}

async function errorText(res, fallback) {
    try {
        const data = await res.json();
        return data.message || fallback;
    } catch (e) {
        return fallback;
    }
}