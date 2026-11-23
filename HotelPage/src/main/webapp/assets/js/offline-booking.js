(() => {
    const form = document.getElementById('offlineForm');
    if (!form)
        return;
    const fields = form.elements;
    const roomPicker = document.getElementById('obRoomPicker');
    const roomSearch = document.getElementById('obRoomSearch');
    const results = Array.from(form.querySelectorAll('[data-room-type]'), input => ({
            input,
            row: input.closest('.ob-room-choice'),
            id: Number(input.dataset.roomType),
            label: input.dataset.label,
            capacity: Number(input.dataset.capacity),
            price: Number(input.dataset.price),
            stock: Number(input.dataset.stock)
        }));
    const stepButtons = Array.from(form.querySelectorAll('[data-step]'));
    const previous = document.getElementById('obRoomsPrevious');
    const nextPage = document.getElementById('obRoomsNext');
    const promotion = fields.promotionCode;
    const pageSize = 10;
    let roomPage = 0;
    let searchingCustomers = false;
    const create = form.querySelector('button[value="create"]');
    const preview = form.querySelector('button[value="preview"]');
    const reviewed = form.dataset.reviewed === 'true';
    const uncertain = form.dataset.uncertain === 'true';
    const matchingVersion = form.dataset.formVersion === 'multi-room-quote-1';
    const signatureFields = ['checkIn', 'checkOut', 'adults', 'children', 'roomSelection', 'customerMode',
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
    const stayFields = [fields.checkIn, fields.checkOut, fields.adults, fields.children];
    const criteriaKey = ([checkIn, checkOut, adults, children]) => JSON.stringify([
            checkIn, checkOut, adults.trim() === '' ? '' : Number(adults), children.trim() === '' ? '' : Number(children)
        ]);
    const stayKey = () => criteriaKey(stayFields.map(field => field.value));
    if (stayFields.some(field => !field) || !fields.promotionCode) {
        show('obRoomProgress', 'Form chưa tải đủ thông tin. Tải lại trang rồi tìm phòng lại.');
        return;
    }
    const promotionOptions = Array.from(promotion.options).filter(option => option.value);
    const roomSelection = fields.namedItem('roomSelection') || document.createElement('input');
    if (!roomSelection.form) {
        roomSelection.type = 'hidden';
        roomSelection.name = 'roomSelection';
        form.appendChild(roomSelection);
    }
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
    let searchedStay = '';
    if (form.dataset.searched === 'true') {
        try {
            const criteria = JSON.parse(form.dataset.searchKey || 'null');
            if (Array.isArray(criteria) && criteria.length === stayFields.length
                    && criteria.every(value => typeof value === 'string')) {
                searchedStay = criteriaKey(criteria);
                // Dùng lại dữ liệu máy chủ nếu ô bị bỏ trống khi trình duyệt tải kết quả.
                stayFields.forEach((field, index) => {
                    if (!field.value && !field.validity.badInput)
                        field.value = criteria[index];
                });
            }
        } catch (error) {
            // Không mở lại kết quả khi thiếu thông tin lần tìm. Lễ tân có thể tìm lại.
        }
    }
    // Giữ số lượng từ yêu cầu vừa gửi nếu trang không điền lại được giá trị ở từng dòng.
    if (searchedStay && stayKey() === searchedStay) {
        const previous = new Map(roomSelection.value.split(',').filter(line => /^\d{1,10}:\d{1,4}$/.test(line))
                .map(line => line.split(':').map(Number)));
        for (const room of results) {
            if (room.input.valueAsNumber === 0 && previous.has(room.id)) {
                room.input.value = Math.min(room.stock, previous.get(room.id));
            }
        }
    }
    const reviewedSignature = form.dataset.quoteSignature || signature();
    let quoteExpired = reviewed && !(Number(form.dataset.quoteAge) > 0);
    function utcDate(field) {
        const time = new Date(field.value + 'T00:00:00Z');
        if (!Number.isFinite(time.getTime()) || time.toISOString().slice(0, 10) !== field.value)
            return NaN;
        return time.getTime();
    }
    const nights = () => (utcDate(fields.checkOut) - utcDate(fields.checkIn)) / 86400000;

    function renderRoomList() {
        if (!roomPicker)
            return;
        const query = roomSearch.value.trim().toLocaleLowerCase().replace(/^#/, '');
        const matches = results.filter(({label}) => label.toLocaleLowerCase().includes(query));
        const offset = roomPage * pageSize;
        const visible = new Set(matches.slice(offset, offset + pageSize));
        results.forEach(result => {
            result.row.hidden = !visible.has(result);
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

    function updatePromotions(gross) {
        promotionOptions.forEach(option => {
            const eligible = gross !== null && Number(option.dataset.minimum) <= gross;
            if (!eligible && option.selected)
                promotion.value = '';
            option.hidden = !eligible;
            option.disabled = !eligible;
        });
        promotion.disabled = uncertain || !promotionOptions.some(option => !option.disabled);
    }

    function renderSummary(selected, roomCount, gross) {
        const stayNights = nights();
        const date = value => value ? value.split('-').reverse().join('/') : '—';
        show('obRoom', selected.length ? selected.map(room => room.label + ' × ' + room.count).join(', ') : 'Chọn loại phòng');
        show('obType', roomCount ? roomCount + ' phòng' : 'Thông tin đặt phòng');
        show('obDates', date(fields.checkIn.value) + ' → ' + date(fields.checkOut.value));
        const nightText = stayNights > 0 && fields.checkIn.value >= fields.checkIn.getAttribute('min')
                ? stayNights + ' đêm' : '';
        show('obNights', nightText || '—');
        show('obStayNights', nightText);
        hide('obStayNights', !nightText);
        show('obGuests', fields.adults.value
                ? fields.adults.value + ' người lớn · ' + (fields.children.value || 0) + ' trẻ em' : '—');
        const money = new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'});
        show('obAmount', gross === null ? '—' : money.format(gross));
    }
    function updateSearchFields() {
        const checkIn = new Date(utcDate(fields.checkIn));
        const earliest = fields.checkIn.getAttribute('min');
        let checkoutMin = earliest;
        if (Number.isFinite(checkIn.getTime())) {
            checkIn.setUTCDate(checkIn.getUTCDate() + 1);
            checkoutMin = [earliest, checkIn.toISOString().slice(0, 10)].sort().pop();
        }
        if (fields.checkOut.getAttribute('min') !== checkoutMin) {
            fields.checkOut.setAttribute('min', checkoutMin);
        }
        const adults = Number(fields.adults.value);
        const children = Number(fields.children.value);
        fields.adults.max = Math.max(1, 1000 - children);
        fields.children.max = Math.max(0, 1000 - adults);
    }

    function stepField(button) {
        return document.getElementById(button.getAttribute('aria-controls'));
    }

    function updateStepButtons(roomCount, adultCount) {
        stepButtons.forEach(button => {
            const field = stepField(button);
            if (!field) {
                button.disabled = true;
                return;
            }
            const value = Number(field.value);
            const decrease = button.dataset.step === '-1';
            button.disabled = uncertain || field.disabled || (decrease
                    ? value <= Number(field.min)
                    : value >= Number(field.max) || (button.hasAttribute('data-room-step') && roomCount >= adultCount));
        });
    }

    function update() {
        updateSearchFields();
        const stale = form.dataset.searched === 'true' && stayKey() !== searchedStay;
        const adultCount = Number(fields.adults.value);
        const guestCount = adultCount + Number(fields.children.value);
        const selected = [];
        let validQuantities = true;
        for (const room of results) {
            if (stale)
                room.input.value = '0';
            room.input.disabled = stale || uncertain || room.stock === 0;
            room.input.max = Math.min(room.stock, Math.max(0, adultCount));
            const count = room.input.valueAsNumber;
            if ((!room.input.disabled && !room.input.validity.valid) || (count > 0 && count > room.stock)) {
                validQuantities = false;
            }
            room.row.classList.toggle('is-selected', count > 0);
            if (count > 0)
                selected.push({...room, count});
        }
        selected.sort((a, b) => a.id - b.id);
        roomSelection.value = selected.map(room => room.id + ':' + room.count).join(',');
        const roomCount = selected.reduce((sum, room) => sum + room.count, 0);
        updateStepButtons(roomCount, adultCount);
        const capacity = selected.reduce((sum, room) => sum + room.capacity * room.count, 0);
        const nightly = selected.reduce((sum, room) => sum + room.price * room.count, 0);
        const gross = validQuantities && roomCount && nights() > 0 ? nightly * nights() : null;
        const canQuoteRooms = matchingVersion && !stale && !uncertain && !searchingCustomers
                && validQuantities && roomCount > 0 && roomCount <= adultCount && capacity >= guestCount
                && stayFields.every(field => field.validity.valid);
        const customerMode = form.querySelector('[name="customerMode"]:checked')?.value || 'existing';
        const customerFields = customerMode === 'new'
                ? [fields.lastName, fields.firstName, fields.phone, fields.email] : [fields.customerId];
        const hasCustomer = customerFields.every(field => field && field.validity.valid && (field.value.trim() || !field.required));
        const canBook = canQuoteRooms && hasCustomer;
        const customerHint = customerMode === 'new' ? 'Nhập đủ họ tên và điện thoại của người đại diện.'
                : 'Chọn một hồ sơ ở mục Khách hàng để kiểm tra giá.';
        show('obQuoteCustomerHint', customerHint);
        hide('obQuoteCustomerHint', !canQuoteRooms || hasCustomer);
        hide('obFormVersionError', matchingVersion);
        updatePromotions(gross);
        hide('obSearchChanged', !stale);
        hide('obAdultLimit', stale || roomCount <= adultCount);
        const progress = capacity >= guestCount ? 'Đủ chỗ cho ' : 'Đã chọn chỗ cho ';
        const selectionStatus = 'Đã chọn ' + roomCount + ' phòng (tối đa ' + adultCount + ') · '
                + progress + Math.min(capacity, guestCount) + '/' + guestCount + ' khách';
        const searchStatus = searchedStay
                ? 'Ngày hoặc số khách đã đổi. Bấm Tìm phòng trống để chọn lại.'
                : 'Thiếu thông tin của lần tìm. Bấm Tìm phòng trống để chọn lại.';
        show('obRoomProgress', uncertain ? 'Hãy kiểm tra kết quả của yêu cầu trước khi chọn phòng.'
                : stale ? searchStatus : selectionStatus);
        renderSummary(selected, roomCount, gross);
        if (preview)
            preview.disabled = !canBook;
        const quoteChanged = !reviewed || quoteExpired || signature() !== reviewedSignature;
        if (create)
            create.disabled = quoteChanged || !canBook;
        hide('obQuoteChanged', !quoteChanged);
        const quote = document.getElementById('obReviewedQuote');
        if (quote)
            quote.style.opacity = quoteChanged ? '.55' : '1';
    }
    stepButtons.forEach(button => {
        button.addEventListener('click', () => {
            const field = stepField(button);
            if (!field)
                return;
            const next = (field.valueAsNumber || 0) + Number(button.dataset.step);
            field.value = Math.min(Number(field.max), Math.max(Number(field.min), next));
            update();
        });
    });
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
    async function searchCustomers(button) {
        searchingCustomers = true;
        const request = new URLSearchParams(new FormData(form));
        request.set(button.name, button.value);
        // Tìm khách xóa báo giá cũ trên máy chủ. Giữ nguyên lựa chọn phòng trên trang.
        quoteExpired = reviewed;
        button.disabled = true;
        show('obCustomerSearchStatus', 'Đang tìm khách…');
        hide('obCustomerSearchStatus', false);
        update();
        try {
            const response = await fetch(form.getAttribute('action'), {
                method: 'POST',
                body: request,
                credentials: 'same-origin'
            });
            const page = new DOMParser().parseFromString(await response.text(), 'text/html');
            const current = form.querySelector('[data-customer-search-results]');
            const found = page.querySelector('[data-customer-search-results]');
            if (response.redirected || response.status === 403) {
                show('obCustomerSearchStatus', 'Phiên đã hết hạn. Mở lại màn hình đặt tại quầy và đăng nhập nếu cần.');
                return;
            }
            if (!response.ok || !found) {
                const error = page.querySelector('[data-offline-booking-error]')?.textContent.trim();
                show('obCustomerSearchStatus', error || 'Chưa thể tải danh sách khách. Vui lòng thử lại.');
                return;
            }
            current.replaceWith(document.importNode(found, true));
            hide('obCustomerSearchStatus', true);
        } catch (error) {
            show('obCustomerSearchStatus', 'Chưa thể tìm khách do lỗi kết nối. Vui lòng thử lại.');
        } finally {
            searchingCustomers = false;
            mode();
            update();
        }
    }

    let locked = [];
    form.addEventListener('submit', event => {
        if (form.dataset.submitting === 'true' || searchingCustomers) {
            event.preventDefault();
            return;
        }
        if (event.submitter?.hasAttribute('data-search-rooms')) {
            updateSearchFields();
            const valid = stayFields.every(field => field.reportValidity());
            if (!valid)
                event.preventDefault();
            return;
        }
        if (event.submitter?.hasAttribute('data-search-customers')) {
            update();
            if (typeof fetch === 'function' && form.querySelector('[data-customer-search-results]')) {
                event.preventDefault();
                searchCustomers(event.submitter);
            }
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
        form.dataset.submitting = 'true';
        form.setAttribute('aria-busy', 'true');
        // Giữ nút đã bấm ở trong dữ liệu gửi để máy chủ nhận đúng action.
        locked = Array.from(form.querySelectorAll('button:not(:disabled)'))
                .filter(button => button !== event.submitter);
        locked.forEach(button => {
            button.disabled = true;
        });
    });
    window.addEventListener('pageshow', () => {
        delete form.dataset.submitting;
        form.removeAttribute('aria-busy');
        locked.forEach(button => {
            button.disabled = false;
        });
        locked = [];
        update();
    });
    renderRoomList();
    update();
})();
