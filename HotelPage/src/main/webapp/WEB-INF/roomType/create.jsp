<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : create
    Created on : Oct 19, 2026, 4:28:05 AM
    Author     : Lương Thành Đạt
--%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>${requestScope.i18n['screen.cf53dfbb45bc51f9']}</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
            <link rel="stylesheet" href="<c:url value='/assets/css/admin-room-types.css?v=2'/>">
    </head>
    <body class="bg-light">
        <div class="body-wrapper">
            <%@ include file="/WEB-INF/include/header.jsp" %>

            <main class="min-vh-100 d-flex flex-column justify-content-center align-items-center">
                <c:if test="${not empty roomTypeError}">
                    <p class="alert alert-danger" role="alert"><ui:text key="${roomTypeError}"/></p>
                </c:if>
                <form action="room-type" method="post" enctype="multipart/form-data"
                      class="bg-white p-5 rounded shadow-lg w-100" style="max-width: 600px;">
                    <input type="hidden" name="action" value="create">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <h2 class="mb-4 text-center text-dark"><ui:text key="screen.4e5d70d7ca7b5ab9"/></h2>

                    <!-- Room Type Name -->
                    <div class="mb-3">
                        <label for="name" class="form-label fs-5 fw-semibold"><ui:text key="screen.741b94aab7248c7b"/></label>
                        <input type="text" id="name" name="name" class="form-control form-control-lg" maxlength="50" value="<c:out value='${formValues.name}'/>" required>
                    </div>

                    <!-- Description -->
                    <div class="mb-3">
                        <label for="description" class="form-label fs-5 fw-semibold"><ui:text key="screen.526e0087cc3f254d"/></label>
                        <textarea id="description" name="description" class="form-control form-control-lg" rows="4" maxlength="500" required><c:out value="${formValues.description}"/></textarea>
                    </div>

                    <!-- Price Per Night -->
                    <div class="mb-3">
                        <label for="pricePerNight" class="form-label fs-5 fw-semibold"><ui:text key="screen.0702805f3bf73124"/></label>
                        <input type="number" step="1" id="pricePerNight" name="pricePerNight"
                               class="form-control form-control-lg" min="0" value="<c:out value='${formValues.pricePerNight}'/>" required>
                    </div>

                    <!-- Beds -->
                    <div class="mb-3">
                        <label for="beds" class="form-label fs-5 fw-semibold"><ui:text key="screen.551b235c36a098f1"/></label>
                        <input type="number" id="beds" name="beds" class="form-control form-control-lg" min="1" value="<c:out value='${formValues.beds}'/>" required>
                    </div>

                    <!-- Capacity -->
                    <div class="mb-3">
                        <label for="capacity" class="form-label fs-5 fw-semibold"><ui:text key="room.type.capacity.adults"/></label>
                        <input type="number" id="capacity" name="capacity" class="form-control form-control-lg" min="1" value="<c:out value='${formValues.capacity}'/>" required>
                    </div>

                    <div class="mb-3">
                        <label for="maxChildren" class="form-label fs-5 fw-semibold"><ui:text key="room.type.capacity.children"/></label>
                        <input type="number" id="maxChildren" name="maxChildren" class="form-control form-control-lg"
                               min="0" value="<c:out value='${formValues.maxChildren}'/>" required>
                    </div>

                    <%@ include file="amenity-picker.jspf" %>

                    <!-- Room Picture -->
                    <div class="mb-4">
                        <label for="picture" class="form-label fs-5 fw-semibold"><ui:text key="screen.1f26160dc5f15704"/></label>
                        <input type="file" name="img" accept=".png, .jpg, .jpeg"
                               class="form-control" id="picture"/>
                    </div>

                    <!-- Submit Button -->
                    <div class="text-end">
                        <button type="submit" class="btn btn-primary btn-lg px-4 rounded-3"><ui:text key="screen.cf53dfbb45bc51f9"/></button>
                    </div>
                </form>
            </main>

            <%@ include file="/WEB-INF/include/footer.jsp" %>
        </div>

        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
            <script src="<c:url value='/assets/js/admin-amenity-search.js'/>" defer></script>
    </body>
</html>
