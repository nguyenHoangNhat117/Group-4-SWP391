(() => {
    const selection = document.getElementById('confirmed-room-selection');
    const draftKey = 'hotel-room-selection:' + new URL(selection.dataset.roomUrl, window.location.href).pathname;
    try {
        if (sessionStorage.getItem(draftKey + ':confirmation') === selection.dataset.requestKey) {
            sessionStorage.removeItem(draftKey);
            sessionStorage.removeItem(draftKey + ':confirmation');
        }
    } catch {
        // Browser storage can be disabled.
    }
})();
