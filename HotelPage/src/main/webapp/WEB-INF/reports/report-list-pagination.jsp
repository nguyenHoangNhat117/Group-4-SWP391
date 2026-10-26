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
         aria-label="Report pages">

        <c:choose>
            <c:when test="${currentPage > 1}">

                <a href="<c:out value='${firstPageUrl}' />">
                    First
                </a>

                <a href="<c:out value='${previousPageUrl}' />">
                    Previous
                </a>

            </c:when>

            <c:otherwise>

                <span class="disabled" aria-disabled="true">
                    First
                </span>

                <span class="disabled" aria-disabled="true">
                    Previous
                </span>

            </c:otherwise>
        </c:choose>

        <span aria-current="page">
            ${currentPage} / ${totalPages}
        </span>

        <c:choose>
            <c:when test="${currentPage < totalPages}">

                <a href="<c:out value='${nextPageUrl}' />">
                    Next
                </a>

                <a href="<c:out value='${lastPageUrl}' />">
                    Last
                </a>

            </c:when>

            <c:otherwise>

                <span class="disabled" aria-disabled="true">
                    Next
                </span>

                <span class="disabled" aria-disabled="true">
                    Last
                </span>

            </c:otherwise>
        </c:choose>

    </nav>

</c:if>