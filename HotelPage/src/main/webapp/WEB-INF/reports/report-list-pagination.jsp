<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : report-list-pagination
    Created on : Oct 26, 2026, 9:56:02 PM
    Author     : Nguyen Hoang Nhat
--%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- Pagination URLs are prepared by ReportsServlet and retain the date filter. --%>

<c:if test="${empty reportError and totalPages > 1}">

    <nav class="report-pagination"
         aria-label="${requestScope.i18n['screen.797d81d2f4c89f38']}" data-i18n-aria-label="screen.797d81d2f4c89f38">

        <c:choose>
            <c:when test="${currentPage > 1}">

                <a href="<c:out value='${firstPageUrl}' />">
                    <ui:text key="screen.a151ceb1711aad52"/>
                </a>

                <a href="<c:out value='${previousPageUrl}' />">
                    <ui:text key="screen.a57b08a480b822a0"/>
                </a>

            </c:when>

            <c:otherwise>

                <span class="disabled" aria-disabled="true">
                    <ui:text key="screen.a151ceb1711aad52"/>
                </span>

                <span class="disabled" aria-disabled="true">
                    <ui:text key="screen.a57b08a480b822a0"/>
                </span>

            </c:otherwise>
        </c:choose>

        <span aria-current="page">
            ${currentPage} / ${totalPages}
        </span>

        <c:choose>
            <c:when test="${currentPage < totalPages}">

                <a href="<c:out value='${nextPageUrl}' />">
                    <ui:text key="screen.1ff57a29d7c9d11b"/>
                </a>

                <a href="<c:out value='${lastPageUrl}' />">
                    <ui:text key="screen.eb970eb0951c6cde"/>
                </a>

            </c:when>

            <c:otherwise>

                <span class="disabled" aria-disabled="true">
                    <ui:text key="screen.1ff57a29d7c9d11b"/>
                </span>

                <span class="disabled" aria-disabled="true">
                    <ui:text key="screen.eb970eb0951c6cde"/>
                </span>

            </c:otherwise>
        </c:choose>

    </nav>

</c:if>