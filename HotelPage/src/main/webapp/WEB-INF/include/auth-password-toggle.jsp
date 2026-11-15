<%@ taglib prefix="msg" uri="/WEB-INF/i18n.tld" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : auth-password-toggle
    Created on : Nov 2, 2026, 8:37:10 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@page pageEncoding="UTF-8"%>
<%-- Updated by Nguyen Hoang Nhat --%>

<style>
    .input-box.auth-password-field > input {
        padding-inline-end: 52px !important;
    }

    input.auth-password-input::-ms-reveal {
        display: none;
    }

    .input-box > button.auth-password-toggle {
        position: absolute;
        inset-inline-end: 4px;
        top: 50%;
        transform: translateY(-50%);
        display: grid;
        place-items: center;
        width: 42px;
        height: 42px;
        padding: 0;
        border: 0;
        border-radius: 9px;
        background: transparent;
        color: inherit;
        cursor: pointer;
    }

    .input-box > button.auth-password-toggle:hover {
        background: rgba(255, 255, 255, .16);
    }

    .input-box > button.auth-password-toggle:focus-visible {
        outline: 2px solid currentColor;
        outline-offset: -3px;
    }

    .auth-password-toggle svg {
        width: 20px;
        height: 20px;
    }

    .auth-password-toggle .eye-slash {
        visibility: hidden;
    }

    .auth-password-toggle[data-visible="true"] .eye-slash {
        visibility: visible;
    }

    .auth-message {
        font-family: inherit;
        font-size: 0.9rem;
        line-height: 1.5;
        text-shadow: none;
    }

    .page-login .remember-me {
        gap: 12px;
        flex-wrap: wrap;
    }
</style>

<script>
    (() => {
        function initializePasswordFields() {
            const labels = {
                show: ${msg:js("Show password", requestScope.uiLanguage)},
                hide: ${msg:js("Hide password", requestScope.uiLanguage)}
            };

            document.querySelectorAll('.input-box input[type="password"], .ua-password input[type="password"]').forEach(input => {
                if (input.classList.contains('auth-password-input')) return;
                const box = input.parentElement;
                const existingButton = box.querySelector('[data-password-toggle]');
                const button = existingButton || document.createElement('button');

                input.classList.add('auth-password-input');
                box.classList.add('auth-password-field');
                button.type = 'button';
                button.setAttribute('aria-controls', input.id);
                if (!existingButton) {
                    button.className = 'auth-password-toggle';
                    button.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">'
                            + '<path d="M2 12s3.6-6 10-6 10 6 10 6-3.6 6-10 6-10-6-10-6Z"/>'
                            + '<circle cx="12" cy="12" r="2.5"/><path class="eye-slash" d="m4 4 16 16"/></svg>';
                    box.append(button);
                }

                function updateToggle() {
                    const visible = input.type === 'text';
                    const key = visible ? 'screen.a60a56c584b3b05b' : 'screen.6aeaa6a53d09dcad';
                    const translated = window.HotelLanguage?.text(key);
                    const label = translated && translated !== key ? translated : labels[visible ? 'hide' : 'show'];
                    button.dataset.visible = String(visible);
                    button.setAttribute('aria-label', label);
                    button.title = label;
                    if (existingButton) button.textContent = label;
                }

                button.addEventListener('click', () => {
                    input.type = input.type === 'password' ? 'text' : 'password';
                    updateToggle();
                });
                document.addEventListener('hotel:languagechange', updateToggle);
                updateToggle();
            });

            // Add confirmation feedback without changing server validation.
            document.querySelectorAll("form").forEach(form => {
                if (form.dataset.authPasswordValidation === "true") {
                    return;
                }

                const newPassword = form.querySelector(
                        'input[name="newPassword"], '
                        + 'input[name="password"][autocomplete="new-password"]'
                        );

                const confirmation = form.querySelector(
                        'input[name="confirmPassword"]'
                        );

                const currentPassword = form.querySelector(
                        'input[name="currentPassword"]'
                        );

                // Login forms do not have a confirmation field.
                if (!newPassword || !confirmation) {
                    return;
                }

                form.dataset.authPasswordValidation = "true";

                function validatePasswords() {
                    const mismatch = confirmation.value !== ""
                            && confirmation.value !== newPassword.value;

                    confirmation.setCustomValidity(
                            mismatch
                            ? ${msg:js("Passwords do not match.", requestScope.uiLanguage)}
                            : ""
                            );

                    if (currentPassword) {
                        const sameAsCurrent = newPassword.value !== ""
                                && newPassword.value
                                === currentPassword.value;

                        newPassword.setCustomValidity(
                                sameAsCurrent
                                ? ${msg:js("New password must differ from the current password.", requestScope.uiLanguage)}
                                : ""
                                );
                    }
                }

                newPassword.addEventListener(
                        "input",
                        validatePasswords
                        );

                confirmation.addEventListener(
                        "input",
                        validatePasswords
                        );

                if (currentPassword) {
                    currentPassword.addEventListener(
                            "input",
                            validatePasswords
                            );
                }
            });
        }

        // Wait until all form fields are available.
        if (document.readyState === "loading") {
            document.addEventListener(
                    "DOMContentLoaded",
                    initializePasswordFields
                    );
        } else {
            initializePasswordFields();
        }
    })();
</script>
