<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : create
    Created on : Oct 19, 2026, 4:28:05 AM
    Author     : Lương Thành Đạt
--%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title data-i18n="screen.cf53dfbb45bc51f9">${requestScope.i18n['screen.cf53dfbb45bc51f9']}</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
    </head>
    <body class="bg-light">
        <div class="body-wrapper">
            <%@ include file="/WEB-INF/include/header.jsp" %>

            <main class="min-vh-100 d-flex flex-column justify-content-center align-items-center">
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
                    <c:when test="${param.error == 'number-format'}">
                        <p class="alert alert-danger text-center">
                            <ui:text key="screen.1469fc1a6acdc7d0"/>
                        </p>
                    </c:when>
                    <c:when test="${param.error == 'missing-image'}">
                        <p class="alert alert-danger text-center">
                            <ui:text key="screen.de2e7a78d75b71fd"/>
                        </p>
                    </c:when>
                </c:choose>
                <form action="room-type" method="post" enctype="multipart/form-data"
                      class="bg-white p-5 rounded shadow-lg w-100" style="max-width: 600px;">
                    <input type="hidden" name="action" value="create">
                    <h2 class="mb-4 text-center text-dark"><ui:text key="screen.4e5d70d7ca7b5ab9"/></h2>

                    <!-- Room Type Name -->
                    <div class="mb-3">
                        <label for="name" class="form-label fs-5 fw-semibold"><ui:text key="screen.741b94aab7248c7b"/></label>
                        <input type="text" id="name" name="name" class="form-control form-control-lg" required>
                    </div>

                    <!-- Description -->
                    <div class="mb-3">
                        <label for="description" class="form-label fs-5 fw-semibold"><ui:text key="screen.526e0087cc3f254d"/></label>
                        <textarea id="description" name="description" class="form-control form-control-lg" rows="4" required></textarea>
                    </div>

                    <!-- Price Per Night -->
                    <div class="mb-3">
                        <label for="pricePerNight" class="form-label fs-5 fw-semibold"><ui:text key="screen.0702805f3bf73124"/></label>
                        <input type="number" step="1" id="pricePerNight" name="pricePerNight"
                               class="form-control form-control-lg" min="1" required>
                    </div>

                    <!-- Beds -->
                    <div class="mb-3">
                        <label for="beds" class="form-label fs-5 fw-semibold"><ui:text key="screen.551b235c36a098f1"/></label>
                        <input type="number" id="beds" name="beds" class="form-control form-control-lg" min="1" required>
                    </div>

                    <!-- Capacity -->
                    <div class="mb-3">
                        <label for="capacity" class="form-label fs-5 fw-semibold"><ui:text key="screen.ae65d096550ff4eb"/></label>
                        <input type="number" id="capacity" name="capacity" class="form-control form-control-lg" min="1" required>
                    </div>

                    <!-- Amenities Checkboxes -->
                    <div class="mb-4">
                        <label class="form-label fs-5 fw-semibold"><ui:text key="screen.fa7a5f35ea119b26"/></label>
                        <div class="row">
                            <c:forEach items="${allAmenities}" var="a">
                                <div class="col-md-6 mb-2">
                                    <div class="form-check">
                                        <!-- Gọi trực tiếp hàm getAmenityID() -->
                                        <input class="form-check-input" type="checkbox" name="amenities" value="${a.getAmenityID()}" id="amenity_${a.getAmenityID()}">
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
    </body>
</html>