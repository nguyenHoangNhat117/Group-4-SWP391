<%-- 
    Document   : create
    Created on : Jun 23, 2025, 4:28:05 PM
    Author     : Đặng Hoàng Vũ
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Create Room Type</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="body-wrapper">
            <%@ include file="/WEB-INF/include/header.jsp" %>

            <main class="min-vh-100 d-flex flex-column justify-content-center align-items-center">
                <c:choose>
                    <c:when test="${param.error == 'missing-params'}">
                        <p class="alert alert-danger text-center">
                            Missing parameters in the form. Please try again.
                        </p>
                    </c:when>
                    <c:when test="${param.error == 'name-exist'}">
                        <p class="alert alert-danger text-center">
                            The room name you entered is already exist. Please edit it or try another name.
                        </p>
                    </c:when>
                    <c:when test="${param.error == 'number-format'}">
                        <p class="alert alert-danger text-center">
                            Try enter only positive numbers in these following field: Price Per Night, Beds, Capacity.
                        </p>
                    </c:when>
                    <c:when test="${param.error == 'missing-image'}">
                        <p class="alert alert-danger text-center">
                            A room type must have an image. Please try again.
                        </p>
                    </c:when>
                </c:choose>
                <form action="room-type" method="post" enctype="multipart/form-data"
                      class="bg-white p-5 rounded shadow-lg w-100" style="max-width: 600px;">
                    <input type="hidden" name="action" value="create">
                    <h2 class="mb-4 text-center text-dark">Create New Room Type</h2>

                    <!-- Room Type Name -->
                    <div class="mb-3">
                        <label for="name" class="form-label fs-5 fw-semibold">Room Type Name</label>
                        <input type="text" id="name" name="name" class="form-control form-control-lg" required>
                    </div>

                    <!-- Description -->
                    <div class="mb-3">
                        <label for="description" class="form-label fs-5 fw-semibold">Description</label>
                        <textarea id="description" name="description" class="form-control form-control-lg" rows="4" required></textarea>
                    </div>

                    <!-- Price Per Night -->
                    <div class="mb-3">
                        <label for="pricePerNight" class="form-label fs-5 fw-semibold">Price per Night</label>
                        <input type="number" step="0.01" id="pricePerNight" name="pricePerNight"
                               class="form-control form-control-lg" min="1" required>
                    </div>

                    <!-- Beds -->
                    <div class="mb-3">
                        <label for="beds" class="form-label fs-5 fw-semibold">Number of Beds</label>
                        <input type="number" id="beds" name="beds" class="form-control form-control-lg" min="1" required>
                    </div>

                    <!-- Capacity -->
                    <div class="mb-3">
                        <label for="capacity" class="form-label fs-5 fw-semibold">Capacity</label>
                        <input type="number" id="capacity" name="capacity" class="form-control form-control-lg" min="1" required>
                    </div>

                    <!-- Room Picture -->
                    <div class="mb-4">
                        <label for="picture" class="form-label fs-5 fw-semibold">Room Picture</label>
                        <input type="file" name="img" accept=".png, .jpg, .jpeg"
                               class="form-control" id="picture"/>
                    </div>

                    <!-- Submit Button -->
                    <div class="text-end">
                        <button type="submit" class="btn btn-primary btn-lg px-4 rounded-3">Create Room Type</button>
                    </div>
                </form>
            </main>

            <%@ include file="/WEB-INF/include/footer.jsp" %>
        </div>

        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
    </body>

</html>



