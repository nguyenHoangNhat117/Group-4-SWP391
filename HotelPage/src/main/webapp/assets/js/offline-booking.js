(() => {
    const form = document.getElementById('offlineForm');
    if (!form)
        return;
    const fields = form.elements;
    const room = fields.roomNumber;
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
    const signatureFields = ['checkIn', 'checkOut', 'guests', 'roomNumber', 'customerMode',
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
    const stayKey = () => JSON.stringify([fields.checkIn.value, fields.checkOut.value, fields.guests.value]);
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
    function renderSummary() {
        const option = room.selectedOptions[0];
        const nights = (Date.parse(fields.checkOut.value + 'T00:00:00Z') - Date.parse(fields.checkIn.value + 'T00:00:00Z')) / 86400000;
        const date = value => value ? value.split('-').reverse().join('/') : '—';
        show('obRoom', room.value ? translated('Room {number}', 'Phòng {number}', {number: room.value}) : translated('Select a room', 'Chọn phòng'));
        show('obType', room.value ? option?.dataset.label || '' : translated('Booking information', 'Thông tin đặt phòng'));
        show('obDates', date(fields.checkIn.value) + ' → ' + date(fields.checkOut.value));
        show('obNights', nights > 0 ? translated(nights === 1 ? '{count} night' : '{count} nights', '{count} đêm', {count: nights}) : '—');
        show('obGuests', fields.guests.value ? translated(Number(fields.guests.value) === 1 ? '{count} guest' : '{count} guests', '{count} khách', {count: fields.guests.value}) : '—');
        const price = Number(option?.dataset.price);
        const money = new Intl.NumberFormat(isVi() ? 'vi-VN' : 'en-US', {style: 'currency', currency: 'VND'});
        show('obAmount', room.value && nights > 0 && Number.isFinite(price) ? money.format(price * nights) : '—');
    }
    function update() {
        const stale = stayKey() !== searchedStay;
        if (stale)
            room.value = '';
        room.disabled = stale || uncertain;
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
            preview.disabled = stale || !room.value || uncertain;
        const quoteChanged = !reviewed || quoteExpired || signature() !== reviewedSignature;
        if (create)
            create.disabled = quoteChanged || stale || !room.value || uncertain;
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
    update();
})();