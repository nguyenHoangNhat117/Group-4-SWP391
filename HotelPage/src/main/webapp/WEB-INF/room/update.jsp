<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">

    <head>

        <meta
            http-equiv="Content-Type"
            content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>${requestScope.i18n['screen.28dd027503c97963']}</title>

        <link
            href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css"
            rel="stylesheet">

        <link
            href="<%= request.getContextPath()%>/assets/css/general.css"
            rel="stylesheet">

        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
        <link rel="stylesheet" href="<c:url value='/assets/css/online-booking-select.css?v=4'/>">
        <script type="module" src="<c:url value='/assets/js/admin-room-controls.js'/>"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/management.css">
</head>

    <body class="management-page bg-light">

        <div class="body-wrapper">

            <%@include file="/WEB-INF/include/header.jsp" %>

            <main
                class="management-edit">

                <div class="management-hero"><div><div class="eyebrow">LUXE ESCAPE</div><h1><ui:text key="screen.9181744d5adbd775"/> #${room.roomNumber}</h1></div></div>
<!-- Error Messages -->
                <c:choose>

                    <c:when test="${param.error == 'active-booking'}">
                        <p class="alert alert-danger text-center" role="alert">
                            <ui:text key="room.update.activeBooking"/>
                            <c:if test="${not empty param.stayFrom and not empty param.stayTo}">
                                <span class="d-block"><ui:text key="room.inventory.affectedStay"/> <c:out value="${param.stayFrom}"/> → <c:out value="${param.stayTo}"/></span>
                            </c:if>
                        </p>
                    </c:when>
                    <c:when test="${param.error == 'missing-params'}">

                        <p class="alert alert-danger text-center">

                            <ui:text key="screen.9eca9b88d5fc8dff"/>

                        </p>

                    </c:when>

                    <c:when test="${param.error == 'room-not-exist'}">

                        <p class="alert alert-danger text-center">

                            <ui:text key="screen.3fdba5bec2f36527"/>

                        </p>

                    </c:when>

                    <c:when test="${param.error == 'number-format'}">

                        <p class="alert alert-danger text-center">

                            <ui:text key="screen.c3aca855791617f8"/>

                        </p>

                    </c:when>

                    <c:when test="${param.error == 'invalid-status'}">

                        <p class="alert alert-danger text-center">

                            <ui:text key="screen.3e84be96fde86c7f"/>

                        </p>

                    </c:when>

                    <c:when test="${param.error == 'invalid-room-type'}">

                        <p class="alert alert-danger text-center">

                            <ui:text key="screen.cb5b07a14e46aec9"/>

                        </p>

                    </c:when>

                    <c:when test="${param.error == 'update-failed'}">

                        <p class="alert alert-danger text-center">

                            <ui:text key="screen.7a6796bf186f2fdc"/>

                        </p>

                    </c:when>

                </c:choose>

                <div
                    class="management-edit-card shadow rounded-4 bg-white
                           p-5 w-50">

                    <form
                        action="${pageContext.request.contextPath}/room?action=update"
                        method="post"
                        class="d-flex flex-column">

                        <h2 class="mb-4 text-center">
                            <ui:text key="screen.9181744d5adbd775"/>
                        </h2>

                        <!-- Room Number -->
                        <div class="mb-4">

                            <label
                                for="room-number"
                                class="form-label fs-5 fw-semibold">

                                <ui:text key="screen.f91ba3e6d86ddac0"/>

                            </label>

                            <!--
                                RoomNumber is PRIMARY KEY / IDENTITY.
                                It must NOT be changed.
                            -->
                            <input
                                id="room-number"
                                type="number"
                                value="${requestScope.room.roomNumber}"
                                class="form-control form-control-lg"
                                disabled>

                            <!--
                                Disabled fields are not submitted,
                                therefore a hidden input is required.
                            -->
                            <input
                                type="hidden"
                                name="room-number"
                                value="${requestScope.room.roomNumber}">

                        </div>

                        <!-- Status -->
                        <div class="mb-4">

                            <label
                                for="status"
                                class="form-label fs-5 fw-semibold">

                                <ui:text key="screen.920e413c7d411b61"/>

                            </label>

                            <select
                                id="status"
                                name="status"
                                class="form-select booking-select" data-room-admin-select
                                required>

                                <option
                                    value="available"
                                    ${requestScope.room.status == 'available'
                                      ? 'selected'
                                      : ''}>

                                    ${requestScope.i18n['screen.e674447337e83c13']}

                                </option>

                                <option
                                    value="maintenance"
                                    ${requestScope.room.status == 'maintenance'
                                      ? 'selected'
                                      : ''}>

                                    ${requestScope.i18n['screen.17ccfa5b681e3e7b']}

                                </option>

                                <option
                                    value="out_of_service"
                                    ${requestScope.room.status == 'out_of_service'
                                      ? 'selected'
                                      : ''}>

                                    ${requestScope.i18n['screen.3cd4b662003a9db7']}

                                </option>

                            </select>

                        </div>

                        <!-- Room Type -->
                        <div class="mb-4">

                            <label
                                for="room-type"
                                class="form-label fs-5 fw-semibold">

                                <ui:text key="screen.82dea9a01c0033ec"/>

                            </label>

                            <select
                                id="room-type"
                                name="room-type-id"
                                class="form-select booking-select" data-room-admin-select
                                required>

                                <c:forEach
                                    var="roomType"
                                    items="${requestScope.roomTypes}">

                                    <option
                                        value="${roomType.id}"
                                        ${requestScope.room.roomType.id
                                          == roomType.id
                                          ? 'selected'
                                          : ''}>

                                        ${roomType.id}
                                        -
                                        <c:out value="${roomType.name}"/>

                                    </option>

                                </c:forEach>

                            </select>

                        </div>

                        <!-- Submit -->
                        <div class="d-flex justify-content-between gap-3"><a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/room"><ui:text key="management.back"/></a>

                            <button
                                type="submit"
                                class="btn btn-primary
                                       fs-5 px-4 rounded-3">

                                <ui:text key="screen.c1c1009d3f37ec05"/>

                            </button>

                        </div>

                    </form>

                </div>

            </main>

            <%@include file="/WEB-INF/include/footer.jsp" %>

        </div>

        <script
            src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js">
        </script>

    <script src="${pageContext.request.contextPath}/assets/js/management.js"></script>
</body>

</html>
