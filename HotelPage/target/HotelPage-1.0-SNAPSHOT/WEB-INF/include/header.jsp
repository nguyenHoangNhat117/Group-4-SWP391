<%-- 
    Document   : header
    Created on : Jun 14, 2025, 8:38:12 PM
    Author     : ??ng Hoàng V?
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page import="model.User"%>

<%
    User loggedUser = (User) session.getAttribute("loggedUser");
    boolean loggedIn = session.getAttribute("loggedUser") != null;
%>

<header>
    <nav>
        <span id="nav-heading"><a href="<%= request.getContextPath()%>/">Luxe Escape</a></span>
        <ul id="navbar">
            <c:choose>
                <c:when test="${sessionScope.loggedUser.role == 'admin'}">
                    <li><a href="<%= request.getContextPath()%>/room">Rooms</a></li>
                    <li><a href="<%= request.getContextPath()%>/room-type">Room Types</a></li>
                    <li class="dropdown">
                        <a class="user-dropdown" href="#" role="button" id="dropdownMenuLink" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            Reports
                        </a>
                        <div class="dropdown-menu" aria-labelledby="dropdownMenuLink">
                            <a class="dropdown-item" href="<%= request.getContextPath()%>/reports?view=bookings">Bookings</a>
                            <a class="dropdown-item" href="<%= request.getContextPath()%>/reports?view=payments">Payments</a>
                            <a class="dropdown-item" href="<%= request.getContextPath()%>/reports?view=reviews">Reviews</a>
                        </div>
                    </li>
                    <li><a href="<%= request.getContextPath()%>/customers">Customers</a></li>
                    <li><a href="<%= request.getContextPath()%>/discounts">Discounts</a></li>
                    </c:when>
                    <c:otherwise>
                    <li><a href="<%= request.getContextPath()%>/#rooms">Rooms</a></li>
                    <li><a href="<%= request.getContextPath()%>/#restaurant">Restaurant</a></li>
                    <li><a href="<%= request.getContextPath()%>/booking">Booking</a></li>
                    </c:otherwise>
                </c:choose>
        </ul>

        <!--<ion-icon name="person-circle-outline" style="vertical-align: middle; font-size: 18px; position: relative; top: -2px;"></ion-icon>-->

        <% if (loggedIn) {%>

        <c:choose>
            <c:when test="${sessionScope.loggedUser.role == 'admin'}">
                <div class="auth-nav">    
                    <span class="fw-bold">
                        Hi, ${loggedUser.displayName} 
                    </span>                          
                    <a href="<%= request.getContextPath()%>/logout" id="logout">
                        Logout
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="auth-nav d-flec align-items-center position-absolute top-0 end-0 mt-3 me-5">    
                    <div class="dropdown">

                        <a class="user-dropdown" href="#" role="button" id="dropdownMenuLink" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            <img width="40" height="40" class="rounded-circle border border-3 user-avatar object-fit-cover"  src="<%= request.getContextPath()%>/assets/img/${sessionScope.customer.avatar != null ? customer.avatar : "avatar.jpg"}" alt="avatar">          
                        </a>


                        <div class="dropdown-menu" aria-labelledby="dropdownMenuLink">
                            <a class="dropdown-item" href="<%= request.getContextPath()%>/profile?id=${loggedUser.id}">Profile</a>
                            <a class="dropdown-item" href="<%= request.getContextPath()%>/history?id=${sessionScope.customer.customerID}&view=bookings">Booking History</a>
                            <a class="dropdown-item" href="<%= request.getContextPath()%>/history?id=${sessionScope.customer.customerID}&view=payments">Payment History</a>
                            <a class="dropdown-item" href="<%= request.getContextPath()%>/logout" id="logout">Logout</a>
                        </div>
                    </div>

                </div>
            </c:otherwise>
        </c:choose>

        <% } else {%>
        <div class="auth-nav">
            <a href="<%=request.getContextPath()%>/login" id="login">Login</a>
        </div>
        <%}%>
    </nav>
    <hr />
</header>
<script type="module" src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.esm.js"></script>
<script nomodule src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.js"></script>
<script>
  const link = document.createElement('link');
  link.rel = 'icon';
  link.type = 'image/png';
  link.href = '<%= request.getContextPath() %>/assets/img/favicon.png?v=' + new Date().getTime(); 
  document.head.appendChild(link);
</script>
