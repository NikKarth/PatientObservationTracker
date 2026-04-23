const state = {
    currentUser: null,
    users: [],
    patients: [],
    currentPatient: null,
    observations: [],
    phenomenonTypes: [],
    phenomena: [],
    protocols: [],
    associativeFunctions: [],
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
    userSelect: document.getElementById('user-select'),
    currentUserLabel: document.getElementById('current-user-label'),
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
    phenomenaTableBody: document.querySelector('#phenomena-table tbody'),
    phenomenonTypeForm: document.getElementById('phenomenon-type-form'),
    protocolForm: document.getElementById('protocol-form'),
    associativeFunctionTableBody: document.querySelector('#associative-function-table tbody'),
    associativeFunctionForm: document.getElementById('associative-function-form'),
    commandLogTableBody: document.querySelector('#command-log-table tbody'),
    auditLogTableBody: document.querySelector('#audit-log-table tbody'),
};

const navMap = {
    patients: elements.patientsSection,
    catalog: elements.catalogSection,
    logs: elements.logsSection,
};

function apiGet(path) {
    return fetch(path, {
        credentials: 'same-origin'
    }).then(response => response.json());
}

function apiPost(path, data) {
    return fetch(path, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(data),
    }).then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text || response.statusText); });
        }
        return response.text().then(text => text ? JSON.parse(text) : null);
    });
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

function renderUserOptions() {
    elements.userSelect.innerHTML = '';
    state.users.forEach(user => {
        const option = document.createElement('option');
        option.value = user.username;
        option.textContent = `${user.username} (${user.role})`;
        if (state.currentUser && state.currentUser.username === user.username) {
            option.selected = true;
        }
        elements.userSelect.appendChild(option);
    });
}

