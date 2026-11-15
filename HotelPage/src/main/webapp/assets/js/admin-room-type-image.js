(() => {
    const input = document.getElementById("picture");
    const preview = document.getElementById("room-picture");
    const error = document.getElementById("picture-error");
    const reset = document.getElementById("picture-reset");
    const originalSource = preview.src;
    const maxFileSize = 10 * 1024 * 1024;
    let previewUrl;
    let errorKind;

    function showError(kind) {
        errorKind = kind;
        let message = "";
        if (kind) {
            const key = "room.type.image." + kind;
            const translated = window.HotelLanguage?.text(key);
            message = translated && translated !== key ? translated : error.dataset[kind + "Error"];
        }
        input.setCustomValidity(message);
        error.textContent = message;
        if (message) {
            input.setAttribute("aria-invalid", "true");
        } else {
            input.removeAttribute("aria-invalid");
        }
    }

    async function updatePreview() {
        preview.src = originalSource;
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
                preview.src = source;
            }
        } catch {
            if (previewUrl === source) {
                showError("invalid");
            }
        }
    }

    input.addEventListener("change", updatePreview);
    document.addEventListener("hotel:languagechange", () => showError(errorKind));
    reset.addEventListener("click", () => {
        input.value = "";
        updatePreview();
        input.focus();
    });
})();
