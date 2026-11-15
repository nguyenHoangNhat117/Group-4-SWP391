(() => {
    "use strict";
    if (window.UC24Labels)
        return;

    const bindings = new WeakMap();

    const localLabels = {
        reportBrand: [
            "LUXE ESCAPE / REPORTS",
            "LUXE ESCAPE / BÁO CÁO"
        ],
        roomNights: [
            "{count} room nights; through {date}",
            "{count} đêm phòng; đến {date}"
        ],
        available: ["Available", "Còn trống"],
        maintenance: ["Maintenance", "Bảo trì"],
        out_of_service: ["Out of service", "Ngừng phục vụ"]
    };

    const groups = [
        [
            ".hotel-reports",
            ".report-heading h1,.report-eyebrow,.report-description," +
                    ".report-tabs a,.report-date-filter label," +
                    ".report-date-filter option,.report-button," +
                    ".uc23-period label,.uc23-cards>div>span," +
                    ".uc23-cards>div>small,.uc23-scope," +
                    ".report-panel-heading h2,.report-panel-heading>span," +
                    ".report-table thead th,.report-empty,.report-status," +
                    ".uc23-columns li>span,.uc23-review-summary p"
        ],
        [
            ".wallet-wrap,.wallet-container,.hotel-wallet-container," +
                    ".withdrawal-container,.management-container,.page-container",
            "h1,h2,h3,h4,label,.wallet-back-link,.wallet-links a," +
                    ".wallet-balance>.small,.wallet-panel-head p," +
                    ".hotel-wallet-container>p,.wallet-container>p,.wallet-panel>p," +
                    ".wallet-filter option,select[name='status'] option," +
                    ".wallet-filter button,.wallet-filter a," +
                    ".wallet-table thead th,.management-table thead th," +
                    ".wallet-pill,.wallet-empty,.wallet-pagination a," +
                    ".wallet-pagination button,.wallet-pagination span," +
                    ".wallet-pagination li,.wallet-pagination strong," +
                    ".wallet-btn,.wallet-btn-outline,.wallet-btn-outline-secondary," +
                    ".alert,.balance-card>.small,.page-container>p.text-muted," +
                    ".management-container>p.text-muted," +
                    ".withdrawal-container>p.text-muted," +
                    ".wallet-table td:last-child .small.text-muted," +
                    ".wallet-table td:last-child .small.text-danger," +
                    ".form-text,button[type='submit'],a.btn,.badge,.small.text-success"
        ],
        [
            ".ob-page",
            ".ob-heading h1,.ob-note,.ob-button,.ob-quote-title," +
                    ".ob-quote-badge,.ob-quote-customer>span,.ob-customer-label," +
                    ".ob-quote-lines dt,.ob-quote-total>span," +
                    ".ob-quote-deposit span,.ob-quote-deposit small," +
                    "#obSearchChanged,#obQuoteChanged,.ob-summary label"
        ],
        [
            ".admin-users-page,.users-layout,.admin-shell",
            ".page-heading h1,.metric small,.alert,.form-label," +
                    ".form-text,.invalid-feedback,.modal-title,button[type='submit']"
        ],
        [
            ".walkin-activation",
            "h1,label,.auth-hint,.alert,button,.btn"
        ]
    ];

    const blocked =
            "script,style,input,textarea,code,pre,[contenteditable],[data-user-content]";

    function api() {
        return window.HotelLanguage;
    }

    function norm(value) {
        return String(value).trim().replace(/\s+/g, " ");
    }

    function identify(value) {
        const lang = api();

        for (const [name, values] of Object.entries(localLabels)) {
            if (values.some(text =>
                norm(text).toLowerCase() === value.toLowerCase())) {
                return {local: name, args: {}};
            }
        }

        if (Object.prototype.hasOwnProperty.call(
                localLabels, value.toLowerCase())) {
            return {local: value.toLowerCase(), args: {}};
        }

        const roomNights = value.match(
                /^([\d.,]+\s*\/\s*[\d.,]+)\s+(?:room nights; through|đêm phòng; đến)\s+(\d{4}-\d{2}-\d{2})$/i
                );

        if (roomNights) {
            return {
                local: "roomNights",
                args: {
                    count: roomNights[1],
                    date: roomNights[2]
                }
            };
        }

        const key = lang.key(value);

        if (key)
            return {key, args: {}};

        const pagination = value.match(
                /^(?:Page|Trang)\s+(\d+)\s+(?:of|trên)\s+(\d+)\s*\(\s*(\d+)\s+(?:transactions|giao dịch)\s*\)$/i
                );

        if (pagination) {
            const pageKey = lang.key(
                    "Page {page} of {pages} ({count} transactions)"
                    );

            if (pageKey) {
                return {
                    key: pageKey,
                    args: {
                        page: pagination[1],
                        pages: pagination[2],
                        count: pagination[3]
                    }
                };
            }
        }

        const patterns = [
            [
                /^(?:Kỳ|Period):\s*(.+)$/i,
                "Period: {value}", "value"
            ],
            [
                /^(?:Theo năm|Year):\s*(.+)$/i,
                "Year: {value}", "value"
            ],
            [
                /^(\d+)\s+(?:kết quả|results)$/i,
                "{count} results", "count"
            ],
            [
                /^(\d+)\s+(?:khách|guests)$/i,
                "{count} guests", "count"
            ],
            [
                /^(?:Tiền phòng|Room charge)\s*\((\d+)\s+(?:đêm|nights)\)$/i,
                "Room charge ({count} nights)", "count"
            ],
            [
                /^(?:Cọc dự kiến|Estimated deposit)\s*\(([^)]+)\)$/i,
                "Estimated deposit ({value})", "value"
            ],
            [
                /^(?:Chờ\s*\/\s*Từ chối|Pending\s*\/\s*Rejected):\s*(.+)$/i,
                "Pending / Rejected: {value}", "value"
            ],
            [
                /^([\d.,]+)%\s+(?:tổng tiền|of total)$/i,
                "{value}% of total", "value"
            ],
            [
                /^(?:Wallet Balance|Số dư ví):\s*(.+)$/i,
                "Wallet Balance: {value}", "value"
            ],
            [
                /^(?:Reserved for Withdrawals|Đang giữ cho yêu cầu rút tiền):\s*(.+)$/i,
                "Reserved for Withdrawals: {value}", "value"
            ],
            [
                /^(?:Reason|Lý do):\s*(.+)$/i,
                "Reason: {value}", "value"
            ]
        ];

        for (const [pattern, phrase, name] of patterns) {
            const match = value.match(pattern);
            const patternKey = lang.key(phrase);

            if (match && patternKey) {
                return {
                    key: patternKey,
                    args: {[name]: match[1]}
                };
            }
        }

        return null;
    }

    function renderText(node) {
        if (!node.parentElement ||
                node.parentElement.closest(blocked + ",[data-i18n]")) {
            return;
        }

        const current = node.data;

        // Không dịch tùy tiện dữ liệu khách hàng trong bảng.
        if (node.parentElement.closest("tbody") &&
                !node.parentElement.closest(
                        ".report-status,.wallet-pill,.badge,button,label," +
                        ".report-actions a,[data-uc24-controlled-label]"
                        ) &&
                !/^(?:Reason|Lý do):/i.test(norm(current)) &&
                ![
                    "Awaiting payout confirmation",
                    "Payout recorded",
                    "Đang chờ xác nhận chi trả",
                    "Đã ghi nhận chi trả"
                ].includes(norm(current))) {
            return;
        }

        if (!norm(current))
            return;

        let binding = bindings.get(node);

        if (!binding || current !== binding.last) {
            binding = identify(norm(current));
        }

        if (!binding)
            return;

        let output = binding.local
                ? localLabels[binding.local][api().language === "vi" ? 1 : 0]
                : api().text(binding.key);

        output = output.replace(/\{(\w+)\}/g, (match, name) =>
            Object.prototype.hasOwnProperty.call(binding.args, name)
                    ? binding.args[name]
                    : match
        );

        const next =
                (current.match(/^\s*/)?.[0] || "") +
                output +
                (current.match(/\s*$/)?.[0] || "");

        binding.last = next;
        bindings.set(node, binding);

        if (current !== next)
            node.data = next;
    }

    function label(element) {
        if (element.closest(blocked))
            return;

        const walker = document.createTreeWalker(
                element, NodeFilter.SHOW_TEXT
                );
        const nodes = [];

        while (walker.nextNode())
            nodes.push(walker.currentNode);

        nodes.forEach(renderText);
    }

    function apply() {
        const lang = api();

        if (!lang || !lang.key("Language"))
            return;

        let knownPage = false;

        for (const [rootSelector, labels] of groups) {
            document.querySelectorAll(rootSelector).forEach(root => {
                knownPage = true;
                root.querySelectorAll(labels).forEach(label);

                root.querySelectorAll("[placeholder]").forEach(element => {
                    const key = lang.key(
                            element.getAttribute("placeholder")
                            );

                    if (key &&
                            element.getAttribute("data-i18n-placeholder") !== key) {
                        element.setAttribute("data-i18n-placeholder", key);
                    }
                });
            });
        }

        document.querySelectorAll(".hotel-reports table").forEach(table => {
            const cells = table.tHead?.rows[0]?.cells;

            if (cells?.length === 3 &&
                    /^(status|trạng thái)$/i.test(norm(cells[0].textContent))) {
                table.querySelectorAll("tbody tr").forEach(row => {
                    const cell = row.cells[0];

                    if (cell) {
                        cell.dataset.uc24ControlledLabel = "true";
                        label(cell);
                    }
                });
            }
        });

        document.querySelectorAll(
                ".hotel-wallet-container table tbody tr"
                ).forEach(row => {
            if (row.cells.length === 7) {
                [3, 4, 5].forEach(index => {
                    row.cells[index].dataset.uc24ControlledLabel = "true";
                    label(row.cells[index]);
                });
            }
        });

        document.querySelectorAll(
                ".wallet-table td[colspan],.management-table td[colspan]"
                ).forEach(cell => {
            cell.dataset.uc24ControlledLabel = "true";
            label(cell);
        });

        document.querySelectorAll(
                '[data-luxe-header] a[href$="/withdrawal-management"]'
                ).forEach(label);

        document.querySelectorAll("[data-ui-label]").forEach(element => {
            const key = lang.key(element.getAttribute("data-ui-label"));

            if (key && !element.hasAttribute("data-i18n")) {
                element.setAttribute("data-i18n", key);
            }
        });

        document.querySelectorAll(
                "[data-i18n-data-confirm]"
                ).forEach(element => {
            const key = element.getAttribute("data-i18n-data-confirm");
            const value = lang.text(key);

            if (value && value !== key) {
                element.setAttribute("data-confirm", value);
            }
        });

        document.querySelectorAll(
                '[data-bs-toggle="tooltip"][data-i18n-title]'
                ).forEach(element => {
            element.setAttribute(
                    "data-bs-original-title",
                    lang.text(element.getAttribute("data-i18n-title"))
                    );
        });

        if (knownPage) {
            const title = document.title;
            const split = title.match(
                    /^(.*?)\s*([|–-])\s*(Luxe Escape)$/
                    );

            const translated = split
                    ? lang.translate(split[1]) + " " + split[2] + " " + split[3]
                    : lang.translate(title);

            if (document.title !== translated) {
                document.title = translated;
            }
        }

        lang.apply();
    }

    let queued = false;

    function schedule() {
        if (queued)
            return;

        queued = true;

        queueMicrotask(() => {
            queued = false;
            apply();
        });
    }

    window.UC24Translate = (value, args) =>
        api() ? api().translate(value, args) : value;

    window.UC24Labels = {apply};

    document.addEventListener("hotel:languagechange", apply);

    new MutationObserver(schedule).observe(document.body, {
        childList: true,
        subtree: true,
        characterData: true
    });

    apply();
})();