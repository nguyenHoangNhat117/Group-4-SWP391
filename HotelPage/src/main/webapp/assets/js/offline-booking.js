(() => {
    const form = document.getElementById("offlineForm");

    if (!form) {
        return;
    }

    const dateIn = form.elements.checkIn;
    const dateOut = form.elements.checkOut;
    const guests = form.elements.guests;
    const room = form.elements.roomNumber;

    const money = new Intl.NumberFormat("vi-VN", {style: "currency", currency: "VND"});

    function show(id, value) {
        const element = document.getElementById(id);

        if (element) {
            element.textContent = value;
        }
    }

    function formatDate(value) {
        return value
                ? value.split("-").reverse().join("/")
                : "—";
    }

    function update() {
        const option = room.options[room.selectedIndex];

        const nights = dateIn.value && dateOut.value
                ? (
                        Date.parse(dateOut.value + "T00:00:00Z")
                        - Date.parse(dateIn.value + "T00:00:00Z")
                        ) / 86400000
                : 0;

        show(
                "obRoom",
                room.value ? "Phòng " + room.value : "Chọn phòng"
                );

        show(
                "obType",
                room.value
                ? option.dataset.label
                : "Thông tin đặt phòng"
                );

        show(
                "obDates",
                formatDate(dateIn.value)
                + " → "
                + formatDate(dateOut.value)
                );

        show(
                "obNights",
                nights > 0 ? nights + " đêm" : "—"
                );

        show(
                "obGuests",
                guests.value ? guests.value + " khách" : "—"
                );

        const price = Number(option?.dataset.price);

        show(
                "obAmount",
                room.value && nights > 0 && Number.isFinite(price)
                ? money.format(price * nights)
                : "—"
                );

        if (dateIn.value) {
            const next = new Date(
                    dateIn.value + "T00:00:00Z"
                    );

            next.setUTCDate(next.getUTCDate() + 1);
            dateOut.min = next.toISOString().slice(0, 10);
        }
    }

    function customerMode() {
        const mode = form.querySelector(
                '[name="customerMode"]:checked'
                )?.value || "existing";

        form.querySelectorAll(
                "[data-customer-pane]"
                ).forEach(pane => {
            const active =
                    pane.dataset.customerPane === mode;

            pane.hidden = !active;

            pane.querySelectorAll(
                    "input, select, button"
                    ).forEach(input => {
                input.disabled = !active;
            });
        });
    }

    form.addEventListener("input", update);

    form.addEventListener("change", event => {
        if (event.target.name === "customerMode") {
            customerMode();
        }

        update();
    });

    customerMode();
    update();

    // Giữ action=create khi khóa nút: nút disabled sẽ không được browser gửi lên.
    let submittedAction = null;
    let disabledButtons = [];
    form.addEventListener("submit", event => {
        if (form.dataset.creating === "true") {
            event.preventDefault();
            return;
        }
        if (event.submitter?.value !== "create") {
            return;
        }
        submittedAction = document.createElement("input");
        submittedAction.type = "hidden";
        submittedAction.name = "action";
        submittedAction.value = "create";
        form.appendChild(submittedAction);
        form.dataset.creating = "true";
        form.setAttribute("aria-busy", "true");
        disabledButtons = Array.from(form.querySelectorAll("button:not(:disabled)"));
        disabledButtons.forEach(button => { button.disabled = true; });
    });

    window.addEventListener("pageshow", () => {
        delete form.dataset.creating;
        form.removeAttribute("aria-busy");
        submittedAction?.remove();
        submittedAction = null;
        disabledButtons.forEach(button => { button.disabled = false; });
        disabledButtons = [];
    });
})();
