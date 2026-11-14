(() => {
    const vi = () => document.documentElement.lang.startsWith("vi");
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
                target.textContent = new Intl.NumberFormat(vi() ? 'vi-VN' : 'en-US').format(amount) + ' VND';
            }
            if (!paymentForm) return;
            const qr = method.value === "bank_transfer";
            action.value = qr ? "qr" : "pay";
            button.removeAttribute("data-i18n");
            button.textContent = qr
                    ? (vi() ? "Tạo / mở QR thanh toán" : "Create / open payment QR")
                    : (vi() ? "Xác nhận đã nhận tiền mặt" : "Confirm cash received");
            form.querySelector('[data-counter-method-note]').textContent = qr
                    ? (vi() ? "Chỉ tạo QR; chưa ghi nhận đã thu. Hệ thống tự xác nhận sau khi đối soát payOS."
                        : "Creates a QR only. Payment is recorded after payOS verification.")
                    : (vi() ? "Chỉ xác nhận sau khi nhận đủ số tiền cần thu hiển thị bên dưới."
                        : "Confirm only after receiving the amount due shown below.");
        };
        select.addEventListener('change', update);
        if (paymentForm) method.addEventListener('change', update);
        document.addEventListener('hotel:languagechange', update);
        update();
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
                        ? (vi() ? 'Xác nhận trả phòng sớm? Tổng tiền giữ nguyên; phòng sẽ chuyển sang chờ dọn.'
                            : 'Confirm early checkout? The agreed total is unchanged; the room will require cleaning.')
                        : (vi() ? 'Xác nhận khách đã trả phòng và đã quyết toán đủ các khoản?'
                            : 'Confirm checkout and settlement of all charges?');
                if (!(early && form.hasAttribute('data-online-checkout')) && !window.confirm(message)) {
                    event.preventDefault();
                    return;
                }
            }
            if (form.elements.action?.value === 'pay' && form.elements.method?.value === 'cash') {
                const due = form.querySelector('[data-counter-due]')?.textContent || '';
                const message = vi() ? 'Bạn đã nhận đủ ' + due + ' tiền mặt từ khách?'
                        : 'Have you received ' + due + ' in cash from the guest?';
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
