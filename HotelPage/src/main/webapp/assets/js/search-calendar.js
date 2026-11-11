(() => {
    const input = document.getElementById('floatingBookingDate');
    const status = document.getElementById('search-calendar-status');
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
            ? date <= checkIn || date > inventory.dates[checkIn]
            : !inventory.dates[date]);
        if (pending || unavailable) {
            if (!day.classList.contains('is-locked')) {
                day.dataset.searchLock = 'true';
                day.classList.add('is-locked');
            }
            day.classList.toggle('search-unavailable', Boolean(unavailable));
            day.setAttribute('aria-disabled', 'true');
            day.tabIndex = -1;
        } else if (day.dataset.searchLock) {
            delete day.dataset.searchLock;
            day.classList.remove('is-locked', 'search-unavailable');
            day.removeAttribute('aria-disabled');
            day.tabIndex = 0;
        } else {
            day.classList.remove('search-unavailable');
        }
    }

    function refreshDays() {
        document.querySelectorAll('.litepicker [data-stay-date]').forEach(markDay);
    }

    async function refreshAvailability() {
        if (pending) {
            pending.abort();
        }
        const query = new URL(form.action);
        query.searchParams.set('action', 'availability');
        filters.forEach(name => query.searchParams.set(name, form.elements.namedItem(name).value));
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
            if (start && end && !canBook(start, end)) {
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

    const picker = new Litepicker({
        element: input,
        singleMode: false,
        numberOfMonths: 2,
        numberOfColumns: 2,
        selectForward: true,
        format: 'MMM D, YYYY',
        minDate,
        maxDate,
        setup(calendar) {
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
    filters.forEach(name => form.elements.namedItem(name).addEventListener('change', refreshAvailability));
    form.addEventListener('submit', event => {
        if (pending) {
            event.preventDefault();
        } else if (!picker.getStartDate() || !picker.getEndDate()) {
            event.preventDefault();
            picker.show();
        }
    });
})();
