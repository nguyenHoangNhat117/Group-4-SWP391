(() => {
    const form = document.getElementById('checkout-form');
    if (!form) return;

    const plan = form.elements.namedItem('payment-plan');
    const voucher = form.elements.namedItem('discount-code');
    const money = new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'});
    function updateSummary() {
        const total = BigInt((voucher.selectedOptions[0].dataset.total || form.dataset.total).split('.')[0]);
        const due = plan.value === 'deposit' ? (total + 1n) / 2n : total;
        document.getElementById('checkout-total').textContent = money.format(total);
        document.getElementById('checkout-due').textContent = money.format(due);
        document.getElementById('checkout-balance').textContent = money.format(total - due);
    }
    plan.addEventListener('change', updateSummary);
    voucher.addEventListener('change', updateSummary);
    updateSummary();
})();
