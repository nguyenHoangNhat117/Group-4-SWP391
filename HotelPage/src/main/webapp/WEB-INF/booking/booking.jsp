<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta charset="UTF-8">
        <title data-i18n="screen.126850cb3c7e0489">${requestScope.i18n['screen.126850cb3c7e0489']}</title>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/litepicker/dist/css/litepicker.css" />
        <script src="https://cdn.jsdelivr.net/npm/litepicker/dist/bundle.js"></script>
            <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-modern.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
</head>
    <body>
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp" %>
            <main class="d-flex flex-column justify-content-center align-items-center">
                <c:if test="${param.error == 'already-booked'}">
                    <p class="alert alert-danger text-center">
                        <ui:text key="screen.6ddd82eba57508d6"/>
                    </p>
                </c:if>
                <c:if test="${param.error == 'invalid-payment-option'}">
                    <p class="alert alert-danger text-center">
                        <ui:text key="screen.bcc23b87d3979559"/>
                    </p>
                </c:if>
                <form action="./booking?action=confirm" method="post" class="rounded-4 bg-light p-5" style="min-width: 400px;">

                    <h2 class="text-center mb-4"><ui:text key="screen.c5b08e9a585393a8"/></h2>

                    <!-- Hidden customer ID from session -->
                    <input type="hidden" name="customer-id" value="${sessionScope.customerId}" />

                    <!-- Room Number -->
                    <div class="mb-3">
                        <input type="text" class="form-control form-control-lg"
                               placeholder="${requestScope.i18n['screen.f91ba3e6d86ddac0']}" value="Room ${param.roomNumber}" readonly required data-i18n-placeholder="screen.f91ba3e6d86ddac0">
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
                            <ui:text key="screen.d104a659abae9ebc"/> <span class="text-primary" id="totalPrice"><ui:text key="screen.d5ad0a70d0af2aaa"/></span>
                        </p>
                    </div>

                    <!-- Initial payment option -->
                    <div class="mb-3">
                        <label for="prepayment-percent" class="form-label fw-semibold"><ui:text key="screen.6462ea238a580eb8"/></label>
                        <select id="prepayment-percent" name="prepayment-percent" class="form-select form-select-lg" required>
                            <option value="0" data-i18n="screen.6500570368820548">${requestScope.i18n['screen.6500570368820548']}</option>
                            <option value="10" data-i18n="screen.ba120e159f543d2e">${requestScope.i18n['screen.ba120e159f543d2e']}</option>
                            <option value="20" data-i18n="screen.4a6ff799acf7bfce">${requestScope.i18n['screen.4a6ff799acf7bfce']}</option>
                            <option value="100" data-i18n="screen.8e92879488898a6c">${requestScope.i18n['screen.8e92879488898a6c']}</option>
                        </select>
                        <div class="form-text"><ui:text key="screen.b6db22362a703521"/></div>
                    </div>

                    <div class="border rounded-3 p-3 mb-4 bg-white">
                        <div class="d-flex justify-content-between"><span><ui:text key="screen.bded39f26f017751"/></span><strong class="text-success"><span id="payNowAmount">0 ₫</span></strong></div>
                        <div class="d-flex justify-content-between"><span><ui:text key="screen.8d779b7e1ba93913"/></span><strong><span id="remainingAmount">0 ₫</span></strong></div>
                    </div>

                    <button type="submit" class="btn btn-lg w-100 text-white" style="background-color: #b29575;">
                        <i class="bi bi-check-circle"></i> <span id="booking-submit-text"><ui:text key="screen.4755e39fe9490bb6"/></span>
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
                const money = new Intl.NumberFormat("vi-VN", {style: "currency", currency: "VND"});

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

                    totalPriceElement.textContent = money.format(totalPrice);
                    updatePaymentSplit(totalPrice);
                }

                const percentSelect = document.getElementById("prepayment-percent");

                function updatePaymentSplit(total) {
                    if (!Number.isFinite(total)) {
                        total = 0;
                    }
                    const payNow = Math.round(total * Number(percentSelect.value) / 100);
                    document.getElementById("payNowAmount").textContent = money.format(payNow);
                    document.getElementById("remainingAmount").textContent = money.format(Math.max(0, total - payNow));
                    document.getElementById("booking-submit-text").textContent = Number(percentSelect.value) === 0
                        ? "Confirm Booking (No Payment Required)"
                        : "Confirm Booking & Continue to Payment";
                }

                percentSelect.addEventListener("change", calculateTotalPrice);

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
