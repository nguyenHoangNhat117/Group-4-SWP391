<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : offline
    Created on : Nov 5, 2026, 9:48:13 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}" class="uc09-page">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title data-i18n="screen.5a779d440195be98">${requestScope.i18n['screen.5a779d440195be98']}</title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/general.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/offline-booking.css?v=stable-1">
    </head>

    <body class="offline-booking-page">
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp"%>

            <main class="ob-page">
                <div class="ob-heading">
                    <h1><ui:text key="screen.02c223c42181cf2b"/></h1>

                    <a class="ob-back"
                       href="${pageContext.request.contextPath}/booking-management">
                        <ui:text key="screen.c94d50d55899f838"/>
                    </a>
                </div>

                <ol class="ob-steps" aria-label="${requestScope.i18n['screen.9c39e2bcadf8ddf1']}" data-i18n-aria-label="screen.9c39e2bcadf8ddf1">
                    <li class="${empty quote ? 'is-current' : ''}">
                        <span>01</span> <ui:text key="screen.08fd1de4b04df4a2"/>
                    </li>
                    <li>
                        <span>02</span> <ui:text key="screen.bf3763383aaf4306"/>
                    </li>
                    <li class="${not empty quote ? 'is-current' : ''}">
                        <span>03</span> <ui:text key="screen.eebdd24a77d9ad32"/>
                    </li>
                </ol>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger mb-4" role="alert">
                        <ui:message text="${error}"/>
                    </div>
                </c:if>

                <c:choose>
                    <%-- Màn xác nhận --%>
                    <c:when test="${not empty quote}">
                        <div class="ob-grid">
                            <section class="ob-panel">
                                <div class="ob-panel-top">
                                    <h2><ui:text key="screen.126850cb3c7e0489"/></h2>
                                    <span class="ob-number">03</span>
                                </div>

                                <div class="ob-review-room">
                                    <div class="ob-room-mark" aria-hidden="true">
                                        <svg viewBox="0 0 32 32">
                                        <path d="M5 24V10m22 14V10M5 20h22M5 14h22v6H5zm4 0V9h6v5m2 0V9h6v5"/>
                                        </svg>
                                    </div>

                                    <div>
                                        <strong>
                                            <ui:text key="screen.911ea89c43d9dbb8"/> ${quote.draft.roomNumber}
                                        </strong>
                                        <span>
                                            ${quote.draft.guests} <ui:text key="screen.45f69c6d7bebdee3"/> ${quote.nights} <ui:text key="screen.de5c9050dc18c72f"/>
                                        </span>
                                    </div>
                                </div>

                                <dl class="ob-review-details">
                                    <div class="ob-full">
                                        <dt><ui:text key="screen.bf3763383aaf4306"/></dt>
                                        <dd>
                                            <c:out value="${quote.customerName}"/>
                                        </dd>
                                    </div>

                                    <div>
                                        <dt><ui:text key="screen.5d7b2d63203faae8"/></dt>
                                        <dd>${quote.draft.checkIn}</dd>
                                    </div>

                                    <div>
                                        <dt><ui:text key="screen.1a51b94f713b1ee9"/></dt>
                                        <dd>${quote.draft.checkOut}</dd>
                                    </div>

                                    <div>
                                        <dt><ui:text key="screen.eba034a48e45899e"/></dt>
                                        <dd>
                                            <fmt:formatNumber value="${quote.nightly}"/>
                                            VND
                                        </dd>
                                    </div>

                                    <div>
                                        <dt><ui:text key="screen.8c6a0024dad9f069"/></dt>
                                        <dd>
                                            <c:out value="${empty quote.draft.promotionCode
                                                            ? 'Không áp dụng'
                                                            : quote.draft.promotionCode}"/>
                                        </dd>
                                    </div>

                                    <div class="ob-full">
                                        <dt><ui:text key="screen.8d40b90ad50b8351"/></dt>
                                        <dd>
                                            <c:out value="${empty quote.draft.specialRequest
                                                            ? 'Không có'
                                                            : quote.draft.specialRequest}"/>
                                        </dd>
                                    </div>
                                </dl>

                                <div class="ob-review-note">
                                    <a class="ob-back"
                                       href="${pageContext.request.contextPath}/offline-booking?edit=${quote.draft.requestKey}">
                                        <ui:text key="screen.5526444a562e0046"/>
                                    </a>
                                </div>
                            </section>

                            <aside class="ob-panel ob-summary">
                                <div class="ob-summary-head">
                                    <small>LUXE ESCAPE</small>
                                    <h2><ui:text key="screen.f439e95643ea7efa"/></h2>
                                </div>

                                <div class="ob-summary-body">
                                    <dl>
                                        <div class="ob-summary-row">
                                            <dt><ui:text key="screen.e2c9b215945296ae"/></dt>
                                            <dd>
                                                <fmt:formatNumber value="${quote.gross}"/>
                                                VND
                                            </dd>
                                        </div>

                                        <div class="ob-summary-row">
                                            <dt><ui:text key="screen.4c2ca6bb46ef5d52"/></dt>
                                            <dd>
                                                − <fmt:formatNumber value="${quote.gross - quote.payable}"/>
                                                VND
                                            </dd>
                                        </div>

                                        <div class="ob-summary-row">
                                            <dt><ui:text key="screen.3622756f73037b11"/></dt>
                                            <dd>${holdMinutes} <ui:text key="screen.90e63d85fa1ab2c8"/></dd>
                                        </div>

                                        <div class="ob-summary-row">
                                            <dt><ui:text key="screen.157ed3242bd5a322"/></dt>
                                            <dd>${depositPercent}%</dd>
                                        </div>
                                    </dl>

                                    <div class="ob-total">
                                        <span><ui:text key="screen.8019400caf0913fa"/></span>
                                        <strong class="ob-price">
                                            <fmt:formatNumber value="${quote.payable}"/>
                                            VND
                                        </strong>
                                    </div>

                                    <form action="${pageContext.request.contextPath}/offline-booking"
                                          method="post">

                                        <input type="hidden"
                                               name="csrfToken"
                                               value="${sessionScope.csrfToken}">

                                        <input type="hidden"
                                               name="action"
                                               value="create">

                                        <input type="hidden"
                                               name="requestKey"
                                               value="${quote.draft.requestKey}">

                                        <button class="ob-button ob-primary"
                                                type="submit">
                                            <ui:text key="screen.58982c7a6d7fdc02"/>
                                            <span aria-hidden="true">→</span>
                                        </button>
                                    </form>

                                    <p class="ob-note">
                                        <ui:text key="screen.1a96043bb056ae9a"/>
                                    </p>
                                </div>
                            </aside>
                        </div>
                    </c:when>

                    <%-- Màn nhập thông tin --%>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/offline-booking"
                              method="post"
                              id="offlineForm">

                            <input type="hidden"
                                   name="csrfToken"
                                   value="${sessionScope.csrfToken}">

                            <div class="ob-grid">
                                <div class="ob-stack">

                                    <section class="ob-panel">
                                        <div class="ob-panel-top">
                                            <h2><ui:text key="screen.8a25de2296303d74"/></h2>
                                            <span class="ob-number">01</span>
                                        </div>

                                        <div class="ob-fields">
                                            <div class="ob-field">
                                                <label for="checkIn">
                                                    <ui:text key="screen.5d7b2d63203faae8"/>
                                                </label>

                                                <input class="ob-input"
                                                       type="date"
                                                       id="checkIn"
                                                       name="checkIn"
                                                       required
                                                       min="${today}"
                                                       value="${fn:escapeXml(form.checkIn)}">
                                            </div>

                                            <div class="ob-field">
                                                <label for="checkOut">
                                                    <ui:text key="screen.1a51b94f713b1ee9"/>
                                                </label>

                                                <input class="ob-input"
                                                       type="date"
                                                       id="checkOut"
                                                       name="checkOut"
                                                       required
                                                       min="${today}"
                                                       value="${fn:escapeXml(form.checkOut)}">
                                            </div>

                                            <div class="ob-search ob-full">
                                                <div class="ob-field">
                                                    <label for="guests">
                                                        <ui:text key="screen.3c8e0fde6fcc71e0"/>
                                                    </label>

                                                    <input class="ob-input"
                                                           type="number"
                                                           id="guests"
                                                           name="guests"
                                                           min="1"
                                                           max="1000"
                                                           required
                                                           value="${fn:escapeXml(form.guests)}">
                                                </div>

                                                <button class="ob-button ob-secondary"
                                                        type="submit"
                                                        name="action"
                                                        value="search"
                                                        formnovalidate>
                                                    <ui:text key="screen.40081e387cab1d5c"/>
                                                    <span aria-hidden="true">→</span>
                                                </button>
                                            </div>
                                        </div>

                                        <c:if test="${searched and empty rooms}">
                                            <p class="ob-empty" role="status">
                                                <ui:text key="screen.1066e476402ece31"/>
                                            </p>
                                        </c:if>

                                        <div class="ob-field ob-room-field">
                                            <label for="roomNumber"><ui:text key="screen.7c2d837276e934c7"/></label>

                                            <select class="ob-input"
                                                    id="roomNumber"
                                                    name="roomNumber"
                                                    required>

                                                <option value="" data-i18n="screen.2ccbf4a8c0509a35">
                                                    ${requestScope.i18n['screen.2ccbf4a8c0509a35']}
                                                </option>

                                                <c:forEach items="${rooms}" var="room">
                                                    <option value="${room.roomNumber}"
                                                            data-price="${room.roomType.pricePerNight}"
                                                            data-label="${fn:escapeXml(room.roomType.name)}"
                                                            ${form.roomNumber == room.roomNumber
                                                              ? 'selected' : ''}>

                                                        ${requestScope.i18n['screen.911ea89c43d9dbb8']} ${room.roomNumber}
                                                        · <c:out value="${room.roomType.name}"/>
                                                        · ${room.roomType.capacity} ${requestScope.i18n['screen.45f69c6d7bebdee3']} <fmt:formatNumber value="${room.roomType.pricePerNight}"/>
                                                        ${requestScope.i18n['screen.0d48d80c52b5c1ef']}
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </section>

                                    <section class="ob-panel">
                                        <div class="ob-panel-top">
                                            <h2><ui:text key="screen.bf3763383aaf4306"/></h2>
                                            <span class="ob-number">02</span>
                                        </div>

                                        <div class="ob-mode"
                                             role="group"
                                             aria-label="${requestScope.i18n['screen.4ac86ab4f546c113']}" data-i18n-aria-label="screen.4ac86ab4f546c113">

                                            <label>
                                                <input type="radio"
                                                       name="customerMode"
                                                       value="existing"
                                                       ${form.customerMode != 'new'
                                                         ? 'checked' : ''}>
                                                <span><ui:text key="screen.b941d2696ce7958d"/></span>
                                            </label>

                                            <label>
                                                <input type="radio"
                                                       name="customerMode"
                                                       value="new"
                                                       ${form.customerMode == 'new'
                                                         ? 'checked' : ''}>
                                                <span><ui:text key="screen.855efa16ef338b26"/></span>
                                            </label>
                                        </div>

                                        <div data-customer-pane="existing">
                                            <div class="ob-search">
                                                <div class="ob-field">
                                                    <label for="q">
                                                        <ui:text key="screen.80ac476328a1a6ca"/>
                                                    </label>

                                                    <input class="ob-input"
                                                           type="search"
                                                           id="q"
                                                           name="q"
                                                           maxlength="100"
                                                           value="${fn:escapeXml(form.q)}"
                                                           placeholder="${requestScope.i18n['screen.bd9ab9176cf69767']}" data-i18n-placeholder="screen.bd9ab9176cf69767">
                                                </div>

                                                <button class="ob-button ob-secondary"
                                                        type="submit"
                                                        name="action"
                                                        value="search"
                                                        formnovalidate>
                                                    <ui:text key="screen.57805bd2d7e63e4d"/>
                                                </button>
                                            </div>

                                            <c:if test="${not empty form.q and empty customers}">
                                                <p class="ob-empty" role="status">
                                                    <ui:text key="screen.892733be5dbea18d"/>
                                                </p>
                                            </c:if>

                                            <div class="ob-field" style="margin-top:18px">
                                                <label for="customerId">
                                                    <ui:text key="screen.71bc48dd14a2be24"/>
                                                </label>

                                                <select class="ob-input"
                                                        name="customerId"
                                                        id="customerId">

                                                    <option value="" data-i18n="screen.255c4597f3d79f5a">
                                                        ${requestScope.i18n['screen.255c4597f3d79f5a']}
                                                    </option>

                                                    <c:forEach items="${customers}"
                                                               var="customer">
                                                        <option value="${customer.customerID}"
                                                                ${form.customerId == customer.customerID
                                                                  ? 'selected' : ''}
                                                                  >

                                                                    <c:out value="${customer.lastName}
                                                                           ${customer.firstName}
                                                                           · ${customer.email}
                                                                           · ${customer.phone}
                                                                           "/>
                                                                </option>
                                                        </c:forEach>
                                                    </select>
                                                </div>
                                            </div>

                                            <div data-customer-pane="new">
                                                <div class="ob-fields">
                                                    <div class="ob-field">
                                                        <label for="lastName">
                                                            <ui:text key="screen.cecbf3ef59fef946"/>
                                                        </label>

                                                        <input class="ob-input"
                                                               id="lastName"
                                                               name="lastName"
                                                               maxlength="50"
                                                               autocomplete="family-name"
                                                               value="${fn:escapeXml(form.lastName)}"
                                                               placeholder="Nguyễn Hoàng">
                                                    </div>

                                                    <div class="ob-field">
                                                        <label for="firstName"><ui:text key="screen.96ce6e6193a04726"/></label>

                                                        <input class="ob-input"
                                                               id="firstName"
                                                               name="firstName"
                                                               maxlength="50"
                                                               autocomplete="given-name"
                                                               value="${fn:escapeXml(form.firstName)}"
                                                               placeholder="Nhật">
                                                    </div>

                                                    <div class="ob-field">
                                                        <label for="email"><ui:text key="offline.emailOptional"/></label>

                                                        <input class="ob-input"
                                                               type="email"
                                                               id="email"
                                                               name="email"
                                                               maxlength="100"
                                                               autocomplete="email"
                                                               value="${fn:escapeXml(form.email)}"
                                                               placeholder="khach@example.com">
                                                    </div>

                                                    <div class="ob-field">
                                                        <label for="phone"><ui:text key="screen.63dceb8800b2dd4a"/></label>

                                                        <input class="ob-input"
                                                               type="tel"
                                                               id="phone"
                                                               name="phone"
                                                               maxlength="20"
                                                               inputmode="tel"
                                                               autocomplete="tel"
                                                               value="${fn:escapeXml(form.phone)}"
                                                               placeholder="+84901234567"><p class="ob-note"><ui:text key="offline.phoneHint"/></p>
                                                    </div>
                                                </div>
                                            </div>
                                        </section>

                                        <section class="ob-panel">
                                            <div class="ob-panel-top">
                                                <h2><ui:text key="screen.a42f24143514fb13"/></h2>
                                                <span class="ob-number">03</span>
                                            </div>

                                            <div class="ob-fields">
                                                <div class="ob-field ob-full">
                                                    <label for="promotionCode">
                                                        <ui:text key="screen.fd947a841a798fb7"/>
                                                    </label>

                                                    <input class="ob-input"
                                                           name="promotionCode"
                                                           id="promotionCode"
                                                           maxlength="50"
                                                           value="${fn:escapeXml(form.promotionCode)}"
                                                           placeholder="${requestScope.i18n['screen.16be5a510aa9cbab']}" data-i18n-placeholder="screen.16be5a510aa9cbab">
                                                </div>

                                                <div class="ob-field ob-full">
                                                    <label for="specialRequest">
                                                        <ui:text key="screen.8d40b90ad50b8351"/>
                                                    </label>

                                                    <textarea class="ob-input"
                                                              name="specialRequest"
                                                              id="specialRequest"
                                                              maxlength="500"
                                                              rows="3"
                                                              placeholder="${requestScope.i18n['screen.8ec7552bf0a4f249']}" data-i18n-placeholder="screen.8ec7552bf0a4f249"><c:out value="${form.specialRequest}"/></textarea>
                                                </div>
                                            </div>
                                        </section>
                                    </div>

                                    <aside class="ob-panel ob-summary"
                                           aria-label="${requestScope.i18n['screen.6b00218f389a7460']}" data-i18n-aria-label="screen.6b00218f389a7460">

                                        <div class="ob-summary-head">
                                            <small>LUXE ESCAPE</small>
                                            <h2 id="obRoom"><ui:text key="screen.7c2d837276e934c7"/></h2>
                                            <p id="obType" class="ob-note">
                                                <ui:text key="screen.e3c166be8ed36aba"/>
                                            </p>
                                        </div>

                                        <div class="ob-summary-body">
                                            <dl>
                                                <div class="ob-summary-row">
                                                    <dt><ui:text key="screen.08fd1de4b04df4a2"/></dt>
                                                    <dd id="obDates">—</dd>
                                                </div>

                                                <div class="ob-summary-row">
                                                    <dt><ui:text key="screen.6a14a27a176f3d9a"/></dt>
                                                    <dd id="obNights">—</dd>
                                                </div>

                                                <div class="ob-summary-row">
                                                    <dt><ui:text key="screen.3c8e0fde6fcc71e0"/></dt>
                                                    <dd id="obGuests">—</dd>
                                                </div>

                                                <div class="ob-summary-row">
                                                    <dt><ui:text key="screen.3622756f73037b11"/></dt>
                                                    <dd>${holdMinutes} <ui:text key="screen.90e63d85fa1ab2c8"/></dd>
                                                </div>

                                                <div class="ob-summary-row">
                                                    <dt><ui:text key="screen.157ed3242bd5a322"/></dt>
                                                    <dd>${depositPercent}%</dd>
                                                </div>
                                            </dl>

                                            <div class="ob-total">
                                                <span><ui:text key="screen.544ddc571296b6b9"/></span>
                                                <strong class="ob-price"
                                                        id="obAmount"
                                                        aria-live="polite">
                                                    —
                                                </strong>
                                            </div>

                                            <button type="submit"
                                                    name="action"
                                                    value="preview"
                                                    class="ob-button ob-primary"
                                                    ${empty rooms ? 'disabled' : ''}>
                                                <ui:text key="screen.6aff1ed4ee82bb55"/>
                                                <span aria-hidden="true">→</span>
                                            </button>

                                            <p class="ob-note">
                                                <ui:text key="screen.a6cfe7eba9782ff9"/>
                                            </p>

                                            <a class="ob-cancel"
                                               href="${pageContext.request.contextPath}/booking-management">
                                                <ui:text key="screen.a9197554370f5880"/>
                                            </a>
                                        </div>
                                    </aside>
                                </div>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </main>

                <%@include file="/WEB-INF/include/footer.jsp"%>
            </div>

            <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
            <script src="${pageContext.request.contextPath}/assets/js/offline-booking.js?v=1" defer></script>
        </body>
    </html>