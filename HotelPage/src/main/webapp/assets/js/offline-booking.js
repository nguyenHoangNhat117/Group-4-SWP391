(() => {
    const form = document.getElementById('offlineForm');
    if (!form)
        return;
    const fields = form.elements;
    const roomPicker = document.getElementById('obRoomPicker');
    const roomSearch = document.getElementById('obRoomSearch');
    const roomChoices = Array.from(form.querySelectorAll('input[name="roomTypeId"]'));
    const previous = document.getElementById('obRoomsPrevious');
    const nextPage = document.getElementById('obRoomsNext');
    const promotion = fields.promotionCode;
    const promotionOptions = Array.from(promotion.options).filter(option => option.value);
    const pageSize = 10;
    let roomPage = Math.max(0, Math.floor(roomChoices.findIndex(input => input.checked) / pageSize));
    const selectedRoom = () => roomChoices.find(input => input.checked);
    const create = form.querySelector('button[value="create"]');
    const preview = form.querySelector('button[value="preview"]');
    const reviewed = form.dataset.reviewed === 'true';
    const uncertain = form.dataset.uncertain === 'true';
    const isVi = () => document.documentElement.lang.startsWith('vi');
    const translated = (en, vi, args = {}) => {
        if (window.HotelLanguage?.key(en))
            return window.HotelLanguage.translate(en, args);
        return (isVi() ? vi : en).replace(/\{(\w+)\}/g, (match, key) => args[key] ?? match);
    };
    const signatureFields = ['checkIn', 'checkOut', 'guests', 'quantity', 'roomTypeId', 'customerMode',
        'customerId', 'firstName', 'lastName', 'email', 'phone', 'promotionCode', 'specialRequest'];
    const show = (id, text) => {
        const node = document.getElementById(id);
        if (node)
            node.textContent = text;
    };
    const hide = (id, value) => {
        const node = document.getElementById(id);
        if (node)
            node.hidden = value;
    };
    const stayKey = () => JSON.stringify([fields.checkIn.value, fields.checkOut.value, fields.guests.value, fields.quantity.value]);
    function signature() {
        const data = new FormData(form);
        return JSON.stringify(signatureFields.map(key => String(data.get(key) || '').trim()));
    }
    function mode() {
        const selected = form.querySelector('[name="customerMode"]:checked')?.value || 'existing';
        form.querySelectorAll('[data-customer-pane]').forEach(pane => {
            const active = pane.dataset.customerPane === selected;
            pane.hidden = !active;
            pane.querySelectorAll('input, select, button').forEach(input => {
                input.disabled = !active;
                if (['customerId', 'firstName', 'lastName', 'phone'].includes(input.name))
                    input.required = active;
            });
        });
    }
    mode();
    const searchedStay = form.dataset.searchKey || stayKey();
    const reviewedSignature = form.dataset.quoteSignature || signature();
    let quoteExpired = reviewed && !(Number(form.dataset.quoteAge) > 0);
    const quantity = () => Number(fields.quantity.value);
    const nights = () => (Date.parse(fields.checkOut.value + 'T00:00:00Z')
                - Date.parse(fields.checkIn.value + 'T00:00:00Z')) / 86400000;

    function renderRoomList() {
        if (!roomPicker)
            return;
        const query = roomSearch.value.trim().toLocaleLowerCase().replace(/^#/, '');
        const matches = roomChoices.filter(input =>
            input.dataset.label.toLocaleLowerCase().includes(query));
        const offset = roomPage * pageSize;
        const visible = new Set(matches.slice(offset, offset + pageSize));
        roomChoices.forEach(input => {
            input.closest('.ob-room-choice').hidden = !visible.has(input);
        });
        previous.disabled = roomPage === 0;
        nextPage.disabled = offset + pageSize >= matches.length;
        hide('obNoMatchingRooms', matches.length !== 0);
        show('obRoomRange', matches.length ? `${offset + 1}–${Math.min(offset + pageSize, matches.length)} / ${matches.length}` : '');
    }

    roomSearch?.addEventListener('input', () => {
        roomPage = 0;
        renderRoomList();
    });
    roomSearch?.addEventListener('keydown', event => {
        if (event.key === 'Enter')
            event.preventDefault();
    });
    previous?.addEventListener('click', () => {
        roomPage--;
        renderRoomList();
    });
    nextPage?.addEventListener('click', () => {
        roomPage++;
        renderRoomList();
    });

    function updatePromotions(room) {
        const gross = room && nights() > 0 ? Number(room.dataset.price) * nights() * quantity() : null;
        promotionOptions.forEach(option => {
            const eligible = gross !== null && Number(option.dataset.minimum) <= gross;
            if (!eligible && option.selected)
                promotion.value = '';
            option.hidden = !eligible;
            option.disabled = !eligible;
        });
        promotion.disabled = uncertain || !promotionOptions.some(option => !option.disabled);
    }

    function renderSummary() {
        const room = selectedRoom();
        const stayNights = nights();
        const date = value => value ? value.split('-').reverse().join('/') : '—';
        show('obRoom', room ? room.dataset.label : 'Chọn loại phòng');
        show('obType', room ? `${quantity()} phòng` : 'Thông tin đặt phòng');
        show('obDates', date(fields.checkIn.value) + ' → ' + date(fields.checkOut.value));
        show('obNights', stayNights > 0 ? translated(stayNights === 1 ? '{count} night' : '{count} nights', '{count} đêm', {count: stayNights}) : '—');
        show('obGuests', fields.guests.value ? translated(Number(fields.guests.value) === 1 ? '{count} guest' : '{count} guests', '{count} khách', {count: fields.guests.value}) : '—');
        const price = Number(room?.dataset.price);
        const money = new Intl.NumberFormat(isVi() ? 'vi-VN' : 'en-US', {style: 'currency', currency: 'VND'});
        show('obAmount', room && stayNights > 0 && Number.isFinite(price) ? money.format(price * stayNights * quantity()) : '—');
    }
    function update() {
        const stale = stayKey() !== searchedStay;
        roomChoices.forEach(input => {
            if (stale)
                input.checked = false;
            const insufficient = quantity() > Number(input.dataset.available);
            if (insufficient && !uncertain)
                input.checked = false;
            input.disabled = stale || uncertain || insufficient;
        });
        const room = selectedRoom();
        const notReady = room && room.dataset.ready !== '' && Number(room.dataset.ready) < quantity();
        hide('obReadinessWarning', !notReady);
        updatePromotions(room);
        hide('obSearchChanged', !stale);
        renderSummary();
        if (fields.checkIn.value) {
            const next = new Date(fields.checkIn.value + 'T00:00:00Z');
            if (Number.isFinite(next.getTime())) {
                next.setUTCDate(next.getUTCDate() + 1);
                fields.checkOut.min = next.toISOString().slice(0, 10);
            }
        }
        if (preview)
            preview.disabled = !room || notReady || uncertain;
        const quoteChanged = !reviewed || quoteExpired || signature() !== reviewedSignature;
        if (create)
            create.disabled = quoteChanged || !room || notReady || uncertain;
        hide('obQuoteChanged', !quoteChanged);
        const quote = document.getElementById('obReviewedQuote');
        if (quote)
            quote.style.opacity = quoteChanged ? '.55' : '1';
    }
    form.addEventListener('input', update);
    form.addEventListener('change', event => {
        if (event.target.name === 'customerMode')
            mode();
        update();
    });
    if (reviewed && !quoteExpired) {
        window.setTimeout(() => {
            quoteExpired = true;
            update();
        }, Number(form.dataset.quoteAge));
    }
    let actionInput;
    let locked = [];
    form.addEventListener('submit', event => {
        if (form.dataset.submitting === 'true') {
            event.preventDefault();
            return;
        }
        const action = event.submitter?.value;
        if (!['preview', 'create'].includes(action))
            return;
        update();
        if (event.submitter.disabled) {
            event.preventDefault();
            return;
        }
        actionInput = document.createElement('input');
        actionInput.type = 'hidden';
        actionInput.name = 'action';
        actionInput.value = action;
        form.appendChild(actionInput);
        form.dataset.submitting = 'true';
        form.setAttribute('aria-busy', 'true');
        locked = Array.from(form.querySelectorAll('button:not(:disabled)'));
        locked.forEach(button => {
            button.disabled = true;
        });
    });
    window.addEventListener('pageshow', () => {
        delete form.dataset.submitting;
        form.removeAttribute('aria-busy');
        actionInput?.remove();
        actionInput = null;
        locked.forEach(button => {
            button.disabled = false;
        });
        locked = [];
        update();
    });
    document.addEventListener('hotel:languagechange', renderSummary);
    renderRoomList();
    update();
})();