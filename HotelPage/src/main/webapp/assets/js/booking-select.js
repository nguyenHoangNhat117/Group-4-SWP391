export function enhanceBookingSelect(select) {
    const label = select.labels[0];
    const wrapper = document.createElement('div');
    wrapper.className = 'booking-dropdown';
    wrapper.classList.toggle('mb-3', select.classList.contains('mb-3'));
    const trigger = document.createElement('button');
    trigger.type = 'button';
    trigger.id = select.id + '-trigger';
    trigger.className = 'form-select booking-select booking-dropdown-trigger';
    trigger.setAttribute('aria-invalid', select.getAttribute('aria-invalid') || 'false');
    const value = document.createElement('span');
    value.id = select.id + '-selection';
    trigger.append(value);
    label.id = select.id + '-label';
    label.htmlFor = trigger.id;
    trigger.setAttribute('aria-labelledby', label.id + ' ' + value.id);

    const menu = document.createElement('div');
    menu.id = select.id + '-choices';
    menu.className = 'booking-dropdown-choices';
    menu.hidden = true;
    trigger.setAttribute('aria-expanded', 'false');
    trigger.setAttribute('aria-controls', menu.id);

    const buttons = Array.from(select.options, (option, index) => {
        const choice = document.createElement('button');
        choice.type = 'button';
        choice.className = 'booking-dropdown-option';
        choice.textContent = option.label.trim();
        const mark = document.createElement('span');
        mark.className = 'booking-dropdown-check';
        mark.textContent = '✓';
        mark.setAttribute('aria-hidden', 'true');
        choice.append(mark);
        choice.addEventListener('click', () => {
            select.selectedIndex = index;
            sync();
            close(true);
            select.dispatchEvent(new Event('change', {bubbles: true}));
        });
        menu.append(choice);
        return choice;
    });

    select.before(wrapper);
    wrapper.append(select, trigger, menu);
    select.hidden = true;

    function sync() {
        value.textContent = select.selectedOptions[0]?.label.trim() || '';
        buttons.forEach((choice, index) => {
            choice.disabled = select.options[index].disabled;
            choice.setAttribute('aria-pressed', String(index === select.selectedIndex));
        });
    }

    function open() {
        menu.hidden = false;
        trigger.setAttribute('aria-expanded', 'true');
        const selected = buttons[select.selectedIndex];
        (selected && !selected.disabled ? selected : buttons.find(choice => !choice.disabled))?.focus();
    }

    function close(restoreFocus = false) {
        menu.hidden = true;
        trigger.setAttribute('aria-expanded', 'false');
        if (restoreFocus) {
            trigger.focus();
        }
    }

    trigger.addEventListener('click', () => menu.hidden ? open() : close());
    wrapper.addEventListener('focusout', event => {
        if (!wrapper.contains(event.relatedTarget)) {
            close();
        }
    });
    document.addEventListener('pointerdown', event => {
        if (!menu.hidden && !wrapper.contains(event.target)) {
            close();
        }
    });
    wrapper.addEventListener('keydown', event => {
        if (event.key === 'Escape' && !menu.hidden) {
            event.preventDefault();
            close(true);
        } else if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
            event.preventDefault();
            if (menu.hidden) {
                open();
                return;
            }
            const enabled = buttons.filter(choice => !choice.disabled);
            const index = enabled.indexOf(event.target);
            const step = event.key === 'ArrowDown' ? 1 : -1;
            const next = index < 0 ? (step > 0 ? 0 : enabled.length - 1)
                : (index + step + enabled.length) % enabled.length;
            enabled[next]?.focus();
        }
    });
    window.addEventListener('pageshow', sync);
    sync();
    return sync;
}