function updateCurrentUserDisplay() {
    if (state.currentUser) {
        elements.currentUserLabel.textContent = `Logged in as ${state.currentUser.username} (${state.currentUser.role})`;
    } else {
        elements.currentUserLabel.textContent = 'Not logged in';
    }
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
        const anomalyLabel = obs.anomaly ? ' <span style="color: red; font-style: italic;">[ANOMALY]</span>' : '';
        const isInferred = obs.source === 'INFERRED';
        const italicStyle = isInferred ? 'font-style: italic; color: #999;' : '';
        let action = '';
        if (obs.status === 'ACTIVE') {
            action = `<button data-id="${obs.id}" class="reject-button">Reject</button>`;
        } else if (obs.status === 'REJECTED') {
            const rejectionCommand = state.logs.commands.find(cmd =>
                cmd.commandType === 'RejectObservationCommand' &&
                !cmd.undone &&
                cmd.payload &&
                JSON.parse(cmd.payload).observationId == obs.id
            );
            if (rejectionCommand && state.currentUser && rejectionCommand.user === state.currentUser.username) {
                action = `<button data-id="${rejectionCommand.id}" class="undo-rejection-button">Undo Rejection</button>`;
            }
        }
        row.style.cssText = italicStyle;
        row.innerHTML = `
            <td>${obs.id}</td>
            <td>${obs.type}</td>
            <td>${obs.type === 'measurement' ? obs.phenomenonType : obs.phenomenon || ''}</td>
            <td>${value}${anomalyLabel}</td>
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
    elements.observationTableBody.querySelectorAll('.undo-rejection-button').forEach(button => {
        button.addEventListener('click', () => undoCommand(button.dataset.id));
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
            <td>${type.kind === 'QUANTITATIVE' ? Array.from(type.allowedUnits || []).join(', ') : (type.phenomena || []).map(p => `${p.name}${p.parentConceptName ? ` (parent: ${p.parentConceptName})` : ''}`).join(', ')}
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
    elements.phenomenaTableBody.innerHTML = '';
    state.phenomena.forEach(phenomenon => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${phenomenon.id}</td>
            <td>${phenomenon.name}</td>
            <td>${phenomenon.phenomenonTypeName || ''}</td>
            <td>${phenomenon.parentConceptName || ''}</td>
        `;
        elements.phenomenaTableBody.appendChild(row);
    });
    elements.associativeFunctionTableBody.innerHTML = '';
    state.associativeFunctions.forEach(af => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${af.id}</td>
            <td>${af.name}</td>
            <td>${af.arguments.join(', ')}</td>
            <td>${af.productConcept}</td>
            <td>${af.strategyType}</td>
            <td>${af.threshold}</td>
        `;
        elements.associativeFunctionTableBody.appendChild(row);
    });
}

function loadPatients() {
    return apiGet('/api/patients').then(list => {
        state.patients = list;
        renderPatients();
    });
}

function loadSession() {
    return apiGet('/api/session').then(user => {
        state.currentUser = user;
        updateCurrentUserDisplay();
    }).catch(() => {
        state.currentUser = null;
        updateCurrentUserDisplay();
    });
}

function loginUser(username) {
    return apiPost('/api/login', { username }).then(user => {
        state.currentUser = user;
        updateCurrentUserDisplay();
        renderUserOptions();
        return user;
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

function loadUsers() {
    return apiGet('/api/users').then(list => {
        state.users = list;
        renderUserOptions();
        if (!state.currentUser && state.users.length > 0) {
            return loginUser(state.users[0].username);
        }
        return state.currentUser;
    });
}

function loadAssociativeFunctions() {
    return apiGet('/api/associative-functions').then(list => {
        state.associativeFunctions = list;
        refreshCatalogue();
    });
}

function loadLogs() {
    apiGet('/api/command-log').then(commands => {
        state.logs.commands = commands;
        elements.commandLogTableBody.innerHTML = '';
        commands.forEach(entry => {
            const canUndo = !entry.undone && state.currentUser && entry.user === state.currentUser.username
                && ['RecordMeasurementCommand', 'RecordCategoryObservationCommand', 'RejectObservationCommand'].includes(entry.commandType);
            const action = canUndo ? `<button data-id="${entry.id}" class="undo-command-button">Undo</button>` : '';
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${entry.id}</td>
                <td>${entry.commandType}</td>
                <td>${entry.user}</td>
                <td>${new Date(entry.executedAt).toLocaleString()}</td>
                <td>${entry.undone ? 'UNDONE' : 'ACTIVE'}</td>
                <td><pre>${escape(entry.payload)}</pre></td>
                <td>${action}</td>
            `;
            elements.commandLogTableBody.appendChild(row);
        });
        elements.commandLogTableBody.querySelectorAll('.undo-command-button').forEach(button => {
            button.addEventListener('click', () => undoCommand(button.dataset.id));
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
    Promise.all([
        loadObservations(patientId),
        apiGet('/api/command-log')
    ]).then(([_, commands]) => {
        state.logs.commands = commands;
        renderPatientDetail();
    });
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
    }).catch(error => {
        console.error('Error creating patient:', error);
        alert('Error creating patient: ' + error.message);
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
        return Promise.all([
            loadObservations(state.currentPatient.id),
            apiGet('/api/command-log')
        ]).then(([_, commands]) => {
            state.logs.commands = commands;
            renderPatientDetail();
        });
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
        return Promise.all([
            loadObservations(state.currentPatient.id),
            apiGet('/api/command-log')
        ]).then(([_, commands]) => {
            state.logs.commands = commands;
            renderPatientDetail();
        });
    });
}

function rejectObservation(observationId) {
    const reason = window.prompt('Rejection reason:');
    if (!reason) return;
    return apiPost(`/api/observations/${observationId}/reject`, { reason }).then(() => {
        return Promise.all([
            loadObservations(state.currentPatient.id),
            apiGet('/api/command-log')
        ]).then(([_, commands]) => {
            state.logs.commands = commands;
            renderPatientDetail();
        });
    });
}

