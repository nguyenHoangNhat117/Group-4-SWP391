(() => {
    const form = document.getElementById('booking-confirmation-form');
    const consent = form.elements.namedItem('booking-consent');
    const plan = form.elements.namedItem('payment-plan');
    const voucher = form.elements.namedItem('discount-code');
    const deposit = plan.querySelector('[value="deposit"]');
    const button = form.querySelector('[type="submit"]');
    const due = document.getElementById('confirmation-due');
    const gross = BigInt(form.dataset.total.split('.')[0]);
    const total = document.getElementById('confirmation-total');
    const discountRow = document.getElementById('confirmation-discount-row');
    const discount = document.getElementById('confirmation-discount');
    const money = new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'});
    let submitting = false;

    function update() {
        const net = BigInt((voucher.selectedOptions[0].dataset.total || form.dataset.total).split('.')[0]);
        deposit.disabled = net <= 0n || net % 2n !== 0n;
        if (deposit.disabled && plan.value === 'deposit') {
            plan.value = 'transfer';
        }
        discountRow.hidden = net === gross;
        discount.textContent = money.format(gross - net);
        total.textContent = money.format(net);
        due.textContent = money.format(plan.value === 'deposit' ? net / 2n : net);
        button.disabled = submitting || !consent.checked || form.dataset.conflict === 'true';
    }

    consent.addEventListener('change', update);
    plan.addEventListener('change', update);
    voucher.addEventListener('change', update);
    form.addEventListener('submit', event => {
        if (event.submitter?.name === 'back-to-rooms' && !submitting) {
            return;
        }
        if (button.disabled) {
            event.preventDefault();
            return;
        }
        submitting = true;
        update();
    });
    window.addEventListener('pageshow', () => {
        submitting = false;
        update();
    });
    update();
})();
