<%-- 
    Document   : language-switcher
    Created on : Nov 6, 2026, 12:47:26 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/language.css?v=uc24-2">

<div class="hotel-language"
     data-language-endpoint="${pageContext.request.contextPath}/change-language"
     data-language="${requestScope.uiLanguage}"
     data-language-token="${requestScope.uc24Token}">

    <details>
        <summary
            data-i18n-aria-label="language.choose"
            aria-label="${requestScope.i18n['language.choose']}">

            <span aria-hidden="true">🌐</span>

            <span data-language-current>
                ${requestScope.uiLanguage == 'vi' ? 'VI' : 'EN'}
            </span>

            <span aria-hidden="true">▾</span>
        </summary>

        <div class="hotel-language__panel">
            <span class="hotel-language__heading"
                  data-i18n="language.choose">

                <c:out value="${requestScope.i18n['language.choose']}"/>
            </span>

            <button type="button"
                    data-language-option="vi"
                    aria-pressed="${requestScope.uiLanguage == 'vi'}">

                <span lang="vi">Tiếng Việt</span>
            </button>

            <button type="button"
                    data-language-option="en"
                    aria-pressed="${requestScope.uiLanguage == 'en'}">

                <span lang="en">English</span>
            </button>
        </div>
    </details>

    <span class="hotel-language__status"
          data-language-status
          role="status"
          aria-live="polite"></span>

    <noscript>
    Bật JavaScript để đổi ngôn ngữ mà không mất dữ liệu đang nhập.
    / Enable JavaScript to switch language without losing your form.
    </noscript>
</div>

<script defer
        src="${pageContext.request.contextPath}/assets/js/language.js?v=uc24-2">
</script>
<script defer src="${pageContext.request.contextPath}/assets/js/language-ui.js?v=site-1"></script>
