<%@ page pageEncoding="UTF-8" %>
<%@ page import="util.AuthSupport" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>


<%
    model.Customer headerCustomer = null;

    model.User headerUser
            = (model.User) session.getAttribute("loggedUser");

    if (headerUser != null
            && "customer".equals(headerUser.getRole())) {

        dao.CustomerDAO headerDAO = new dao.CustomerDAO();

        headerCustomer = headerDAO
                .getCustomerByUserID(headerUser.getId());
    }

    boolean headerHasAvatar
            = headerCustomer != null
            && headerCustomer.getAvatar() != null
            && !headerCustomer.getAvatar().trim().isEmpty();
%>


<link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/luxe-account-menu.css?v=header-v6">


<link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/header-layout.css?v=header-row-2">
<header class="luxe-shared-header luxe-header-v4 luxe-header-v5"
        data-luxe-header
        data-context-path="${pageContext.request.contextPath}">

    <nav>
        <span id="nav-heading">
            <a href="${pageContext.request.contextPath}/"
               aria-label="Luxe Escape">

                <span class="luxe-brand-full">
                    Luxe Escape
                </span>

                <span class="luxe-brand-short"
                      aria-hidden="true">
                    Luxe
                </span>
            </a>
        </span>

        <ul id="navbar" class="${empty sessionScope.loggedUser or sessionScope.loggedUser.role == 'customer' ? 'luxe-guest-nav' : ''}">
            <c:choose>
                <c:when test="${sessionScope.loggedUser.role == 'admin'}">
                    <li>
                        <a href="${pageContext.request.contextPath}/admin/users">
                            <ui:text key="nav.accounts"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/room">
                            <ui:text key="nav.rooms"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/room-type">
                            <ui:text key="nav.roomTypes"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/booking-management">
                            <ui:text key="nav.bookings"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/offline-booking">
                            <ui:text key="nav.offline"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/customers">
                            <ui:text key="nav.customers"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/discounts">
                            <ui:text key="nav.discounts"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/admin/reviews">
                            <ui:text key="nav.reviews"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/hotel-wallet">
                            Hotel Wallet
                        </a>
                    </li>

                </c:when>

                <c:when test="${sessionScope.loggedUser.role == 'staff'}">
                    <li>
                        <a href="${pageContext.request.contextPath}/booking">
                            <ui:text key="nav.search"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/booking-management">
                            <ui:text key="nav.bookings"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/offline-booking">
                            <ui:text key="nav.offline"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/customers">
                            <ui:text key="nav.customers"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/hotel-wallet">
                            Hotel Wallet
                        </a>
                    </li>

                </c:when>

                <c:when test="${sessionScope.loggedUser.role == 'manager'}">
                    <li>
                        <a href="${pageContext.request.contextPath}/dashboard">
                            <ui:text key="nav.dashboard"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/reports?view=bookings&amp;page-index=1">
                            <ui:text key="nav.reports"/>
                        </a>
                    </li>
                </c:when>

                <c:otherwise>
                    <li>
                        <a href="${pageContext.request.contextPath}/#rooms">
                            <ui:text key="nav.rooms"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/#restaurant">
                            <ui:text key="nav.restaurant"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/booking">
                            <ui:text key="nav.booking"/>
                        </a>
                    </li>

                    <li>
                        <a href="${pageContext.request.contextPath}/promotions">
                            <ui:text key="nav.promotions"/>
                        </a>
                    </li>
                </c:otherwise>
            </c:choose>
        </ul>

        <%-- One group containing navigation toggle, language and account. --%>
        <div class="luxe-header-actions">

            <button type="button"
                    class="luxe-nav-trigger"
                    data-luxe-nav-toggle
                    aria-controls="navbar"
                    aria-expanded="false"
                    data-i18n-aria-label="nav.toggle"
                    aria-label="${requestScope.i18n['nav.toggle']}">

                <svg viewBox="0 0 24 24"
                     aria-hidden="true">
                <path d="M4 6h16M4 12h16M4 18h16"/>
                </svg>
            </button>

            <jsp:include page="/WEB-INF/include/language-switcher.jsp"/>

            <c:choose>
                <c:when test="${empty sessionScope.loggedUser}">
                    <div class="auth-nav">
                        <a href="${pageContext.request.contextPath}/login"
                           id="login"
                           class="luxe-auth-button">

                            <ui:text key="auth.login"/>
                        </a>
                    </div>
                </c:when>

                <c:otherwise>
                    <c:set var="luxeHeaderName"
                           value="${not empty sessionScope.loggedUser.displayName
                                    ? sessionScope.loggedUser.displayName
                                    : sessionScope.loggedUser.username}"/>

                    <c:choose>
                        <c:when test="${sessionScope.loggedUser.role == 'admin'}">
                            <c:set var="luxeRoleKey"
                                   value="role.admin"/>
                        </c:when>

                        <c:when test="${sessionScope.loggedUser.role == 'staff'}">
                            <c:set var="luxeRoleKey"
                                   value="role.staff"/>
                        </c:when>

                        <c:when test="${sessionScope.loggedUser.role == 'manager'}">
                            <c:set var="luxeRoleKey"
                                   value="role.manager"/>
                        </c:when>

                        <c:when test="${sessionScope.loggedUser.role == 'customer'}">
                            <c:set var="luxeRoleKey"
                                   value="role.customer"/>
                        </c:when>

                        <c:otherwise>
                            <c:set var="luxeRoleKey"
                                   value="role.account"/>
                        </c:otherwise>
                    </c:choose>

                    <%-- The same account component is used for all roles. --%>
                    <div class="auth-nav luxe-unified-auth">
                        <details class="luxe-account-details"
                                 data-luxe-account>

                            <summary class="luxe-account-trigger"
                                     data-i18n-aria-label="account.options"
                                     aria-label="${requestScope.i18n['account.options']}">



                                <span class="luxe-account-avatar"
                                      aria-hidden="true"
                                      style="
                                      width: 44px !important;
                                      height: 44px !important;
                                      min-width: 44px !important;
                                      min-height: 44px !important;
                                      flex: 0 0 44px !important;
                                      display: inline-flex !important;
                                      align-items: center !important;
                                      justify-content: center !important;
                                      overflow: hidden !important;
                                      border-radius: 50% !important;
                                      padding: 0 !important;
                                      box-sizing: border-box !important;
                                      ">

                                    <% if (headerHasAvatar) {%>
                                    <img
                                        src="<%= request.getContextPath()%>/avatar/image"
                                        alt=""
                                        width="44"
                                        height="44"
                                        style="
                                        width: 44px !important;
                                        height: 44px !important;
                                        min-width: 44px !important;
                                        min-height: 44px !important;
                                        max-width: 44px !important;
                                        max-height: 44px !important;
                                        object-fit: cover !important;
                                        border-radius: 50% !important;
                                        display: block !important;
                                        position: static !important;
                                        margin: 0 !important;
                                        padding: 0 !important;
                                        "
                                        >
                                    <% } else { %>
                                    <c:out value="${fn:substring(luxeHeaderName, 0, 1)}"/>
                                    <% }%>
                                </span>



                                <span class="luxe-account-text">
                                    <strong>
                                        <c:out value="${luxeHeaderName}"/>
                                    </strong>

                                    <small>
                                        <ui:text key="${luxeRoleKey}"/>
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
                                 data-i18n-aria-label="account.actions"
                                 aria-label="${requestScope.i18n['account.actions']}">

                                <div class="luxe-account-panel-heading">
                                    <strong>
                                        <c:out value="${luxeHeaderName}"/>
                                    </strong>

                                    <span>
                                        @<c:out value="${sessionScope.loggedUser.username}"/>
                                    </span>
                                </div>

                                <c:if test="${sessionScope.loggedUser.role == 'customer'}">
                                    <a class="luxe-account-item"
                                       href="${pageContext.request.contextPath}/profile?id=${sessionScope.loggedUser.id}">

                                        <svg viewBox="0 0 24 24"
                                             aria-hidden="true">

                                        <circle cx="12" cy="8" r="4"/>
                                        <path d="M4 21v-2a8 8 0 0 1 16 0v2"/>
                                        </svg>

                                        <ui:text key="account.profile"/>
                                    </a>

                                    <a class="luxe-account-item"
                                       href="${pageContext.request.contextPath}/history?view=bookings">




                                        <svg viewBox="0 0 24 24"
                                             aria-hidden="true">

                                        <rect x="4" y="5" width="16"
                                              height="16" rx="2"/>

                                        <path d="M8 3v4m8-4v4M4 11h16"/>
                                        </svg>

                                        <ui:text key="account.bookingHistory"/>
                                    </a>

                                    <a class="luxe-account-item"
                                       href="${pageContext.request.contextPath}/history?view=payments">

                                        <svg viewBox="0 0 24 24"
                                             aria-hidden="true">

                                        <rect x="3" y="5" width="18"
                                              height="14" rx="2"/>

                                        <path d="M3 10h18M7 15h3"/>
                                        </svg>

                                        <ui:text key="account.paymentHistory"/>
                                    </a>

                                    <a class="luxe-account-item"
                                       href="${pageContext.request.contextPath}/wallet">

                                        <svg viewBox="0 0 24 24"
                                             aria-hidden="true">

                                        <path d="M20 8V5H5a2 2 0 0 0 0 4h15v11H5a2 2 0 0 1-2-2V7"/>
                                        <path d="M20 12h-5v4h5"/>
                                        </svg>

                                        <ui:text key="account.wallet"/>
                                    </a>
                                </c:if>

                                <a class="luxe-account-item"
                                   href="${pageContext.request.contextPath}/change-password">

                                    <svg viewBox="0 0 24 24"
                                         aria-hidden="true">

                                    <circle cx="8" cy="15" r="5"/>
                                    <path d="m12 11 8-8m-3 3 3 3m-6 0 3 3"/>
                                    </svg>

                                    <ui:text key="auth.changePassword"/>
                                </a>

                                <c:if test="${sessionScope.loggedUser.role == 'staff'
                                              or sessionScope.loggedUser.role == 'admin'
                                              or sessionScope.loggedUser.role == 'manager'}">

                                      <%-- Wallet Management for Admin and Staff --%>
                                      <c:if test="${sessionScope.loggedUser.role == 'admin'
                                                    or sessionScope.loggedUser.role == 'staff'}">

                                            <a class="luxe-account-item"
                                               href="${pageContext.request.contextPath}/wallet-management">

                                                <svg viewBox="0 0 24 24"
                                                     aria-hidden="true">
                                                <path d="M20 8V5H5a2 2 0 0 0 0 4h15v11H5a2 2 0 0 1-2-2V7"/>
                                                <path d="M20 12h-5v4h5"/>
                                                </svg>

                                                <span>Wallet Management</span>
                                            </a>

                                      </c:if>


                                      <form method="post"
                                            action="${pageContext.request.contextPath}/toggle-animation"
                                            class="luxe-account-form">

                                          <input type="hidden"
                                                 name="csrfToken"
                                                 value="<%= AuthSupport.token(session)%>">

                                          <input type="hidden"
                                                 name="toggleAnimation"
                                                 value="${cookie.animation.value == 'disable'
                                                          ? 'enable'
                                                          : 'disable'}">

                                          <button type="submit"
                                                  class="luxe-account-item">

                                              <svg viewBox="0 0 24 24"
                                                   aria-hidden="true">

                                              <path d="m13 2-8 12h6l-1 8 9-13h-6l1-7Z"/>
                                              </svg>

                                              <c:choose>
                                                  <c:when test="${cookie.animation.value == 'disable'}">
                                                      <ui:text key="account.enableAnimations"/>
                                                  </c:when>

                                                  <c:otherwise>
                                                      <ui:text key="account.disableAnimations"/>
                                                  </c:otherwise>
                                              </c:choose>

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

                                        <svg viewBox="0 0 24 24"
                                             aria-hidden="true">

                                        <path d="M9 4H4v16h5m5-12 4 4-4 4m-6-4h13"/>
                                        </svg>

                                        <ui:text key="auth.logout"/>
                                    </button>
                                </form>
                            </div>
                        </details>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </nav>
</header>

<script type="module"
        src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.esm.js">
</script>

<script nomodule
        src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.js">
</script>

<script defer
        src="${pageContext.request.contextPath}/assets/js/luxe-account-menu.js?v=header-v5">
</script>