function undoCommand(commandId) {
    return apiPost(`/api/command-log/${commandId}/undo`, {}).then(() => {
        loadLogs();
        if (state.currentPatient) {
            Promise.all([
                loadObservations(state.currentPatient.id),
                apiGet('/api/command-log')
            ]).then(([_, commands]) => {
                state.logs.commands = commands;
                renderPatientDetail();
            });
        }
    }).catch(error => {
        alert('Unable to undo command: ' + error.message);
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
    const phenomenaWithParents = formData.phenomena ? formData.phenomena.split(',').map(s => s.trim()).filter(Boolean) : [];
    const phenomena = phenomenaWithParents.map(p => {
        const parts = p.split(':');
        return { name: parts[0], parentConceptId: parts[1] ? Number(parts[1]) : null };
    });
    return apiPost('/api/phenomenon-types', {
        name: formData.name,
        kind: formData.kind,
        allowedUnits,
        phenomena,
        normalMin: formData.normalMin ? parseFloat(formData.normalMin) : null,
        normalMax: formData.normalMax ? parseFloat(formData.normalMax) : null,
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

function createAssociativeFunction(formData) {
    return apiPost('/api/associative-functions', {
        name: formData.name,
        arguments: formData.arguments.split(',').map(s => s.trim()),
        productConcept: formData.productConcept,
        strategyType: formData.strategyType,
        threshold: parseFloat(formData.threshold) || 0,
    }).then(() => {
        elements.associativeFunctionForm.reset();
        return loadAssociativeFunctions();
    });
}

function loadPhenomena() {
    return apiGet('/api/phenomena').then(list => {
        state.phenomena = list;
        refreshCatalogue();
        updateParentConceptsDropdown();
    });
}

function updateParentConceptsDropdown() {
    const parentConceptSelect = document.querySelector('#phenomenon-type-parent-concepts');
    parentConceptSelect.innerHTML = '<option value="">Select parent concept (optional)</option>';
    state.phenomena.forEach(phenomenon => {
        const option = document.createElement('option');
        option.value = phenomenon.id;
        option.textContent = `${phenomenon.name}${phenomenon.parentConceptName ? ` (parent: ${phenomenon.parentConceptName})` : ''}`;
        parentConceptSelect.appendChild(option);
    });
    
    // Add event listener to help populate phenomena field
    parentConceptSelect.addEventListener('change', function() {
        const selectedId = this.value;
        if (selectedId) {
            const selectedPhenomenon = state.phenomena.find(p => p.id == selectedId);
            if (selectedPhenomenon) {
                const phenomenaInput = document.querySelector('#phenomenon-type-form input[name="phenomena"]');
                const currentValue = phenomenaInput.value.trim();
                const parentReference = `:${selectedId}`;
                
                // If there's already text, append with comma
                if (currentValue) {
                    // Check if the last entry already has a parent
                    const entries = currentValue.split(',').map(s => s.trim());
                    const lastEntry = entries[entries.length - 1];
                    if (!lastEntry.includes(':')) {
                        entries[entries.length - 1] = lastEntry + parentReference;
                        phenomenaInput.value = entries.join(', ');
                    } else {
                        phenomenaInput.value = currentValue + ', ';
                    }
                } else {
                    phenomenaInput.value = parentReference.substring(1); // Remove the colon
                }
                
                // Reset dropdown
                this.value = '';
            }
        }
    });
}

function escape(value) {
    return String(value || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function wireEvents() {
    elements.navPatients.addEventListener('click', () => setActiveTab('patients'));
    elements.navCatalog.addEventListener('click', () => setActiveTab('catalog'));
    elements.navLogs.addEventListener('click', () => setActiveTab('logs'));
    elements.userSelect.addEventListener('change', event => loginUser(event.target.value).then(loadLogs));
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

    elements.associativeFunctionForm.addEventListener('submit', event => {
        event.preventDefault();
        const form = event.target;
        createAssociativeFunction(Object.fromEntries(new FormData(form).entries()));
    });
    elements.evaluateRulesButton.addEventListener('click', evaluateRules);
    elements.phenomenonTypeSelect.addEventListener('change', renderMeasurementUnits);
}

function init() {
    wireEvents();
    loadSession()
        .then(() => Promise.all([loadUsers(), loadPhenomenonTypes(), loadProtocols(), loadAssociativeFunctions(), loadPhenomena()]))
        .then(() => {
            loadPatients();
            if (state.currentUser) {
                loadLogs();
            }
            setActiveTab('patients');
        });
}

document.addEventListener('DOMContentLoaded', init);
