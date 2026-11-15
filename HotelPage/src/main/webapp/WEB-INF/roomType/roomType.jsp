<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title data-i18n="screen.c797cf9b6771ad9e">${requestScope.i18n['screen.c797cf9b6771ad9e']}</title>

        <link href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css"
              rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/general.css"
              rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-admin-suite.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-ui.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/admin-room-types.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/management.css" rel="stylesheet">
    </head>

    <body class="luxe-admin-suite luxe-admin-types">
        <div class="body-wrapper">
            <%@include file="../include/header.jsp" %>

            <main class="room-types-page">
                <c:url var="createUrl" value="/room-type">
                    <c:param name="view" value="create"/>
                </c:url>

                <div class="room-types-hero">
                    <div class="room-types-hero-copy">
                        <h1><ui:text key="screen.c797cf9b6771ad9e"/></h1>
                        <p><ui:text key="screen.189f9b42d65eb4f1"/></p>
                        <a class="btn room-types-hero-button" href="<c:out value='${createUrl}'/>">
                            <ui:text key="screen.dd593edcde22d368"/>
                        </a>
                    </div>
                </div>

                <c:if test="${param.deleted == '1'}">
                    <p class="alert alert-success" role="status"><ui:text key="room.type.delete.success"/></p>
                </c:if>
                <c:choose>
                    <c:when test="${param.error == 'in-use'}">
                        <p class="alert alert-danger" role="alert"><ui:text key="room.type.delete.inUse"/></p>
                    </c:when>
                    <c:when test="${param.error == 'delete-failed'}">
                        <p class="alert alert-danger" role="alert"><ui:text key="room.type.delete.failed"/></p>
                    </c:when>
                </c:choose>
                <c:choose>
                    <c:when test="${empty roomTypes}">
                        <div class="room-types-panel room-types-empty">
                            <p class="mb-0"><ui:text key="screen.072ba368d69fd3d8"/></p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <table class="room-types-table room-types-panel" role="table">
                            <caption class="visually-hidden">
                                <ui:text key="screen.b1f907e35229d4b8"/>
                            </caption>

                            <thead role="rowgroup">
                                <tr role="row">
                                    <th scope="col" role="columnheader"><ui:text key="screen.741b94aab7248c7b"/></th>
                                    <th scope="col" role="columnheader"><ui:text key="screen.2de469ee4cd7c680"/></th>
                                    <th scope="col" role="columnheader"><ui:text key="screen.2e2a3cd1a00809d3"/></th>
                                    <th scope="col" role="columnheader"><ui:text key="screen.ae65d096550ff4eb"/></th>
                                    <th scope="col" role="columnheader"><ui:text key="screen.ff8059dc6752afdd"/></th>
                                </tr>
                            </thead>

                            <tbody role="rowgroup">
                                <c:forEach var="roomType" items="${roomTypes}">
                                    <c:url var="pictureUrl"
                                           value="/assets/img/${empty roomType.picture ? 'avatar.jpg' : roomType.picture}"/>

                                    <c:url var="editUrl" value="/room-type">
                                        <c:param name="view" value="update"/>
                                        <c:param name="id" value="${roomType.id}"/>
                                        <c:param name="page-index" value="${pageIndex}"/>
                                    </c:url>

                                    <tr role="row">
                                        <th scope="row" role="rowheader" class="room-types-identity">
                                            <div class="room-types-record">
                                                <img class="room-types-thumbnail" src="<c:out value='${pictureUrl}'/>"
                                                     alt="" width="112" height="84" loading="lazy">
                                                <div>
                                                    <span class="room-types-id">#${roomType.id}</span>
                                                    <span class="room-types-name"><c:out value="${roomType.name}"/></span>
                                                    <p class="room-types-description"><c:out value="${roomType.description}"/></p>
                                                </div>
                                            </div>
                                        </th>
                                        <td role="cell" class="room-types-price">
                                            <span class="room-types-mobile-label" aria-hidden="true"><ui:text key="screen.2de469ee4cd7c680"/></span>
                                            <ui:money amount="${roomType.pricePerNight}"/>
                                        </td>
                                        <td role="cell">
                                            <span class="room-types-mobile-label" aria-hidden="true"><ui:text key="screen.2e2a3cd1a00809d3"/></span>
                                            ${roomType.beds}
                                        </td>
                                        <td role="cell">
                                            <span class="room-types-mobile-label" aria-hidden="true"><ui:text key="screen.ae65d096550ff4eb"/></span>
                                            ${roomType.capacity}
                                        </td>
                                        <td role="cell" class="room-types-actions-cell">
                                            <div class="room-types-actions">
                                                <a class="btn room-types-secondary" href="<c:out value='${editUrl}'/>">
                                                    <ui:text key="screen.464c4ffd019e1e96"/>
                                                    <span class="visually-hidden"><c:out value="${roomType.name}"/></span>
                                                </a>

                                                <form method="post" action="${pageContext.request.contextPath}/room-type?action=delete"
                                                      data-confirm="<c:out value="${requestScope.i18n['room.type.delete.prompt']}"/>"
                                                      data-confirm-key="room.type.delete.prompt"
                                                      data-confirm-target="<c:out value='${roomType.name}'/>">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="id" value="${roomType.id}">
                                                    <input type="hidden" name="page-index" value="${pageIndex}">
                                                    <button type="button" class="btn room-types-danger" data-confirm-trigger>
                                                        <ui:text key="screen.e2d0a54968ead24e"/>
                                                        <span class="visually-hidden"><c:out value="${roomType.name}"/></span>
                                                    </button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>

                <c:if test="${totalPages > 1}">
                    <nav class="room-types-pagination" aria-label="${requestScope.i18n['screen.7b3f674b625e7226']}" data-i18n-aria-label="screen.7b3f674b625e7226">
                        <ul class="pagination mb-0">
                            <li class="page-item ${pageIndex <= 1 ? 'disabled' : ''}">
                                <c:choose>
                                    <c:when test="${pageIndex > 1}">
                                        <a class="page-link"
                                           href="${pageContext.request.contextPath}/room-type?page-index=${pageIndex - 1}">
                                            <ui:text key="screen.a57b08a480b822a0"/>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="page-link"><ui:text key="screen.a57b08a480b822a0"/></span>
                                    </c:otherwise>
                                </c:choose>
                            </li>

                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <li class="page-item ${pageIndex == i ? 'active' : ''}">
                                    <c:choose>
                                        <c:when test="${pageIndex == i}">
                                            <span class="page-link" aria-current="page">
                                                ${i}
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <a class="page-link"
                                               href="${pageContext.request.contextPath}/room-type?page-index=${i}">
                                                ${i}
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </li>
                            </c:forEach>

                            <li class="page-item ${pageIndex >= totalPages ? 'disabled' : ''}">
                                <c:choose>
                                    <c:when test="${pageIndex < totalPages}">
                                        <a class="page-link"
                                           href="${pageContext.request.contextPath}/room-type?page-index=${pageIndex + 1}">
                                            <ui:text key="screen.1ff57a29d7c9d11b"/>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="page-link"><ui:text key="screen.1ff57a29d7c9d11b"/></span>
                                    </c:otherwise>
                                </c:choose>
                            </li>
                        </ul>
                    </nav>
                </c:if>
            </main>

            <%@include file="../include/footer.jsp" %>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
        <script src="${pageContext.request.contextPath}/assets/js/management.js" defer></script>
    </body>
</html>
