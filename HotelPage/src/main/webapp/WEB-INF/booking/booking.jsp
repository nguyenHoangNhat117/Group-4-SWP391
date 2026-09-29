<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Confirm Booking</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/litepicker/dist/css/litepicker.css" />
        <script src="https://cdn.jsdelivr.net/npm/litepicker/dist/bundle.js"></script>
    </head>
    <body>
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp" %>
            <main class="d-flex flex-column justify-content-center align-items-center">
                <c:if test="${param.error == 'already-booked'}">
                    <p class="alert alert-danger text-center">
                        This room has already been booked within that date range.
                    </p>
                </c:if>
                <form action="./booking?action=confirm" method="post" class="rounded-4 bg-light p-5" style="min-width: 400px;">

                    <h2 class="text-center mb-4">Book Your Room</h2>

                    <!-- Hidden customer ID from session -->
                    <input type="hidden" name="customer-id" value="${sessionScope.customerId}" />

                    <!-- Room Number -->
                    <div class="mb-3">
                        <input type="text" class="form-control form-control-lg"
                               placeholder="Room Number" value="Room ${param.roomNumber}" readonly required>
                        <input type="hidden" name="room-number" value="${param.roomNumber}">
                    </div>

                    <!-- Booking Dates -->
                    <div class="mb-3">
                        <input type="text" class="form-control form-control-lg" id="booking-dates"
                               name="booking-dates" value="${sessionScope.dates}" readonly required>
                    </div>

                    <!-- Total Price Display -->
                    <div class="mb-3">
                        <p class="fs-5 fw-bold text-center text-dark border rounded py-2 bg-white">
                            Total Price: <span class="text-primary" id="totalPrice">Calculating...</span> USD
                        </p>
                    </div>

                    <button type="submit" class="btn btn-lg w-100 text-white" style="background-color: #b29575;">
                        <i class="bi bi-check-circle"></i> Confirm Booking
                    </button>
                </form>
            </main>
            <%@include file="/WEB-INF/include/footer.jsp" %>
        </div>

        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
        <script>
            document.addEventListener("DOMContentLoaded", function () {
                const bookingDateInput = document.getElementById("booking-dates");
                const pricePerNight = parseFloat(${room.roomType.pricePerNight}); // from hidden input
                const totalPriceElement = document.getElementById("totalPrice");

                function calculateTotalPrice() {
                    const dateRange = bookingDateInput.value;
                    if (!dateRange.includes(" - "))
                        return;

                    const [start, end] = dateRange.split(" - ");
                    const options = {year: "numeric", month: "short", day: "numeric"};

                    // Parse dates using Date constructor and string format "MMM d, yyyy"
                    const checkIn = new Date(start);
                    const checkOut = new Date(end);

                    const diffTime = checkOut - checkIn;
                    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

                    const totalPrice = diffDays * pricePerNight;
                    console.log(totalPrice.toFixed(2));
                    console.log("Element:", totalPriceElement);

                    totalPriceElement.textContent = totalPrice.toFixed(2);
                }

                bookingDateInput.addEventListener("change", calculateTotalPrice);

                // Initialize picker and calculate on load
                const picker = new Litepicker({
                    element: bookingDateInput,
                    singleMode: false,
                    numberOfMonths: 2,
                    numberOfColumns: 2,
                    autoApply: true,
                    format: 'MMM D, YYYY',
                    tooltipText: {one: 'day', other: 'days'},
                    minDate: new Date(),
                    setup: (picker) => {
                        picker.on('selected', () => {
                            calculateTotalPrice();
                        });
                    }
                });

                calculateTotalPrice();
            });



        </script>
    </body>
</html>
