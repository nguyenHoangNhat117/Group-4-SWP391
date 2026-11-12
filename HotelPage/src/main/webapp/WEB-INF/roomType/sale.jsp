<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><ui:text key="room.admin.sale.manage"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/general.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
</head>
<body>
    <div class="body-wrapper">
        <%@ include file="../include/header.jsp" %>
        <main class="container py-5">
            <h1><ui:text key="${roomType.active ? 'room.admin.sale.stop' : 'room.admin.sale.resume'}"/></h1>
            <p><strong><c:out value="${roomType.name}"/></strong></p>
            <p><ui:text key="room.admin.sale.rooms"/> <strong>${roomCount}</strong></p>
            <p><ui:text key="room.admin.sale.bookings"/> <strong>${activeBookingCount}</strong></p>
            <c:if test="${roomType.active}">
                <p><ui:text key="room.admin.sale.existing"/></p>
            </c:if>
            <form action="${pageContext.request.contextPath}/room-type?action=sale" method="post" class="d-flex gap-2">
                <input type="hidden" name="csrfToken" value="${roomAdminCsrf}"/>
                <input type="hidden" name="id" value="${roomType.id}"/>
                <input type="hidden" name="active" value="${not roomType.active}"/>
                <button type="submit" class="btn btn-primary">
                    <ui:text key="${roomType.active ? 'room.admin.sale.stop' : 'room.admin.sale.resume'}"/>
                </button>
                <a href="${pageContext.request.contextPath}/room-type" class="btn btn-secondary">
                    <ui:text key="screen.1ea442a134b2a184"/>
                </a>
            </form>
        </main>
        <%@ include file="../include/footer.jsp" %>
    </div>
</body>
</html>
