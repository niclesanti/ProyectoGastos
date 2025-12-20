/**
 * @file Script principal para el dashboard de Finanzas.
 * @description Maneja la lógica de la interfaz, interacciones de usuario, y visualización de datos.
 * @author Nicle Santiago
 * @version 1.0.1
 */

// Módulo principal de la aplicación (IIFE)
(function() {
    "use strict";

    const appState = {
        transactions: [],
        reasons: [],
        contacts: [],
        bankAccounts: [],
        budgets: [],
        workspaces: [],
        charts: {
            monthly: null,
            expenses: null,
            balanceTrend: null
        },
        dashboardCache: {},
        lastSearchResults: [],
        currentBalance: 0,
    };

    const DOMElements = {
        sideMenu: document.getElementById('sideMenu'),
        sideMenuOverlay: document.getElementById('sideMenuOverlay'),
        notificationsMenu: document.getElementById('notificationsMenu'),
        transactionModal: document.getElementById('transactionModal'),
        searchModal: document.getElementById('searchModal'),
        budgetModal: document.getElementById('budgetModal'),
        manageWorkspacesModal: document.getElementById('manageWorkspacesModal'),
        shareWorkspaceSelect: document.getElementById('shareWorkspaceSelect'),
        transactionForm: document.getElementById('transactionForm'),
        searchForm: document.getElementById('searchForm'),
        budgetForm: document.getElementById('budgetForm'),
        workspaceForm: document.getElementById('workspaceForm'),
        shareForm: document.getElementById('shareForm'),
        transferForm: document.getElementById('transferForm'),
        newReasonForm: document.getElementById('newReasonForm'),
        newContactForm: document.getElementById('newContactForm'),
        newBankAccountForm: document.getElementById('newBankAccountForm'),
        reasonSelect: document.getElementById('reason'),
        contactSelect: document.getElementById('recipient'),
        bankAccountSelect: document.getElementById('bankAccount'),
        searchReasonSelect: document.getElementById('searchReason'),
        searchContactSelect: document.getElementById('searchRecipient'),
        searchYearInput: document.getElementById('searchYear'),
        budgetReasonSelect: document.getElementById('budgetReason'),
        newReasonInput: document.getElementById('newReasonInput'),
        newContactInput: document.getElementById('newContactInput'),
        newAccountNameInput: document.getElementById('newAccountNameInput'),
        financialEntitySelect: document.getElementById('financialEntitySelect'),
        orderBySelect: document.getElementById('orderBy'),
        recentTransactionsList: document.getElementById('recentTransactionsList'),
        bankAccountsList: document.getElementById('lista-cuentas-bancarias'),
        searchResultsContainer: document.getElementById('searchResults'),
        alertsListContainer: document.getElementById('alertsList'),
        notificationContainer: document.getElementById('notification-container'),
        balanceAmount: document.querySelector('.balance__value'),
        balanceContainer: document.querySelector('.balance__amount'),
        balanceDate: document.querySelector('.balance__date'),
        transactionDetailModal: document.getElementById('transactionDetailModal'),
        transactionDetailContent: document.getElementById('transactionDetailContent'),
    };

    /**
     * Parsea una fecha en formato UTC (ej: '2025-08-05T00:00:00Z')
     * y la ajusta para evitar el problema de la zona horaria,
     * mostrando la fecha como si fuera local.
     * @param {string} dateString - La fecha en formato string desde el backend.
     * @returns {Date} Un objeto Date ajustado.
     */
    function parseUTCDate(dateString) {
        if (!dateString) return null;
        const date = new Date(dateString);
        // Ajusta la fecha sumándole el offset de la zona horaria del navegador en minutos.
        date.setMinutes(date.getMinutes() + date.getTimezoneOffset());
        return date;
    }

    function initializeApp() {
        loadSampleData();
        setupEventListeners();
        updateDashboard();
        DOMElements.searchYearInput.value = new Date().getFullYear();
        loadAuthenticatedUser();
        populateFinancialEntities();
    }

    function loadAuthenticatedUser() {
        fetch('/usuario/me')
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error('No se pudo obtener la información del usuario.');
            })
            .then(usuario => {
                if (usuario) {
                    document.querySelector('.side-menu__username').textContent = usuario.nombre;
                    document.querySelector('.side-menu__email').textContent = usuario.email;
                    const profileImg = document.getElementById('userProfileImg');
                    if (usuario.fotoPerfil && usuario.fotoPerfil.trim() !== '') {
                        profileImg.src = usuario.fotoPerfil;
                    } else {
                        profileImg.src = '/default-profile.png'; // Imagen por defecto
                    }
                    profileImg.alt = `Foto de perfil de ${usuario.nombre}`; // Agregar texto alternativo
                    appState.authenticatedUserId = usuario.id;
                    loadWorkspacesForUser(appState.authenticatedUserId);
                }
            })
            .catch(error => {
                console.error('Error al cargar datos del usuario:', error);
            });
    }

    function loadWorkspacesForUser(userId) {
        if (!userId) {
            console.warn('No se puede cargar espacios de trabajo: ID de usuario no disponible.');
            return;
        }
        fetch(`/espaciotrabajo/listar/${userId}`)
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error('No se pudieron cargar los espacios de trabajo.');
            })
            .then(workspaces => {
                appState.workspaces = workspaces;
                populateShareWorkspaceSelect();
                populateMainWorkspaceSelect();
            })
            .catch(error => {
                console.error('Error al cargar espacios de trabajo:', error);
                showNotification('Error al cargar los espacios de trabajo', 'error');
            });
    }

    function populateMainWorkspaceSelect() {
        const selectElement = document.getElementById('workspaceSelect');
        if (!selectElement) return;

        selectElement.innerHTML = '<option value="">Seleccionar espacio de trabajo</option>';
        appState.workspaces.forEach(workspace => {
            const option = document.createElement('option');
            option.value = workspace.id;
            option.textContent = workspace.nombre;
            selectElement.appendChild(option);
        });
    }

    function loadSampleData() {
        appState.transactions = [];
    }

    function setupEventListeners() {
        document.getElementById('newTransactionBtn').addEventListener('click', () => toggleModal('transactionModal', true));
        document.getElementById('searchTransactionsBtn').addEventListener('click', () => toggleModal('searchModal', true));
        document.getElementById('setupAlertsBtn').addEventListener('click', () => toggleModal('budgetModal', true));
        document.getElementById('manageWorkspacesBtn').addEventListener('click', () => {
            toggleModal('manageWorkspacesModal', true);
            populateShareWorkspaceSelect();
        });

        // Listeners para el menú lateral
        document.getElementById('menuBtn').addEventListener('click', () => toggleSideMenu(true));
        document.getElementById('closeSideMenuBtn').addEventListener('click', () => toggleSideMenu(false));
        DOMElements.sideMenuOverlay.addEventListener('click', () => toggleSideMenu(false));

        // Listener para el menú de notificaciones
        document.getElementById('notificationsBtn').addEventListener('click', (event) => {
            event.stopPropagation(); // Evita que el evento de clic se propague al window
            toggleNotificationsMenu();
        });

        // Listener para el selector de espacio de trabajo principal
        const workspaceSelect = document.getElementById('workspaceSelect');
        if (workspaceSelect) {
            workspaceSelect.addEventListener('change', (event) => {
                const idEspacioTrabajo = event.target.value;
                if (idEspacioTrabajo) {
                    const selectedWorkspace = appState.workspaces.find(ws => ws.id == idEspacioTrabajo);
                    if (selectedWorkspace) {
                        appState.currentBalance = selectedWorkspace.saldo;
                        updateBalance();
                    }
                    cargarMotivos(idEspacioTrabajo);
                    cargarContactos(idEspacioTrabajo);
                    cargarCuentasBancarias(idEspacioTrabajo);
                    cargarTransaccionesRecientes(idEspacioTrabajo);
                    actualizarDashboard(idEspacioTrabajo); // <-- AÑADIDO
                } else {
                    appState.currentBalance = 0;
                    updateBalance();
                    clearMotivos();
                    clearContactos();
                    clearCuentasBancarias();
                    appState.transactions = [];
                    renderRecentTransactions();
                    // Limpiar gráficos si no hay espacio seleccionado
                    Object.values(appState.charts).forEach(chart => {
                        if (chart) {
                            chart.data.labels = [];
                            chart.data.datasets.forEach(dataset => {
                                dataset.data = [];
                            });
                            chart.update();
                        }
                    });
                }
            });
        }

        document.getElementById('closeTransactionModalBtn').addEventListener('click', () => toggleModal('transactionModal', false));
        document.getElementById('closeSearchModalBtn').addEventListener('click', () => toggleModal('searchModal', false));
        document.getElementById('closeBudgetModalBtn').addEventListener('click', () => toggleModal('budgetModal', false));
        document.getElementById('closeManageWorkspacesModalBtn').addEventListener('click', () => toggleModal('manageWorkspacesModal', false));
        document.getElementById('cancelTransactionBtn').addEventListener('click', () => toggleModal('transactionModal', false));
        document.getElementById('cancelTransferBtn').addEventListener('click', () => toggleModal('transactionModal', false));
        document.getElementById('cancelBudgetBtn').addEventListener('click', () => toggleModal('budgetModal', false));
        document.getElementById('cancelWorkspaceBtn').addEventListener('click', () => toggleModal('manageWorkspacesModal', false));
        document.getElementById('cancelShareBtn').addEventListener('click', () => toggleModal('manageWorkspacesModal', false));

        // Listeners para el nuevo modal de detalle
        document.getElementById('closeTransactionDetailModalBtn').addEventListener('click', () => toggleModal('transactionDetailModal', false));
        document.getElementById('backTransactionDetailBtn').addEventListener('click', () => toggleModal('transactionDetailModal', false));
        document.getElementById('removeTransactionBtn').addEventListener('click', handleRemoveTransaction);


        // Envíos de formularios
        DOMElements.transactionForm.addEventListener('submit', handleTransactionSubmit);
        DOMElements.searchForm.addEventListener('submit', handleSearchSubmit);
        DOMElements.budgetForm.addEventListener('submit', handleBudgetSubmit);
        DOMElements.workspaceForm.addEventListener('submit', handleWorkspaceSubmit);
        DOMElements.shareForm.addEventListener('submit', handleShareSubmit);
        DOMElements.transferForm.addEventListener('submit', handleTransferSubmit);

        // Listeners para las pestañas de los modales
        const modalsWithTabs = document.querySelectorAll('.modal');
        modalsWithTabs.forEach(modal => {
            const tabs = modal.querySelectorAll('.tabs__button');
            const tabContents = modal.querySelectorAll('.tab-content');

            if (tabs.length > 0 && tabContents.length > 0) {
                tabs.forEach(tab => {
                    tab.addEventListener('click', () => {
                        // Dentro del modal actual, desactivar todas las pestañas y contenidos
                        tabs.forEach(t => t.classList.remove('tabs__button--active'));
                        tabContents.forEach(c => c.classList.remove('tab-content--active'));

                        // Activar la pestaña y el contenido seleccionados
                        tab.classList.add('tabs__button--active');
                        const targetContentId = `tab-${tab.dataset.tab}`;
                        const targetContent = modal.querySelector(`#${targetContentId}`);
                        if (targetContent) {
                            targetContent.classList.add('tab-content--active');
                        }
                    });
                });
            }
        });

        document.getElementById('newReasonBtn').addEventListener('click', () => toggleNestedForm('newReasonForm', true));
        document.getElementById('cancelReasonBtn').addEventListener('click', () => toggleNestedForm('newReasonForm', false));
        document.getElementById('saveReasonBtn').addEventListener('click', handleNewReason);
        document.getElementById('newContactBtn').addEventListener('click', () => toggleNestedForm('newContactForm', true));
        document.getElementById('cancelContactBtn').addEventListener('click', () => toggleNestedForm('newContactForm', false));
        document.getElementById('saveContactBtn').addEventListener('click', handleNewContact);
        document.getElementById('newBankAccountBtn').addEventListener('click', () => toggleNestedForm('newBankAccountForm', true));
        document.getElementById('cancelBankAccountBtn').addEventListener('click', () => toggleNestedForm('newBankAccountForm', false));
        document.getElementById('saveBankAccountBtn').addEventListener('click', handleNewBankAccount);
        document.getElementById('clearSearchBtn').addEventListener('click', clearSearch);
        document.getElementById('viewAllTransactionsBtn').addEventListener('click', showAllTransactions);
        DOMElements.orderBySelect.addEventListener('change', handleSearchOrderChange);
        window.addEventListener('click', (event) => {
            if (event.target.classList.contains('modal')) {
                toggleModal(event.target.id, false);
            }
            // Cierra el menú de notificaciones si se hace clic fuera de él
            if (!DOMElements.notificationsMenu.hidden && !DOMElements.notificationsMenu.contains(event.target) && event.target.id !== 'notificationsBtn') {
                toggleNotificationsMenu(false);
            }
        });
    }

    function updateDashboard() {
        actualizarTarjetasResumen();
        renderRecentTransactions();
        initializeCharts();
        populateAllSelectors();
    }

    function updateBalance() {
        const balance = appState.currentBalance;
        const historicalBalanceAmountEl = document.getElementById('historicalBalanceAmount');
        const historicalBalanceValueEl = historicalBalanceAmountEl.querySelector('.balance__value');
        const historicalBalanceDate = document.getElementById('historicalBalanceDate');

        if (historicalBalanceValueEl) {
            historicalBalanceValueEl.textContent = balance.toLocaleString('es-AR');
        }
        if (historicalBalanceDate) {
            historicalBalanceDate.textContent = `Actualizado: ${new Date().toLocaleDateString('es-AR')}`;
        }

        // Aplicar clases de color de forma segura
        historicalBalanceAmountEl.classList.remove('balance__amount--positive', 'balance__amount--negative');
        if (balance >= 0) {
            historicalBalanceAmountEl.classList.add('balance__amount--positive');
        } else {
            historicalBalanceAmountEl.classList.add('balance__amount--negative');
        }
    }

    function renderRecentTransactions() {
        const container = DOMElements.recentTransactionsList;
        const recent = appState.transactions.slice(0, 6);
        renderTransactionList(container, recent);
    }

    function renderBankAccounts() {
        const container = DOMElements.bankAccountsList;
        container.innerHTML = '';
        if (appState.bankAccounts.length === 0) {
            container.innerHTML = '<p class="transactions-list__empty">No hay cuentas bancarias.</p>';
            return;
        }
        // Crear una copia invertida del array para no modificar el estado original
        const reversedAccounts = [...appState.bankAccounts].reverse();
        reversedAccounts.forEach(account => {
            const item = document.createElement('div');
            item.className = 'account-item';
            item.innerHTML = `
                <span class="account-item__name">${account.nombre}</span>
                <span class="account-item__meta">${account.entidadFinanciera}</span>
                <span class="account-item__balance">$ ${account.saldoActual.toLocaleString('es-AR')}</span>
            `;
            container.appendChild(item);
        });
    }

    function renderTransactionList(container, transactions) {
        container.innerHTML = '';
        if (transactions.length === 0) {
            container.innerHTML = '<p class="transactions-list__empty">No se encontraron transacciones.</p>';
            return;
        }
        transactions.forEach(transaction => {
            const item = createTransactionElement(transaction);
            container.appendChild(item);
        });
    }

    function createTransactionElement(transaction) {
        const item = document.createElement('div');
        item.className = 'transaction-item';
        item.dataset.transactionId = transaction.id;
        const isIncome = transaction.type === 'income';
        item.innerHTML = `
            <div class="transaction-item__info">
                <div class="transaction-item__details">
                    <span class="transaction-item__reason">${transaction.reason}</span>
                    <span class="transaction-item__meta">${parseUTCDate(transaction.date).toLocaleDateString('es-AR')} &bull; ${transaction.recipient || 'N/A'}</span>
                </div>
            </div>
            <div class="transaction-item__amount ${isIncome ? 'transaction-item__amount--income' : 'transaction-item__amount--expense'}">
                ${isIncome ? '+' : '-'}${transaction.amount.toLocaleString('es-AR')}
            </div>
        `;
        item.addEventListener('click', () => showTransactionDetails(transaction.id));
        return item;
    }

    function initializeCharts() {
        Object.values(appState.charts).forEach(chart => chart?.destroy());
        createMonthlyChart();
        createExpensesChart();
        createBalanceTrendChart();
    }

    function createMonthlyChart() {
        const ctx = document.getElementById('monthlyChart').getContext('2d');
        const data = { labels: [], datasets: [ { label: 'Ingresos', data: [], backgroundColor: '#65548e' }, { label: 'Gastos', data: [], backgroundColor: '#20015d' } ] };
        appState.charts.monthly = new Chart(ctx, { type: 'bar', data, options: getChartOptions() });
    }

    function createExpensesChart() {
        const ctx = document.getElementById('expensesChart').getContext('2d');
        const data = { 
            labels: [], 
            datasets: [{ 
                data: [], 
                backgroundColor: []
            }] 
        }; 
        appState.charts.expenses = new Chart(ctx, { type: 'doughnut', data, options: getChartOptions('doughnut') });
    }

    /**
     * Genera una paleta de colores en una gama de púrpuras y lilas.
     * @param {number} count - El número de colores a generar.
     * @returns {string[]} Un array de colores en formato HSL.
     */
    function generatePurplePalette(count) {
        const colors = [];
        const baseHue = 270; // Tono base para el púrpura
        const saturation = 75; // Saturación constante para colores vibrantes
        const startLightness = 30; // Comienza con un púrpura oscuro (#20015d)
        const endLightness = 85;   // Termina con un púrpura claro (#ebddfe)
        const step = count > 1 ? (endLightness - startLightness) / (count - 1) : 0;

        for (let i = 0; i < count; i++) {
            const lightness = startLightness + (i * step);
            colors.push(`hsl(${baseHue}, ${saturation}%, ${lightness}%)`);
        }
        return colors;
    }

    function createBalanceTrendChart() {
        const ctx = document.getElementById('balanceTrendChart').getContext('2d');
        const data = { labels: [], datasets: [{ label: 'Saldo Acumulado', data: [], borderColor: '#20015d', tension: 0.4, fill: true, backgroundColor: 'rgba(3, 50, 79, 0.1)' }] };
        appState.charts.balanceTrend = new Chart(ctx, { type: 'line', data, options: getChartOptions('line') });
    }

    function getChartOptions(type = 'bar') {
        const options = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: type === 'doughnut' ? 'bottom' : 'top' },
                tooltip: { 
                    callbacks: { 
                        label: (c) => `${c.dataset.label || c.label}: ${(c.raw.y || c.raw).toLocaleString('es-AR')}` 
                    } 
                }
            }
        };
        if (type === 'line') {
            options.scales = {
                x: {
                    type: 'category', // Corregido de 'time' a 'category'
                    title: {
                        display: true,
                        text: 'Mes' // Cambiado de 'Fecha' a 'Mes'
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: 'Saldo Acumulado ($)'
                    }
                }
            };
        }
        return options;
    }

    function toggleModal(modalId, show) {
        const modal = document.getElementById(modalId);
        if (show) {
            modal.hidden = false;
            // Resetear formularios al abrir
            if (modalId === 'transactionModal') DOMElements.transactionForm.reset();
            if (modalId === 'budgetModal') DOMElements.budgetForm.reset();
            if (modalId === 'manageWorkspacesModal') {
                DOMElements.workspaceForm.reset();
                DOMElements.shareForm.reset();
            }

            // Caso especial para el modal de búsqueda
            if (modalId === 'searchModal') {
                DOMElements.searchForm.reset();
                DOMElements.searchYearInput.value = new Date().getFullYear();
            }
        } else {
            modal.hidden = true;
        }
    }

    function toggleNestedForm(formId, show) {
        const form = document.getElementById(formId);
        form.hidden = !show;
        if (!show) {
            const input = form.querySelector('input');
            if (input) input.value = '';
        }
    }

    function handleTransactionSubmit(event) {
        event.preventDefault();
        const formData = new FormData(event.target);

        const transactionType = formData.get('transactionType');
        const transactionDate = formData.get('transactionDate');
        const amount = parseFloat(formData.get('amount'));
        const idMotivo = formData.get('reason'); // idMotivo
        const idContacto = formData.get('recipient'); // idContacto (opcional)
        const idCuentaBancaria = formData.get('bankAccount'); // idCuentaBancaria (opcional)
        const description = formData.get('description');

        const idEspacioTrabajo = document.getElementById('workspaceSelect').value;
        const nombreCompletoAuditoria = document.querySelector('.side-menu__username').textContent; // Obtener del menú de usuario

        // Validaciones de campos obligatorios
        if (!idEspacioTrabajo) {
            showNotification('Por favor, seleccione un espacio de trabajo.', 'error');
            return;
        }
        if (!transactionType || !transactionDate || isNaN(amount) || amount <= 0 || !idMotivo) {
            showNotification('Por favor, complete todos los campos obligatorios (Tipo, Fecha, Monto, Motivo).', 'error');
            return;
        }

        const transaccionData = {
            fecha: transactionDate,
            monto: amount,
            tipo: transactionType === 'income' ? 'INGRESO' : 'GASTO', // Mapear a los valores del enum de Java
            descripcion: description || null, // Si es vacío, enviar null
            nombreCompletoAuditoria: nombreCompletoAuditoria,
            idEspacioTrabajo: idEspacioTrabajo,
            idMotivo: idMotivo,
            idContacto: idContacto || null, // Si es vacío, enviar null
            idCuentaBancaria: idCuentaBancaria || null // Si es vacío, enviar null
        };

        fetch('/transaccion/registrar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(transaccionData)
        })
        .then(response => {
            if (response.ok) {
                return response.json(); // Asumiendo que la API devuelve el nuevo saldo o la transacción completa
            }
            throw new Error('Error al registrar la transacción.');
        })
        .then(transaccionGuardada => {
            // Actualizar el saldo directamente con el monto de la transacción
            if (transaccionGuardada.tipo === 'INGRESO') {
                appState.currentBalance += transaccionGuardada.monto;
            } else if (transaccionGuardada.tipo === 'GASTO') {
                appState.currentBalance -= transaccionGuardada.monto;
            }

            // Actualizar el saldo del espacio de trabajo en appState.workspaces
            const currentWorkspaceId = document.getElementById('workspaceSelect').value;
            const currentWorkspace = appState.workspaces.find(ws => ws.id == currentWorkspaceId);
            if (currentWorkspace) {
                currentWorkspace.saldo = appState.currentBalance;
            }

            updateBalance();

            // Añadir la nueva transacción a la lista de recientes
            const reasonObj = appState.reasons.find(r => r.id == transaccionGuardada.idMotivo);
            const contactObj = appState.contacts.find(c => c.id == transaccionGuardada.idContacto);
            const bankAccountObj = appState.bankAccounts.find(b => b.id == transaccionGuardada.idCuentaBancaria);
            const newUITransaction = {
                id: transaccionGuardada.id,
                type: transaccionGuardada.tipo === 'INGRESO' ? 'income' : 'expense',
                amount: transaccionGuardada.monto,
                reason: reasonObj ? reasonObj.motivo : 'N/A',
                recipient: contactObj ? contactObj.nombre : null,
                description: transaccionGuardada.descripcion,
                date: transaccionGuardada.fecha,
                fechaCreacion: transaccionGuardada.fechaCreacion, // Corregido: Añadir fecha de creación
                nombreCompletoAuditoria: transaccionGuardada.nombreCompletoAuditoria, // Corregido: Añadir usuario de auditoría
                cuentaBancaria: bankAccountObj ? bankAccountObj.nombre : null
            };


            appState.transactions.unshift(newUITransaction);
            if (appState.transactions.length > 6) {
                appState.transactions.pop();
            }
            renderRecentTransactions();

            // Invalidar la caché para este espacio de trabajo
            delete appState.dashboardCache[currentWorkspaceId];

            // Actualizar el dashboard y las cuentas bancarias
            actualizarDashboard(currentWorkspaceId);
            cargarCuentasBancarias(currentWorkspaceId);

            toggleModal('transactionModal', false);
            showNotification('Transacción guardada con éxito', 'success');
        })
        .catch(error => {
            console.error('Error:', error);
            showNotification('Error al guardar la transacción', 'error');
        });
    }

    function handleTransferSubmit(event) {
        event.preventDefault();
        const idCuentaOrigen = document.getElementById('transferSourceAccount').value;
        const idCuentaDestino = document.getElementById('transferDestinationAccount').value;
        const monto = parseFloat(document.getElementById('transferAmount').value);

        if (!idCuentaOrigen || !idCuentaDestino || isNaN(monto) || monto <= 0) {
            showNotification('Por favor, complete todos los campos.', 'error');
            return;
        }

        if (idCuentaOrigen === idCuentaDestino) {
            showNotification('La cuenta de origen y destino no pueden ser la misma.', 'error');
            return;
        }

        fetch(`/cuentabancaria/transaccion/${idCuentaOrigen}/${idCuentaDestino}/${monto}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                toggleModal('transactionModal', false);
                showNotification('Transferencia realizada con éxito', 'success');
                const idEspacioTrabajo = document.getElementById('workspaceSelect').value;
                cargarCuentasBancarias(idEspacioTrabajo);
                cargarTransaccionesRecientes(idEspacioTrabajo);
                actualizarDashboard(idEspacioTrabajo);
            } else {
                throw new Error('Error al realizar la transferencia.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showNotification(error.message, 'error');
        });
    }

    function showTransactionDetails(transactionId) {
        DOMElements.transactionDetailContent.innerHTML = ''; // Limpiar contenido anterior
        const transaction = appState.transactions.find(t => t.id === transactionId) || appState.lastSearchResults.find(t => t.id === transactionId);

        if (!transaction) {
            showNotification('No se pudo encontrar la transacción.', 'error');
            return;
        }

        const content = DOMElements.transactionDetailContent;
        const isIncome = transaction.type === 'income';

        const formattedDate = parseUTCDate(transaction.date).toLocaleDateString('es-AR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });

        let auditHTML = '';
        if (transaction.fechaCreacion && transaction.nombreCompletoAuditoria) {
            const formattedCreationDate = new Date(transaction.fechaCreacion).toLocaleString('es-AR', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });

            auditHTML = `
                <h3 class="transaction-detail__audit-title">Auditoría</h3>
                <div class="transaction-detail__item">
                    <span class="transaction-detail__label">Fecha de Creación</span>
                    <span class="transaction-detail__value">${formattedCreationDate} hs</span>
                </div>
                <div class="transaction-detail__item">
                    <span class="transaction-detail__label">Usuario</span>
                    <span class="transaction-detail__value">${transaction.nombreCompletoAuditoria}</span>
                </div>
            `;
        }

        content.innerHTML = `
            <div class="transaction-detail__item">
                <span class="transaction-detail__label">Tipo</span>
                <span class="transaction-detail__value ${isIncome ? 'transaction-detail__value--income' : 'transaction-detail__value--expense'}">${isIncome ? 'Ingreso' : 'Gasto'}</span>
            </div>
            <div class="transaction-detail__item">
                <span class="transaction-detail__label">Fecha</span>
                <span class="transaction-detail__value">${formattedDate}</span>
            </div>
            <div class="transaction-detail__item">
                <span class="transaction-detail__label">Motivo</span>
                <span class="transaction-detail__value">${transaction.reason}</span>
            </div>
            <div class="transaction-detail__item">
                <span class="transaction-detail__label">Cuenta Bancaria</span>
                <span class="transaction-detail__value">${transaction.cuentaBancaria || '-'}</span>
            </div>
            <div class="transaction-detail__item">
                <span class="transaction-detail__label">Contacto</span>
                <span class="transaction-detail__value">${transaction.recipient || '-'}</span>
            </div>
            <div class="transaction-detail__item">
                <span class="transaction-detail__label">Descripción</span>
                <span class="transaction-detail__value">${transaction.description || '-'}</span>
            </div>
            <div class="transaction-detail__item">
                <span class="transaction-detail__label">Monto</span>
                <span class="transaction-detail__value ${isIncome ? 'transaction-detail__value--income' : 'transaction-detail__value--expense'}">
                    $ ${transaction.amount.toLocaleString('es-AR')}
                </span>
            </div>
            ${auditHTML}
        `;

        toggleModal('transactionDetailModal', true);
        DOMElements.transactionDetailModal.dataset.transactionId = transaction.id;
    }

    function handleSearchSubmit(event) {
        event.preventDefault();
        const idEspacioTrabajo = document.getElementById('workspaceSelect').value;

        if (!idEspacioTrabajo) {
            showNotification('Por favor, seleccione un espacio de trabajo.', 'error');
            return;
        }

        const reasonSelect = DOMElements.searchForm.searchReason;
        const recipientSelect = DOMElements.searchForm.searchRecipient;

        const searchData = {
            mes: DOMElements.searchForm.searchMonth.value ? parseInt(DOMElements.searchForm.searchMonth.value) : null,
            anio: DOMElements.searchForm.searchYear.value ? parseInt(DOMElements.searchForm.searchYear.value) : null,
            motivo: reasonSelect.value ? reasonSelect.options[reasonSelect.selectedIndex].textContent : null,
            contacto: recipientSelect.value ? recipientSelect.options[recipientSelect.selectedIndex].textContent : null,
            idEspacioTrabajo: parseInt(idEspacioTrabajo)
        };

        fetch('/transaccion/buscar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(searchData)
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Error al buscar las transacciones.');
        })
        .then(transactions => {
            appState.lastSearchResults = transactions.map(t => ({
                id: t.id,
                type: t.tipo === 'INGRESO' ? 'income' : 'expense',
                amount: t.monto,
                reason: t.nombreMotivo,
                recipient: t.nombreContacto,
                description: t.descripcion,
                date: t.fecha,
                fechaCreacion: t.fechaCreacion,
                nombreCompletoAuditoria: t.nombreCompletoAuditoria,
                cuentaBancaria: t.nombreCuentaBancaria
            }));
            applySearchOrder();
            showNotification(`Se encontraron ${transactions.length} transacciones.`, 'success');
        })
        .catch(error => {
            console.error('Error:', error);
            showNotification(error.message, 'error');
            DOMElements.searchResultsContainer.innerHTML = '<p class="transactions-list__empty">Error al cargar los resultados.</p>';
            // Resetear totales en caso de error
            const totalIngresosEl = document.getElementById('total-ingresos');
            const totalGastosEl = document.getElementById('total-gastos');
            if (totalIngresosEl && totalGastosEl) {
                totalIngresosEl.textContent = '0,00';
                totalGastosEl.textContent = '0,00';
            }
        });
    }

    function handleWorkspaceSubmit(event) {
        event.preventDefault();
        const workspaceNameInput = document.getElementById('workspaceName');
        const workspaceName = workspaceNameInput.value.trim();
        const errorElement = document.getElementById('workspaceNameError');

        if (workspaceName) {
            errorElement.hidden = true;
            const workspaceData = {
                nombre: workspaceName,
                idUsuarioAdmin: appState.authenticatedUserId
            };

            console.log('Enviando datos:', JSON.stringify(workspaceData));

            fetch('/espaciotrabajo/registrar', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(workspaceData)
            })
            .then(response => {
                if (!response.ok) {
                    // Si la respuesta no es OK, lanzamos un error para que lo capture el .catch
                    throw new Error('Error en la solicitud al servidor');
                }
                // No es necesario procesar un cuerpo JSON si la respuesta es exitosa pero vacía
            })
            .then(() => {
                // Limpiar el campo de texto
                workspaceNameInput.value = '';
                // Cerrar el modal
                toggleModal('manageWorkspacesModal', false);
                // Mostrar notificación de éxito
                showNotification(`Espacio "${workspaceName}" creado con éxito`, 'success');
                // Actualizar la lista de espacios de trabajo en la UI
                loadWorkspacesForUser(appState.authenticatedUserId); 
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Error al crear el espacio de trabajo', 'error');
            });
        } else {
            errorElement.hidden = false;
            showNotification('Por favor, ingrese un nombre para el espacio de trabajo', 'error');
        }
    }

    function handleShareSubmit(event) {
        event.preventDefault();
        const workspaceId = DOMElements.shareWorkspaceSelect.value;
        const emailInput = document.getElementById('shareEmail');
        const email = emailInput.value.trim();

        if (!workspaceId) {
            showNotification('Por favor, seleccione un espacio de trabajo.', 'error');
            return;
        }

        if (!email) {
            showNotification('Por favor, ingrese un email válido.', 'error');
            return;
        }

        if (email.length > 100) {
            showNotification('El email no puede exceder los 100 caracteres.', 'error');
            return;
        }

        if (!appState.authenticatedUserId) {
            showNotification('Error: ID de usuario autenticado no disponible.', 'error');
            console.error('Error: appState.authenticatedUserId es null o undefined.');
            return;
        }

        const idUsuarioAdmin = appState.authenticatedUserId;

        fetch(`/espaciotrabajo/compartir/${email}/${workspaceId}/${idUsuarioAdmin}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            // No se envía body ya que los datos van en la URL
        })
        .then(response => {
            if (response.ok) {
                // Limpiar campos y cerrar modal
                DOMElements.shareWorkspaceSelect.value = '';
                emailInput.value = '';
                toggleModal('manageWorkspacesModal', false);
                showNotification(`Invitación enviada a ${email} para el espacio de trabajo.`, 'success');
            } else {
                throw new Error('Error al compartir el espacio de trabajo.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showNotification('Error al compartir el espacio de trabajo.', 'error');
        });
    }

    function handleBudgetSubmit(event) {
        event.preventDefault();
        toggleModal('budgetModal', false);
        showNotification('¡Esta función se implementará pronto!', 'info');
    }

    function populateAllSelectors() {
        // Ya no se necesita poblar los selectores de contactos desde aquí
    }

    function populateShareWorkspaceSelect() {
        const selectElement = DOMElements.shareWorkspaceSelect;
        selectElement.innerHTML = '<option value="">Seleccionar espacio de trabajo</option>';
        appState.workspaces.forEach(workspace => {
            const option = document.createElement('option');
            option.value = workspace.id;
            option.textContent = workspace.nombre;
            selectElement.appendChild(option);
        });
    }

    function populateSelector(selectElement, options, placeholder) {
        selectElement.innerHTML = `<option value="">${placeholder}</option>`;
        options.forEach(option => {
            const opt = document.createElement('option');
            opt.value = option;
            opt.textContent = option;
            selectElement.appendChild(opt);
        });
    }

    function cargarMotivos(idEspacioTrabajo) {
        fetch(`/transaccion/motivo/listar/${idEspacioTrabajo}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('No se pudieron cargar los motivos.');
                }
                return response.json();
            })
            .then(motivos => {
                appState.reasons = motivos;
                populateMotivoSelector(DOMElements.reasonSelect, 'Seleccionar motivo');
                populateMotivoSelector(DOMElements.searchReasonSelect, 'Todos los motivos');
                populateMotivoSelector(DOMElements.budgetReasonSelect, 'Seleccionar motivo');
            })
            .catch(error => {
                console.error('Error al cargar motivos:', error);
                showNotification('Error al cargar los motivos', 'error');
            });
    }

    function clearMotivos() {
        appState.reasons = [];
        populateMotivoSelector(DOMElements.reasonSelect, 'Seleccionar motivo');
        populateMotivoSelector(DOMElements.searchReasonSelect, 'Todos los motivos');
        populateMotivoSelector(DOMElements.budgetReasonSelect, 'Seleccionar motivo');
    }

    function populateMotivoSelector(selectElement, placeholder) {
        selectElement.innerHTML = `<option value="">${placeholder}</option>`;
        appState.reasons.forEach(motivo => {
            const opt = document.createElement('option');
            opt.value = motivo.id; // Usar el ID del motivo como valor
            opt.textContent = motivo.motivo; // Mostrar el nombre del motivo
            selectElement.appendChild(opt);
        });
    }

    function addReasonToSelectors(reason) {
        appState.reasons.push(reason);
        const option = document.createElement('option');
        option.value = reason.id;
        option.textContent = reason.motivo;
        DOMElements.reasonSelect.appendChild(option.cloneNode(true));
        DOMElements.searchReasonSelect.appendChild(option.cloneNode(true));
        DOMElements.budgetReasonSelect.appendChild(option.cloneNode(true));
    }

    function cargarContactos(idEspacioTrabajo) {
        fetch(`/transaccion/contacto/listar/${idEspacioTrabajo}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('No se pudieron cargar los contactos.');
                }
                return response.json();
            })
            .then(contactos => {
                appState.contacts = contactos;
                populateContactoSelector(DOMElements.contactSelect, 'Seleccionar contacto');
                populateContactoSelector(DOMElements.searchContactSelect, 'Todos los contactos');
            })
            .catch(error => {
                console.error('Error al cargar contactos:', error);
                showNotification('Error al cargar los contactos', 'error');
            });
    }

    function clearContactos() {
        appState.contacts = [];
        populateContactoSelector(DOMElements.contactSelect, 'Seleccionar contacto');
        populateContactoSelector(DOMElements.searchContactSelect, 'Todos los contactos');
    }

    function populateContactoSelector(selectElement, placeholder) {
        selectElement.innerHTML = `<option value="">${placeholder}</option>`;
        appState.contacts.forEach(contacto => {
            const opt = document.createElement('option');
            opt.value = contacto.id;
            opt.textContent = contacto.nombre;
            selectElement.appendChild(opt);
        });
    }

    function addContactToSelectors(contact) {
        appState.contacts.push(contact);
        const option = document.createElement('option');
        option.value = contact.id;
        option.textContent = contact.nombre;
        DOMElements.contactSelect.appendChild(option.cloneNode(true));
        DOMElements.searchContactSelect.appendChild(option.cloneNode(true));
    }

    function cargarCuentasBancarias(idEspacioTrabajo) {
        fetch(`/cuentabancaria/listar/${idEspacioTrabajo}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('No se pudieron cargar las cuentas bancarias.');
                }
                return response.json();
            })
            .then(cuentas => {
                appState.bankAccounts = cuentas;
                populateCuentaBancariaSelector(DOMElements.bankAccountSelect, 'Seleccionar cuenta');
                renderBankAccounts(); // Renderiza la lista de cuentas
            })
            .catch(error => {
                console.error('Error al cargar cuentas bancarias:', error);
                showNotification('Error al cargar las cuentas bancarias', 'error');
            });
    }

    function clearCuentasBancarias() {
        appState.bankAccounts = [];
        populateCuentaBancariaSelector(DOMElements.bankAccountSelect, 'Seleccionar cuenta');
        renderBankAccounts(); // Limpia y renderiza la lista de cuentas vacía
    }

    function populateCuentaBancariaSelector(selectElement, placeholder) {
        selectElement.innerHTML = `<option value="">${placeholder}</option>`;
        appState.bankAccounts.forEach(cuenta => {
            const opt = document.createElement('option');
            opt.value = cuenta.id;
            opt.textContent = cuenta.nombre;
            selectElement.appendChild(opt);
        });

        // Poblar también los selects del formulario de transferencia
        const transferSourceSelect = document.getElementById('transferSourceAccount');
        const transferDestinationSelect = document.getElementById('transferDestinationAccount');
        if (transferSourceSelect && transferDestinationSelect) {
            transferSourceSelect.innerHTML = `<option value="">Seleccionar cuenta de origen</option>`;
            transferDestinationSelect.innerHTML = `<option value="">Seleccionar cuenta de destino</option>`;
            appState.bankAccounts.forEach(cuenta => {
                const opt = document.createElement('option');
                opt.value = cuenta.id;
                opt.textContent = cuenta.nombre;
                transferSourceSelect.appendChild(opt.cloneNode(true));
                transferDestinationSelect.appendChild(opt.cloneNode(true));
            });
        }
    }

    function addBankAccountToSelector(cuenta) {
        appState.bankAccounts.push(cuenta);
        const option = document.createElement('option');
        option.value = cuenta.id;
        option.textContent = cuenta.nombre;
        DOMElements.bankAccountSelect.appendChild(option.cloneNode(true));
    }

    function populateFinancialEntities() {
        const select = DOMElements.financialEntitySelect;
        const entidades = [
            'Banco Credicoop',
            'Banco de Santa Fe',
            'Banco Macro',
            'Banco Patagonia',
            'Banco Santander',
            'BBVA',
            'BNA',
            'Brubank',
            'Galicia',
            'HSBC',
            'ICBC',
            'Lemon Cash',
            'Mercado Pago',
            'Naranja X',
            'Personal Pay',
            'Ualá'
        ];

        entidades.forEach(entidad => {
            const option = document.createElement('option');
            option.value = entidad;
            option.textContent = entidad;
            select.appendChild(option);
        });
    }

    function handleNewBankAccount() {
        const accountName = DOMElements.newAccountNameInput.value.trim();
        const entity = DOMElements.financialEntitySelect.value;
        const idEspacioTrabajo = document.getElementById('workspaceSelect').value;

        if (!idEspacioTrabajo) {
            showNotification('Por favor, seleccione un espacio de trabajo primero', 'error');
            return;
        }

        if (accountName && entity) {
            const cuentaData = {
                nombre: accountName,
                entidadFinanciera: entity,
                idEspacioTrabajo: idEspacioTrabajo
            };

            fetch('/cuentabancaria/crear', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(cuentaData)
            })
            .then(response => {
                if (response.ok) {
                    toggleNestedForm('newBankAccountForm', false);
                    showNotification('Cuenta bancaria guardada con éxito', 'success');
                    cargarCuentasBancarias(idEspacioTrabajo); // Recargar la lista
                } else {
                    throw new Error('Error al registrar la cuenta bancaria.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Error al guardar la cuenta bancaria', 'error');
            });
        } else {
            showNotification('Por favor, ingrese un nombre y seleccione una entidad', 'error');
        }
    }

    function handleNewReason() {
        const newReason = DOMElements.newReasonInput.value.trim();
        const idEspacioTrabajo = document.getElementById('workspaceSelect').value;

        if (!idEspacioTrabajo) {
            showNotification('Por favor, seleccione un espacio de trabajo primero', 'error');
            return;
        }

        if (newReason) {
            const motivoData = {
                motivo: newReason,
                idEspacioTrabajo: idEspacioTrabajo
            };

            fetch('/transaccion/motivo/registrar', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(motivoData)
            })
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error('Error al registrar el motivo.');
            })
            .then(motivoGuardado => {
                toggleNestedForm('newReasonForm', false);
                showNotification('Motivo guardado con éxito', 'success');
                addReasonToSelectors(motivoGuardado);
                DOMElements.reasonSelect.value = motivoGuardado.id;
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Error al guardar el motivo', 'error');
            });
        } else {
            showNotification('Por favor, ingrese un nombre para el motivo', 'error');
        }
    }

    function handleNewContact() {
        const newContactName = DOMElements.newContactInput.value.trim();
        const idEspacioTrabajo = document.getElementById('workspaceSelect').value;

        if (!idEspacioTrabajo) {
            showNotification('Por favor, seleccione un espacio de trabajo primero', 'error');
            return;
        }

        if (newContactName) {
            const contactoData = {
                nombre: newContactName,
                idEspacioTrabajo: idEspacioTrabajo
            };

            fetch('/transaccion/contacto/registrar', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(contactoData)
            })
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error('Error al registrar el contacto.');
            })
            .then(contactoGuardado => {
                toggleNestedForm('newContactForm', false);
                showNotification('Contacto guardado con éxito', 'success');
                addContactToSelectors(contactoGuardado);
                DOMElements.contactSelect.value = contactoGuardado.id;
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Error al guardar el contacto', 'error');
            });
        } else {
            showNotification('Por favor, ingrese un nombre para el contacto', 'error');
        }
    }

    function renderAlertsList() {
        const container = DOMElements.alertsListContainer;
        container.innerHTML = '';
        if (appState.budgets.length === 0) {
            container.innerHTML = '<p>No hay alertas configuradas.</p>';
            return;
        }
        appState.budgets.forEach(budget => {
            const item = document.createElement('div');
            item.className = 'alert-item';
            item.innerHTML = `
                <span class="alert-item__info">
                    ${budget.reason}:
                    <span class="alert-item__threshold">$${budget.threshold.toLocaleString('es-AR')}</span>
                </span>
                <button class="alert-item__delete-btn" data-id="${budget.id}"><i class="fas fa-trash"></i></button>
            `;
            item.querySelector('.alert-item__delete-btn').addEventListener('click', (e) => {
                const id = parseInt(e.currentTarget.dataset.id);
                appState.budgets = appState.budgets.filter(b => b.id !== id);
                renderAlertsList();
            });
            container.appendChild(item);
        });
    }

    function clearSearch() {
        DOMElements.searchForm.reset();
        DOMElements.searchResultsContainer.innerHTML = '';
        appState.lastSearchResults = [];

        // Resetear totales
        const totalIngresosEl = document.getElementById('total-ingresos');
        const totalGastosEl = document.getElementById('total-gastos');
        if (totalIngresosEl && totalGastosEl) {
            totalIngresosEl.textContent = '0,00';
            totalGastosEl.textContent = '0,00';
        }
    }

    function showAllTransactions() {
        appState.lastSearchResults = [...appState.transactions];
        applySearchOrder();
        toggleModal('searchModal', true);
    }

    function handleSearchOrderChange() {
        applySearchOrder();
    }

    function cargarTransaccionesRecientes(idEspacioTrabajo) {
        fetch(`/transaccion/buscarRecientes/${idEspacioTrabajo}`)
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Error al cargar las transacciones recientes.');
        })
        .then(transactions => {
            appState.transactions = transactions.map(t => ({
                id: t.id,
                type: t.tipo === 'INGRESO' ? 'income' : 'expense',
                amount: t.monto,
                reason: t.nombreMotivo,
                recipient: t.nombreContacto,
                description: t.descripcion,
                date: t.fecha,
                fechaCreacion: t.fechaCreacion,
                nombreCompletoAuditoria: t.nombreCompletoAuditoria,
                cuentaBancaria: t.nombreCuentaBancaria
            }));
            renderRecentTransactions();
        })
        .catch(error => {
            console.error('Error:', error);
            showNotification(error.message, 'error');
            DOMElements.recentTransactionsList.innerHTML = '<p class="transactions-list__empty">Error al cargar transacciones.</p>';
        });
    }

    function applySearchOrder() {
        const order = DOMElements.orderBySelect.value;
        let sorted = [...appState.lastSearchResults];
        switch (order) {
            case 'date-asc': sorted.sort((a, b) => parseUTCDate(a.date) - parseUTCDate(b.date)); break;
            case 'date-desc': sorted.sort((a, b) => parseUTCDate(b.date) - parseUTCDate(a.date)); break;
            case 'amount-asc': sorted.sort((a, b) => a.amount - b.amount); break;
            case 'amount-desc': sorted.sort((a, b) => b.amount - a.amount); break;
        }
        renderTransactionList(DOMElements.searchResultsContainer, sorted);

        // Calcular y mostrar los totales
        let totalIngresos = 0;
        let totalGastos = 0;

        sorted.forEach(transaction => {
            if (transaction.type === 'income') {
                totalIngresos += transaction.amount;
            } else if (transaction.type === 'expense') {
                totalGastos += transaction.amount;
            }
        });

        const totalIngresosEl = document.getElementById('total-ingresos');
        const totalGastosEl = document.getElementById('total-gastos');

        if (totalIngresosEl && totalGastosEl) {
            totalIngresosEl.textContent = totalIngresos.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
            totalGastosEl.textContent = totalGastos.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        }
    }

    function showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `notification notification--${type}`;
        notification.textContent = message;
        DOMElements.notificationContainer.appendChild(notification);
        setTimeout(() => {
            notification.remove();
        }, 3000);
    }

    

    /**
     * Convierte una fecha en formato 'YYYY-MM' a un nombre de mes abreviado en español.
     * @param {string} fechaString - La fecha en formato 'YYYY-MM'.
     * @returns {string} El nombre del mes abreviado (ej: 'Ene').
     */
    function formatearMes(fechaString) {
        const [year, month] = fechaString.split('-');
        const fecha = new Date(year, month - 1);
        const monthName = fecha.toLocaleDateString('es-ES', { month: 'long' });
        return monthName.charAt(0).toUpperCase() + monthName.slice(1);
    }

    /**
     * Obtiene los datos del dashboard desde la API y actualiza los gráficos.
     * @param {number} idEspacio - El ID del espacio de trabajo.
     */
    function actualizarDashboard(idEspacio) {
        if (!idEspacio) return;

        // 1. Comprobar la caché primero
        if (appState.dashboardCache[idEspacio]) {
            const cachedData = appState.dashboardCache[idEspacio];
            actualizarGraficoIngresosGastos(cachedData.ingresosGastos);
            actualizarGraficoDistribucionGastos(cachedData.distribucionGastos);
            actualizarGraficoTendenciaSaldo(cachedData.saldoAcumuladoMes);
            showNotification('Datos del dashboard cargados desde caché', 'info');
            return; // Salir de la función si los datos están en caché
        }

        // 2. Si no están en caché, hacer la llamada a la API
        fetch(`/transaccion/dashboardinfo/${idEspacio}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('No se pudieron cargar los datos del dashboard.');
                }
                return response.json();
            })
            .then(dashboardData => {
                // 3. Guardar los datos en la caché
                appState.dashboardCache[idEspacio] = dashboardData;

                // 4. Actualizar los gráficos
                actualizarGraficoIngresosGastos(dashboardData.ingresosGastos);
                actualizarGraficoDistribucionGastos(dashboardData.distribucionGastos);
                actualizarGraficoTendenciaSaldo(dashboardData.saldoAcumuladoMes);
            })
            .catch(error => {
                console.error('Error al actualizar el dashboard:', error);
                showNotification('Error al cargar los datos del dashboard', 'error');
            });
    }

    /**
     * Actualiza el gráfico de ingresos vs. gastos.
     * @param {Array<object>} data - Los datos de ingresos y gastos por mes.
     */
    function actualizarGraficoIngresosGastos(data) {
        const chart = appState.charts.monthly;
        if (!chart) return;

        const labels = data.map(d => formatearMes(d.mes));
        const ingresos = data.map(d => d.ingresos);
        const gastos = data.map(d => d.gastos);

        chart.data.labels = labels;
        chart.data.datasets[0].data = ingresos;
        chart.data.datasets[1].data = gastos;
        chart.update();

        // Llama a la función para actualizar las tarjetas de resumen
        actualizarTarjetasResumen();
    }

    /**
     * Actualiza el gráfico de distribución de gastos.
     * @param {Array<object>} data - Los datos de distribución de gastos por motivo.
     */
    function actualizarGraficoDistribucionGastos(data) {
        const chart = appState.charts.expenses;
        if (!chart) return;

        const labels = data.map(d => d.motivo);
        const porcentajes = data.map(d => d.porcentaje);

        chart.data.labels = labels;
        chart.data.datasets[0].data = porcentajes;
        // Generar nuevos colores para la cantidad actual de categorías
        chart.data.datasets[0].backgroundColor = generatePurplePalette(labels.length);
        chart.update();
    }

    /**
     * Actualiza el gráfico de tendencia de saldo acumulado.
     * @param {Array<object>} data - Los datos de saldo acumulado por mes.
     */
    function actualizarGraficoTendenciaSaldo(data) {
        const chart = appState.charts.balanceTrend;
        if (!chart) return;

        const labels = data.map(d => formatearMes(d.mes));
        const saldos = data.map(d => d.saldoAcumulado);

        chart.data.labels = labels;
        chart.data.datasets[0].data = saldos;
        chart.update();
    }

    function actualizarTarjetasResumen() {
        const monthlyData = appState.charts.monthly?.data;
        if (!monthlyData || monthlyData.labels.length === 0) {
            // Si no hay datos, resetea las tarjetas a 0 o a un estado inicial
            return;
        }

        const lastIndex = monthlyData.labels.length - 1;
        const lastMonthLabel = monthlyData.labels[lastIndex];
        const income = monthlyData.datasets[0].data[lastIndex];
        const expense = monthlyData.datasets[1].data[lastIndex];
        const net = income - expense;

        // Actualizar tarjeta de Ingresos
        document.getElementById('incomeAmount').querySelector('.balance__value').textContent = income.toLocaleString('es-AR');
        document.getElementById('incomeMonth').textContent = lastMonthLabel;

        // Actualizar tarjeta de Gastos
        document.getElementById('expenseAmount').querySelector('.balance__value').textContent = expense.toLocaleString('es-AR');
        document.getElementById('expenseMonth').textContent = lastMonthLabel;

        // Actualizar tarjeta de Saldo Neto
        const netAmountEl = document.getElementById('netAmount');
        netAmountEl.querySelector('.balance__value').textContent = net.toLocaleString('es-AR');
        document.getElementById('netMonth').textContent = lastMonthLabel;
        netAmountEl.className = `balance__amount ${net >= 0 ? 'balance__amount--positive' : 'balance__amount--negative'}`;

        // Actualizar tarjeta de Saldo Histórico (que ahora es manejada por updateBalance)
        updateBalance();
    }

    document.addEventListener('DOMContentLoaded', () => {
        if (document.getElementById('loginForm')) {
            initializeLoginPage();
        } else {
            initializeApp();
        }
    });

    function initializeLoginPage() {
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', handleLoginSubmit);
        }

        const googleLoginBtn = document.getElementById('googleLoginBtn');
        if (googleLoginBtn) {
            googleLoginBtn.addEventListener('click', () => {
                showNotification('Función no implementada todavía.', 'info');
            });
        }
    }

    function handleLoginSubmit(event) {
        event.preventDefault();
        const form = event.target;
        form.submit();
    }

    /**
     * Muestra u oculta el menú lateral.
     * @param {boolean} show - Indica si se debe mostrar u ocultar el menú.
     */
    function toggleSideMenu(show) {
        const sideMenu = DOMElements.sideMenu;
        if (show) {
            sideMenu.hidden = false;
            // Se usa un pequeño retardo para permitir que el navegador aplique `hidden = false`
            // antes de añadir la clase que inicia la transición.
            setTimeout(() => {
                sideMenu.classList.add('is-open');
            }, 10);
        } else {
            sideMenu.classList.remove('is-open');
            // Espera a que la transición termine para ocultar el elemento
            sideMenu.addEventListener('transitionend', () => {
                sideMenu.hidden = true;
            }, { once: true });
        }
    }

    /**
     * Muestra u oculta el menú de notificaciones.
     * @param {boolean} [forceShow] - Si se proporciona, fuerza la apertura o cierre del menú.
     */
    function toggleNotificationsMenu(forceShow) {
        const menu = DOMElements.notificationsMenu;
        const shouldShow = forceShow !== undefined ? forceShow : menu.hidden;

        menu.hidden = !shouldShow;
    }

    function handleRemoveTransaction() {
        const transactionId = DOMElements.transactionDetailModal.dataset.transactionId;
        if (!transactionId) {
            showNotification('No se pudo identificar la transacción a eliminar.', 'error');
            return;
        }

        const confirmed = confirm('¿Deseas borrar permanentemente esta transacción?');

        if (confirmed) {
            fetch(`/transaccion/remover/${transactionId}`, {
                method: 'DELETE'
            })
            .then(response => {
                if (response.ok) {
                    // Eliminar la transacción del estado local
                    const removedTransaction = appState.transactions.find(t => t.id == transactionId);
                    appState.transactions = appState.transactions.filter(t => t.id != transactionId);
                    appState.lastSearchResults = appState.lastSearchResults.filter(t => t.id != transactionId);

                    // Actualizar el saldo
                    if (removedTransaction) {
                        if (removedTransaction.type === 'income') {
                            appState.currentBalance -= removedTransaction.amount;
                        } else {
                            appState.currentBalance -= removedTransaction.amount;
                        }
                        const currentWorkspaceId = document.getElementById('workspaceSelect').value;
                        const currentWorkspace = appState.workspaces.find(ws => ws.id == currentWorkspaceId);
                        if (currentWorkspace) {
                            currentWorkspace.saldo = appState.currentBalance;
                        }
                        updateBalance();
                    }

                    // Invalidar caché y actualizar UI
                    const idEspacioTrabajo = document.getElementById('workspaceSelect').value;
                    delete appState.dashboardCache[idEspacioTrabajo];
                    actualizarDashboard(idEspacioTrabajo);
                    cargarCuentasBancarias(idEspacioTrabajo);
                    renderRecentTransactions();
                    if (!DOMElements.searchModal.hidden) {
                        applySearchOrder();
                    }

                    toggleModal('transactionDetailModal', false);
                    showNotification('Transacción eliminada con éxito.', 'success');
                } else {
                    throw new Error('No se pudo eliminar la transacción.');
                }
            })
            .catch(error => {
                console.error('Error al eliminar la transacción:', error);
                showNotification(error.message, 'error');
            });
        }
    }

    /**
     * Cambia dinámicamente el color de la barra superior del móvil.
     * @param {string} color - El color en formato hexadecimal (ej: "#FFFFFF").
     */
    function updateThemeColor(color) {
        const themeColorMeta = document.querySelector('meta[name="theme-color"]');
        if (themeColorMeta) {
            themeColorMeta.setAttribute('content', color);
        }
    }

    // Detectar la página actual y aplicar el color correspondiente
    function applyThemeColorBasedOnPage() {
        const currentPath = window.location.pathname;

        if (currentPath.includes('login.html')) {
            updateThemeColor('#F8FAFC'); // Fondo blanco para la pantalla de login
        } else if (currentPath.includes('dashboard.html')) {
            updateThemeColor('#3B82F6'); // Fondo celeste para el dashboard
        }
    }

    // Ejecutar la lógica al cargar el DOM
    window.addEventListener('DOMContentLoaded', applyThemeColorBasedOnPage);

})();
