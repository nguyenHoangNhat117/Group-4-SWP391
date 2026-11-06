<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<fmt:setLocale value="vi_VN" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- Calculate display totals from the aggregated report rows. --%>
<c:set var="reportTotalRevenue" value="${0}" />
<c:set var="reportMaximumRevenue" value="${0}" />

<c:forEach var="item" items="${reportData}">
    <c:set var="reportTotalRevenue"
           value="${reportTotalRevenue + item.totalRevenue}" />

    <c:if test="${item.totalRevenue > reportMaximumRevenue}">
        <c:set var="reportMaximumRevenue"
               value="${item.totalRevenue}" />
    </c:if>
</c:forEach>

<%-- HTML meter requires a maximum greater than its minimum. --%>
<c:set var="reportMeterMaximum"
       value="${reportMaximumRevenue > 0 ? reportMaximumRevenue : 1}" />

<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}" class="hotel-reports-page">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title data-i18n="screen.e67911ea05589dbe">${requestScope.i18n['screen.e67911ea05589dbe']}</title>

        <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css">
        <link rel="stylesheet" href="${ctx}/assets/css/general.css">
        <link rel="stylesheet" href="${ctx}/assets/css/luxe-modern.css">
        <link rel="stylesheet" href="${ctx}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet" href="${ctx}/assets/css/luxe-ui.css">
        <link rel="stylesheet" href="${ctx}/assets/css/hotel-reports.css">
    </head>

    <body class="hotel-reports-body ${cookie.animation.value == 'disable' ? 'disable-animation' : ''}">

        <%@ include file="/WEB-INF/include/header.jsp" %>

        <main class="hotel-reports">

            <div class="report-heading">
                <div>
                    <p class="report-eyebrow"><ui:text key="screen.2178403c69e346cf"/></p>
                    <h1><ui:text key="screen.528b29eef603567f"/></h1>

                    <p class="report-description">
                        <ui:text key="screen.6c13e23d64623ef0"/>
                    </p>
                </div>

                <a class="report-button" href="<c:out value='${dashboardUrl}' />">
                    <ui:text key="screen.a7750c2b09f60f0b"/>
                </a>
            </div>

            <div class="report-tabs" aria-label="${requestScope.i18n['screen.19025f8911e31c54']}" data-i18n-aria-label="screen.19025f8911e31c54">
                <a href="${ctx}/reports?view=bookings&amp;page-index=1">
                    <ui:text key="screen.4e5f81ada70c344e"/>
                </a>

                <a href="${ctx}/reports?view=payments&amp;page-index=1">
                    <ui:text key="screen.632e1c5839411a10"/>
                </a>

                <a href="${ctx}/reports?view=reviews"><ui:text key="screen.84cb7871b741c32e"/></a>

                <a href="${ctx}/reports?view=month"
                   aria-current="page"><ui:text key="screen.c4b7330bd91e0c97"/></a>
            </div>

            <div class="report-toolbar">

                <c:if test="${view == 'period'}">
                    <strong class="report-period-label">${fromDate} → ${toDate}</strong>
                </c:if>
                <a class="report-button ${view == 'month' ? 'primary' : ''}"
                   href="${ctx}/reports?view=month&amp;year=${selectedYear}">
                    <ui:text key="screen.4f882faa2c05da9f"/>
                </a>

                <a class="report-button ${view == 'year' ? 'primary' : ''}"
                   href="${ctx}/reports?view=year">
                    <ui:text key="screen.0f2aa9af9d53016f"/>
                </a>

                <c:if test="${view == 'month'}">
                    <form action="${ctx}/reports" method="get">
                        <input type="hidden" name="view" value="month">

                        <label for="reportYear"><ui:text key="screen.cf86732f74bb8446"/></label>

                        <select id="reportYear" name="year">
                            <%-- Preserve the selected year even when it has no paid records. --%>
                            <option value="${selectedYear}" selected>
                                ${selectedYear}
                            </option>

                            <c:forEach var="year" items="${availableYears}">
                                <c:if test="${year != selectedYear}">
                                    <option value="${year}">${year}</option>
                                </c:if>
                            </c:forEach>
                        </select>

                        <button type="submit" class="report-button">
                            <ui:text key="screen.31e392d1c0378bec"/>
                        </button>
                    </form>
                </c:if>
            </div>

            <div class="report-total">
                <span>
                    <c:choose>
                        <c:when test="${view == 'period'}"><ui:text key="screen.dfeca3e02aa8b709"/> ${fromDate} → ${toDate}</c:when>
                        <c:when test="${view == 'month'}">
                            <ui:text key="screen.b17feebf2d53bc81"/> ${selectedYear}
                        </c:when>

                        <c:otherwise>
                            <ui:text key="screen.03e422bd6786afb2"/>
                        </c:otherwise>
                    </c:choose>
                </span>

                <strong>
                    <fmt:formatNumber
                        value="${reportTotalRevenue}"
                        pattern="#,##0.##" />
                    ₫
                </strong>

                <small>
                    <ui:text key="screen.a349105c4ce55fb4"/>
                </small>
            </div>

            <c:if test="${reportTotalRevenue == 0}">
                <div class="report-note">
                    <ui:text key="screen.8a5c6959db35ed40"/>
                </div>
            </c:if>

            <section class="report-panel">

                <div class="report-panel-heading">
                    <h2>
                        <c:choose>
                            <c:when test="${view == 'period'}"><ui:text key="screen.e4e6d0cb054ca22e"/></c:when>
                            <c:when test="${view == 'month'}">
                                <ui:text key="screen.fbd7c48bcbd7114c"/> ${selectedYear}
                            </c:when>

                            <c:otherwise>
                                <ui:text key="screen.7e55f0cd9ba01ed9"/>
                            </c:otherwise>
                        </c:choose>
                    </h2>

                    <span class="report-meta"><ui:text key="screen.dafe6d618c939493"/></span>
                </div>

                <c:choose>
                    <c:when test="${not empty reportData}">
                        <div class="report-table-scroll">
                            <table class="report-table">
                                <thead>
                                    <tr>
                                        <th scope="col">
                                            <c:choose>
                                                <c:when test="${view == 'month'}">
                                                    <ui:text key="screen.310ca503ef36f177"/>
                                                </c:when>

                                                <c:when test="${view == 'period'}"><ui:text key="screen.6e795d4d3cc21c5c"/></c:when>
                                                <c:otherwise><ui:text key="screen.89f6832560dec0b7"/></c:otherwise>
                                            </c:choose>
                                        </th>

                                        <th scope="col" class="numeric">
                                            <ui:text key="screen.6a9dc608ac78821b"/>
                                        </th>

                                        <th scope="col">
                                            <ui:text key="screen.75b6e40fccbd44e6"/>
                                        </th>
                                    </tr>
                                </thead>

                                <tbody>
                                    <c:forEach var="item" items="${reportData}">
                                        <tr>
                                            <td class="report-nowrap">
                                                <strong>
                                                    <c:if test="${view == 'month'}">
                                                        <ui:text key="screen.310ca503ef36f177"/>
                                                    </c:if>

                                                    <c:out value="${item.timeUnit}" />
                                                </strong>
                                            </td>

                                            <td class="numeric">
                                                <fmt:formatNumber
                                                    value="${item.totalRevenue}"
                                                    pattern="#,##0.##" />
                                                ₫
                                            </td>

                                            <td class="report-chart-cell">
                                                <meter class="report-meter"
                                                       min="0"
                                                       max="${reportMeterMaximum}"
                                                       value="${item.totalRevenue}"
                                                       aria-label="Paid revenue for period ${item.timeUnit}">
                                                    <c:out value="${item.totalRevenue}" />
                                                </meter>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="report-empty">
                            <ui:text key="screen.e7f481db98c67e75"/>
                        </div>
                    </c:otherwise>
                </c:choose>

            </section>

            <div class="report-note">
                <ui:text key="screen.6df61cba3c3f8e22"/>
            </div>

        </main>

        <%@ include file="/WEB-INF/include/footer.jsp" %>

        <script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>

    </body>
</html>