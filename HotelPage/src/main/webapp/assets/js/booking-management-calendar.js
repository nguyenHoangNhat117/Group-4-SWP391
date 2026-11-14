(() => {
    if (typeof Litepicker === 'undefined') return;

    const locale = () => document.documentElement.lang === 'vi' ? 'vi-VN' : 'en-US';
    document.querySelectorAll('[data-booking-calendar]').forEach(input => {
        const earliest = input.getAttribute('min');
        const start = input.dataset.afterDate && input.form.elements.namedItem(input.dataset.afterDate);
        const minDate = () => {
            if (!start?.value) return earliest;
            const next = new Date(start.value + 'T00:00:00Z');
            next.setUTCDate(next.getUTCDate() + 1);
            return next.toISOString().slice(0, 10);
        };
        input.type = 'text';
        const picker = new Litepicker({
            element: input,
            singleMode: true,
            numberOfMonths: 1,
            numberOfColumns: 1,
            format: 'YYYY-MM-DD',
            lang: locale(),
            minDate: minDate(),
            setup(calendar) {
                calendar.on('selected', () => input.dispatchEvent(new Event('change', {bubbles: true})));
            }
        });
        if (earliest) input.readOnly = true;
        if (start) {
            start.addEventListener('change', () => {
                const min = minDate();
                picker.setOptions({minDate: min});
                if (input.value && input.value < min) {
                    picker.clearSelection();
                    input.dispatchEvent(new Event('change', {bubbles: true}));
                }
            });
        }
        document.addEventListener('hotel:languagechange', () => {
            picker.setOptions({lang: locale()});
        });
    });
})();
