(() => {
    "use strict";
    if (window.HotelLanguage)
        return;
    const host = document.querySelector("[data-language-endpoint]");
    if (!host)
        return;
    const endpoint = host.dataset.languageEndpoint;
    const status = host.querySelector("[data-language-status]");
    const attributes = ["placeholder", "title", "aria-label", "alt"];
    const markers = ["data-i18n", ...attributes.map(name => `data-i18n-${name}`)];
    const selector = markers.map(name => `[${name}]`).join(",");
    const state = {
        language: host.dataset.language || "en", token: host.dataset.languageToken || "",
        messages: Object.create(null), signature: "", busy: false
    };
    let queue = Promise.resolve();
    let lastRefresh = 0;
    const noticeKey = `hotel:language:${new URL(endpoint, location.href).pathname}`;
    let channel;
    try {
        if (typeof BroadcastChannel === "function")
            channel = new BroadcastChannel(noticeKey);
    } catch (ignored) { /* Cookie/session still work when browser messaging is disabled. */
    }

    function text(key) {
        return typeof key === "string" && Object.prototype.hasOwnProperty.call(state.messages, key)
                ? state.messages[key] : null;
    }
    function showStatus(value) {
        if (status) {
            status.textContent = value || "";
            status.hidden = !value;
        }
    }
    function failure() {
        return text("language.unavailable") || (state.language === "vi"
                ? "Chưa thể đổi ngôn ngữ. Vui lòng thử lại."
                : "Unable to change language. Please try again.");
    }
    function writeLabel(element, value) {
        // Dịch nhãn, không xóa icon, input, hay dữ liệu con trong nút/form.
        if (!element.children.length) {
            if (element.textContent !== value)
                element.textContent = value;
            return;
        }
        const nodes = Array.from(element.childNodes).filter(node => node.nodeType === Node.TEXT_NODE && node.textContent.trim());
        if (nodes.length) {
            if (nodes[0].textContent.trim() !== value)
                nodes[0].textContent = ` ${value} `;
            nodes.slice(1).forEach(node => {
                node.textContent = "";
            });
        }
    }
    function applyElement(element) {
        const value = text(element.getAttribute("data-i18n"));
        if (value !== null && !element.matches("input,textarea"))
            writeLabel(element, value);
        for (const name of attributes) {
            const translated = text(element.getAttribute(`data-i18n-${name}`));
            if (translated !== null && element.getAttribute(name) !== translated)
                element.setAttribute(name, translated);
        }
    }
    function apply(root = document) {
        if (root.nodeType === Node.ELEMENT_NODE && root.matches(selector))
            applyElement(root);
        root.querySelectorAll(selector).forEach(applyElement);
        document.documentElement.lang = state.language;
        host.dataset.language = state.language;
        document.querySelectorAll("[data-language-current]").forEach(node => {
            node.textContent = state.language.toUpperCase();
        });
        document.querySelectorAll("[data-language-option]").forEach(button => {
            button.setAttribute("aria-pressed", String(button.dataset.languageOption === state.language));
        });
    }
    function commit(data) {
        const signature = JSON.stringify([data.lang, data.messages]);
        const changed = signature !== state.signature;
        state.language = data.lang;
        state.messages = Object.assign(Object.create(null), data.messages);
        state.signature = signature;
        apply();
        if (changed)
            document.dispatchEvent(new CustomEvent("hotel:languagechange", {detail: {lang: state.language}}));
    }
    async function payload(options = {}) {
        const response = await fetch(endpoint, {
            credentials: "same-origin", cache: "no-store", redirect: "error",
            headers: {Accept: "application/json"}, ...options
        });
        const data = await response.json();
        if (typeof data.csrfToken === "string")
            state.token = data.csrfToken;
        if (!response.ok) {
            const error = new Error(data.error || failure());
            error.status = response.status;
            throw error;
        }
        if (!["en", "vi"].includes(data.lang) || !data.messages || typeof data.messages !== "object"
                || Array.isArray(data.messages) || Object.values(data.messages).some(value => typeof value !== "string")) {
            throw new Error(failure());
        }
        return data;
    }
    function schedule(task) {
        const result = queue.catch(() => {
        }).then(task);
        queue = result;
        return result;
    }
    function refresh() {
        return schedule(async () => {
            try {
                commit(await payload());
                showStatus("");
            } catch (error) {
                showStatus(failure());
            }
        });
    }
    function notifyOtherTabs() {
        try {
            channel?.postMessage({changed: true});
        } catch (ignored) {
        }
        try {
            localStorage.setItem(noticeKey, `${Date.now()}:${Math.random()}`);
        } catch (ignored) {
        }
    }
    function change(language) {
        if (!["en", "vi"].includes(language))
            return Promise.resolve();
        return schedule(async () => {
            state.busy = true;
            host.setAttribute("aria-busy", "true");
            showStatus("");
            document.querySelectorAll("[data-language-option]").forEach(button => {
                button.disabled = true;
            });
            try {
                // Làm mới token, nhưng chỉ đổi nhãn sau khi lưu lựa chọn thành công.
                let data = await payload();
                if (data.lang !== language) {
                    const post = () => payload({method: "POST", body: new URLSearchParams({lang: language, csrfToken: state.token})});
                    try {
                        data = await post();
                    } catch (error) {
                        if (error.status !== 403)
                            throw error;
                        await payload(); // Session vừa thay đổi: lấy token mới và thử đúng một lần.
                        data = await post();
                    }
                }
                commit(data);
                notifyOtherTabs();
                host.querySelector("details")?.removeAttribute("open");
            } catch (error) {
                showStatus(error instanceof TypeError || error instanceof SyntaxError ? failure() : error.message || failure());
            } finally {
                state.busy = false;
                host.removeAttribute("aria-busy");
                document.querySelectorAll("[data-language-option]").forEach(button => {
                    button.disabled = false;
                });
            }
        });
    }
    function syncVisible() {
        if (document.visibilityState === "hidden" || state.busy || Date.now() - lastRefresh < 1000)
            return;
        lastRefresh = Date.now();
        refresh();
    }
    document.addEventListener("click", event => {
        if (!(event.target instanceof Element))
            return;
        const button = event.target.closest("[data-language-option]");
        if (button && !button.disabled)
            change(button.dataset.languageOption);
        if (!host.contains(event.target))
            host.querySelector("details")?.removeAttribute("open");
    });
    host.addEventListener("keydown", event => {
        if (event.key === "Escape") {
            host.querySelector("details")?.removeAttribute("open");
            host.querySelector("summary")?.focus();
        }
    });
    new MutationObserver(records => {
        for (const record of records) {
            if (record.type === "attributes")
                applyElement(record.target);
            for (const node of record.addedNodes || [])
                if (node.nodeType === Node.ELEMENT_NODE)
                    apply(node);
        }
    }).observe(document.body, {childList: true, subtree: true, attributes: true, attributeFilter: markers});
    if (channel)
        channel.onmessage = () => refresh();
    window.addEventListener("storage", event => {
        if (event.key === noticeKey)
            refresh();
    });
    window.addEventListener("focus", syncVisible);
    window.addEventListener("pageshow", event => {
        if (event.persisted)
            syncVisible();
    });
    document.addEventListener("visibilitychange", syncVisible);
    window.HotelLanguage = {change, apply, refresh, text(key) {
            return text(key) ?? key;
        }, get language() {
            return state.language;
        }};
    showStatus("");
    refresh();
})();
