<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title data-i18n="screen.c797cf9b6771ad9e">${requestScope.i18n['screen.c797cf9b6771ad9e']}</title>

        <link href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css"
              rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/general.css"
              rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/admin-list.css"
              rel="stylesheet">
    
    <link href="${pageContext.request.contextPath}/assets/css/luxe-admin-suite.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
</head>

    <body class="luxe-admin-suite luxe-admin-types">
        <div class="body-wrapper">
            <%@include file="../include/header.jsp" %>

            <main class="admin-list">
        <div class="luxe-admin-banner" aria-label="${requestScope.i18n['screen.15bb8d1ae99d691e']}" data-i18n-aria-label="screen.15bb8d1ae99d691e">
          <span class="luxe-admin-banner-eyebrow">LUXE ESCAPE <span aria-hidden="true">/</span> <ui:text key="screen.4b39ac62e788aec4"/></span>
          <span class="luxe-admin-banner-title"><ui:text key="screen.15bb8d1ae99d691e"/></span>
          <span class="luxe-admin-banner-art" aria-hidden="true"></span>
        </div>

                <div class="list-heading">
                    <div>
                        <h1><ui:text key="screen.c797cf9b6771ad9e"/></h1>
                        <p class="text-muted mb-0">
                            <ui:text key="screen.189f9b42d65eb4f1"/>
                        </p>
                    </div>

                    <a class="btn btn-primary"
                       href="${pageContext.request.contextPath}/room-type?view=create">
                        <ui:text key="screen.dd593edcde22d368"/>
                    </a>
                </div>

                <div class="table-panel">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle">
                            <caption class="visually-hidden">
                                <ui:text key="screen.b1f907e35229d4b8"/>
                            </caption>

                            <thead>
                                <tr>
                                    <th scope="col"><ui:text key="screen.3843971dcfdee508"/></th>
                                    <th scope="col"><ui:text key="screen.1aa4cb0bcca76e92"/></th>
                                    <th scope="col"><ui:text key="screen.dcd1d5223f73b3a9"/></th>
                                    <th scope="col"><ui:text key="screen.526e0087cc3f254d"/></th>
                                    <th scope="col"><ui:text key="screen.2de469ee4cd7c680"/></th>
                                    <th scope="col"><ui:text key="screen.2e2a3cd1a00809d3"/></th>
                                    <th scope="col"><ui:text key="screen.ae65d096550ff4eb"/></th>
                                    <th scope="col"><ui:text key="screen.ff8059dc6752afdd"/></th>
                                </tr>
                            </thead>

                            <tbody>
                                <c:forEach var="roomType" items="${roomTypes}">
                                    <c:url var="pictureUrl"
                                           value="/assets/img/${empty roomType.picture ? 'avatar.jpg' : roomType.picture}"/>

                                    <c:url var="deleteUrl" value="/room-type">
                                        <c:param name="view" value="delete"/>
                                        <c:param name="id" value="${roomType.id}"/>
                                    </c:url>

                                    <tr>
                                        <td class="fw-semibold">
                                            #${roomType.id}
                                        </td>

                                        <td>
                                            <img class="room-thumbnail"
                                                 src="<c:out value='${pictureUrl}'/>"
                                                 alt="${requestScope.i18n['screen.800a7c505ba7c476']}"
                                                 loading="lazy" data-i18n-alt="screen.800a7c505ba7c476">
                                        </td>

                                        <td class="fw-semibold">
                                            <c:out value="${roomType.name}"/>
                                        </td>

                                        <td class="description">
                                            <c:out value="${roomType.description}"/>
                                        </td>

                                        <td>${roomType.pricePerNight} USD</td>
                                        <td>${roomType.beds}</td>
                                        <td>${roomType.capacity}</td>

                                        <td>
                                            <div class="row-actions">
                                                <a class="btn btn-outline-primary btn-sm"
                                                   href="${pageContext.request.contextPath}/room-type?view=update&id=${roomType.id}"
                                                   aria-label="Edit room type ${roomType.id}">
                                                    <ui:text key="screen.464c4ffd019e1e96"/>
                                                </a>

                                                <a class="btn btn-outline-danger btn-sm"
                                                   href="<c:out value='${deleteUrl}'/>"
                                                   aria-label="Delete room type ${roomType.id}">
                                                    <ui:text key="screen.e2d0a54968ead24e"/>
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>

                                <c:if test="${empty roomTypes}">
                                    <tr>
                                        <td colspan="8" class="empty-list">
                                            <ui:text key="screen.072ba368d69fd3d8"/>
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <c:if test="${totalPages > 1}">
                    <nav class="list-pagination" aria-label="${requestScope.i18n['screen.7b3f674b625e7226']}" data-i18n-aria-label="screen.7b3f674b625e7226">
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
    </body>
</html>