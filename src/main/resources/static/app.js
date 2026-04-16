const state = {
    patients: [],
    currentPatient: null,
    observations: [],
    phenomenonTypes: [],
    protocols: [],
    logs: { commands: [], audit: [] },
};

const elements = {
    patientsSection: document.getElementById('patients-section'),
    catalogSection: document.getElementById('catalog-section'),
    logsSection: document.getElementById('logs-section'),
    navPatients: document.getElementById('nav-patients'),
    navCatalog: document.getElementById('nav-catalog'),
    navLogs: document.getElementById('nav-logs'),
    patientTableBody: document.querySelector('#patient-table tbody'),
    patientForm: document.getElementById('patient-form'),
    patientDetailEmpty: document.getElementById('patient-detail-empty'),
    patientDetailBody: document.getElementById('patient-detail-body'),
    patientName: document.getElementById('patient-name'),
    patientDob: document.getElementById('patient-dob'),
    observationTableBody: document.querySelector('#observation-table tbody'),
    measurementForm: document.getElementById('measurement-form'),
    categoryForm: document.getElementById('category-form'),
    evaluateRulesButton: document.getElementById('evaluate-rules-button'),
    ruleInferences: document.getElementById('rule-inferences'),
    phenomenonTypeSelect: document.querySelector('#measurement-form select[name="phenomenonTypeId"]'),
    measurementUnitSelect: document.querySelector('#measurement-form select[name="unit"]'),
    categoryPhenomenonSelect: document.querySelector('#category-form select[name="phenomenonId"]'),
    protocolSelects: document.querySelectorAll('select[name="protocolId"]'),
    phenomenonTypeTableBody: document.querySelector('#phenomenon-type-table tbody'),
    protocolTableBody: document.querySelector('#protocol-table tbody'),
    phenomenonTypeForm: document.getElementById('phenomenon-type-form'),
    protocolForm: document.getElementById('protocol-form'),
    commandLogTableBody: document.querySelector('#command-log-table tbody'),
    auditLogTableBody: document.querySelector('#audit-log-table tbody'),
};

const navMap = {
    patients: elements.patientsSection,
    catalog: elements.catalogSection,
    logs: elements.logsSection,
};

function apiGet(path) {
    return fetch(path).then(response => response.json());
}

function apiPost(path, data) {
    return fetch(path, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
    }).then(response => response.json());
}

function setActiveTab(tab) {
    Object.keys(navMap).forEach(key => {
        navMap[key].classList.toggle('hidden', key !== tab);
    });
    elements.navPatients.classList.toggle('active', tab === 'patients');
    elements.navCatalog.classList.toggle('active', tab === 'catalog');
    elements.navLogs.classList.toggle('active', tab === 'logs');
    if (tab === 'logs') {
        loadLogs();
    }
    if (tab === 'catalog') {
        refreshCatalogue();
    }
}

