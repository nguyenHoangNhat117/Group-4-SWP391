<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${requestScope.i18n['screen.c797cf9b6771ad9e']}</title>

        <link href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css"
              rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/general.css"
              rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-admin-suite.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-ui.css" rel="stylesheet">
        <link href="<c:url value='/assets/css/admin-room-types.css?v=6'/>" rel="stylesheet">
        <link href="<c:url value='/assets/css/management.css?v=2'/>" rel="stylesheet">
    </head>

    <body class="luxe-admin-suite luxe-admin-types">
        <div class="body-wrapper">
            <%@include file="../include/header.jsp" %>

            <main class="room-types-page">
                <c:url var="createUrl" value="/room-type">
                    <c:param name="view" value="create"/>
                    <c:param name="page-index" value="${pageIndex}"/>
                </c:url>

                <div class="room-types-hero">
                    <div class="room-types-hero-copy">
                        <h1><ui:text key="screen.c797cf9b6771ad9e"/></h1>
                        <p><ui:text key="screen.189f9b42d65eb4f1"/></p>
                        <div class="d-flex flex-wrap gap-2">
                            <a class="btn room-types-hero-button" href="<c:out value='${createUrl}'/>">
                                <ui:text key="screen.dd593edcde22d368"/>
                            </a>
                            <a class="btn btn-outline-light" href="<c:url value='/amenity'/>">
                                <ui:text key="room.amenity.manage"/>
                            </a>
                        </div>
                    </div>
                </div>

                <c:if test="${param.deleted == '1'}">
                    <p class="alert alert-success" role="status"><ui:text key="room.type.delete.success"/></p>
                </c:if>
                <c:if test="${param.saved == '1' or param['sale-updated'] == '1'}">
                    <p class="alert alert-success" role="status">
                        <ui:text key="${param.saved == '1' ? 'room.type.saved' : 'room.admin.sale.updated'}"/>
                    </p>
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
                            <colgroup>
                                <col class="room-types-col-identity">
                                <col class="room-types-col-price">
                                <col class="room-types-col-beds">
                                <col class="room-types-col-capacity">
                                <col class="room-types-col-actions">
                            </colgroup>

                            <thead role="rowgroup">
                                <tr role="row">
                                    <th scope="col" role="columnheader"><ui:text key="screen.741b94aab7248c7b"/></th>
                                    <th scope="col" role="columnheader"><ui:text key="screen.2de469ee4cd7c680"/></th>
                                    <th scope="col" role="columnheader" class="room-types-beds"><ui:text key="screen.2e2a3cd1a00809d3"/></th>
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
                                    <c:set var="saleAction" value="${roomType.active ? 'room.admin.sale.action.stop' : 'room.admin.sale.action.resume'}"/>
                                    <c:set var="salePrompt" value="${roomType.active ? 'room.admin.sale.confirm.stop' : 'room.admin.sale.confirm.resume'}"/>

                                    <tr role="row">
                                        <th scope="row" role="rowheader" class="room-types-identity">
                                            <div class="room-types-record">
                                                <img class="room-types-thumbnail" src="<c:out value='${pictureUrl}'/>"
                                                     alt="" width="112" height="84" loading="lazy">
                                                <div>
                                                    <div class="room-types-record-title">
                                                        <span class="room-types-name"><c:out value="${roomType.name}"/></span>
                                                        <span class="room-types-id">#${roomType.id}</span>
                                                        <span class="room-types-badges">
                                                            <span class="catalog-status ${roomType.active ? 'is-active' : ''}">
                                                                <ui:text key="${roomType.active ? 'room.admin.sale.active' : 'room.admin.sale.inactive'}"/>
                                                            </span>
                                                            <span class="catalog-status room-types-usage">
                                                                <span>${roomType.roomCount} <ui:text key="room.selection.roomUnit"/> &middot;
                                                                    ${roomType.activeBookingCount} <ui:text key="room.admin.sale.activeBookingUnit"/></span>
                                                            </span>
                                                        </span>
                                                    </div>
                                                    <p class="room-types-description"><c:out value="${roomType.description}"/></p>
                                                </div>
                                            </div>
                                        </th>
                                        <td role="cell" class="room-types-price">
                                            <span class="room-types-mobile-label" aria-hidden="true"><ui:text key="screen.2de469ee4cd7c680"/></span>
                                            <ui:money amount="${roomType.pricePerNight}"/>
                                        </td>
                                        <td role="cell" class="room-types-beds">
                                            <span class="room-types-mobile-label" aria-hidden="true"><ui:text key="screen.2e2a3cd1a00809d3"/></span>
                                            ${roomType.beds}
                                        </td>
                                        <td role="cell" class="room-types-capacity">
                                            <span class="room-types-mobile-label" aria-hidden="true"><ui:text key="screen.ae65d096550ff4eb"/></span>
                                            <div>${roomType.capacity} <ui:text key="room.selection.adultUnit"/></div>
                                            <div>${roomType.maxChildren} <ui:text key="room.selection.childUnit"/></div>
                                        </td>
                                        <td role="cell" class="room-types-actions-cell">
                                            <div class="room-types-actions">
                                                <a class="btn room-types-secondary" href="<c:out value='${editUrl}'/>">
                                                    <ui:text key="room.type.action.edit"/>
                                                    <span class="visually-hidden"><c:out value="${roomType.name}"/></span>
                                                </a>
                                                <form method="post" action="<c:url value='/room-type'/>"
                                                      data-confirm="<c:out value='${requestScope.i18n[salePrompt]}'/>"
                                                      data-confirm-target="<c:out value='${roomType.name}'/>"
                                                      data-confirm-title="<c:out value='${requestScope.i18n[saleAction]}'/>"
                                                      data-confirm-label="<c:out value='${requestScope.i18n[saleAction]}'/>">
                                                    <input type="hidden" name="action" value="sale">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="id" value="${roomType.id}">
                                                    <input type="hidden" name="active" value="${not roomType.active}">
                                                    <input type="hidden" name="page-index" value="${pageIndex}">
                                                    <button class="btn room-types-secondary" type="button" data-confirm-trigger>
                                                        <ui:text key="${saleAction}"/>
                                                        <span class="visually-hidden"><c:out value="${roomType.name}"/></span>
                                                    </button>
                                                </form>

                                                <c:if test="${roomType.roomCount == 0 and roomType.bookingCount == 0}">
                                                    <form method="post" action="${pageContext.request.contextPath}/room-type?action=delete"
                                                          data-confirm="<c:out value="${requestScope.i18n['room.type.delete.prompt']}"/>"
                                                          data-confirm-target="<c:out value='${roomType.name}'/>">
                                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                        <input type="hidden" name="id" value="${roomType.id}">
                                                        <input type="hidden" name="page-index" value="${pageIndex}">
                                                        <button type="button" class="btn room-types-danger" data-confirm-trigger>
                                                            <ui:text key="screen.e2d0a54968ead24e"/>
                                                            <span class="visually-hidden"><c:out value="${roomType.name}"/></span>
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>

                <c:if test="${totalPages > 1}">
                    <nav class="room-types-pagination" aria-label="${requestScope.i18n['screen.7b3f674b625e7226']}">
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
        <script src="<c:url value='/assets/js/management.js?v=2'/>" defer></script>
    </body>
</html>
