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

                    <c:when test="${param.error == 'missing-params'}">
                        <p class="alert alert-danger text-center">
                            <ui:text key="screen.9eca9b88d5fc8dff"/>
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

                    <c:when test="${param.error == 'create-failed'}">
                        <p class="alert alert-danger text-center">
                            <ui:text key="screen.ee26f12320833edd"/>
                        </p>
                    </c:when>

                </c:choose>

                <div
                    class="form-card shadow rounded-4
                           bg-white p-5 w-50">

                    <form
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

                                <option value="available" data-i18n="screen.e674447337e83c13">
                                    ${requestScope.i18n['screen.e674447337e83c13']}
                                </option>

                                <option value="maintenance" data-i18n="screen.17ccfa5b681e3e7b">
                                    ${requestScope.i18n['screen.17ccfa5b681e3e7b']}
                                </option>

                                <option value="out_of_service" data-i18n="screen.3cd4b662003a9db7">
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

                                    <option value="${category.id}">

                                        ${category.id}
                                        -
                                        ${category.name}

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

                                <ui:text key="screen.4759498ac2a719c6"/>

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