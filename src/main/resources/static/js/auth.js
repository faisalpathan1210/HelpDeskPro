function showMessage(text, type) {
    document.getElementById('message').innerHTML =
        '<div class="alert alert-' + type + '">' + text + '</div>';
}

document.getElementById('loginBtn').addEventListener('click', async () => {
    const email = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPassword').value;

    const res = await fetch('/api/me', {
        headers: {
            'X-Requested-With': 'XMLHttpRequest',
            'Authorization': 'Basic ' + btoa(email + ':' + password)
        }
    });

    if (res.ok) {
        const me = await res.json();
        saveAuth(email, password);
        sessionStorage.setItem('roles', JSON.stringify(me.roles));
        window.location.href = 'tickets.html';
    } else {
        showMessage('Wrong email or password', 'danger');
    }
});

document.getElementById('registerBtn').addEventListener('click', async () => {
    const body = {
        name: document.getElementById('regName').value.trim(),
        email: document.getElementById('regEmail').value.trim(),
        password: document.getElementById('regPassword').value
    };

    const res = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });

    if (res.status === 201) {
        showMessage('Account created. You can log in now.', 'success');
    } else {
        showMessage('Registration failed. The email may already be used.', 'danger');
    }
});