(() => {
    if (typeof Litepicker === 'undefined') return;

    const isVietnamese = () => (window.HotelLanguage?.language
        || document.querySelector('[data-language-endpoint]')?.dataset.language
        || document.documentElement.lang) === 'vi';
    const locale = () => isVietnamese() ? 'vi-VN' : 'en-US';
    const isoDate = /^\d{4}-\d{2}-\d{2}$/;

    document.querySelectorAll('input[type="date"]').forEach(input => {
        const start = input.dataset.afterDate && input.form?.elements.namedItem(input.dataset.afterDate);
        const nativeDate = document.createElement('input');
        nativeDate.type = 'date';

        const minDate = () => {
            const earliest = input.getAttribute('min') || '';
            if (!isoDate.test(start?.value || '')) return earliest;
            const next = new Date(start.value + 'T00:00:00Z');
            if (Number.isNaN(next.getTime())) return earliest;
            next.setUTCDate(next.getUTCDate() + 1);
            return [earliest, next.toISOString().slice(0, 10)].sort().pop();
        };

        const validate = () => {
            nativeDate.min = minDate();
            nativeDate.max = input.getAttribute('max') || '';
            nativeDate.step = input.getAttribute('step') || '1';
            nativeDate.value = input.value;
            const invalid = input.value && (!isoDate.test(input.value) || !nativeDate.value || !nativeDate.validity.valid);
            const error = isVietnamese()
                ? 'Nhập ngày hợp lệ theo định dạng YYYY-MM-DD và trong khoảng cho phép.'
                : 'Enter a valid date in YYYY-MM-DD format within the allowed range.';
            input.setCustomValidity(invalid ? error : '');
        };

        const updatePlaceholder = () => {
            const key = input.dataset.datePlaceholder;
            if (key === 'management.createdDate') {
                input.placeholder = isVietnamese() ? 'Ngày tạo tài khoản' : 'Created date';
            } else {
                input.placeholder = isVietnamese() ? 'Chọn ngày' : 'Choose a date';
            }
        };

        input.type = 'text';
        input.classList.add('shared-date');
        if (!input.closest('.search-calendar-field')) input.classList.add('has-calendar-icon');
        updatePlaceholder();
        const picker = new Litepicker({
            element: input,
            singleMode: true,
            numberOfMonths: 1,
            numberOfColumns: 1,
            format: 'YYYY-MM-DD',
            lang: locale(),
            minDate: minDate() || null,
            maxDate: input.getAttribute('max') || null,
            setup(calendar) {
                calendar.on('selected', () => input.dispatchEvent(new Event('change', {bubbles: true})));
            }
        });

        // Typed dates retain the validation of the original native date input.
        input.addEventListener('input', validate);
        input.addEventListener('change', validate);
        input.addEventListener('keydown', event => {
            if (event.key === 'ArrowDown' && event.altKey) {
                event.preventDefault();
                picker.show();
            }
            if (event.key === 'Escape') picker.hide();
        });
        const syncBounds = () => {
            const min = minDate();
            picker.setOptions({minDate: min || null, maxDate: input.getAttribute('max') || null});
            validate();
        };
        new MutationObserver(syncBounds).observe(input, {attributes: true, attributeFilter: ['min', 'max', 'step']});
        if (start) {
            start.addEventListener('change', () => {
                if (isoDate.test(input.value) && input.value < minDate()) {
                    picker.clearSelection();
                    input.value = '';
                    input.dispatchEvent(new Event('change', {bubbles: true}));
                }
                syncBounds();
            });
        }
        input.form?.addEventListener('reset', () => setTimeout(syncBounds, 0));
        document.addEventListener('hotel:languagechange', () => {
            picker.setOptions({lang: locale()});
            updatePlaceholder();
            validate();
        });
        validate();
    });
})();
