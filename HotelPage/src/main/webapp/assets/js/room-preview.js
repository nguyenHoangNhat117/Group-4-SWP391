export function createRoomPreview(element, onUpdate = () => {}) {
    if (!element || !window.bootstrap?.Modal) {
        return null;
    }

    const modal = window.bootstrap.Modal.getOrCreateInstance(element);
    const title = element.querySelector('.modal-title');
    const close = element.querySelector('.btn-close');
    const status = element.querySelector('[data-preview-status]');
    const content = element.querySelector('[data-preview-content]');
    let active;

    async function open(trigger, {id, name, url}) {
        if (active) {
            return;
        }
        const current = {id, trigger, request: new AbortController(), loaded: false, shown: false};
        active = current;
        title.textContent = name;
        status.textContent = element.dataset.loading;
        status.hidden = false;
        content.replaceChildren();
        onUpdate(current);
        modal.show(trigger);

        try {
            const response = await fetch(url, {
                signal: current.request.signal, cache: 'no-store', redirect: 'error'
            });
            if (!response.ok) {
                throw new Error('Room preview request failed');
            }
            const fragment = await response.text();
            if (!current.request.signal.aborted) {
                content.innerHTML = fragment;
                status.hidden = true;
                current.loaded = true;
                onUpdate(current);
            }
        } catch {
            if (!current.request.signal.aborted) {
                status.textContent = element.dataset.failed;
            }
        }
    }

    element.addEventListener('shown.bs.modal', () => {
        active.shown = true;
        onUpdate(active);
        close.focus({preventScroll: true});
    });
    element.addEventListener('hide.bs.modal', () => {
        active.shown = false;
        active.request.abort();
        onUpdate(active);
    });
    element.addEventListener('hidden.bs.modal', () => {
        const trigger = active.trigger;
        active = null;
        trigger.focus({preventScroll: true});
    });

    return {
        open,
        hide: () => modal.hide(),
        get active() { return active; }
    };
}
