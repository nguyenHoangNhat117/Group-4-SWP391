(() => {
    "use strict";

    function init() {
        const header = document.querySelector(
                "[data-luxe-header]"
                );

        if (
                !header
                || header.dataset.luxeReady === "true"
                ) {
            return;
        }

        header.dataset.luxeReady = "true";

        const account = header.querySelector(
                "[data-luxe-account]"
                );

        const accountTrigger = account
                ? account.querySelector("summary")
                : null;

        const navToggle = header.querySelector(
                "[data-luxe-nav-toggle]"
                );

        const navbar = header.querySelector(
                "#navbar"
                );

        const language = header.querySelector(
                ".hotel-language details"
                );

        const compactScreen = window.matchMedia(
                "(max-width: 1580px)"
                );

        function closeNavigation() {
            header.classList.remove("is-nav-open");

            if (navToggle) {
                navToggle.setAttribute(
                        "aria-expanded",
                        "false"
                        );
            }
        }

        function closeAccount() {
            if (account) {
                account.open = false;
            }
        }

        function closeLanguage() {
            if (language) {
                language.open = false;
            }
        }

        if (account && accountTrigger) {
            accountTrigger.setAttribute(
                    "aria-expanded",
                    String(account.open)
                    );

            account.addEventListener(
                    "toggle",
                    () => {
                accountTrigger.setAttribute(
                        "aria-expanded",
                        String(account.open)
                        );

                if (account.open) {
                    closeNavigation();
                    closeLanguage();
                }
            }
            );

            account.addEventListener(
                    "focusout",
                    () => {
                window.setTimeout(() => {
                    if (
                            !account.contains(
                                    document.activeElement
                                    )
                            ) {
                        closeAccount();
                    }
                }, 0);
            }
            );
        }

        if (language) {
            language.addEventListener(
                    "toggle",
                    () => {
                if (language.open) {
                    closeAccount();
                    closeNavigation();
                }
            }
            );
        }

        if (navToggle && navbar) {
            navToggle.addEventListener(
                    "click",
                    () => {
                const open =
                        header.classList.toggle(
                                "is-nav-open"
                                );

                navToggle.setAttribute(
                        "aria-expanded",
                        String(open)
                        );

                if (open) {
                    closeAccount();
                    closeLanguage();
                }
            }
            );

            navbar.addEventListener(
                    "click",
                    event => {
                        if (event.target.closest("a")) {
                            closeNavigation();
                        }
                    }
            );
        }

        document.addEventListener(
                "click",
                event => {
                    if (
                            account
                            && account.open
                            && !account.contains(event.target)
                            ) {
                        closeAccount();
                    }

                    if (
                            header.classList.contains(
                                    "is-nav-open"
                                    )
                            && !header.contains(event.target)
                            ) {
                        closeNavigation();
                    }
                }
        );

        document.addEventListener(
                "keydown",
                event => {
                    if (event.key !== "Escape") {
                        return;
                    }

                    if (account && account.open) {
                        closeAccount();
                        accountTrigger?.focus();
                        return;
                    }

                    if (
                            header.classList.contains(
                                    "is-nav-open"
                                    )
                            ) {
                        closeNavigation();
                        navToggle?.focus();
                    }
                }
        );

        compactScreen.addEventListener(
                "change",
                () => {
            closeNavigation();
        }
        );

        if (!document.querySelector('link[rel="icon"]')) {
            const favicon =
                    document.createElement("link");

            favicon.rel = "icon";
            favicon.type = "image/png";

            favicon.href =
                    (header.dataset.contextPath || "")
                    + "/assets/img/favicon.png";

            document.head.appendChild(favicon);
        }
    }

    if (document.readyState === "loading") {
        document.addEventListener(
                "DOMContentLoaded",
                init,
                {once: true}
        );

    } else {
        init();
    }
})();