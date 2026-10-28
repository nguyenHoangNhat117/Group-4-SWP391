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
        <p class="report-eyebrow">Luxe Escape / Reports</p>

        <h1>
            <c:choose>
                <c:when test="${reportView == 'bookings'}">
                    Booking report
                </c:when>

                <c:when test="${reportView == 'reviews'}">Review report</c:when>
                <c:otherwise>
                    Payment report
                </c:otherwise>
            </c:choose>
        </h1>

        <p class="report-description">
            <c:choose>
                <c:when test="${reportView == 'bookings'}">
                    Filter reservations by their booking creation date.
                </c:when>

                <c:when test="${reportView == 'reviews'}">Filter feedback by review date.</c:when>
                <c:otherwise>
                    Filter transactions by their payment date.
                </c:otherwise>
            </c:choose>
        </p>
    </div>

    <a class="report-button"
       href="<c:out value='${dashboardUrl}' />">
        Back to dashboard ↗
    </a>
</div>

<div class="report-tabs" aria-label="Report navigation">

    <a href="<c:out value='${bookingsUrl}' />"
       aria-current="${reportView == 'bookings' ? 'page' : 'false'}">
        Bookings
    </a>

    <a href="<c:out value='${paymentsUrl}' />"
       aria-current="${reportView == 'payments' ? 'page' : 'false'}">
        Payments
    </a>

    <a href="<c:out value='${reviewsUrl}' />" aria-current="${reportView == 'reviews' ? 'page' : 'false'}">
        Reviews
    </a>

    <a href="<c:out value='${revenueUrl}' />">
        Revenue
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
        <label for="fromDate">Start date</label>

        <input type="date"
               id="fromDate"
               name="fromDate"
               min="0001-01-01"
               max="9998-12-31"
               value="${fromDate}">
    </div>

    <div>
        <label for="toDate">End date</label>

        <input type="date"
               id="toDate"
               name="toDate"
               min="0001-01-01"
               max="9998-12-31"
               value="${toDate}">
    </div>

    <div>
        <label for="reportStatus">Status</label>
        <select id="reportStatus" name="status">
            <option value="">All statuses</option>
            <c:forEach var="option" items="${filter.statuses}">
                <option value="${option}" ${filter.status == option ? 'selected' : ''}>
                    <c:out value="${fn:replace(option, '_', ' ')}" />
                </option>
            </c:forEach>
        </select>
    </div>
    <c:if test="${reportView == 'payments'}">
        <div>
            <label for="reportMethod">Method</label>
            <select id="reportMethod" name="method">
                <option value="">All methods</option>
                <c:forEach var="option" items="${filter.methods}">
                    <option value="${option}" ${filter.method == option ? 'selected' : ''}>
                        <c:out value="${fn:replace(option, '_', ' ')}" />
                    </option>
                </c:forEach>
            </select>
        </div>
    </c:if>
    <div>
        <label for="reportBookingId">Booking ID</label>
        <input id="reportBookingId" type="number" min="1" max="2147483647" name="bookingId" value="${filter.bookingId}" placeholder="All bookings">
    </div>
    <div>
        <label for="reportPageSize">Rows</label>
        <select id="reportPageSize" name="pageSize">
            <c:forEach var="size" items="${[10,25,50]}">
                <option value="${size}" ${filter.pageSize == size ? 'selected' : ''}>${size}</option>
            </c:forEach>
        </select>
    </div>

    <button type="submit"
            class="report-button primary">
        Apply filter
    </button>

    <a class="report-button"
       href="<c:out value='${resetUrl}' />">
        Show all
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
                    Selected period:
                    <strong>${fromDate} → ${toDate}</strong>.
                    Both dates are included.
                </c:when>

                <c:otherwise>
                    Showing all reporting periods.
                </c:otherwise>
            </c:choose>

            <c:if test="${reportView == 'payments'}">
                Only paid transactions contribute to paid revenue.
                Transactions without a payment date appear only
                in the all-periods view.
            </c:if>

            <c:if test="${reportView == 'bookings'}">
                Booking totals represent booking values,
                not money received.
            </c:if>

        </div>
    </c:otherwise>
</c:choose>