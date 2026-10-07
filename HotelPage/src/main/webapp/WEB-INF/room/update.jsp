<%-- 
    Document   : update
    Created on : May 26, 2025, 9:46:21 AM
    Author     : Đặng Hoàng Vũ
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

    <head>

        <meta
            http-equiv="Content-Type"
            content="text/html; charset=UTF-8">

        <title>Update Room</title>

        <link
            href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css"
            rel="stylesheet">

        <link
            href="<%= request.getContextPath()%>/assets/css/general.css"
            rel="stylesheet">

    </head>

    <body class="bg-light">

        <div class="body-wrapper">

            <%@include file="/WEB-INF/include/header.jsp" %>

            <main
                class="min-vh-50 d-flex flex-column
                       align-items-center justify-content-center">

                <!-- Error Messages -->
                <c:choose>

                    <c:when test="${param.error == 'missing-params'}">

                        <p class="alert alert-danger text-center">

                            Missing parameters in the form.
                            Please try again.

                        </p>

                    </c:when>

                    <c:when test="${param.error == 'room-not-exist'}">

                        <p class="alert alert-danger text-center">

                            The selected room does not exist.
                            Please try again.

                        </p>

                    </c:when>

                    <c:when test="${param.error == 'number-format'}">

                        <p class="alert alert-danger text-center">

                            Invalid room number.
                            Please try again.

                        </p>

                    </c:when>

                    <c:when test="${param.error == 'invalid-status'}">

                        <p class="alert alert-danger text-center">

                            Invalid room status.
                            Please try again.

                        </p>

                    </c:when>

                    <c:when test="${param.error == 'invalid-room-type'}">

                        <p class="alert alert-danger text-center">

                            Invalid room type.
                            Please try again.

                        </p>

                    </c:when>

                    <c:when test="${param.error == 'update-failed'}">

                        <p class="alert alert-danger text-center">

                            Room could not be updated.
                            Please try again.

                        </p>

                    </c:when>

                </c:choose>

                <div
                    class="shadow rounded-4 bg-white
                           p-5 w-50">

                    <form
                        action="${pageContext.request.contextPath}/room?action=update"
                        method="post"
                        class="d-flex flex-column">

                        <h2 class="mb-4 text-center">
                            Room Update
                        </h2>

                        <!-- Room Number -->
                        <div class="mb-4">

                            <label
                                for="room-number"
                                class="form-label fs-5 fw-semibold">

                                Room Number

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

                                Status

                            </label>

                            <select
                                id="status"
                                name="status"
                                class="form-select form-select-lg"
                                required>

                                <option
                                    value="available"
                                    ${requestScope.room.status == 'available'
                                      ? 'selected'
                                      : ''}>

                                    Available

                                </option>

                                <option
                                    value="maintenance"
                                    ${requestScope.room.status == 'maintenance'
                                      ? 'selected'
                                      : ''}>

                                    Maintenance

                                </option>

                                <option
                                    value="out_of_service"
                                    ${requestScope.room.status == 'out_of_service'
                                      ? 'selected'
                                      : ''}>

                                    Out of Service

                                </option>

                            </select>

                        </div>

                        <!-- Room Type -->
                        <div class="mb-4">

                            <label
                                for="room-type"
                                class="form-label fs-5 fw-semibold">

                                Room Type

                            </label>

                            <select
                                id="room-type"
                                name="room-type-id"
                                class="form-select form-select-lg"
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
                                        ${roomType.name}

                                    </option>

                                </c:forEach>

                            </select>

                        </div>

                        <!-- Submit -->
                        <div class="text-end">

                            <button
                                type="submit"
                                class="btn btn-primary
                                       fs-5 px-4 rounded-3">

                                Update

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

    </body>

</html>