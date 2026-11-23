import {enhanceBookingSelect} from './booking-select.js';

(() => {
    const selection = document.getElementById('room-selection');
    if (!selection) {
        return;
    }

    const labels = selection.dataset;
    const catalog = selection.querySelector('[data-room-catalog]');
    const lines = selection.querySelector('[data-cart-lines]');
    const cartParty = selection.querySelector('[data-cart-party]');
    const total = selection.querySelector('[data-cart-total]');
    const warning = selection.querySelector('[data-cart-warning]');
    const notice = selection.querySelector('[data-cart-notice]');
    const continueButton = selection.querySelector('[data-cart-continue]');
    const preview = document.getElementById('room-preview');
    let openPreview;
    let updatePreview;
    const search = document.getElementById('floatingBookingDate').form;
    enhanceBookingSelect(search.elements.namedItem('sort-by'));
    const adults = Number(labels.adults);
    const children = Number(labels.children);
    const nights = Number(labels.nights);
    const currency = new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'});
    const draftKey = 'hotel-room-selection:' + window.location.pathname;
    const rooms = new Map();
    const cart = new Map();
    let checking = false;

    for (const card of catalog.querySelectorAll('[data-room-type]')) {
        const data = card.dataset;
        rooms.set(data.roomType, {
            card,
            name: card.querySelector('[data-room-name]').textContent.trim(),
            available: Number(data.available),
            capacity: Number(data.capacity),
            maxChildren: Number(data.maxChildren),
            nightly: Number(data.nightly),
            minus: card.querySelector('[data-quantity-minus]'),
            plus: card.querySelector('[data-quantity-plus]'),
            count: card.querySelector('[data-quantity]'),
            badge: card.querySelector('[data-room-badge]')
        });
    }

    function setNotice(message, tone = 'text-muted') {
        notice.textContent = message;
        notice.className = 'small ' + tone;
        notice.hidden = !message;
    }

    function saveDraft() {
        try {
            sessionStorage.setItem(draftKey, JSON.stringify({stay: labels.stay, lines: [...cart]}));
            sessionStorage.removeItem(draftKey + ':confirmation');
        } catch {
            // The current page still works when browser storage is unavailable.
        }
    }

    function restoreDraft() {
        try {
            const stored = JSON.parse(sessionStorage.getItem(draftKey) || '{}');
            if (Array.isArray(stored.lines)) {
                for (const [id, line] of stored.lines) {
                    if (!line || typeof line.name !== 'string' || !Number.isFinite(line.nightly)
                            || line.nightly < 0 || !Array.isArray(line.parties) || line.parties.length === 0
                            || !line.parties.every(party => party
                                && Number.isSafeInteger(party.adults) && party.adults > 0
                                && Number.isSafeInteger(party.children) && party.children >= 0)) {
                        continue;
                    }
                    const room = rooms.get(id);
                    cart.set(id, room ? {...line, name: room.name, nightly: room.nightly} : line);
                }
                if (cart.size > 0 && stored.stay !== labels.stay) {
                    setNotice(labels.stayChanged);
                    saveDraft();
                }
                return;
            }
            for (const [id, quantity] of Object.entries(stored)) {
                const room = rooms.get(id);
                if (room && Number.isSafeInteger(quantity) && quantity > 0 && quantity <= 1000) {
                    cart.set(id, {name: room.name, nightly: room.nightly,
                        parties: Array.from({length: quantity}, () => ({adults, children}))});
                }
            }
            if (cart.size > 0) {
                saveDraft();
            }
        } catch {
            cart.clear();
        }
    }

    function searchIsDirty() {
        const dates = search.elements.namedItem('booking-dates');
        return dates.value !== dates.defaultValue || Number(search.elements.adults.value) !== adults
            || Number(search.elements.children.value) !== children || !search.elements.children.value;
    }

    function render() {
        let totalAdults = 0;
        let totalChildren = 0;
        let subtotal = 0;
        const dirty = searchIsDirty();
        lines.replaceChildren();

        for (const [id, room] of rooms) {
            const quantity = cart.get(id)?.parties.length || 0;
            room.count.value = quantity;
            room.minus.disabled = checking || quantity === 0;
            room.plus.disabled = checking || dirty || quantity >= room.available;
        }

        for (const [id, line] of cart) {
            const quantity = line.parties.length;
            const lineAdults = line.parties.reduce((sum, party) => sum + party.adults, 0);
            const lineChildren = line.parties.reduce((sum, party) => sum + party.children, 0);
            totalAdults += lineAdults;
            totalChildren += lineChildren;
            const amount = line.nightly * quantity * nights;
            subtotal += amount;

            const item = document.createElement('li');
            item.className = 'mb-3';
            const title = document.createElement('div');
            title.className = 'fw-semibold mb-2';
            title.textContent = quantity + ' × ' + line.name;

            const groups = new Map();
            line.parties.forEach((party, index) => {
                const key = party.adults + ':' + party.children;
                const group = groups.get(key) ?? {party, quantity: 0};
                group.quantity++;
                group.index = index;
                groups.set(key, group);
            });
            const parties = document.createElement('ul');
            parties.className = 'list-unstyled d-grid gap-1 mb-2';
            for (const group of groups.values()) {
                const row = document.createElement('li');
                row.className = 'room-cart-party';
                const text = document.createElement('span');
                const guests = partyText(group.party.adults, group.party.children);
                const count = groups.size > 1 ? group.quantity + ' ' + labels.roomUnit + ' · ' : '';
                const perRoom = group.quantity > 1 ? labels.perRoom + ' ' : '';
                text.textContent = count + perRoom + guests;
                const remove = document.createElement('button');
                remove.type = 'button';
                remove.className = 'btn btn-sm btn-outline-secondary room-cart-remove';
                remove.textContent = '−';
                remove.setAttribute('aria-label', labels.remove + ' ' + line.name + ' (' + guests + ')');
                remove.disabled = checking;
                remove.addEventListener('click', () => {
                    const buttons = [...lines.querySelectorAll('.room-cart-remove')];
                    const position = buttons.indexOf(remove);
                    if (changeQuantity(id, -1, group.index)) {
                        const updated = lines.querySelectorAll('.room-cart-remove');
                        (updated[Math.min(position, updated.length - 1)]
                            ?? document.getElementById('room-cart-heading')).focus({preventScroll: true});
                    }
                });
                row.append(text, remove);
                parties.append(row);
            }

            const calculation = document.createElement('div');
            calculation.className = 'small text-muted';
            const roomFactor = quantity > 1 ? ' × ' + quantity + ' ' + labels.roomUnit : '';
            calculation.textContent = currency.format(line.nightly) + roomFactor
                + ' × ' + nights + ' ' + labels.nightUnit + ' = ';
            const price = document.createElement('strong');
            price.className = 'text-nowrap text-body';
            price.textContent = currency.format(amount);
            calculation.append(price);
            item.append(title, parties, calculation);
            lines.append(item);
        }
        if (cart.size === 0) {
            const empty = document.createElement('li');
            empty.className = 'small text-muted';
            empty.textContent = labels.empty;
            lines.append(empty);
        }
        cartParty.textContent = partyText(totalAdults, totalChildren);
        total.value = currency.format(subtotal);

        const message = dirty ? labels.applySearch : totalAdults + totalChildren > 1000 ? labels.invalid : '';
        warning.textContent = message;
        warning.hidden = !message;
        continueButton.disabled = checking || cart.size === 0 || Boolean(message);
        if (updatePreview) {
            updatePreview();
        }
    }

    function partyText(adultCount, childCount) {
        return adultCount + ' ' + labels.adultUnit + ' · ' + childCount + ' ' + labels.childUnit;
    }

    function changeQuantity(id, difference, partyIndex) {
        const room = rooms.get(id);
        const line = cart.get(id);
        if (checking || (difference > 0 && (!room || room.plus.disabled)) || (difference < 0 && !line)) {
            return false;
        }
        if (difference > 0) {
            const parties = line ? line.parties : [];
            parties.push({adults, children});
            cart.set(id, {name: room.name, nightly: room.nightly, parties});
        } else {
            line.parties.splice(partyIndex ?? line.parties.length - 1, 1);
            if (line.parties.length === 0) {
                cart.delete(id);
            }
        }
        setNotice('');
        saveDraft();
        render();
        return true;
    }

    function refreshStock(stock) {
        for (const [id, room] of rooms) {
            room.available = stock[id] || 0;
            room.badge.textContent = room.available > 0 ? room.available + ' ' + labels.available : labels.soldOut;
        }
        const direction = labels.priceOrder === 'price_desc' ? -1 : 1;
        const ordered = [...rooms.entries()].sort(([leftId, left], [rightId, right]) =>
            Number(right.available > 0) - Number(left.available > 0)
            || direction * (left.nightly - right.nightly) || Number(leftId) - Number(rightId));
        for (const [, room] of ordered) {
            catalog.append(room.card);
        }
        const unavailable = [...cart].filter(([id, line]) => line.parties.length > (stock[id] || 0));
        setNotice(labels.stockChanged + ' ' + unavailable.map(([id, line]) =>
            line.name + ' (' + (stock[id] || 0) + ' ' + labels.available + ')').join(', '), 'text-danger');
    }

    continueButton.addEventListener('click', async () => {
        render();
        if (continueButton.disabled) {
            return;
        }
        checking = true;
        setNotice(labels.checking);
        render();
        const body = new URLSearchParams({criteria: labels.criteria, csrfToken: labels.csrfToken});
        for (const [id, line] of cart) {
            for (const party of line.parties) {
                body.append('roomTypeId', id);
                body.append('roomAdults', party.adults);
                body.append('roomChildren', party.children);
            }
        }
        try {
            const response = await fetch(labels.checkUrl, {method: 'POST', body, cache: 'no-store'});
            const result = await response.json();
            if (response.ok && result.status === 'available' && result.nextUrl) {
                try {
                    sessionStorage.setItem(draftKey + ':confirmation', result.requestKey);
                } catch {
                    // Continue when browser storage is unavailable.
                }
                window.location.assign(result.nextUrl);
            } else if (response.status === 409 && result.status === 'stock') {
                refreshStock(result.stock);
            } else if (result.status === 'search-changed') {
                setNotice(labels.reload, 'text-danger');
            } else {
                setNotice(result.status === 'capacity' ? labels.invalid : labels.failed, 'text-danger');
            }
        } catch {
            setNotice(labels.failed, 'text-danger');
        } finally {
            checking = false;
            render();
        }
    });

    if (preview && window.bootstrap?.Modal) {
        const modal = window.bootstrap.Modal.getOrCreateInstance(preview);
        const previewLabels = preview.dataset;
        const title = preview.querySelector('.modal-title');
        const close = preview.querySelector('.btn-close');
        const status = preview.querySelector('[data-preview-status]');
        const content = preview.querySelector('[data-preview-content]');
        const stock = preview.querySelector('[data-preview-stock]');
        const add = preview.querySelector('[data-preview-add]');
        let active;

        updatePreview = () => {
            if (!active) {
                return;
            }
            const room = rooms.get(active.id);
            const limitReached = (cart.get(active.id)?.parties.length || 0) >= room.available;
            stock.textContent = room.available === 0 ? labels.soldOut
                : limitReached ? previewLabels.limit : room.available + ' ' + labels.available;
            add.disabled = !active.shown || !active.loaded || room.plus.disabled;
        };

        openPreview = async (id, trigger) => {
            if (active) {
                return;
            }
            const current = {id, trigger, request: new AbortController(), loaded: false, shown: false};
            active = current;
            const room = rooms.get(id);
            title.textContent = room.name;
            status.textContent = previewLabels.loading;
            status.hidden = false;
            content.replaceChildren();
            updatePreview();
            modal.show(trigger);

            try {
                const response = await fetch(room.card.dataset.previewUrl, {
                    signal: current.request.signal, cache: 'no-store', redirect: 'error'
                });
                if (!response.ok) {
                    throw new Error('Room preview request failed');
                }
                const fragment = await response.text();
                if (!current.request.signal.aborted) {
                    content.innerHTML = fragment;
                    content.querySelector('[data-preview-capacity]').textContent =
                        room.capacity + ' ' + previewLabels.adultUnit
                        + ' + ' + room.maxChildren + ' ' + previewLabels.childUnit;
                    status.hidden = true;
                    current.loaded = true;
                    updatePreview();
                }
            } catch {
                if (!current.request.signal.aborted) {
                    status.textContent = previewLabels.failed;
                }
            }
        };

        add.addEventListener('click', () => {
            if (active && !add.disabled && changeQuantity(active.id, 1)) {
                modal.hide();
            }
        });
        preview.addEventListener('shown.bs.modal', () => {
            active.shown = true;
            updatePreview();
            close.focus({preventScroll: true});
        });
        preview.addEventListener('hide.bs.modal', () => {
            active.shown = false;
            active.request.abort();
            add.disabled = true;
        });
        preview.addEventListener('hidden.bs.modal', () => {
            const trigger = active.trigger;
            active = null;
            trigger.focus({preventScroll: true});
        });
    }

    for (const [id, room] of rooms) {
        room.minus.addEventListener('click', () => changeQuantity(id, -1));
        room.plus.addEventListener('click', () => changeQuantity(id, 1));
        if (openPreview) {
            for (const trigger of room.card.querySelectorAll('[data-preview-open]')) {
                trigger.addEventListener('click', () => openPreview(id, trigger));
            }
        }
    }
    search.addEventListener('input', render);
    restoreDraft();
    render();
})();
