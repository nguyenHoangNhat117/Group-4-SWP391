(() => {
    const input = document.getElementById('floatingBookingDate') || document.getElementById('detailBookingDates');
    const details = input && input.id === 'detailBookingDates';
    const status = document.getElementById(details ? 'detail-calendar-status' : 'search-calendar-status');
    if (!input || !status || typeof Litepicker === 'undefined') {
        return;
    }

    const form = input.form;
    const roomTypeFilter = details ? null : form.elements.namedItem('room-type-id');
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
        } else if (roomTypeFilter && roomTypeFilter.value) {
            query.searchParams.set('room-type-id', roomTypeFilter.value);
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
        lang: 'vi-VN',
        format: 'DD/MM/YYYY',
        tooltipText: {one: 'ngày', other: 'ngày'},
        minDate,
        maxDate,
        setup(calendar) {
            calendar.on('selected', (start, end) => {
                if (details) {
                    form.elements.checkIn.value = start.format('YYYY-MM-DD');
                    form.elements.checkOut.value = end.format('YYYY-MM-DD');
                    input.dispatchEvent(new Event('change', {bubbles: true}));
                }
                if (input.hasAttribute('data-submit-on-select') && start.format('YYYY-MM-DD') < end.format('YYYY-MM-DD')) {
                    form.requestSubmit();
                }
            });
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
    if (roomTypeFilter) {
        roomTypeFilter.addEventListener('change', refreshAvailability);
    }
    form.addEventListener('submit', event => {
        const start = picker.getStartDate();
        const end = picker.getEndDate();
        if (pending) {
            event.preventDefault();
        } else if (!start || !end || start.format('YYYY-MM-DD') >= end.format('YYYY-MM-DD')) {
            event.preventDefault();
            picker.show();
        }
    });
})();
