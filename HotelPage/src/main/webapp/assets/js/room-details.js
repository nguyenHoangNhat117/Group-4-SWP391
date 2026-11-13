(() => {
    const form = document.getElementById('details-stay-form');
    if (!form) {
        return;
    }

    const checkIn = form.elements.checkIn;
    const checkOut = form.elements.checkOut;
    const quantity = form.elements.quantity;
    const guests = form.elements.guests;
    const datePicker = document.getElementById('detailBookingDates');
    const roomLimit = document.getElementById('detailMaxRooms');
    const guestLimit = document.getElementById('detailMaxGuests');
    const capacity = Number(form.dataset.capacity);
    const quote = document.getElementById('detailQuote');
    const reserve = document.getElementById('detailReserve');
    const messages = document.querySelectorAll('#detailStatus [data-message]');
    const money = new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'});
    let timer;
    let pending;

    function showMessage(key) {
        messages.forEach(message => { message.hidden = message.dataset.message !== key; });
    }

    function enableBooking(enabled) {
        if (reserve) {
            reserve.disabled = !enabled;
        }
    }

    function clamp(field) {
        if (Number.isInteger(field.valueAsNumber)) {
            field.value = Math.min(Number(field.max), Math.max(Number(field.min), field.valueAsNumber));
        }
    }

    function updateGuestLimit() {
        const rooms = quantity.valueAsNumber;
        guests.min = Number.isInteger(rooms) && rooms > 0 ? rooms : 1;
        guests.max = Math.min(1000, Number(guests.min) * capacity);
        guestLimit.textContent = guests.max;
        clamp(guests);
    }

    async function refreshAvailability() {
        const query = new URL(form.dataset.endpoint, window.location.href);
        query.search = new URLSearchParams(new FormData(form)).toString();
        query.searchParams.set('action', 'availability');
        const controller = new AbortController();
        pending = controller;
        try {
            const response = await fetch(query, {signal: controller.signal, cache: 'no-store'});
            if (!response.ok) {
                throw new Error('Availability request failed');
            }
            const stay = await response.json();
            if (pending !== controller) {
                return;
            }
            quantity.max = Math.max(1, stay.maxRooms);
            quantity.readOnly = stay.maxRooms === 0;
            guests.readOnly = quantity.readOnly;
            roomLimit.textContent = stay.maxRooms;
            if (quantity.valueAsNumber > Number(quantity.max)) {
                clamp(quantity);
                updateGuestLimit();
                if (stay.maxRooms > 0) {
                    await refreshAvailability();
                    return;
                }
            }
            document.getElementById('detailNights').textContent = stay.nights;
            document.getElementById('detailAvailable').textContent = stay.available;
            document.getElementById('detailTotal').textContent =
                stay.canBook ? money.format(stay.total) : '—';
            quote.hidden = stay.nights === 0;
            showMessage(stay.error);
            enableBooking(stay.canBook);
        } catch (error) {
            if (pending === controller) {
                showMessage('room.details.load.error');
            }
        }
    }

    function scheduleRefresh() {
        clearTimeout(timer);
        if (pending) {
            pending.abort();
            pending = null;
        }
        quote.hidden = true;
        enableBooking(false);
        if (!checkIn.value || !checkOut.value || checkOut.value <= checkIn.value) {
            showMessage('room.details.dates.error');
        } else if (!quantity.validity.valid || !guests.validity.valid) {
            showMessage('room.quantity.error');
        } else {
            showMessage('room.calendar.loading');
            timer = setTimeout(refreshAvailability, 250);
        }
    }

    quantity.addEventListener('input', () => {
        clamp(quantity);
        updateGuestLimit();
        scheduleRefresh();
    });
    guests.addEventListener('input', () => {
        clamp(guests);
        scheduleRefresh();
    });
    datePicker.addEventListener('change', scheduleRefresh);
})();
