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
        document.addEventListener("hotel:languagechange", syncSelection);
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

    let picker;
    let activeLanguage;
    function useLanguage(language) {
        if (language === activeLanguage) {
            return;
        }
        activeLanguage = language;
        if (picker) {
            picker.destroy();
        }
        display.placeholder = language === "vi" ? "Chọn ngày" : "Select a date";
        picker = new Litepicker({
            element: display,
            singleMode: true,
            autoApply: true,
            numberOfMonths: 1,
            numberOfColumns: 1,
            lang: language === "vi" ? "vi-VN" : "en-US",
            format: "DD/MM/YYYY",
            startDate: dateField.value ? new Date(dateField.value + "T00:00:00") : null,
            setup(calendar) {
                calendar.on("selected", start => {
                    dateField.value = start.format("YYYY-MM-DD");
                });
            }
        });
    }

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
    useLanguage(document.documentElement.lang);
    document.addEventListener("hotel:languagechange", ({ detail }) => useLanguage(detail.lang));
})();

document.addEventListener("hotel:languagechange", ({ detail }) => {
    const paymentHistory = document.querySelector(".payment-history");
    const locale = detail.lang === "vi" ? "vi-VN" : "en-US";
    const numberFormat = new Intl.NumberFormat(locale, { maximumFractionDigits: 2 });
    const dateFormat = new Intl.DateTimeFormat(locale, { dateStyle: "medium", timeZone: "UTC" });
    const dateTimeFormat = new Intl.DateTimeFormat(locale, {
        dateStyle: "medium",
        timeStyle: "short",
        timeZone: "UTC"
    });

    paymentHistory.querySelectorAll("[data-history-money]").forEach(amount => {
        amount.textContent = numberFormat.format(amount.dataset.historyMoney);
    });

    // PaymentDate and stay dates are stored as local calendar values.
    paymentHistory.querySelectorAll("[data-history-date], [data-history-datetime]").forEach(time => {
        const isStayDate = time.hasAttribute("data-history-date");
        const value = isStayDate ? time.dateTime + "T00:00:00" : time.dateTime;
        time.textContent = (isStayDate ? dateFormat : dateTimeFormat).format(new Date(value + "Z"));
    });
});
