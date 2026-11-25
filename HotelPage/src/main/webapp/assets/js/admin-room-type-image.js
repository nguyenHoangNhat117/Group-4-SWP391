(() => {
    const input = document.getElementById("picture");
    const preview = document.getElementById("room-picture");
    const placeholder = document.getElementById("room-picture-placeholder");
    const error = document.getElementById("picture-error");
    const reset = document.getElementById("picture-reset");
    const originalSource = preview.getAttribute("src");
    const maxFileSize = 10 * 1024 * 1024;
    let previewUrl;

    function showError(kind) {
        const message = kind ? error.dataset[kind + "Error"] : "";
        input.setCustomValidity(message);
        error.textContent = message;
        if (message) {
            input.setAttribute("aria-invalid", "true");
        } else {
            input.removeAttribute("aria-invalid");
        }
    }

    function showPreview(source) {
        if (source) {
            preview.src = source;
        } else {
            preview.removeAttribute("src");
        }
        preview.hidden = !source;
        if (placeholder) {
            placeholder.hidden = Boolean(source);
        }
    }

    async function updatePreview() {
        showPreview(originalSource);
        if (previewUrl) {
            URL.revokeObjectURL(previewUrl);
            previewUrl = undefined;
        }
        showError();

        const file = input.files[0];
        reset.hidden = !file;
        if (!file) {
            return;
        }
        if (!/\.(png|jpe?g)$/i.test(file.name)) {
            showError("type");
            return;
        }
        if (file.size > maxFileSize) {
            showError("size");
            return;
        }

        const candidate = new Image();
        const source = URL.createObjectURL(file);
        previewUrl = source;
        candidate.src = source;
        try {
            await candidate.decode();
            if (previewUrl === source) {
                showPreview(source);
            }
        } catch {
            if (previewUrl === source) {
                showError("invalid");
            }
        }
    }

    input.addEventListener("change", updatePreview);
    reset.addEventListener("click", () => {
        input.value = "";
        updatePreview();
        input.focus();
    });
})();
