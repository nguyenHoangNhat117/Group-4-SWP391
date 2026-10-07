<%@ taglib
    uri="http://java.sun.com/jsp/jstl/core"
    prefix="c" %>

<%@page contentType="text/html"
        pageEncoding="UTF-8"%>

<header>

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
                        id="login">
                        Login
                    </a>

                </div>

            </c:when>


            <c:when test="${sessionScope.loggedUser.role == 'customer'}">

                <div
                    class="auth-nav d-flex align-items-center position-absolute top-0 end-0 mt-3 me-5">

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


                            <a
                                class="dropdown-item"
                                href="${pageContext.request.contextPath}/logout">
                                Logout
                            </a>

                        </div>

                    </div>

                </div>

            </c:when>


            <c:otherwise>

                <div class="auth-nav">

                    <span class="fw-bold">

                        Hi,

                        <c:choose>

                            <c:when test="${not empty sessionScope.loggedUser.displayName}">
                                ${sessionScope.loggedUser.displayName}
                            </c:when>

                            <c:otherwise>
                                ${sessionScope.loggedUser.username}
                            </c:otherwise>

                        </c:choose>

                    </span>


                    <a
                        href="${pageContext.request.contextPath}/logout"
                        id="logout">
                        Logout
                    </a>

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