(() => {
    const selection = document.getElementById('room-selection');
    if (!selection) {
        return;
    }

    const labels = selection.dataset;
    const catalog = selection.querySelector('[data-room-catalog]');
    const lines = selection.querySelector('[data-cart-lines]');
    const total = selection.querySelector('[data-cart-total]');
    const warning = selection.querySelector('[data-cart-warning]');
    const notice = selection.querySelector('[data-cart-notice]');
    const continueButton = selection.querySelector('[data-cart-continue]');
    const preview = document.getElementById('room-preview');
    let openPreview;
    let updatePreview;
    const search = document.getElementById('floatingBookingDate').form;
    const adults = Number(labels.adults);
    const children = Number(labels.children);
    const guests = adults + children;
    const nights = Number(labels.nights);
    const currency = new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'});
    const draftKey = 'hotel-room-selection:' + window.location.pathname;
    const rooms = new Map();
    let quantities = new Map();
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
            sessionStorage.setItem(draftKey, JSON.stringify(Object.fromEntries(quantities)));
            sessionStorage.removeItem(draftKey + ':confirmation');
        } catch {
            // The current page still works when browser storage is unavailable.
        }
    }

    function restoreDraft() {
        try {
            const stored = JSON.parse(sessionStorage.getItem(draftKey) || '{}');
            quantities = new Map(Object.entries(stored).filter(([, quantity]) =>
                Number.isSafeInteger(quantity) && quantity > 0));
        } catch {
            quantities.clear();
        }
    }

    function adjustDraft() {
        const adjusted = [];
        for (const [id, quantity] of quantities) {
            const room = rooms.get(id);
            const available = room ? room.available : 0;
            if (quantity > available) {
                if (available > 0) {
                    quantities.set(id, available);
                } else {
                    quantities.delete(id);
                }
                adjusted.push(room ? room.name + ' (' + available + ')' : labels.adjustedUnknown);
            }
        }
        if (adjusted.length > 0) {
            setNotice(labels.adjusted + ' ' + adjusted.join(', '), 'text-danger');
            saveDraft();
        }
    }

    function searchIsDirty() {
        const dates = search.elements.namedItem('booking-dates');
        return dates.value !== dates.defaultValue || Number(search.elements.adults.value) !== adults
            || Number(search.elements.children.value) !== children || !search.elements.children.value;
    }

    function render() {
        let totalCapacity = 0;
        let childCapacity = 0;
        let roomCount = 0;
        let subtotal = 0;
        lines.replaceChildren();

        for (const [id, room] of rooms) {
            const quantity = quantities.get(id) || 0;
            room.count.value = quantity;
            room.minus.disabled = checking || quantity === 0;
            room.plus.disabled = checking || quantity >= room.available;
            if (quantity === 0) {
                continue;
            }

            totalCapacity += room.capacity * quantity;
            childCapacity += room.maxChildren * quantity;
            roomCount += quantity;
            const amount = room.nightly * quantity * nights;
            subtotal += amount;

            const item = document.createElement('li');
            item.className = 'mb-3';
            const name = document.createElement('div');
            name.textContent = quantity + ' × ' + room.name;
            const calculation = document.createElement('div');
            calculation.className = 'small text-muted';
            const roomFactor = quantity > 1 ? ' × ' + quantity + ' ' + labels.roomUnit : '';
            calculation.textContent = currency.format(room.nightly) + roomFactor
                + ' × ' + nights + ' ' + labels.nightUnit + ' = ';
            const price = document.createElement('strong');
            price.className = 'text-nowrap text-body';
            price.textContent = currency.format(amount);
            calculation.append(price);
            item.append(name, calculation);
            lines.append(item);
        }
        if (roomCount === 0) {
            const empty = document.createElement('li');
            empty.className = 'small text-muted';
            empty.textContent = labels.empty;
            lines.append(empty);
        }
        total.value = currency.format(subtotal);

        let message = '';
        if (searchIsDirty()) {
            message = labels.applySearch;
        } else if (roomCount > guests) {
            message = labels.tooMany;
        } else if (roomCount > 0 && totalCapacity < guests) {
            message = labels.shortGuests;
        } else if (roomCount > 0 && childCapacity < children) {
            message = labels.shortChildren;
        }
        warning.textContent = message;
        warning.hidden = !message;
        continueButton.disabled = checking || roomCount === 0 || Boolean(message);
        if (updatePreview) {
            updatePreview();
        }
    }

    function changeQuantity(id, difference) {
        const next = (quantities.get(id) || 0) + difference;
        if (next < 0 || next > rooms.get(id).available || checking) {
            return false;
        }
        if (next === 0) {
            quantities.delete(id);
        } else {
            quantities.set(id, next);
        }
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
        adjustDraft();
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
        for (const [id, quantity] of quantities) {
            body.append('roomTypeId', id);
            body.append('quantity', quantity);
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
            const limitReached = (quantities.get(active.id) || 0) >= room.available;
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
                        room.capacity + ' ' + previewLabels.guestUnit + ' (' + previewLabels.includingMax
                        + ' ' + room.maxChildren + ' ' + previewLabels.childUnit + ')';
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
    adjustDraft();
    render();
})();
