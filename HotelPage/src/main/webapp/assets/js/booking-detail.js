(() => {
    "use strict";

    const form = document.getElementById("booking-status-form");
    const dialog = document.getElementById("booking-status-dialog");
    const status = document.getElementById("status");
    const current = document.getElementById("current-booking-status");
    const currentPreview = document.getElementById("booking-dialog-current");
    const nextPreview = document.getElementById("booking-dialog-next");
    const transition = dialog.querySelector(".booking-dialog-transition");
    const submit = document.getElementById("booking-dialog-submit");
    let confirmed = false;

    form.addEventListener("submit", (event) => {
        if (confirmed) {
            return;
        }

        if (typeof dialog.showModal !== "function") {
            if (!window.confirm(document.getElementById("booking-dialog-description").textContent.trim())) {
                event.preventDefault();
            }
            return;
        }

        event.preventDefault();
        currentPreview.textContent = current.textContent.trim();
        nextPreview.textContent = status.selectedOptions[0].textContent.trim();
        transition.classList.toggle("is-cancellation", status.value === "cancelled");
        dialog.showModal();
    });

    document.getElementById("booking-dialog-back").addEventListener("click", () => dialog.close());
    submit.addEventListener("click", () => {
        confirmed = true;
        form.requestSubmit();
        confirmed = false;
        dialog.close();
    });
})();
