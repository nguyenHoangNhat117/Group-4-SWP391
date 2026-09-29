<%-- 
    Document   : booking
    Created on : Jun 14, 2025, 8:42:03 PM
    Author     : Đặng Hoàng Vũ
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Booking</title>
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/litepicker/dist/css/litepicker.css" />
        <script src="https://cdn.jsdelivr.net/npm/litepicker/dist/bundle.js"></script>
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/general.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/booking.css"/>
    </head>
    <body>
        <div class="body-wrapper">    
            <%@include file="../include/header.jsp" %>
            <main class="d-flex flex-column justify-content-center align-items-center pb-5">
                <c:if test="${param.error == 'missing-dates'}">
                    <p class="alert alert-danger text-center">
                        You must choose a date range to find rooms.
                    </p>
                </c:if>
                <form class="text-center d-flex flex-column justify-content-center mb-5 border-2 shadow-sm" method="post" action="<%= request.getContextPath()%>/room?action=search">

                    <h2 class="mb-4">Pick Your Rooms</h2>

                    <div class="form-floating mb-3">
                        <input type="text" class="form-control" id="floatingBookingDate" placeholder="Dates" name="booking-dates" readonly required>
                        <label for="floatingBookingDate">Dates</label>
                    </div>

<!--                    <div class="d-flex mb-3">
                        <div class="form-floating w-50 me-2">
                            <input type="number" class="form-control" id="floatingRoomQuantity" placeholder="Dates" name="room-quantity" required min="1" max="20">
                            <label for="floatingRoomQuantity">Rooms</label>
                        </div>

                        <div class="form-floating w-50">
                            <input type="number" class="form-control" id="floatingGuestQuantity" placeholder="Dates" name="guest-quantity" required min="1" max="80">
                            <label for="floatingGuestQuantity">Guest</label>
                        </div>
                    </div>-->


                    <button class="w-100 btn btn-lg d-flex justify-content-center align-items-center gap-1" type="submit" id="find-rooms-button"><span><img src="<%= request.getContextPath()%>/assets/icons/search.svg" alt="find rooms icon"/></span>Find Rooms</button>
                </form>
            </main>
            <%@include file="../include/footer.jsp" %>
        </div>
        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
        <script>
            const picker = new Litepicker({
                element: document.getElementById('floatingBookingDate'),
                singleMode: false, // for date range
                numberOfMonths: 2, // show two months side by side
                numberOfColumns: 2,
                autoApply: true,
                format: 'MMM D, YYYY',
                tooltipText: {
                    one: 'day',
                    other: 'days'
                },
                minDate: new Date()
            });
        </script>
    </body>
</html>
