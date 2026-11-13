(() => {
    const container = document.getElementById('payos-checkout');
    if (!container) {
        return;
    }

    const clock = document.getElementById('qr-clock');
    const pageUrl = new URL(container.dataset.pageUrl, window.location.origin);
    const statusUrl = new URL(pageUrl);
    statusUrl.searchParams.set('view', 'qr-status');
    const paymentOptions = document.getElementById('qr-payment-options');
    const deadline = performance.now() + Number(container.dataset.seconds) * 1000;
    let timer;
    let leaving = false;

    const waiting = document.getElementById('qr-waiting');
    function startPolling() {
        waiting.hidden = false;
        window.setTimeout(pollStatus, 8000);
    }

    async function pollStatus() {
        if (leaving) {
            return;
        }
        if (!document.hidden) {
            const waitingText = document.getElementById('qr-waiting-text');
            const processingText = document.getElementById('qr-processing-text');
            const errorText = document.getElementById('qr-poll-error');
            try {
                const response = await fetch(statusUrl, {cache: 'no-store', signal: AbortSignal.timeout(10000)});
                if (response.redirected || response.status === 401 || response.status === 403) {
                    checkStatus();
                    return;
                }
                if (!response.ok) {
                    throw new Error('Status unavailable');
                }
                const status = await response.text();
                if (status === 'paid' || status === 'cancelled' || status === 'expired' || status === 'missing') {
                    checkStatus();
                    return;
                }
                if (status !== 'pending' && status !== 'checking') {
                    throw new Error('Status unavailable');
                }
                waitingText.hidden = status !== 'pending';
                processingText.hidden = status !== 'checking';
                errorText.hidden = true;
            } catch (error) {
                waitingText.hidden = true;
                processingText.hidden = true;
                errorText.hidden = false;
            }
        }
        if (!leaving) {
            window.setTimeout(pollStatus, 8000);
        }
    }

    function checkStatus() {
        if (leaving) {
            return;
        }
        leaving = true;
        window.clearInterval(timer);
        paymentOptions.hidden = true;
        window.location.replace(pageUrl.href);
    }

    function updateClock() {
        const seconds = Math.max(0, Math.ceil((deadline - performance.now()) / 1000));
        const minutes = String(Math.floor(seconds / 60)).padStart(2, '0');
        const remainder = String(seconds % 60).padStart(2, '0');
        clock.textContent = minutes + ':' + remainder;
        if (seconds === 0) {
            checkStatus();
        }
    }

    updateClock();
    if (leaving) {
        return;
    }
    timer = window.setInterval(updateClock, 250);
    if (navigator.clipboard) {
        const copyFailed = document.getElementById('qr-copy-failed');
        document.querySelectorAll('[data-copy-field]').forEach(button => {
            const [label, copied] = button.children;
            let reset;
            button.hidden = false;
            button.addEventListener('click', async () => {
                copyFailed.hidden = true;
                try {
                    const field = document.getElementById(button.dataset.copyField);
                    await navigator.clipboard.writeText(field.textContent.trim());
                    window.clearTimeout(reset);
                    label.hidden = true;
                    copied.hidden = false;
                    button.classList.add('is-copied');
                    reset = window.setTimeout(() => {
                        label.hidden = false;
                        copied.hidden = true;
                        button.classList.remove('is-copied');
                    }, 2000);
                } catch (error) {
                    copyFailed.hidden = false;
                }
            });
        });
    }
    const qrImage = document.getElementById('qr-image');
    const payload = qrImage.dataset.payload;
    if (payload && typeof window.QRCode === 'function') {
        try {
            new window.QRCode(qrImage, {text: payload, width: 256, height: 256});
            qrImage.hidden = false;
            document.getElementById('qr-logos').hidden = false;
            document.getElementById('qr-fallback').hidden = true;
            startPolling();
            return;
        } catch (error) {
            qrImage.hidden = true;
        }
    }

    if (!container.dataset.returnUrl) {
        return;
    }
    const returnUrl = new URL(container.dataset.returnUrl);
    if (pageUrl.href !== returnUrl.href) {
        document.getElementById('qr-public-page').hidden = false;
        return;
    }
    const frame = document.getElementById('qr-embedded-fallback');
    try {
        frame.hidden = false;
        window.PayOSCheckout.usePayOS({
            RETURN_URL: returnUrl.href,
            ELEMENT_ID: frame.id,
            CHECKOUT_URL: container.dataset.checkoutUrl,
            embedded: true,
            onSuccess: checkStatus,
            onCancel: checkStatus
        }).open();
        document.getElementById('qr-fallback').hidden = true;
        startPolling();
    } catch (error) {
        frame.hidden = true;
    }
})();
