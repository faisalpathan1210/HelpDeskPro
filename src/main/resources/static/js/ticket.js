if (!sessionStorage.getItem('auth')) {
    location.href = 'index.html';
}

document.getElementById('who').textContent = sessionStorage.getItem('email');
const ticketId = new URLSearchParams(location.search).get('id');

const roles = JSON.parse(sessionStorage.getItem('roles') || '[]');
const myEmail = sessionStorage.getItem('email');
const isAdmin = roles.includes('ROLE_ADMIN');
const isSupport = roles.includes('ROLE_SUPPORT');
const nextStatus = { OPEN: 'IN_PROGRESS', IN_PROGRESS: 'RESOLVED' };

function fmt(dateString) {
    return dateString ? new Date(dateString).toLocaleString() : '';
}

function showError(text) {
    const box = document.getElementById('error');
    box.textContent = text;
    box.classList.remove('d-none');
}

function clearError() {
    document.getElementById('error').classList.add('d-none');
}

async function renderActions(t) {
    const old = document.getElementById('actions');
    if (old) old.remove();

    const box = document.createElement('div');
    box.id = 'actions';
    box.className = 'mt-3 pt-3 border-top';

    const next = nextStatus[t.status];
    const mine = t.assignedTo && t.assignedTo.email === myEmail;

    if (next && (isAdmin || (isSupport && mine))) {
        const btn = document.createElement('button');
        btn.className = 'btn btn-warning btn-sm me-3';
        btn.textContent = 'Move to ' + next.replace('_', ' ');
        btn.onclick = async () => {
            clearError();
            const res = await api('/api/tickets/' + ticketId + '/status?status=' + next, { method: 'PATCH' });
            if (res.ok) { refresh(); }
            else { showError(await errorText(res, 'Could not change the status.')); }
        };
        box.appendChild(btn);
    }

    if (isAdmin && t.status !== 'RESOLVED') {
        const res = await api('/api/users/support');
        if (res.ok) {
            const people = await res.json();
            const select = document.createElement('select');
            select.className = 'form-select form-select-sm d-inline-block w-auto me-2';
            people.forEach(p => {
                const opt = document.createElement('option');
                opt.value = p.id;
                opt.textContent = p.name;
                select.appendChild(opt);
            });
            const assignBtn = document.createElement('button');
            assignBtn.className = 'btn btn-primary btn-sm';
            assignBtn.textContent = 'Assign';
            assignBtn.onclick = async () => {
                clearError();
                const r = await api('/api/tickets/' + ticketId + '/assign?supportId=' + select.value, { method: 'PATCH' });
                if (r.ok) { refresh(); } else { showError(await errorText(r, 'Could not assign the ticket.')); }
            };
            if (people.length > 0) box.append(select, assignBtn);
        }
    }

    if (box.children.length > 0) {
        document.querySelector('#ticket .card-body').appendChild(box);
    }
}

async function loadTicket() {
    const res = await api('/api/tickets/' + ticketId);
    if (res.status === 401) { logout(); return false; }
    if (res.status === 403) { showError('You cannot view this ticket.'); return false; }
    if (!res.ok) { showError('Ticket not found.'); return false; }

    const t = await res.json();
    document.getElementById('tTitle').textContent = t.title;
    document.getElementById('tDesc').textContent = t.description || '';
    document.getElementById('tStatus').textContent = t.status.replace('_', ' ');
    document.getElementById('tPriority').textContent = t.priority;
    document.getElementById('tBy').textContent = t.createdBy ? t.createdBy.name : '-';
    document.getElementById('tTo').textContent = t.assignedTo ? t.assignedTo.name : 'Unassigned';
    document.getElementById('ticket').classList.remove('d-none');
    renderActions(t);
    return true;
}

async function loadComments() {
    const res = await api('/api/tickets/' + ticketId + '/comments');
    if (!res.ok) return;
    const list = await res.json();
    const box = document.getElementById('comments');
    box.innerHTML = '';
    if (list.length === 0) {
        const p = document.createElement('p');
        p.className = 'text-muted';
        p.textContent = 'No comments yet.';
        box.appendChild(p);
    }
    list.forEach(c => {
        const card = document.createElement('div');
        card.className = 'card mb-2';
        const body = document.createElement('div');
        body.className = 'card-body py-2';
        const head = document.createElement('small');
        head.className = 'text-muted';
        head.textContent = (c.user ? c.user.name : '?') + ' - ' + fmt(c.createdAt);
        const msg = document.createElement('div');
        msg.textContent = c.message;
        body.append(head, msg);
        card.appendChild(body);
        box.appendChild(card);
    });
}

async function loadHistory() {
    const res = await api('/api/tickets/' + ticketId + '/history');
    if (!res.ok) return;
    const list = await res.json();
    const ul = document.getElementById('history');
    ul.innerHTML = '';
    if (list.length === 0) {
        const li = document.createElement('li');
        li.className = 'list-group-item text-muted';
        li.textContent = 'No status changes yet.';
        ul.appendChild(li);
    }
    list.forEach(h => {
        const li = document.createElement('li');
        li.className = 'list-group-item';
        li.textContent = h.oldStatus.replace('_', ' ') + ' \u2192 ' + h.newStatus.replace('_', ' ') +
            ' by ' + (h.changedBy ? h.changedBy.name : '?') + ' on ' + fmt(h.changedAt);
        ul.appendChild(li);
    });
}

async function refresh() {
    const ok = await loadTicket();
    if (ok) loadHistory();
}

document.getElementById('commentBtn').addEventListener('click', async () => {
    const input = document.getElementById('commentText');
    const message = input.value.trim();
    if (!message) return;
    clearError();
    const res = await api('/api/tickets/' + ticketId + '/comments', {
        method: 'POST',
        body: JSON.stringify({ message })
    });
    if (res.status === 201) {
        input.value = '';
        loadComments();
    } else {
        showError(await errorText(res, 'Could not add comment.'));
    }
});

loadTicket().then(ok => {
    if (ok) {
        loadComments();
        loadHistory();
    }
});