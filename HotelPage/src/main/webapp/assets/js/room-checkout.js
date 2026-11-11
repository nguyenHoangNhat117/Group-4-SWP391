(() => {
    const form = document.getElementById('checkout-form');
    if (!form) return;

    const voucher = form.elements.namedItem('discount-code');
    const plan = form.elements.namedItem('payment-plan');
    const deposit = plan.querySelector('[value="deposit"]');
    const money = new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'});

    function updateSummary() {
        const quotedTotal = voucher.selectedOptions[0].dataset.total || form.dataset.total;
        const wholeVnd = /^\d+(?:\.0+)?$/.test(quotedTotal);
        const total = wholeVnd ? BigInt(quotedTotal.split('.')[0]) : 0n;
        const canDeposit = total > 0n && total % 2n === 0n;
        deposit.disabled = !canDeposit;
        if (!canDeposit && plan.value === 'deposit') {
            plan.value = 'transfer';
        }
        document.getElementById('deposit-note').hidden = canDeposit;
        const due = plan.value === 'deposit' ? total / 2n : total;
        document.getElementById('checkout-due').textContent = money.format(due);
        document.getElementById('checkout-balance').textContent = money.format(total - due);
    }

    voucher.addEventListener('change', updateSummary);
    plan.addEventListener('change', updateSummary);
    updateSummary();
})();
