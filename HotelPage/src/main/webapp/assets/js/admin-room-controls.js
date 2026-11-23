import {enhanceBookingSelect} from './booking-select.js';
import {createRoomPreview} from './room-preview.js';

for (const select of document.querySelectorAll('select[data-room-admin-select]')) {
    if (!select.disabled && select.validity.valid && select.options.length > 0) {
        enhanceBookingSelect(select);
    }
}

const previewElement = document.getElementById('room-preview');
const preview = createRoomPreview(previewElement);
if (preview) {
    const badge = previewElement.querySelector('[data-preview-stock]');
    for (const trigger of document.querySelectorAll('[data-admin-room-preview]')) {
        trigger.addEventListener('click', () => {
            window.bootstrap.Tooltip?.getInstance(trigger)?.hide();
            badge.textContent = trigger.closest('tr').querySelector('[data-room-status]').textContent.trim();
            preview.open(trigger, {name: trigger.dataset.previewName, url: trigger.dataset.previewUrl});
        });
    }
}
