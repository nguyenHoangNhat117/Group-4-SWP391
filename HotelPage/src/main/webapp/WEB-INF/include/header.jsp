<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="util.AuthSupport" %>

<header class="luxe-shared-header">

    <nav>

        <span id="nav-heading">
            <a href="${pageContext.request.contextPath}/">
                Luxe Escape
            </a>
        </span>


        <ul id="navbar">

            <c:choose>

                <c:when test="${sessionScope.loggedUser.role == 'admin'}">

                    <li>
                        <a href="${pageContext.request.contextPath}/room">
                            Rooms
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/room-type">
                            Room Types
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/booking-management">
                            Bookings
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/customers">
                            Customers
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/discounts">
                            Discounts
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/admin/reviews">
                            Reviews
                        </a>
                    </li>

                </c:when>


                <c:when test="${sessionScope.loggedUser.role == 'staff'}">

                    <li>
                        <a href="${pageContext.request.contextPath}/booking-management">
                            Bookings
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/customers">
                            Customers
                        </a>
                    </li>

                </c:when>


                <c:when test="${sessionScope.loggedUser.role == 'manager'}">

                    <li>
                        <a href="${pageContext.request.contextPath}/reports">
                            Reports
                        </a>
                    </li>

                </c:when>


                <c:when test="${sessionScope.loggedUser.role == 'customer'}">

                    <li>
                        <a href="${pageContext.request.contextPath}/#rooms">
                            Rooms
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/#restaurant">
                            Restaurant
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/booking">
                            Booking
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/promotions">
                            Promotions
                        </a>
                    </li>

                </c:when>


                <c:when test="${sessionScope.loggedUser.role == 'it_support'}">

                    <li>
                        <a href="${pageContext.request.contextPath}/">
                            Home
                        </a>
                    </li>

                </c:when>


                <c:otherwise>

                    <li>
                        <a href="${pageContext.request.contextPath}/#rooms">
                            Rooms
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/#restaurant">
                            Restaurant
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/promotions">
                            Promotions
                        </a>
                    </li>

                </c:otherwise>

            </c:choose>

        </ul>


        <c:choose>

            <c:when test="${empty sessionScope.loggedUser}">

                <div class="auth-nav">

                    <a
                        href="${pageContext.request.contextPath}/login"
                        id="login" class="luxe-auth-button">
                        Login
                    </a>

                </div>

            </c:when>


            <c:when test="${sessionScope.loggedUser.role == 'customer'}">

                <div
                    class="auth-nav luxe-auth-customer">

                    <div class="dropdown">

                        <a
                            class="user-dropdown"
                            href="#"
                            role="button"
                            id="customerDropdown"
                            data-bs-toggle="dropdown"
                            aria-haspopup="true"
                            aria-expanded="false">

                            <img
                                width="40"
                                height="40"
                                class="rounded-circle border border-3 user-avatar object-fit-cover"
                                src="${pageContext.request.contextPath}/assets/img/${not empty sessionScope.customer.avatar ? sessionScope.customer.avatar : 'avatar.jpg'}"
                                alt="avatar">

                        </a>


                        <div
                            class="dropdown-menu"
                            aria-labelledby="customerDropdown">

                            <a
                                class="dropdown-item"
                                href="${pageContext.request.contextPath}/profile?id=${sessionScope.loggedUser.id}">
                                Profile
                            </a>


                            <a
                                class="dropdown-item"
                                href="${pageContext.request.contextPath}/history?view=bookings">
                                Booking History
                            </a>


                            <a
                                class="dropdown-item"
                                href="${pageContext.request.contextPath}/history?view=payments">
                                Payment History
                            </a>


                            <div class="dropdown-divider">
                            </div>


                            <form method="post" action="<%= request.getContextPath()%>/logout" class="luxe-logout-form luxe-dropdown-logout">
                                <input type="hidden" name="csrfToken" value="<%= AuthSupport.token(session)%>">
                                <button class="dropdown-item" type="submit">Logout</button>
                            </form>

                        </div>

                    </div>

                </div>

            </c:when>


            <c:otherwise>

                <div class="auth-nav">

                    <span class="luxe-user-name">
                        Hi,
                        <c:choose>
                            <c:when test="${not empty sessionScope.loggedUser.displayName}">
                                <c:out value="${sessionScope.loggedUser.displayName}" />
                            </c:when>
                            <c:otherwise>
                                <c:out value="${sessionScope.loggedUser.username}" />
                            </c:otherwise>
                        </c:choose>
                    </span>
                    <form method="post" action="<%= request.getContextPath()%>/logout" class="luxe-logout-form">
                        <input type="hidden" name="csrfToken" value="<%= AuthSupport.token(session)%>">
                        <button type="submit" id="logout" class="luxe-auth-button">Logout</button>
                    </form>

                </div>

            </c:otherwise>

        </c:choose>

    </nav>

    <hr>

</header>


<script
    type="module"
    src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.esm.js">
</script>

<script
    nomodule
    src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.js">
</script>


<script>

    const favicon = document.createElement('link');

    favicon.rel = 'icon';
    favicon.type = 'image/png';

    favicon.href =
        '${pageContext.request.contextPath}'
        + '/assets/img/favicon.png?v='
        + new Date().getTime();

    document.head.appendChild(favicon);

</script>