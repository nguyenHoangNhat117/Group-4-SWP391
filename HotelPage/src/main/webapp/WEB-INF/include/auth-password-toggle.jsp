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
    /*
     * Reduce right padding so the existing password reveal control
     * appears closer to the right edge.
     */
    .input-box.auth-password-field input {
        padding-right: 12px !important;
    }

    /* Hide custom eye buttons left by an older implementation. */
    .input-box .auth-password-toggle {
        display: none !important;
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
            // Keep the existing reveal control instead of adding another eye.
            document.querySelectorAll(
                    ".input-box input[type='password']"
                    ).forEach(input => {
                const box = input.closest(".input-box");

                box.classList.add("auth-password-field");

                // Remove custom buttons created by the older shared script.
                box.querySelectorAll(".auth-password-toggle")
                        .forEach(button => button.remove());
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