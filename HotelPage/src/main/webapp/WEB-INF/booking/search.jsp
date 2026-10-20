<%-- 
    Document   : booking (Search Rooms)
    Created on : Oct 19, 2026, 8:42:03 PM
    Author     : Lương Thành Đạt (Remastered for Luxury UI)
--%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Discover Rooms - Luxe Hotel</title>
        
        <!-- CSS Dependencies -->
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/litepicker/dist/css/litepicker.css" />
        
        <!-- Custom Styles -->
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/general.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-modern.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />

        <style>
            /* Hero Section Styles */
            .hero-section {
                position: relative;
                height: 60vh;
                min-height: 400px;
                background: linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.5)), 
                            url('<%= request.getContextPath()%>/assets/img/hero-bg.jpg') center/cover no-repeat; /* Bạn có thể tạo ảnh hero-bg.jpg để vào thư mục img */
                background-color: #2c3e50; /* Fallback color */
                display: flex;
                align-items: center;
                justify-content: center;
                text-align: center;
            }

            .hero-content h1 {
                font-family: 'Playfair Display', serif;
                font-size: 3.5rem;
                font-weight: 700;
                color: #ffffff;
                text-shadow: 2px 2px 4px rgba(0,0,0,0.5);
                margin-bottom: 1rem;
            }

            .hero-content p {
                color: #f8f9fa;
                font-size: 1.2rem;
                margin-bottom: 2rem;
            }

            /* Search Bar Styles */
            .search-container {
                position: relative;
                margin-top: -60px; /* Pull up over the hero section */
                z-index: 10;
                padding: 0 15px;
            }

            .search-bar {
                background: #ffffff;
                border-radius: 12px;
                box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
                padding: 20px;
            }

            .search-input-group {
                display: flex;
                flex-wrap: wrap;
                gap: 15px;
            }

            .search-field {
                flex: 1;
                min-width: 200px;
                position: relative;
            }

            .search-field label {
                font-size: 0.85rem;
                font-weight: 600;
                color: #6c757d;
                text-transform: uppercase;
                letter-spacing: 1px;
                margin-bottom: 5px;
                display: block;
            }

            .search-field .form-control {
                border: 1px solid #e0e0e0;
                border-radius: 8px;
                padding: 12px 15px 12px 40px;
                font-size: 1rem;
                font-weight: 500;
                color: #333;
                background-color: #f8f9fa;
                transition: all 0.3s ease;
            }

            .search-field .form-control:focus {
                background-color: #fff;
                border-color: #b29575;
                box-shadow: 0 0 0 0.25rem rgba(178, 149, 117, 0.25);
            }

            .search-field i {
                position: absolute;
                left: 15px;
                top: 40px; /* Căn giữa theo chiều dọc ô input */
                color: #b29575;
                font-size: 1.1rem;
            }

            .btn-search {
                background-color: #b29575;
                color: white;
                border: none;
                border-radius: 8px;
                padding: 12px 30px;
                font-size: 1.1rem;
                font-weight: 600;
                transition: all 0.3s ease;
                height: 100%;
                width: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 8px;
            }

            .btn-search:hover {
                background-color: #9a7f60;
                transform: translateY(-2px);
                box-shadow: 0 5px 15px rgba(178, 149, 117, 0.4);
            }

            /* Extra Info Section */
            .benefits-section {
                padding: 80px 0 60px;
                background-color: #fdfbf9;
            }

            .benefit-card {
                text-align: center;
                padding: 20px;
            }

            .benefit-icon {
                font-size: 2.5rem;
                color: #b29575;
                margin-bottom: 15px;
            }
        </style>
    </head>
    
    <body>
        <div class="body-wrapper">    
            <%@include file="../include/header.jsp" %>
            
            <main>
                <!-- Hero Section (Bức ảnh lớn và Câu chào) -->
                <section class="hero-section">
                    <div class="container hero-content">
                        <h1>Experience Luxury & Comfort</h1>
                        <p>Find the perfect room for your unforgettable stay.</p>
                    </div>
                </section>

                <!-- Thanh Tìm kiếm ngang (Horizontal Search Bar) -->
                <section class="search-container container">
                    <div class="search-bar">
                        <c:if test="${param.error == 'missing-dates'}">
                            <div class="alert alert-danger alert-dismissible fade show mb-3" role="alert">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                                You must choose a date range to find rooms.
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>

                        <form method="post" action="<%= request.getContextPath()%>/room?action=search">
                            <div class="row g-3 align-items-end">
                                
                                <!-- Cột 1: Ngày Check-in & Check-out -->
                                <div class="col-lg-6 col-md-12">
                                    <div class="search-field">
                                        <label for="floatingBookingDate">Check-in & Check-out <span class="text-danger">*</span></label>
                                        <i class="bi bi-calendar3"></i>
                                        <input type="text" class="form-control" id="floatingBookingDate" 
                                               placeholder="Select your dates" name="booking-dates" readonly required>
                                    </div>
                                </div>

                                <!-- Cột 2: Số lượng khách (Mô phỏng UI) -->
                                <div class="col-lg-2 col-md-4 col-sm-6">
                                    <div class="search-field">
                                        <label for="adults">Adults</label>
                                        <i class="bi bi-person-fill"></i>
                                        <select class="form-control" id="adults" name="adults">
                                            <option value="1">1 Adult</option>
                                            <option value="2" selected>2 Adults</option>
                                            <option value="3">3 Adults</option>
                                            <option value="4">4 Adults</option>
                                        </select>
                                    </div>
                                </div>

                                <!-- Cột 3: Trẻ em (Mô phỏng UI) -->
                                <div class="col-lg-2 col-md-4 col-sm-6">
                                    <div class="search-field">
                                        <label for="children">Children</label>
                                        <i class="bi bi-person-badge"></i>
                                        <select class="form-control" id="children" name="children">
                                            <option value="0" selected>0 Children</option>
                                            <option value="1">1 Child</option>
                                            <option value="2">2 Children</option>
                                        </select>
                                    </div>
                                </div>

                                <!-- Cột 4: Nút Search -->
                                <div class="col-lg-2 col-md-4 col-sm-12">
                                    <button class="btn btn-search" type="submit" id="find-rooms-button">
                                        <i class="bi bi-search"></i> Check Availability
                                    </button>
                                </div>
                                
                            </div>
                        </form>
                    </div>
                </section>

                <!-- Section: Lợi ích khi đặt phòng (Tạo cảm giác chân thực) -->
                <section class="benefits-section">
                    <div class="container">
                        <div class="row">
                            <div class="col-md-4">
                                <div class="benefit-card">
                                    <i class="bi bi-shield-check benefit-icon"></i>
                                    <h5 class="fw-bold">Secure Booking</h5>
                                    <p class="text-muted">Your booking is protected with 256-bit encryption.</p>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="benefit-card">
                                    <i class="bi bi-tag benefit-icon"></i>
                                    <h5 class="fw-bold">Best Price Guarantee</h5>
                                    <p class="text-muted">You won't find a better rate anywhere else.</p>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="benefit-card">
                                    <i class="bi bi-cup-hot benefit-icon"></i>
                                    <h5 class="fw-bold">Premium Amenities</h5>
                                    <p class="text-muted">Enjoy free Wi-Fi, breakfast, and pool access.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>
                
            </main>
            
            <%@include file="../include/footer.jsp" %>
        </div>

        <!-- Scripts -->
        <script src="https://cdn.jsdelivr.net/npm/litepicker/dist/bundle.js"></script>
        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>
        
        <script>
            document.addEventListener("DOMContentLoaded", function() {
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
            });
        </script>
    </body>
</html>