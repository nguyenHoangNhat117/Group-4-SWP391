if (window.bootstrap?.Dropdown) {
    document.querySelectorAll(".payment-history-filter select").forEach(select => {
        const label = select.labels[0];
        const toggle = document.createElement("button");
        toggle.type = "button";
        toggle.id = select.id + "-toggle";
        toggle.className = "form-select payment-history-filter-toggle";
        toggle.setAttribute("data-bs-toggle", "dropdown");
        toggle.setAttribute("aria-expanded", "false");
        label.id = select.id + "-label";
        label.htmlFor = toggle.id;
        toggle.setAttribute("aria-labelledby", label.id + " " + toggle.id);

        const menu = document.createElement("div");
        menu.className = "dropdown-menu payment-history-filter-menu";
        menu.setAttribute("aria-labelledby", label.id);
        const items = Array.from(select.options, option => {
            const item = document.createElement("button");
            item.type = "button";
            item.className = "dropdown-item";
            item.addEventListener("click", () => {
                select.selectedIndex = option.index;
                select.dispatchEvent(new Event("change", { bubbles: true }));
                toggle.focus();
            });
            menu.append(item);
            return item;
        });

        function syncSelection() {
            toggle.textContent = select.options[select.selectedIndex].text;
            items.forEach((item, index) => {
                const selected = index === select.selectedIndex;
                item.textContent = select.options[index].text;
                item.classList.toggle("active", selected);
                if (selected) {
                    item.setAttribute("aria-current", "true");
                } else {
                    item.removeAttribute("aria-current");
                }
            });
        }

        select.parentElement.classList.add("dropdown");
        select.after(toggle, menu);
        select.hidden = true;
        select.addEventListener("change", syncSelection);
        window.addEventListener("pageshow", syncSelection);
        syncSelection();
    });
}

(function setupPaymentCalendar() {
    const dateField = document.getElementById("payment-date");
    if (!dateField || typeof Litepicker === "undefined") {
        return;
    }

    const display = document.createElement("input");
    display.type = "text";
    display.className = dateField.className + " payment-history-calendar";
    display.id = "payment-date-picker";
    display.readOnly = true;
    dateField.labels[0].htmlFor = display.id;
    dateField.type = "hidden";
    dateField.after(display);

    display.placeholder = "Chọn ngày";
    const picker = new Litepicker({
        element: display,
        singleMode: true,
        autoApply: true,
        numberOfMonths: 1,
        numberOfColumns: 1,
        lang: "vi-VN",
        format: "DD/MM/YYYY",
        startDate: dateField.value ? new Date(dateField.value + "T00:00:00") : null,
        setup(calendar) {
            calendar.on("selected", start => {
                dateField.value = start.format("YYYY-MM-DD");
            });
        }
    });

    display.addEventListener("keydown", event => {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            picker.show();
        } else if (event.key === "Backspace" || event.key === "Delete") {
            event.preventDefault();
            dateField.value = "";
            picker.clearSelection();
        } else if (event.key === "Escape") {
            picker.hide();
        }
    });
})();
