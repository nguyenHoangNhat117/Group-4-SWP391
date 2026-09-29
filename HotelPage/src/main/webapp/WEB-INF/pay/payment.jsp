<%-- 
    Document   : list
    Created on : Jun 26, 2025, 10:11:27 PM
    Author     : Đặng Hoàng Vũ
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Booking Reports</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
    </head>
    <body>
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp" %>
            <main class="d-flex flex-column justify-content-center align-items-center" style="min-height: 90vh;">
                <c:if test="${param.error == 'invalid-discount'}">
                    <p class="alert alert-danger text-center">
                        Discount Code is incorrect or out of usage.
                    </p>
                </c:if>
                <form action="${pageContext.request.contextPath}/payment" method="post" class="rounded-4 bg-light p-5" style="min-width: 400px;">
                    <h2 class="text-center mb-4">Confirm Your Payment</h2>

                    <!-- Hidden booking ID -->
                    <input type="hidden" name="bookingId" value="${booking.id}" />

                    <!-- Room Number -->
                    <div class="mb-3">
                        <input type="text" class="form-control form-control-lg text-center"
                               placeholder="Room Number" value="Room ${booking.roomNumber}" readonly required>
                    </div>

                    <!-- Booking Dates -->
                    <div class="mb-3">
                        <input type="text" class="form-control form-control-lg text-center"
                               value="${booking.checkInDate} to ${booking.checkOutDate}" readonly required>
                    </div>

                    <!-- Total Price Display -->
                    <div class="mb-3">
                        <p class="fs-5 fw-bold text-center text-dark border rounded py-2 bg-white">
                            Total Price: <span class="text-primary">$${booking.totalPrice}</span> USD
                        </p>
                    </div>

                    <div class="mb-3">
                        <label for="discountCode" class="form-label fw-semibold">Discount Code</label>
                        <input type="text" class="form-control form-control-lg text-center" name="discount-code" id="discountCode"
                               placeholder="Enter discount code" />
                    </div>

                    <!-- Confirm Button -->
                    <button type="submit" class="btn btn-lg w-100 text-white" style="background-color: #b29575;">
                        <i class="bi bi-credit-card"></i> Confirm Payment
                    </button>
                </form>
            </main>
            <%@include file="/WEB-INF/include/footer.jsp" %>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>

