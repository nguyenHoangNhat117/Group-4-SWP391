<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : update
    Created on : Oct 19, 2026, 5:55:22 AM
    Author     : Lương Thành Đạt
--%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title data-i18n="screen.dc3f08be28714b12">${requestScope.i18n['screen.dc3f08be28714b12']}</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
    </head>
    <body class="bg-light">
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp" %>
            <main class="min-vh-100 d-flex flex-column align-items-center justify-content-center">
                <c:choose>
                    <c:when test="${param.error == 'missing-params'}">
                        <p class="alert alert-danger text-center">
                            <ui:text key="screen.9eca9b88d5fc8dff"/>
                        </p>
                    </c:when>
                    <c:when test="${param.error == 'name-exist'}">
                        <p class="alert alert-danger text-center">
                            <ui:text key="screen.9dda0b654299ce61"/>
                        </p>
                    </c:when>
                    <c:when test="${param.error == 'id-not-exist'}">
                        <p class="alert alert-danger text-center">
                            <ui:text key="screen.3aa1ebdc8bb042dd"/>
                        </p>
                    </c:when>
                    <c:when test="${param.error == 'id-different'}">
                        <p class="alert alert-danger text-center">
                            <ui:text key="screen.b9ac473e86fe9c3e"/>
                        </p>
                    </c:when>
                    <c:when test="${param.error == 'number-format'}">
                        <p class="alert alert-danger text-center">
                            <ui:text key="screen.1469fc1a6acdc7d0"/>
                        </p>
                    </c:when>
                </c:choose>
                <form action="./room-type?action=update" method="post" enctype="multipart/form-data"
                      class="p-5 rounded shadow-lg bg-white w-100" style="max-width: 600px;">
                    <h2 class="mb-4 text-center text-dark"><ui:text key="screen.dc3f08be28714b12"/></h2>

                    <!-- Hidden input for ID -->
                    <input type="hidden" name="id" value="${roomType.id}" />

                    <!-- Name Field -->
                    <div class="mb-3">
                        <label for="name" class="form-label fs-5 fw-semibold"><ui:text key="screen.741b94aab7248c7b"/></label>
                        <input type="text" class="form-control form-control-lg" id="name" name="name"
                               value="${roomType.name}" required />
                    </div>

                    <!-- Description Field -->
                    <div class="mb-3">
                        <label for="description" class="form-label fs-5 fw-semibold"><ui:text key="screen.526e0087cc3f254d"/></label>
                        <textarea class="form-control form-control-lg" id="description" name="description"
                                  rows="4" required>${roomType.description}</textarea>
                    </div>

                    <!-- PricePerNight -->
                    <div class="mb-3">
                        <label for="pricePerNight" class="form-label fs-5 fw-semibold"><ui:text key="screen.bf193eab4a1aafb0"/></label>
                        <input type="number" class="form-control form-control-lg" id="pricePerNight" name="pricePerNight"
                               value="${roomType.pricePerNight}" min="0" step="1" required />
                    </div>

                    <!-- Beds -->
                    <div class="mb-3">
                        <label for="beds" class="form-label fs-5 fw-semibold"><ui:text key="screen.2e2a3cd1a00809d3"/></label>
                        <input type="number" class="form-control form-control-lg" id="beds" name="beds"
                               value="${roomType.beds}" min="1" required />
                    </div>

                    <!-- Capacity -->
                    <div class="mb-3">
                        <label for="capacity" class="form-label fs-5 fw-semibold"><ui:text key="screen.ae65d096550ff4eb"/></label>
                        <input type="number" class="form-control form-control-lg" id="capacity" name="capacity"
                               value="${roomType.capacity}" min="1" required />
                    </div>

                    <!-- Amenities Checkboxes -->
                    <div class="mb-4">
                        <label class="form-label fs-5 fw-semibold"><ui:text key="screen.fa7a5f35ea119b26"/></label>
                        <div class="row">
                            <c:forEach items="${allAmenities}" var="a">
                                <!-- Logic kiểm tra xem tiện ích này loại phòng đã có chưa -->
                                <c:set var="isChecked" value="false" />
                                <c:forEach items="${roomAmenities}" var="ra">
                                    <c:if test="${ra.getAmenityID() == a.getAmenityID()}">
                                        <c:set var="isChecked" value="true" />
                                    </c:if>
                                </c:forEach>
                                
                                <div class="col-md-6 mb-2">
                                    <div class="form-check">
                                        <!-- Gọi trực tiếp hàm getAmenityID() -->
                                        <input class="form-check-input" type="checkbox" name="amenities" value="${a.getAmenityID()}" id="amenity_${a.getAmenityID()}" ${isChecked ? 'checked' : ''}>
                                        <label class="form-check-label" for="amenity_${a.getAmenityID()}">
                                            ${a.name}
                                        </label>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>

                    <!-- Room Picture -->
                    <div class="mb-4">
                        <label for="picture" class="form-label fs-5 fw-semibold"><ui:text key="screen.1f26160dc5f15704"/></label>
                        <img id="room-picture" src="<%= request.getContextPath()%>/assets/img/${roomType.picture}" class="img-fluid mb-2 rounded border" alt="${requestScope.i18n['screen.4c7ab2f6675d51f6']}" data-i18n-alt="screen.4c7ab2f6675d51f6">
                        <input type="file" name="img" accept=".png, .jpg, .jpeg"
                               class="form-control" id="picture"/>
                    </div>

                    <!-- Submit Button -->
                    <div class="text-end">
                        <button type="submit" class="btn btn-primary btn-lg px-4 rounded-3"><ui:text key="screen.c1c1009d3f37ec05"/></button>
                        <a href="./room-type" class="btn btn-secondary btn-lg px-4 rounded-3 ms-2"><ui:text key="screen.19766ed6ccb2f4a3"/></a>
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
        </script>
    </body>
</html>