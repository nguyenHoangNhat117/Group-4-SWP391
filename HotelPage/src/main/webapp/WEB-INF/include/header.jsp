<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="util.AuthSupport" %>
<%-- Updated by Nguyen Hoang Nhat --%>

<header class="luxe-shared-header">
    <nav>
        <span id="nav-heading">
            <a href="${pageContext.request.contextPath}/">
                Luxe Escape
            </a>
        </span>

        <ul id="navbar">
            <c:choose>
                <%-- Administrator navigation. --%>
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
                    <li><a href="${pageContext.request.contextPath}/admin/users">User Accounts</a></li>
                    </c:when>

                <%-- Staff navigation. --%>
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

                <%-- Hotel Manager navigation. --%>
                <c:when test="${sessionScope.loggedUser.role == 'manager'}">
                    <li>
                        <a href="${pageContext.request.contextPath}/dashboard">
                            Dashboard
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/reports?view=bookings&amp;page-index=1">
                            Reports
                        </a>
                    </li>
                </c:when>

                <%-- Customer navigation. --%>
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

                <%-- Preserve the existing IT Support menu. --%>
                <c:when test="${sessionScope.loggedUser.role == 'it_support'}">
                    <li>
                        <a href="${pageContext.request.contextPath}/">
                            Home
                        </a>
                    </li>
                </c:when>

                <%-- Guest navigation. --%>
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
                    <a href="${pageContext.request.contextPath}/login"
                       id="login"
                       class="luxe-auth-button">
                        Login
                    </a>
                </div>
            </c:when>

            <c:otherwise>
                <div class="auth-nav luxe-auth-account">
                    <details class="luxe-account-menu" data-account-menu>
                        <summary class="luxe-account-toggle" aria-label="Account menu">
                            <c:url var="accountAvatar" value="/assets/img/avatar.jpg" />
                            <c:if test="${sessionScope.loggedUser.role == 'customer' and not empty sessionScope.customer.avatar}">
                                <c:url var="accountAvatar" value="/assets/img/${sessionScope.customer.avatar}" />
                            </c:if>
                            <img width="44" height="44" class="user-avatar"
                                 src="<c:out value='${accountAvatar}'/>" alt="">
                        </summary>
                        <div class="luxe-account-panel">
                            <div class="luxe-account-identity">
                                <strong><c:out value="${not empty sessionScope.loggedUser.displayName ? sessionScope.loggedUser.displayName : sessionScope.loggedUser.username}" /></strong>
                                <span><c:choose>
                                        <c:when test="${sessionScope.loggedUser.role == 'manager'}">Hotel Manager</c:when>
                                        <c:when test="${sessionScope.loggedUser.role == 'admin'}">Administrator</c:when>
                                        <c:when test="${sessionScope.loggedUser.role == 'staff'}">Staff</c:when>
                                        <c:when test="${sessionScope.loggedUser.role == 'customer'}">Customer</c:when>
                                        <c:otherwise>Account</c:otherwise>
                                    </c:choose></span>
                            </div>
                            <c:if test="${sessionScope.loggedUser.role == 'customer'}">
                                <a class="luxe-account-item" href="${pageContext.request.contextPath}/profile?id=${sessionScope.loggedUser.id}">Profile</a>
                            </c:if>
                            <c:if test="${sessionScope.loggedUser.role == 'customer' or sessionScope.loggedUser.role == 'staff' or sessionScope.loggedUser.role == 'manager' or sessionScope.loggedUser.role == 'admin'}">
                                <a class="luxe-account-item" href="${pageContext.request.contextPath}/change-password">Change Password</a>
                            </c:if>
                            <c:if test="${sessionScope.loggedUser.role == 'customer'}">
                                <a class="luxe-account-item" href="${pageContext.request.contextPath}/history?view=bookings">Booking History</a>
                                <a class="luxe-account-item" href="${pageContext.request.contextPath}/history?view=payments">Payment History</a>
                            </c:if>
                            <form method="post" action="${pageContext.request.contextPath}/logout" class="luxe-account-logout">
                                <input type="hidden" name="csrfToken" value="<%= AuthSupport.html(AuthSupport.token(session))%>">
                                <button class="luxe-account-item" type="submit">Logout</button>
                            </form>
                        </div>
                    </details>
                </div>
            </c:otherwise>
        </c:choose>
    </nav>
    <hr>
</header>

<script type="module"
        src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.esm.js">
</script>

<script nomodule
        src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.js">
</script>

<script>
    // Add the favicon without creating duplicate elements.
    (() => {
        if (!document.querySelector('link[rel="icon"]')) {
            const favicon = document.createElement("link");

            favicon.rel = "icon";
            favicon.type = "image/png";
            favicon.href =
                    "${pageContext.request.contextPath}/assets/img/favicon.png";

            document.head.appendChild(favicon);
        }
    })();
</script>
<script>
    (() => {
        const menu = document.querySelector('[data-account-menu]');
        if (!menu)
            return;
        document.addEventListener('click', event => {
            if (!menu.contains(event.target))
                menu.open = false;
        });
        document.addEventListener('keydown', event => {
            if (event.key === 'Escape' && menu.open) {
                menu.open = false;
                menu.querySelector('summary').focus();
            }
        });
    })();
</script>
