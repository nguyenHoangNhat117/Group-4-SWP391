<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ page import="util.AuthSupport" %>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/luxe-account-menu.css?v=account-dropdown-v4">

<header class="luxe-shared-header luxe-header-v4" data-luxe-header>
    <nav>
        <span id="nav-heading">
            <a href="${pageContext.request.contextPath}/">
                Luxe Escape
            </a>
        </span>

        <button type="button"
                class="luxe-nav-trigger"
                data-luxe-nav-toggle
                aria-controls="navbar"
                aria-expanded="false"
                aria-label="Toggle navigation">
            <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M4 6h16M4 12h16M4 18h16"/>
            </svg>
        </button>

        <ul id="navbar">
            <c:choose>
                <%-- Administrator --%>
                <c:when test="${sessionScope.loggedUser.role == 'admin'}">
                    <li>
                        <a href="${pageContext.request.contextPath}/admin/users">
                            Accounts
                        </a>
                    </li>
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
                        <a href="${pageContext.request.contextPath}/offline-booking">
                            Offline Booking
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

                <%-- Staff --%>
                <c:when test="${sessionScope.loggedUser.role == 'staff'}">
                    <li>
                        <a href="${pageContext.request.contextPath}/booking">
                            Search Rooms
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/booking-management">
                            Bookings
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/offline-booking">
                            Offline Booking
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/customers">
                            Customers
                        </a>
                    </li>
                </c:when>

                <%-- Manager --%>
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

                <%-- Customer / Guest --%>
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
                        <a href="${pageContext.request.contextPath}/booking">
                            Booking
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
            <%-- Guest --%>
            <c:when test="${empty sessionScope.loggedUser}">
                <div class="auth-nav">
                    <a href="${pageContext.request.contextPath}/login"
                       id="login"
                       class="luxe-auth-button">
                        Login
                    </a>
                </div>
            </c:when>

            <%-- Customer account dropdown --%>
            <c:when test="${sessionScope.loggedUser.role == 'customer'}">
                <div class="auth-nav luxe-auth-customer">
                    <div class="dropdown">
                        <a class="user-dropdown"
                           href="#"
                           role="button"
                           id="customerDropdown"
                           data-bs-toggle="dropdown"
                           aria-haspopup="true"
                           aria-expanded="false">

                            <img width="40"
                                 height="40"
                                 class="rounded-circle border border-3 user-avatar object-fit-cover"
                                 src="${pageContext.request.contextPath}/assets/img/${not empty sessionScope.customer.avatar ? fn:escapeXml(sessionScope.customer.avatar) : 'avatar.jpg'}"
                                 alt="Customer avatar">
                        </a>

                        <div class="dropdown-menu"
                             aria-labelledby="customerDropdown">

                            <a class="dropdown-item"
                               href="${pageContext.request.contextPath}/profile?id=${sessionScope.loggedUser.id}">
                                Profile
                            </a>

                            <a class="dropdown-item"
                               href="${pageContext.request.contextPath}/change-password">
                                Change Password
                            </a>

                            <a class="dropdown-item"
                               href="${pageContext.request.contextPath}/history?view=bookings">
                                Booking History
                            </a>

                            <a class="dropdown-item"
                               href="${pageContext.request.contextPath}/history?view=payments">
                                Payment History
                            </a>

                            <a class="dropdown-item"
                               href="${pageContext.request.contextPath}/wallet">
                                My Wallet
                            </a>

                            <div class="dropdown-divider"></div>

                            <form method="post"
                                  action="${pageContext.request.contextPath}/logout"
                                  class="luxe-logout-form luxe-dropdown-logout">

                                <input type="hidden"
                                       name="csrfToken"
                                       value="<%= AuthSupport.token(session)%>">

                                <button class="dropdown-item" type="submit">
                                    Logout
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </c:when>

            <%-- Staff / Admin / Manager account menu --%>
            <c:otherwise>
                <c:set var="luxeHeaderName"
                       value="${not empty sessionScope.loggedUser.displayName
                                ? sessionScope.loggedUser.displayName
                                : sessionScope.loggedUser.username}"/>

                <div class="auth-nav luxe-staff-auth">
                    <details class="luxe-account-details" data-luxe-account>
                        <summary class="luxe-account-trigger"
                                 aria-label="Account options">

                            <span class="luxe-account-avatar" aria-hidden="true">
                                <c:out value="${fn:substring(luxeHeaderName, 0, 1)}"/>
                            </span>

                            <span class="luxe-account-text">
                                <strong>
                                    <c:out value="${luxeHeaderName}"/>
                                </strong>

                                <small>
                                    <c:choose>
                                        <c:when test="${sessionScope.loggedUser.role == 'admin'}">
                                            Administrator
                                        </c:when>
                                        <c:when test="${sessionScope.loggedUser.role == 'staff'}">
                                            Hotel Staff
                                        </c:when>
                                        <c:when test="${sessionScope.loggedUser.role == 'manager'}">
                                            Hotel Manager
                                        </c:when>
                                        <c:otherwise>
                                            Account
                                        </c:otherwise>
                                    </c:choose>
                                </small>
                            </span>

                            <svg class="luxe-account-chevron"
                                 viewBox="0 0 24 24"
                                 aria-hidden="true">
                            <path d="m7 10 5 5 5-5"/>
                            </svg>
                        </summary>

                        <div class="luxe-account-panel"
                             role="group"
                             aria-label="Account actions">

                            <div class="luxe-account-panel-heading">
                                <strong>
                                    <c:out value="${luxeHeaderName}"/>
                                </strong>
                                <span>
                                    @<c:out value="${sessionScope.loggedUser.username}"/>
                                </span>
                            </div>

                            <c:if test="${sessionScope.loggedUser.role == 'staff'
                                          or sessionScope.loggedUser.role == 'admin'
                                          or sessionScope.loggedUser.role == 'manager'}">

                                  <a class="luxe-account-item"
                                     href="${pageContext.request.contextPath}/change-password">
                                      <svg viewBox="0 0 24 24" aria-hidden="true">
                                      <circle cx="8" cy="15" r="5"/>
                                      <path d="m12 11 8-8m-3 3 3 3m-6 0 3 3"/>
                                      </svg>
                                      <span>Change Password</span>
                                  </a>

                                  <form method="post"
                                        action="${pageContext.request.contextPath}/toggle-animation"
                                        class="luxe-account-form">

                                      <input type="hidden"
                                             name="csrfToken"
                                             value="<%= AuthSupport.token(session)%>">

                                      <input type="hidden"
                                             name="toggleAnimation"
                                             value="${cookie.animation.value == 'disable'
                                                      ? 'enable' : 'disable'}">

                                      <button type="submit"
                                              class="luxe-account-item">
                                          <svg viewBox="0 0 24 24" aria-hidden="true">
                                          <path d="m13 2-8 12h6l-1 8 9-13h-6l1-7Z"/>
                                          </svg>

                                          <span>
                                              ${cookie.animation.value == 'disable'
                                                ? 'Enable Animations'
                                                : 'Disable Animations'}
                                          </span>

                                          <span class="luxe-animation-dot ${cookie.animation.value == 'disable' ? 'is-off' : 'is-on'}"
                                                aria-hidden="true"></span>
                                      </button>
                                  </form>
                            </c:if>

                            <div class="luxe-account-separator"
                                 aria-hidden="true"></div>

                            <form method="post"
                                  action="${pageContext.request.contextPath}/logout"
                                  class="luxe-account-form">

                                <input type="hidden"
                                       name="csrfToken"
                                       value="<%= AuthSupport.token(session)%>">

                                <button type="submit"
                                        class="luxe-account-item luxe-account-logout">
                                    <svg viewBox="0 0 24 24" aria-hidden="true">
                                    <path d="M9 4H4v16h5m5-12 4 4-4 4m-6-4h13"/>
                                    </svg>
                                    <span>Log out</span>
                                </button>
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

<script defer
        src="${pageContext.request.contextPath}/assets/js/luxe-account-menu.js?v=account-dropdown-v4">
</script>