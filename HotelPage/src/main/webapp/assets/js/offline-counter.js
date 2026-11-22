(() => {
    document.querySelectorAll('select[name="percent"]').forEach(select => {
        const form = select.form;
        const method = form.elements.method;
        const action = form.elements.action;
        const paymentForm = action?.value === "pay";
        const button = form.querySelector('button[type="submit"]');

        const update = () => {
            const target = form.querySelector('[data-counter-due]');
            const amount = Number(select.selectedOptions[0]?.dataset.amount);
            if (target && Number.isFinite(amount)) {
                target.textContent = new Intl.NumberFormat('vi-VN').format(amount) + ' VND';
            }
            if (!paymentForm) return;
            const qr = method.value === "bank_transfer";
            action.value = qr ? "qr" : "pay";
            button.textContent = qr
                    ? "Tạo / mở QR thanh toán"
                    : "Xác nhận đã nhận tiền mặt";
            form.querySelector('[data-counter-method-note]').textContent = qr
                    ? "Chỉ tạo QR; chưa ghi nhận đã thu. Hệ thống tự xác nhận sau khi đối soát payOS."
                    : "Chỉ xác nhận sau khi nhận đủ số tiền cần thu hiển thị bên dưới.";
        };
        select.addEventListener('change', update);
        if (paymentForm) method.addEventListener('change', update);
        update();
    });

    document.querySelectorAll('[data-check-in]').forEach(form => {
        const selected = Array.from(form.querySelectorAll('[data-assigned-room]'));
        const selectors = selected.filter(input => input.tagName === 'SELECT');
        const auto = form.querySelector('[data-auto-assign]');
        const message = form.querySelector('[data-assignment-message]');

        const checkSelection = () => {
            const used = new Set();
            selected.filter(input => input.type === 'hidden').forEach(input => used.add(input.value));
            selectors.forEach(input => {
                const duplicate = input.value && used.has(input.value);
                input.setCustomValidity(duplicate ? 'Phòng này đã được chọn cho dòng khác.' : '');
                if (input.value) used.add(input.value);
            });
            message.textContent = '';
        };
        selectors.forEach(input => input.addEventListener('change', checkSelection));
        if (auto) {
            auto.hidden = false;
            auto.addEventListener('click', () => {
                selectors.forEach(input => { input.value = input.dataset.autoRoom; });
                checkSelection();
                message.textContent = 'Đã chọn phòng đề xuất. Kiểm tra lại trước khi xác nhận.';
            });
        }
    });

    document.querySelectorAll('.oc-form').forEach(form => {
        form.addEventListener('submit', event => {
            if (form.dataset.submitting === 'true') {
                event.preventDefault();
                return;
            }
            if (form.elements.action?.value === 'check_out') {
                const early = form.elements.earlyCheckoutAccepted;
                const message = early
                        ? 'Xác nhận trả phòng sớm? Tổng tiền giữ nguyên; phòng sẽ chuyển sang chờ dọn.'
                        : 'Xác nhận khách đã trả phòng và đã quyết toán đủ các khoản?';
                if (!(early && form.hasAttribute('data-online-checkout')) && !window.confirm(message)) {
                    event.preventDefault();
                    return;
                }
            }
            if (form.elements.action?.value === 'pay' && form.elements.method?.value === 'cash') {
                const due = form.querySelector('[data-counter-due]')?.textContent || '';
                const message = 'Bạn đã nhận đủ ' + due + ' tiền mặt từ khách?';
                if (!window.confirm(message)) {
                    event.preventDefault();
                    return;
                }
            }
            form.dataset.submitting = 'true';
            form.setAttribute('aria-busy', 'true');
            form.querySelectorAll('button[type="submit"]').forEach(button => {
                button.disabled = true;
            });
        });
    });
    document.querySelectorAll('[data-booking-confirm]').forEach(button => {
        button.form.addEventListener('submit', event => {
            if (event.submitter === button && !window.confirm(button.dataset.bookingConfirm)) {
                event.preventDefault();
            }
        });
    });
    window.addEventListener('pageshow', () => {
        document.querySelectorAll('.oc-form').forEach(form => {
            delete form.dataset.submitting;
            form.removeAttribute('aria-busy');
            form.querySelectorAll('button[type="submit"]').forEach(button => {
                button.disabled = false;
            });
        });
    });
})();
