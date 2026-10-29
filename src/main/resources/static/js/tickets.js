if (!sessionStorage.getItem('auth')) {
    location.href = 'index.html';
}

document.getElementById('who').textContent = sessionStorage.getItem('email');

function cell(text) {
    const td = document.createElement('td');
    td.textContent = text ?? '-';
    return td;
}

function linkCell(text, id) {
    const td = document.createElement('td');
    const a = document.createElement('a');
    a.href = 'ticket.html?id=' + id;
    a.textContent = text;
    td.appendChild(a);
    return td;
}

function badge(text, cls) {
    const td = document.createElement('td');
    const span = document.createElement('span');
    span.className = 'badge text-bg-' + cls;
    span.textContent = text;
    td.appendChild(span);
    return td;
}

const statusColor = { OPEN: 'danger', IN_PROGRESS: 'warning', RESOLVED: 'success' };
const priorityColor = { LOW: 'secondary', MEDIUM: 'info', HIGH: 'danger' };

async function loadDashboard() {
    const res = await api('/api/dashboard');
    if (!res.ok) return;
    const d = await res.json();
    const box = document.getElementById('dash');
    box.innerHTML = '';
    const items = [
        ['Total', d.total],
        ['Open', d.byStatus.OPEN],
        ['In progress', d.byStatus.IN_PROGRESS],
        ['Resolved', d.byStatus.RESOLVED]
    ];
    items.forEach(([label, value]) => {
        const col = document.createElement('div');
        col.className = 'col-6 col-md-3';
        const card = document.createElement('div');
        card.className = 'card text-center';
        const body = document.createElement('div');
        body.className = 'card-body';
        const num = document.createElement('div');
        num.className = 'fs-3 fw-bold';
        num.textContent = value;
        const lab = document.createElement('div');
        lab.className = 'text-muted';
        lab.textContent = label;
        body.append(num, lab);
        card.appendChild(body);
        col.appendChild(card);
        box.appendChild(col);
    });
}

async function loadTickets() {
    const res = await api('/api/tickets');
    if (res.status === 401) { logout(); return; }
    const tickets = await res.json();
    const rows = document.getElementById('rows');
    rows.innerHTML = '';
    tickets.forEach(t => {
        const tr = document.createElement('tr');
        tr.append(
            cell(t.id),
            linkCell(t.title, t.id),
            badge(t.priority, priorityColor[t.priority]),
            badge(t.status.replace('_', ' '), statusColor[t.status]),
            cell(t.createdBy ? t.createdBy.name : '-'),
            cell(t.assignedTo ? t.assignedTo.name : 'Unassigned')
        );
        rows.appendChild(tr);
    });
}

document.getElementById('createBtn').addEventListener('click', async () => {
    const body = {
        title: document.getElementById('title').value.trim(),
        description: document.getElementById('description').value.trim(),
        priority: document.getElementById('priority').value
    };
    const msg = document.getElementById('formMsg');
    if (!body.title) {
        msg.textContent = 'Title is required';
        msg.className = 'mt-2 text-danger';
        return;
    }
    const res = await api('/api/tickets', { method: 'POST', body: JSON.stringify(body) });
    if (res.status === 201) {
        msg.textContent = 'Ticket created';
        msg.className = 'mt-2 text-success';
        document.getElementById('title').value = '';
        document.getElementById('description').value = '';
        loadDashboard();
        loadTickets();
    } else {
        msg.textContent = await errorText(res, 'Could not create ticket');
        msg.className = 'mt-2 text-danger';
    }
});

loadDashboard();
loadTickets();