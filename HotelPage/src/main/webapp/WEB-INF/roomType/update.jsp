<%-- 
    Document   : update
    Created on : Jun 23, 2025, 4:28:22 PM
    Author     : Đặng Hoàng Vũ
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Create Room</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp" %>
            <main class="min-vh-100 d-flex flex-column align-items-center justify-content-center">
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
                    <c:when test="${param.error == 'id-not-exist'}">
                        <p class="alert alert-danger text-center">
                            The submitted id is not exist. Please try again.
                        </p>
                    </c:when>
                    <c:when test="${param.error == 'id-different'}">
                        <p class="alert alert-danger text-center">
                            The submitted id is different from the original. Please try again.
                        </p>
                    </c:when>
                    <c:when test="${param.error == 'number-format'}">
                        <p class="alert alert-danger text-center">
                            Try enter only positive numbers in these following field: Price Per Night, Beds, Capacity.
                        </p>
                    </c:when>
                </c:choose>
                <form action="./room-type?action=update" method="post" enctype="multipart/form-data"
                      class="p-5 rounded shadow-lg bg-white w-100" style="max-width: 600px;">
                    <h2 class="mb-4 text-center text-dark">Update Room Type</h2>

                    <!-- Hidden input for ID -->
                    <input type="hidden" name="id" value="${roomType.id}" />

                    <!-- Name Field -->
                    <div class="mb-3">
                        <label for="name" class="form-label fs-5 fw-semibold">Room Type Name</label>
                        <input type="text" class="form-control form-control-lg" id="name" name="name"
                               value="${roomType.name}" required />
                    </div>

                    <!-- Description Field -->
                    <div class="mb-3">
                        <label for="description" class="form-label fs-5 fw-semibold">Description</label>
                        <textarea class="form-control form-control-lg" id="description" name="description"
                                  rows="4" required>${roomType.description}</textarea>
                    </div>

                    <!-- PricePerNight -->
                    <div class="mb-3">
                        <label for="pricePerNight" class="form-label fs-5 fw-semibold">Price Per Night</label>
                        <input type="number" class="form-control form-control-lg" id="pricePerNight" name="pricePerNight"
                               value="${roomType.pricePerNight}" min="0" step="0.01" required />
                    </div>

                    <!-- Beds -->
                    <div class="mb-3">
                        <label for="beds" class="form-label fs-5 fw-semibold">Beds</label>
                        <input type="number" class="form-control form-control-lg" id="beds" name="beds"
                               value="${roomType.beds}" min="1" required />
                    </div>

                    <!-- Capacity -->
                    <div class="mb-3">
                        <label for="capacity" class="form-label fs-5 fw-semibold">Capacity</label>
                        <input type="number" class="form-control form-control-lg" id="capacity" name="capacity"
                               value="${roomType.capacity}" min="1" required />
                    </div>

                    <!-- Room Picture -->
                    <div class="mb-4">
                        <label for="picture" class="form-label fs-5 fw-semibold">Room Picture</label>
                        <img id="room-picture" src="<%= request.getContextPath()%>/assets/img/${roomType.picture}" class="img-fluid mb-2 rounded border" alt="Current Room Image">
                        <input type="file" name="img" accept=".png, .jpg, .jpeg"
                               class="form-control" id="picture"/>
                    </div>

                    <!-- Submit Button -->
                    <div class="text-end">
                        <button type="submit" class="btn btn-primary btn-lg px-4 rounded-3">Update</button>
                        <a href="./room-type" class="btn btn-secondary btn-lg px-4 rounded-3 ms-2">Cancel</a>
                    </div>
                </form>
            </main>
            <%@include file="/WEB-INF/include/footer.jsp" %>
        </div>
        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
        <script>
            const roomPicture = document.getElementById("room-picture");
            const fileInput = document.getElementById("picture");

            fileInput.onchange = function () {
                if (this.files.length > 0) {
                    roomPicture.style.display = "none"; // Hide the image
                } else {
                    roomPicture.style.display = "block"; // Show it back
                    roomPicture.src = "<%= request.getContextPath()%>/assets/img/${roomType.picture}";
                            }
                        };
        s</script>
    </body>
</html>
