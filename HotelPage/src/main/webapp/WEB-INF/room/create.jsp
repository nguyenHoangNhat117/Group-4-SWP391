<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : create
    Created on : May 26, 2025, 9:46:21 AM
    Author     : Đặng Hoàng Vũ
--%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

        <title data-i18n="screen.3f855ba1babcbd8a">${requestScope.i18n['screen.3f855ba1babcbd8a']}</title>

        <link
            href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css"
            rel="stylesheet">

        <link
            href="<%= request.getContextPath()%>/assets/css/general.css"
            rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
</head>

    <body class="bg-light">

        <div class="body-wrapper">

            <%@include file="/WEB-INF/include/header.jsp" %>

            <main
                class="min-vh-50 d-flex flex-column
                       align-items-center justify-content-center">

                <!-- Error Messages -->
                <c:choose>

                    <c:when test="${createError == 'invalid-quantity'}">
                        <p class="alert alert-danger text-center" role="alert">
                            <ui:text key="room.admin.quantity.invalid"/>
                        </p>
                    </c:when>

                    <c:when test="${createError == 'invalid-status'}">
                        <p class="alert alert-danger text-center">
                            <ui:text key="screen.3e84be96fde86c7f"/>
                        </p>
                    </c:when>

                    <c:when test="${createError == 'invalid-room-type'}">
                        <p class="alert alert-danger text-center">
                            <ui:text key="screen.cb5b07a14e46aec9"/>
                        </p>
                    </c:when>

                    <c:when test="${createError == 'create-failed'}">
                        <p class="alert alert-danger text-center">
                            <ui:text key="screen.ee26f12320833edd"/>
                        </p>
                    </c:when>

                </c:choose>

                <div
                    class="form-card shadow rounded-4
                           bg-white p-5 w-50">

                    <form
                        id="create-rooms-form"
                        action="${pageContext.request.contextPath}/room?action=create"
                        method="post"
                        class="d-flex flex-column">

                        <h2 class="mb-4 text-center">
                            <ui:text key="screen.4494068281a6d314"/>
                        </h2>

                        <!--
                            Room Number is NOT entered manually.

                            HotelDB:
                            RoomNumber INT IDENTITY(100,1)

                            SQL Server generates the room number.
                        -->

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
                                class="form-select form-select-lg"
                                required>

                                <option value="available" ${param.status == 'available' ? 'selected' : ''} data-i18n="screen.e674447337e83c13">
                                    ${requestScope.i18n['screen.e674447337e83c13']}
                                </option>

                                <option value="maintenance" ${param.status == 'maintenance' ? 'selected' : ''} data-i18n="screen.17ccfa5b681e3e7b">
                                    ${requestScope.i18n['screen.17ccfa5b681e3e7b']}
                                </option>

                                <option value="out_of_service" ${param.status == 'out_of_service' ? 'selected' : ''} data-i18n="screen.3cd4b662003a9db7">
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
                                class="form-select form-select-lg"
                                required>

                                <c:forEach
                                    var="category"
                                    items="${requestScope.categorys}">

                                    <option value="${category.id}" ${category.id == selectedRoomTypeId ? 'selected' : ''}>

                                        ${category.id}
                                        -
                                        <c:out value="${category.name}"/>

                                    </option>

                                </c:forEach>

                            </select>

                        </div>

                        <div class="mb-4">
                            <label for="room-quantity" class="form-label fs-5 fw-semibold">
                                <ui:text key="room.admin.quantity"/>
                            </label>
                            <input id="room-quantity" name="quantity" class="form-control form-control-lg"
                                   type="number" min="1" max="${maxQuantity}" step="1" required
                                   aria-describedby="room-quantity-help"
                                   value="<c:out value='${param.quantity}' default='1'/>">
                            <div id="room-quantity-help" class="form-text"><ui:text key="room.admin.quantity.help"/></div>
                        </div>

                        <!-- Submit -->
                        <div class="text-end">

                            <button
                                id="create-rooms-submit"
                                type="submit"
                                class="btn btn-primary
                                       fs-5 px-4 rounded-3">

                                <span id="create-rooms-submit-label"><ui:text key="screen.4759498ac2a719c6"/></span>
                                <span id="create-rooms-pending-label" hidden><ui:text key="room.admin.creating"/></span>

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
        <script>
            const createRoomsForm = document.getElementById('create-rooms-form');
            const createRoomsButton = document.getElementById('create-rooms-submit');
            const createRoomsLabel = document.getElementById('create-rooms-submit-label');
            const createRoomsPendingLabel = document.getElementById('create-rooms-pending-label');

            createRoomsForm.addEventListener('submit', (event) => {
                if (createRoomsButton.disabled) {
                    event.preventDefault();
                    return;
                }
                createRoomsButton.disabled = true;
                createRoomsLabel.hidden = true;
                createRoomsPendingLabel.hidden = false;
            });

            window.addEventListener('pageshow', () => {
                createRoomsButton.disabled = false;
                createRoomsLabel.hidden = false;
                createRoomsPendingLabel.hidden = true;
            });
        </script>

    </body>
</html>
