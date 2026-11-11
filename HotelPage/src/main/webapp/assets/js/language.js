(() => {
    "use strict";

    if (window.HotelLanguage) {
        return;
    }

    const host = document.querySelector("[data-language-endpoint]");

    if (!host) {
        return;
    }

    const endpoint = host.dataset.languageEndpoint;

    const state = {
        language: host.dataset.language || "en",
        token: host.dataset.languageToken || "",
        messages: Object.create(null),
        busy: false
    };

    const status = host.querySelector("[data-language-status]");

    const attributes = [
        "placeholder",
        "title",
        "aria-label",
        "alt"
    ];

    const selector = [
        "[data-i18n]",
        ...attributes.map(attribute => `[data-i18n-${attribute}]`)
    ].join(",");

    function translated(key) {
        if (
            typeof key === "string"
            && Object.prototype.hasOwnProperty.call(state.messages, key)
        ) {
            return state.messages[key];
        }

        return null;
    }

    function showStatus(message) {
        if (!status) {
            return;
        }

        status.textContent = message || "";
        status.hidden = !message;
    }

    // Không hiển thị thông báo khi mới tải trang.
    showStatus("");

    function fallbackError() {
        return state.language === "vi"
            ? "Chưa thể đổi ngôn ngữ. Vui lòng thử lại."
            : "Unable to change language. Please try again.";
    }

    function applyElement(element) {
        const text = translated(element.getAttribute("data-i18n"));

        if (text !== null && element.textContent !== text) {
            element.textContent = text;
        }

        for (const attribute of attributes) {
            const key = element.getAttribute(`data-i18n-${attribute}`);
            const value = translated(key);

            if (value !== null) {
                element.setAttribute(attribute, value);
            }
        }
    }

    function apply(root = document) {
        if (
            root.nodeType === Node.ELEMENT_NODE
            && root.matches(selector)
        ) {
            applyElement(root);
        }

        root.querySelectorAll(selector).forEach(applyElement);

        document.documentElement.lang = state.language;

        document.querySelectorAll("[data-language-current]")
            .forEach(element => {
                element.textContent = state.language.toUpperCase();
            });

        document.querySelectorAll("[data-language-option]")
            .forEach(button => {
                button.setAttribute(
                    "aria-pressed",
                    String(
                        button.dataset.languageOption === state.language
                    )
                );
            });
    }

    async function request(options = {}) {
        const response = await fetch(endpoint, {
            credentials: "same-origin",
            cache: "no-store",
            headers: {
                Accept: "application/json"
            },
            ...options
        });

        const data = await response.json();

        if (typeof data.csrfToken === "string") {
            state.token = data.csrfToken;
        }

        if (!response.ok) {
            throw new Error(data.error || fallbackError());
        }

        if (
            !["en", "vi"].includes(data.lang)
            || !data.messages
            || typeof data.messages !== "object"
        ) {
            throw new Error(fallbackError());
        }

        state.language = data.lang;
        state.messages = data.messages;

        apply();

        document.dispatchEvent(
            new CustomEvent("hotel:languagechange", {
                detail: {
                    lang: state.language
                }
            })
        );
    }

    async function change(language) {
        if (
            state.busy
            || !["en", "vi"].includes(language)
        ) {
            return;
        }

        state.busy = true;
        showStatus("");

        const buttons = [
            ...document.querySelectorAll("[data-language-option]")
        ];

        buttons.forEach(button => {
            button.disabled = true;
        });

        host.setAttribute("aria-busy", "true");

        try {
            // Chờ lần tải ban đầu để tránh ghi đè lựa chọn mới.
            await ready.catch(() => {});

            // Làm mới token nếu phiên đăng nhập đã hết hạn.
            await request();

            if (language !== state.language) {
                const body = new URLSearchParams({
                    lang: language,
                    csrfToken: state.token
                });

                await request({
                    method: "POST",
                    body
                });
            }

            // Thành công: đóng menu, không hiện thông báo.
            showStatus("");
            host.querySelector("details")?.removeAttribute("open");
        } catch (error) {
            showStatus(
                error instanceof TypeError
                || error instanceof SyntaxError
                    ? fallbackError()
                    : error.message || fallbackError()
            );
        } finally {
            state.busy = false;
            host.removeAttribute("aria-busy");

            buttons.forEach(button => {
                button.disabled = false;
            });
        }
    }

    document.addEventListener("click", event => {
        if (!(event.target instanceof Element)) {
            return;
        }

        const button = event.target.closest("[data-language-option]");

        if (button) {
            change(button.dataset.languageOption);
        }

        if (!host.contains(event.target)) {
            host.querySelector("details")?.removeAttribute("open");
        }
    });

    host.addEventListener("keydown", event => {
        if (event.key === "Escape") {
            host.querySelector("details")?.removeAttribute("open");
            host.querySelector("summary")?.focus();
        }
    });

    new MutationObserver(records => {
        for (const record of records) {
            for (const node of record.addedNodes) {
                if (node.nodeType === Node.ELEMENT_NODE) {
                    apply(node);
                }
            }
        }
    }).observe(document.body, {
        childList: true,
        subtree: true
    });

    const ready = request();

    window.HotelLanguage = {
        change,
        apply,

        text(key) {
            return translated(key) ?? key;
        },

        get language() {
            return state.language;
        }
    };

    ready.catch(() => {
        showStatus(fallbackError());
    });
})();