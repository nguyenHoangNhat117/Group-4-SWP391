(() => {
    if (window.bootstrap && window.bootstrap.Dropdown) {
        document.querySelectorAll('.booking-search-page .search-field .form-select').forEach(select => {
            const label = select.labels[0];
            const toggle = document.createElement('button');
            toggle.type = 'button';
            toggle.id = select.id + '-toggle';
            toggle.className = 'dropdown-toggle search-filter-toggle';
            toggle.setAttribute('data-bs-toggle', 'dropdown');
            toggle.setAttribute('aria-expanded', 'false');
            label.id = select.id + '-label';
            label.htmlFor = toggle.id;
            toggle.setAttribute('aria-labelledby', label.id + ' ' + toggle.id);

            const menu = document.createElement('div');
            menu.className = 'dropdown-menu search-filter-menu';
            menu.setAttribute('aria-labelledby', label.id);
            const items = Array.from(select.options, option => {
                const item = document.createElement('button');
                item.type = 'button';
                item.className = 'dropdown-item';
                item.addEventListener('click', () => {
                    if (select.selectedIndex !== option.index) {
                        select.selectedIndex = option.index;
                        select.dispatchEvent(new Event('change', {bubbles: true}));
                    }
                    toggle.focus();
                });
                menu.append(item);
                return item;
            });

            function syncSelection() {
                toggle.textContent = select.options[select.selectedIndex].text;
                items.forEach((item, index) => {
                    const selected = index === select.selectedIndex;
                    item.textContent = select.options[index].text;
                    item.classList.toggle('active', selected);
                    if (selected) {
                        item.setAttribute('aria-current', 'true');
                    } else {
                        item.removeAttribute('aria-current');
                    }
                });
            }

            select.after(toggle, menu);
            select.hidden = true;
            select.addEventListener('change', syncSelection);
            document.addEventListener('hotel:languagechange', syncSelection);
            window.addEventListener('pageshow', syncSelection);
            syncSelection();
        });
    }

    const input = document.getElementById('floatingBookingDate') || document.getElementById('detailBookingDates');
    const details = input && input.id === 'detailBookingDates';
    const status = document.getElementById(details ? 'detail-calendar-status' : 'search-calendar-status');
    if (!input || !status || typeof Litepicker === 'undefined') {
        return;
    }

    const form = input.form;
    const filters = ['room-type-id', 'guest-count', 'price-band'];
    const today = new Intl.DateTimeFormat('sv-SE', {timeZone: 'Asia/Ho_Chi_Minh'}).format(new Date());
    const minDate = new Date(today + 'T00:00:00');
    const maxDate = new Date(minDate);
    maxDate.setDate(maxDate.getDate() + 365);
    let inventory;
    let pending;
    let checkIn;

    function markDay(day) {
        const date = day.dataset.stayDate;
        const unavailable = inventory && (checkIn
            ? !inventory.dates[checkIn] || date <= checkIn || date > inventory.dates[checkIn]
            : !inventory.dates[date]);
        if (pending || unavailable) {
            if (!day.classList.contains('is-locked')) {
                day.dataset.searchLock = 'true';
                day.classList.add('is-locked');
            }
            day.classList.toggle('search-unavailable', Boolean(unavailable));
            day.classList.toggle('search-loading', Boolean(pending && day.dataset.searchLock));
            day.setAttribute('aria-disabled', 'true');
            day.tabIndex = -1;
        } else if (day.dataset.searchLock) {
            delete day.dataset.searchLock;
            day.classList.remove('is-locked', 'search-unavailable', 'search-loading');
            day.removeAttribute('aria-disabled');
            day.tabIndex = 0;
        } else {
            day.classList.remove('search-unavailable', 'search-loading');
        }
    }

    function refreshDays() {
        document.querySelectorAll('.litepicker [data-stay-date]').forEach(markDay);
    }

    async function refreshAvailability() {
        if (pending) {
            pending.abort();
        }
        const query = new URL(input.dataset.calendarUrl || form.action, window.location.href);
        query.searchParams.set('action', 'availability');
        if (details) {
            query.searchParams.set('room-type-id', form.elements.roomTypeId.value);
            const quantity = Number(form.elements.quantity.value) || 1;
            const guests = Number(form.elements.guests.value) || 1;
            query.searchParams.set('quantity', quantity);
            query.searchParams.set('guest-count', Math.ceil(guests / quantity));
        } else {
            filters.forEach(name => query.searchParams.set(name, form.elements.namedItem(name).value));
        }
        const request = new AbortController();
        pending = request;
        inventory = null;
        status.textContent = status.dataset.loading;
        refreshDays();
        try {
            const response = await fetch(query, {signal: request.signal, cache: 'no-store'});
            if (!response.ok) {
                throw new Error('Availability request failed');
            }
            const data = await response.json();
            if (pending !== request) {
                return;
            }
            inventory = data;
            const start = picker.getStartDate();
            const end = picker.getEndDate();
            if (!details && start && end && !canBook(start, end)) {
                picker.clearSelection();
                checkIn = null;
                status.textContent = status.dataset.changed;
            } else {
                status.textContent = status.dataset.unavailable;
            }
        } catch (error) {
            if (pending === request) {
                status.textContent = status.dataset.error;
            }
        } finally {
            if (pending === request) {
                pending = null;
                refreshDays();
            }
        }
    }

    function canBook(start, end) {
        const lastCheckout = inventory.dates[start.format('YYYY-MM-DD')];
        return lastCheckout && end.format('YYYY-MM-DD') <= lastCheckout;
    }

    const months = details && window.matchMedia('(max-width: 575px)').matches ? 1 : 2;
    const picker = new Litepicker({
        element: input,
        singleMode: false,
        numberOfMonths: months,
        numberOfColumns: months,
        selectForward: true,
        format: 'MMM D, YYYY',
        minDate,
        maxDate,
        setup(calendar) {
            if (details) {
                calendar.on('selected', (start, end) => {
                    form.elements.checkIn.value = start.format('YYYY-MM-DD');
                    form.elements.checkOut.value = end.format('YYYY-MM-DD');
                    input.dispatchEvent(new Event('change', {bubbles: true}));
                });
            }
            calendar.on('render:day', (day, date) => {
                day.dataset.stayDate = date.format('YYYY-MM-DD');
                markDay(day);
            });
            calendar.on('show', refreshAvailability);
            calendar.on('hide', () => { checkIn = null; });
            calendar.on('preselect', (start, end) => {
                checkIn = start && !end ? start.format('YYYY-MM-DD') : null;
                refreshDays();
            });
        }
    });
    if (!details) {
        filters.forEach(name => form.elements.namedItem(name).addEventListener('change', refreshAvailability));
    }
    form.addEventListener('submit', event => {
        if (pending) {
            event.preventDefault();
        } else if (!picker.getStartDate() || !picker.getEndDate()) {
            event.preventDefault();
            picker.show();
        }
    });
})();
