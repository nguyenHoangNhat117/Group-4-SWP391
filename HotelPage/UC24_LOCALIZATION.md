# UC24 — English / Vietnamese interface

This change extends the existing language selector to screen headings, form labels,
buttons, placeholders, tables, pagination, displayed statuses, account roles,
known validation/flash messages, payment screens and reporting labels.

The existing header layout is preserved. Room names, room type names, guest names,
contact data, review comments and form action/name/value attributes are preserved.
Database status values, currencies and payment/booking operations are unchanged.

## Files and integration

- `src/main/resources/i18n/messages_en.properties` and `messages_vi.properties`
  contain the existing shared labels and the added `screen.*` labels.
- `util.MessageTranslations` resolves known English/Vietnamese messages to keys,
  and provides HTML/JavaScript-safe output helpers. Unknown text is preserved.
- `WEB-INF/tags/message.tag` and `WEB-INF/i18n.tld` support dynamic messages and
  native select options. Existing `text.tag` renders static labels.
- `assets/js/language-ui.js` binds reporting labels created in JavaScript,
  confirmation messages and Bootstrap tooltip text to the existing language API.
- `language-switcher.jsp` loads the additional client helper. No new dependency
  or database migration is required.

Build and redeploy the WAR after copying the complete change. Translation files
are loaded at application startup; hot-editing properties alone is insufficient.
Use a hard refresh after deployment to refresh JavaScript.

For new UI messages, add matching keys to both properties files and render them
through `ui:text`, `ui:message` or `msg:text`. Avoid translating customer text or
changing form values to translated labels.

## Verification

- Java sources compiled with `--release 11` against Jakarta Servlet 6 libraries.
- JavaScript syntax checks passed for the changed scripts.
- 48 JSP document pages compiled and rendered on isolated Tomcat 10.1 using
  fixture data. This includes the updated tag files and EL functions.
- HTTP checks passed for EN/VI login labels and validation messages, account
  management labels/native role options, and offline-booking form labels.
- Missing CSRF and unsupported-language requests were rejected without replacing
  the preference.
- Source checks verified protected data expressions, unchanged form attributes
  and a byte-identical shared header.

These are interface/rendering checks. Financial workflows and production database
integration were not executed by this change.
