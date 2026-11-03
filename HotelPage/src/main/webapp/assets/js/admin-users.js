'use strict';
(() => {
    const input = document.getElementById('password');
    const toggle = document.getElementById('toggle-password');
    toggle.addEventListener('click', () => {
        const show = input.type === 'password';
        input.type = show ? 'text' : 'password';
        toggle.textContent = show ? 'Hide' : 'Show';
        toggle.setAttribute('aria-pressed', String(show));
    });
    const random = limit => {
        const bytes = new Uint32Array(1);
        const cutoff = Math.floor(4294967296 / limit) * limit;
        do {
            crypto.getRandomValues(bytes);
        } while (bytes[0] >= cutoff);
        return bytes[0] % limit;
    };
    document.getElementById('generate-password').addEventListener('click', () => {
        const alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%';
        const chars = ['A', 'a', '7', '!'];
        while (chars.length < 20)
            chars.push(alphabet[random(alphabet.length)]);
        for (let i = chars.length - 1; i > 0; i--) {
            const j = random(i + 1);
            [chars[i], chars[j]] = [chars[j], chars[i]];
        }
        input.value = chars.join('');
        input.type = 'text';
        toggle.textContent = 'Hide';
        toggle.setAttribute('aria-pressed', 'true');
        input.focus();
    });
})();
