<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Room Type Management</title>

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
        <div class="luxe-admin-banner" aria-label="Room collections">
          <span class="luxe-admin-banner-eyebrow">LUXE ESCAPE <span aria-hidden="true">/</span> ROOM CATEGORIES</span>
          <span class="luxe-admin-banner-title">Room collections</span>
          <span class="luxe-admin-banner-art" aria-hidden="true"></span>
        </div>

                <div class="list-heading">
                    <div>
                        <h1>Room Type Management</h1>
                        <p class="text-muted mb-0">
                            Manage room categories, nightly rates and guest capacity.
                        </p>
                    </div>

                    <a class="btn btn-primary"
                       href="${pageContext.request.contextPath}/room-type?view=create">
                        + Add Room Type
                    </a>
                </div>

                <div class="table-panel">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle">
                            <caption class="visually-hidden">
                                Room types on the current page
                            </caption>

                            <thead>
                                <tr>
                                    <th scope="col">ID</th>
                                    <th scope="col">Image</th>
                                    <th scope="col">Name</th>
                                    <th scope="col">Description</th>
                                    <th scope="col">Price / Night</th>
                                    <th scope="col">Beds</th>
                                    <th scope="col">Capacity</th>
                                    <th scope="col">Actions</th>
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
                                                 alt="Room type thumbnail"
                                                 loading="lazy">
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
                                                    Edit
                                                </a>

                                                <a class="btn btn-outline-danger btn-sm"
                                                   href="<c:out value='${deleteUrl}'/>"
                                                   aria-label="Delete room type ${roomType.id}">
                                                    Delete
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>

                                <c:if test="${empty roomTypes}">
                                    <tr>
                                        <td colspan="8" class="empty-list">
                                            No room types found.
                                            Add a room type to get started.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <c:if test="${totalPages > 1}">
                    <nav class="list-pagination" aria-label="Pagination">
                        <ul class="pagination mb-0">
                            <li class="page-item ${pageIndex <= 1 ? 'disabled' : ''}">
                                <c:choose>
                                    <c:when test="${pageIndex > 1}">
                                        <a class="page-link"
                                           href="${pageContext.request.contextPath}/room-type?page-index=${pageIndex - 1}">
                                            Previous
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="page-link">Previous</span>
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
                                            Next
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="page-link">Next</span>
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