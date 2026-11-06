<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : report-list-toolbar
    Created on : Oct 26, 2026, 9:53:07 PM
    Author     : Nguyen Hoang Nhat
--%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%-- Shared heading, navigation and date filter for report pages. --%>

<div class="report-heading">
    <div>
        <p class="report-eyebrow"><ui:text key="screen.5f18b34f7d5f3244"/></p>

        <h1>
            <c:choose>
                <c:when test="${reportView == 'bookings'}">
                    <ui:text key="screen.c596d8da1fd843f7"/>
                </c:when>

                <c:when test="${reportView == 'reviews'}"><ui:text key="screen.d515fdd22c1437ff"/></c:when>
                <c:otherwise>
                    <ui:text key="screen.69095f9b883fb24a"/>
                </c:otherwise>
            </c:choose>
        </h1>

        <p class="report-description">
            <c:choose>
                <c:when test="${reportView == 'bookings'}">
                    <ui:text key="screen.12a9797df1bc48aa"/>
                </c:when>

                <c:when test="${reportView == 'reviews'}"><ui:text key="screen.b76ac54f5654f7c8"/></c:when>
                <c:otherwise>
                    <ui:text key="screen.d09c5afbc1a2fe30"/>
                </c:otherwise>
            </c:choose>
        </p>
    </div>

    <a class="report-button"
       href="<c:out value='${dashboardUrl}' />">
        <ui:text key="screen.a7750c2b09f60f0b"/>
    </a>
</div>

<div class="report-tabs" aria-label="${requestScope.i18n['screen.19025f8911e31c54']}" data-i18n-aria-label="screen.19025f8911e31c54">

    <a href="<c:out value='${bookingsUrl}' />"
       aria-current="${reportView == 'bookings' ? 'page' : 'false'}">
        <ui:text key="screen.4e5f81ada70c344e"/>
    </a>

    <a href="<c:out value='${paymentsUrl}' />"
       aria-current="${reportView == 'payments' ? 'page' : 'false'}">
        <ui:text key="screen.632e1c5839411a10"/>
    </a>

    <a href="<c:out value='${reviewsUrl}' />" aria-current="${reportView == 'reviews' ? 'page' : 'false'}">
        <ui:text key="screen.84cb7871b741c32e"/>
    </a>

    <a href="<c:out value='${revenueUrl}' />">
        <ui:text key="screen.c4b7330bd91e0c97"/>
    </a>

</div>

<form class="report-date-filter"
      action="${pageContext.request.contextPath}/reports"
      method="get">

    <input type="hidden"
           name="view"
           value="${reportView}">

    <input type="hidden"
           name="page-index"
           value="1">

    <div>
        <label for="fromDate"><ui:text key="screen.8169693101a4536c"/></label>

        <input type="date"
               id="fromDate"
               name="fromDate"
               min="0001-01-01"
               max="9998-12-31"
               value="${fromDate}">
    </div>

    <div>
        <label for="toDate"><ui:text key="screen.14303aa0c4a08d39"/></label>

        <input type="date"
               id="toDate"
               name="toDate"
               min="0001-01-01"
               max="9998-12-31"
               value="${toDate}">
    </div>

    <div>
        <label for="reportStatus"><ui:text key="screen.920e413c7d411b61"/></label>
        <select id="reportStatus" name="status">
            <option value="" data-i18n="screen.8ee57323a6f24cc7">${requestScope.i18n['screen.8ee57323a6f24cc7']}</option>
            <c:forEach var="option" items="${filter.statuses}">
                <option value="${option}" ${filter.status == option ? 'selected' : ''}>
                    <c:out value="${fn:replace(option, '_', ' ')}" />
                </option>
            </c:forEach>
        </select>
    </div>
    <c:if test="${reportView == 'payments'}">
        <div>
            <label for="reportMethod"><ui:text key="screen.52a0f9b65b278850"/></label>
            <select id="reportMethod" name="method">
                <option value="" data-i18n="screen.e601c19e94dbad07">${requestScope.i18n['screen.e601c19e94dbad07']}</option>
                <c:forEach var="option" items="${filter.methods}">
                    <option value="${option}" ${filter.method == option ? 'selected' : ''}>
                        <c:out value="${fn:replace(option, '_', ' ')}" />
                    </option>
                </c:forEach>
            </select>
        </div>
    </c:if>
    <div>
        <label for="reportBookingId"><ui:text key="screen.a85aca2f532a24ab"/></label>
        <input id="reportBookingId" type="number" min="1" max="2147483647" name="bookingId" value="${filter.bookingId}" placeholder="${requestScope.i18n['screen.39cefa5922bf6c16']}" data-i18n-placeholder="screen.39cefa5922bf6c16">
    </div>
    <div>
        <label for="reportPageSize"><ui:text key="screen.101f2ff3de22441f"/></label>
        <select id="reportPageSize" name="pageSize">
            <c:forEach var="size" items="${[10,25,50]}">
                <option value="${size}" ${filter.pageSize == size ? 'selected' : ''}>${size}</option>
            </c:forEach>
        </select>
    </div>

    <button type="submit"
            class="report-button primary">
        <ui:text key="screen.d75f056b1bbfeec2"/>
    </button>

    <a class="report-button"
       href="<c:out value='${resetUrl}' />">
        <ui:text key="screen.2150d8df37e48957"/>
    </a>

</form>

<c:choose>
    <c:when test="${not empty reportError}">
        <div class="report-note" role="alert">
            <strong>
                <c:out value="${reportError}" />
            </strong>
        </div>
    </c:when>

    <c:otherwise>
        <div class="report-note">

            <c:choose>
                <c:when test="${filterActive}">
                    <ui:text key="screen.8d688de89d8556f2"/>
                    <strong>${fromDate} → ${toDate}</strong><ui:text key="screen.3425d50137b7431c"/>
                </c:when>

                <c:otherwise>
                    <ui:text key="screen.739484e1d8a75d81"/>
                </c:otherwise>
            </c:choose>

            <c:if test="${reportView == 'payments'}">
                <ui:text key="screen.5487f626e6d9a76b"/>
            </c:if>

            <c:if test="${reportView == 'bookings'}">
                <ui:text key="screen.1c1b3255c6514808"/>
            </c:if>

        </div>
    </c:otherwise>
</c:choose>