function renderPatients() {
    elements.patientTableBody.innerHTML = '';
    state.patients.forEach(patient => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${patient.id}</td>
            <td>${patient.fullName}</td>
            <td>${patient.dateOfBirth || ''}</td>
            <td>${patient.note || ''}</td>
            <td><button data-id="${patient.id}" class="view-button">View</button></td>
        `;
        elements.patientTableBody.appendChild(row);
    });
    elements.patientTableBody.querySelectorAll('.view-button').forEach(button => {
        button.addEventListener('click', () => viewPatient(button.dataset.id));
    });
}

function renderObservationFormOptions() {
    elements.phenomenonTypeSelect.innerHTML = '<option value="">Select phenomenon type</option>';
    state.phenomenonTypes.filter(type => type.kind === 'QUANTITATIVE').forEach(type => {
        const option = document.createElement('option');
        option.value = type.id;
        option.textContent = `${type.name}`;
        elements.phenomenonTypeSelect.appendChild(option);
    });
    elements.categoryPhenomenonSelect.innerHTML = '<option value="">Select phenomenon</option>';
    state.phenomenonTypes
        .filter(type => type.kind === 'QUALITATIVE')
        .flatMap(type => type.phenomena)
        .forEach(phenomenon => {
            const option = document.createElement('option');
            option.value = phenomenon.id;
            option.textContent = phenomenon.name;
            elements.categoryPhenomenonSelect.appendChild(option);
        });
    elements.protocolSelects.forEach(select => {
        select.innerHTML = '<option value="">(none)</option>';
        state.protocols.forEach(protocol => {
            const option = document.createElement('option');
            option.value = protocol.id;
            option.textContent = protocol.name;
            select.appendChild(option);
        });
    });
}

function renderMeasurementUnits() {
    const selectedId = Number(elements.phenomenonTypeSelect.value);
    const type = state.phenomenonTypes.find(item => item.id === selectedId);
    elements.measurementUnitSelect.innerHTML = '<option value="">Select unit</option>';
    if (type && type.allowedUnits) {
        type.allowedUnits.forEach(unit => {
            const option = document.createElement('option');
            option.value = unit;
            option.textContent = unit;
            elements.measurementUnitSelect.appendChild(option);
        });
    }
}

function renderObservations() {
    elements.observationTableBody.innerHTML = '';
    state.observations.forEach(obs => {
        const row = document.createElement('tr');
        const value = obs.type === 'measurement' ? `${obs.amount || ''} ${obs.unit || ''}` : `${obs.phenomenon || ''} (${obs.presence || ''})`;
        const action = obs.status === 'ACTIVE' ? `<button data-id="${obs.id}" class="reject-button">Reject</button>` : '';
        row.innerHTML = `
            <td>${obs.id}</td>
            <td>${obs.type}</td>
            <td>${obs.type === 'measurement' ? obs.phenomenonType : obs.phenomenon || ''}</td>
            <td>${value}</td>
            <td>${obs.protocol || ''}</td>
            <td>${obs.status || ''}</td>
            <td>${obs.recordingTime || ''}<br/>${obs.applicabilityTime || ''}</td>
            <td>${action}</td>
        `;
        elements.observationTableBody.appendChild(row);
    });
    elements.observationTableBody.querySelectorAll('.reject-button').forEach(button => {
        button.addEventListener('click', () => rejectObservation(button.dataset.id));
    });
}

function renderPatientDetail() {
    if (!state.currentPatient) {
        elements.patientDetailBody.classList.add('hidden');
        elements.patientDetailEmpty.classList.remove('hidden');
        return;
    }
    elements.patientDetailEmpty.classList.add('hidden');
    elements.patientDetailBody.classList.remove('hidden');
    elements.patientName.textContent = state.currentPatient.fullName;
    elements.patientDob.textContent = `DOB: ${state.currentPatient.dateOfBirth || ''}`;
    renderObservations();
}

function refreshCatalogue() {
    elements.phenomenonTypeTableBody.innerHTML = '';
    state.phenomenonTypes.forEach(type => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${type.id}</td>
            <td>${type.name}</td>
            <td>${type.kind}</td>
            <td>${type.kind === 'QUANTITATIVE' ? Array.from(type.allowedUnits || []).join(', ') : (type.phenomena || []).map(p => p.name).join(', ')}
            </td>
        `;
        elements.phenomenonTypeTableBody.appendChild(row);
    });
    elements.protocolTableBody.innerHTML = '';
    state.protocols.forEach(protocol => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${protocol.id}</td>
            <td>${protocol.name}</td>
            <td>${protocol.description || ''}</td>
            <td>${protocol.accuracyRating || ''}</td>
        `;
        elements.protocolTableBody.appendChild(row);
    });
}

function loadPatients() {
    return apiGet('/api/patients').then(list => {
        state.patients = list;
        renderPatients();
    });
}

function loadPhenomenonTypes() {
    return apiGet('/api/phenomenon-types').then(list => {
        state.phenomenonTypes = list;
        renderMeasurementUnits();
        renderObservationFormOptions();
        refreshCatalogue();
    });
}

function loadProtocols() {
    return apiGet('/api/protocols').then(list => {
        state.protocols = list;
        renderObservationFormOptions();
        refreshCatalogue();
    });
}

function loadLogs() {
    apiGet('/api/command-log').then(commands => {
        state.logs.commands = commands;
        elements.commandLogTableBody.innerHTML = '';
        commands.forEach(entry => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${entry.id}</td>
                <td>${entry.commandType}</td>
                <td>${entry.user}</td>
                <td>${new Date(entry.executedAt).toLocaleString()}</td>
                <td><pre>${escape(entry.payload)}</pre></td>
            `;
            elements.commandLogTableBody.appendChild(row);
        });
    });
    apiGet('/api/audit-log').then(entries => {
        state.logs.audit = entries;
        elements.auditLogTableBody.innerHTML = '';
        entries.forEach(entry => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${entry.id}</td>
                <td>${new Date(entry.timestamp).toLocaleString()}</td>
                <td>${entry.patientId || ''}</td>
                <td>${entry.observationId || ''}</td>
                <td>${entry.event || ''}</td>
                <td><pre>${escape(entry.details)}</pre></td>
            `;
            elements.auditLogTableBody.appendChild(row);
        });
    });
}

function viewPatient(patientId) {
    state.currentPatient = state.patients.find(item => item.id === Number(patientId));
    if (!state.currentPatient) return;
    loadObservations(patientId).then(() => renderPatientDetail());
}

function loadObservations(patientId) {
    return apiGet(`/api/patients/${patientId}/observations`).then(list => {
        state.observations = list;
    });
}

function createPatient(formData) {
    return apiPost('/api/patients', formData).then(() => {
        elements.patientForm.reset();
        return loadPatients();
    });
}

function createMeasurement(formData) {
    if (!state.currentPatient) return;
    const request = {
        patientId: state.currentPatient.id,
        phenomenonTypeId: Number(formData.phenomenonTypeId),
        amount: Number(formData.amount),
        unit: formData.unit,
        protocolId: formData.protocolId ? Number(formData.protocolId) : null,
        applicabilityTime: formData.applicabilityTime ? new Date(formData.applicabilityTime).toISOString() : null,
    };
    return apiPost('/api/observations/measurement', request).then(() => {
        elements.measurementForm.reset();
        renderMeasurementUnits();
        return loadObservations(state.currentPatient.id).then(renderPatientDetail);
    });
}

function createCategoryObservation(formData) {
    if (!state.currentPatient) return;
    const request = {
        patientId: state.currentPatient.id,
        phenomenonId: Number(formData.phenomenonId),
        presence: formData.presence,
        protocolId: formData.protocolId ? Number(formData.protocolId) : null,
        applicabilityTime: formData.applicabilityTime ? new Date(formData.applicabilityTime).toISOString() : null,
    };
    return apiPost('/api/observations/category', request).then(() => {
        elements.categoryForm.reset();
        return loadObservations(state.currentPatient.id).then(renderPatientDetail);
    });
}

function rejectObservation(observationId) {
    const reason = window.prompt('Rejection reason:');
    if (!reason) return;
    return apiPost(`/api/observations/${observationId}/reject`, { reason }).then(() => {
        return loadObservations(state.currentPatient.id).then(renderPatientDetail);
    });
}

function evaluateRules() {
    if (!state.currentPatient) return;
    apiGet(`/api/patients/${state.currentPatient.id}/evaluate`).then(result => {
        elements.ruleInferences.innerHTML = '';
        if (!result.inferences || result.inferences.length === 0) {
            elements.ruleInferences.innerHTML = '<li>No inferences found.</li>';
            return;
        }
        result.inferences.forEach(inference => {
            const li = document.createElement('li');
            li.textContent = inference;
            elements.ruleInferences.appendChild(li);
        });
    });
}

function createPhenomenonType(formData) {
    const allowedUnits = formData.allowedUnits ? formData.allowedUnits.split(',').map(s => s.trim()).filter(Boolean) : [];
    const phenomena = formData.phenomena ? formData.phenomena.split(',').map(s => s.trim()).filter(Boolean) : [];
    return apiPost('/api/phenomenon-types', {
        name: formData.name,
        kind: formData.kind,
        allowedUnits,
        phenomena,
    }).then(() => {
        elements.phenomenonTypeForm.reset();
        return loadPhenomenonTypes();
    });
}

function createProtocol(formData) {
    return apiPost('/api/protocols', {
        name: formData.name,
        description: formData.description,
        accuracyRating: formData.accuracyRating,
    }).then(() => {
        elements.protocolForm.reset();
        return loadProtocols();
    });
}

function escape(value) {
    return String(value || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function wireEvents() {
    elements.navPatients.addEventListener('click', () => setActiveTab('patients'));
    elements.navCatalog.addEventListener('click', () => setActiveTab('catalog'));
    elements.navLogs.addEventListener('click', () => setActiveTab('logs'));
    elements.patientForm.addEventListener('submit', event => {
        event.preventDefault();
        const form = event.target;
        createPatient(Object.fromEntries(new FormData(form).entries()));
    });
    elements.measurementForm.addEventListener('submit', event => {
        event.preventDefault();
        const form = event.target;
        createMeasurement(Object.fromEntries(new FormData(form).entries()));
    });
    elements.categoryForm.addEventListener('submit', event => {
        event.preventDefault();
        const form = event.target;
        createCategoryObservation(Object.fromEntries(new FormData(form).entries()));
    });
    elements.phenomenonTypeForm.addEventListener('submit', event => {
        event.preventDefault();
        const form = event.target;
        createPhenomenonType(Object.fromEntries(new FormData(form).entries()));
    });
    elements.protocolForm.addEventListener('submit', event => {
        event.preventDefault();
        const form = event.target;
        createProtocol(Object.fromEntries(new FormData(form).entries()));
    });
    elements.evaluateRulesButton.addEventListener('click', evaluateRules);
    elements.phenomenonTypeSelect.addEventListener('change', renderMeasurementUnits);
}

function init() {
    wireEvents();
    loadPatients();
    Promise.all([loadPhenomenonTypes(), loadProtocols()]);
    setActiveTab('patients');
}

document.addEventListener('DOMContentLoaded', init);
