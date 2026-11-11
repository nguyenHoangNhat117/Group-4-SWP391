<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@page contentType="text/html"
        pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>

    <head>

        <meta
            http-equiv="Content-Type"
            content="text/html; charset=UTF-8">

        <title data-i18n="screen.559a9e5847c0b407">${requestScope.i18n['screen.559a9e5847c0b407']}</title>

        <link
            href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css"
            rel="stylesheet">

        <link
            href="<%= request.getContextPath()%>/assets/css/general.css"
            rel="stylesheet">

        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
    </head>

    <body class="bg-light">

        <div class="body-wrapper">

            <%@include file="/WEB-INF/include/header.jsp" %>


            <main class="container py-5">


                <!-- ========================================= -->
                <!-- MESSAGES                                  -->
                <!-- ========================================= -->

                <c:if test="${param.message == 'status-updated'}">

                    <div class="alert alert-success text-center">

                        <ui:text key="screen.8e157b39a58b2875"/>

                    </div>

                </c:if>


                <c:if test="${param.message == 'update-failed'}">

                    <div class="alert alert-danger text-center">

                        <ui:text key="screen.ca3960b905b70262"/>

                    </div>

                </c:if>


                <c:if test="${param.message == 'info-updated'}">
                    <div class="alert alert-success text-center">
                        <ui:text key="screen.bda8a60ea20c078f"/>
                    </div>
                </c:if>

                <c:if test="${param.message == 'info-update-failed'}">
                    <div class="alert alert-danger text-center">
                        <ui:text key="screen.28be1ef3256dad41"/>
                    </div>
                </c:if>

                <c:if test="${param.message == 'invalid-data'}">
                    <div class="alert alert-danger text-center">
                        <ui:text key="screen.5ac5e9b89eeea9ce"/>
                    </div>
                </c:if>

                <div
                    class="card
                    border-0
                    shadow
                    rounded-4">


                    <div class="card-body p-5">


                        <div
                            class="d-flex
                            justify-content-between
                            align-items-center
                            mb-4">


                            <h2 class="mb-0">

                                <ui:text key="screen.559a9e5847c0b407"/>

                            </h2>


                            <a
                                href="${pageContext.request.contextPath}/customers"
                                class="btn btn-outline-secondary">

                                <ui:text key="screen.76900f1bfd16c8d4"/>

                            </a>


                        </div>


                        <!-- ================================= -->
                        <!-- PROFILE                           -->
                        <!-- ================================= -->

                        <div class="row g-4">


                            <div class="col-md-6">


                                <p>

                                    <strong>
                                        <ui:text key="screen.b01e76be2d45ae6f"/>
                                    </strong>

                                    ${customerDetail.customerID}

                                </p>


                                <p>

                                    <strong>
                                        <ui:text key="screen.f1f1fc8826026955"/>
                                    </strong>

                                    <c:choose>

                                        <c:when test="${not empty customerDetail.firstName}">
                                            ${customerDetail.firstName}
                                        </c:when>

                                        <c:otherwise>
                                            -
                                        </c:otherwise>

                                    </c:choose>

                                </p>


                                <p>

                                    <strong>
                                        <ui:text key="screen.53aca2c64b62c3f5"/>
                                    </strong>

                                    <c:choose>

                                        <c:when test="${not empty customerDetail.lastName}">
                                            ${customerDetail.lastName}
                                        </c:when>

                                        <c:otherwise>
                                            -
                                        </c:otherwise>

                                    </c:choose>

                                </p>


                                <p>

                                    <strong>
                                        <ui:text key="screen.17507dcf845793fa"/>
                                    </strong>

                                    ${customerDetail.email}

                                </p>


                                <p>

                                    <strong>
                                        <ui:text key="screen.3305eb58093dce17"/>
                                    </strong>

                                    <c:choose>

                                        <c:when test="${not empty customerDetail.phone}">
                                            ${customerDetail.phone}
                                        </c:when>

                                        <c:otherwise>
                                            -
                                        </c:otherwise>

                                    </c:choose>

                                </p>


                            </div>


                            <div class="col-md-6">


                                <p>

                                    <strong>
                                        <ui:text key="screen.3806d61c34063f85"/>
                                    </strong>

                                    ${customerDetail.user.username}

                                </p>


                                <p>

                                    <strong>
                                        <ui:text key="screen.01c17432f91bc0ec"/>
                                    </strong>

                                    <c:choose>

                                        <c:when test="${not empty customerDetail.user.displayName}">

                                            ${customerDetail.user.displayName}

                                        </c:when>

                                        <c:otherwise>
                                            -
                                        </c:otherwise>

                                    </c:choose>

                                </p>


                                <p>

                                    <strong>
                                        <ui:text key="screen.813c3318f9a1d31a"/>
                                    </strong>

                                    <c:choose>

                                        <c:when test="${not empty customerDetail.country}">
                                            ${customerDetail.country}
                                        </c:when>

                                        <c:otherwise>
                                            -
                                        </c:otherwise>

                                    </c:choose>

                                </p>


                                <p>

                                    <strong>
                                        <ui:text key="screen.c5aaab3ec67f76d9"/>
                                    </strong>

                                    <c:choose>

                                        <c:when test="${not empty customerDetail.city}">
                                            ${customerDetail.city}
                                        </c:when>

                                        <c:otherwise>
                                            -
                                        </c:otherwise>

                                    </c:choose>

                                </p>


                                <p>

                                    <strong>
                                        <ui:text key="screen.1ad0a1ffe3863887"/>
                                    </strong>

                                    <c:choose>

                                        <c:when test="${not empty customerDetail.street}">
                                            ${customerDetail.street}
                                        </c:when>

                                        <c:otherwise>
                                            -
                                        </c:otherwise>

                                    </c:choose>

                                </p>


                            </div>


                        </div>


                        <hr class="my-4">


                        <h4 class="mb-3"><ui:text key="screen.7435c82a2428274c"/></h4>

                        <form action="${pageContext.request.contextPath}/customers" method="post" class="row g-3 mb-4">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="action" value="update-info">
                            <input type="hidden" name="customer-id" value="${customerDetail.customerID}">

                            <div class="col-md-6">
                                <label class="form-label"><ui:text key="screen.96ce6e6193a04726"/></label>
                                <input class="form-control" name="first-name" maxlength="50" value="<c:out value='${customerDetail.firstName}'/>">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label"><ui:text key="screen.f55563b386a39dc3"/></label>
                                <input class="form-control" name="last-name" maxlength="50" value="<c:out value='${customerDetail.lastName}'/>">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label"><ui:text key="screen.969ccbd3cf6300ec"/></label>
                                <input type="email" class="form-control" name="email" maxlength="100" ${customerDetail.user.id gt 0 ? 'required' : ''} value="<c:out value='${customerDetail.email}'/>">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label"><ui:text key="screen.63dceb8800b2dd4a"/></label>
                                <input class="form-control" name="phone" maxlength="20" value="<c:out value='${customerDetail.phone}'/>">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label"><ui:text key="screen.701d021d08c54579"/></label>
                                <input class="form-control" name="country" maxlength="50" value="<c:out value='${customerDetail.country}'/>">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label"><ui:text key="screen.fc33f73246f48295"/></label>
                                <input class="form-control" name="city" maxlength="50" value="<c:out value='${customerDetail.city}'/>">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label"><ui:text key="screen.c666591df0537cf2"/></label>
                                <input class="form-control" name="street" maxlength="100" value="<c:out value='${customerDetail.street}'/>">
                            </div>
                            <div class="col-12 text-end">
                                <button type="submit" class="btn btn-primary" onclick="return confirm('Update this customer information?');">
                                    <ui:text key="screen.593f5aaf288e59f6"/>
                                </button>
                            </div>
                        </form>

                        <hr class="my-4">

                        <!-- ================================= -->
                        <!-- ACCOUNT INFORMATION               -->
                        <!-- ================================= -->

                        <h4 class="mb-3">

                            <ui:text key="screen.4e59b6c72f6fd0b3"/>

                        </h4>


                        <div class="row">


                            <div class="col-md-6">


                                <p>

                                    <strong>
                                        <ui:text key="screen.44dd5f163cbadd27"/>
                                    </strong>

                                    <ui:message text="${customerDetail.user.role}"/>

                                </p>


                                <p>

                                    <strong>
                                        <ui:text key="screen.8fd7542a821729b1"/>
                                    </strong>

                                    ${customerDetail.user.createdAt}

                                </p>


                            </div>


                            <div class="col-md-6">


                                <p>

                                    <strong>
                                        <ui:text key="screen.90cdddb9ee1882a6"/>
                                    </strong>


                                    <c:choose>


                                        <c:when test="${customerDetail.user.id eq 0}"><span class="badge bg-secondary"><ui:text key="offline.noAccount"/></span></c:when>
<c:when test="${customerDetail.user.accountStatus == 'active'}">

                                            <span
                                                class="badge bg-success">

                                                <ui:text key="screen.92340695899bd2d8"/>

                                            </span>

                                        </c:when>


                                        <c:when test="${customerDetail.user.accountStatus == 'locked'}">

                                            <span
                                                class="badge bg-danger">

                                                <ui:text key="screen.a424e33d90931d1e"/>

                                            </span>

                                        </c:when>


                                        <c:otherwise>

                                            <span
                                                class="badge bg-secondary">

                                                <ui:text key="screen.ac7c949f1211b781"/>

                                            </span>

                                        </c:otherwise>


                                    </c:choose>


                                </p>


                            </div>


                        </div>


                        <hr class="my-4">


                        <!-- ================================= -->
                        <!-- CHANGE ACCOUNT STATUS             -->
                        <!-- ================================= -->

                        <c:if test="${customerDetail.user.id gt 0}"><h4 class="mb-3">

                            <ui:text key="screen.9f00d170c98434c5"/>

                        </h4>


                        <form
                            action="${pageContext.request.contextPath}/customers"
                            method="post"
                            class="row g-3">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">


                            <input
                                type="hidden"
                                name="action"
                                value="update-status">


                            <input
                                type="hidden"
                                name="customer-id"
                                value="${customerDetail.customerID}">


                            <div class="col-md-8">


                                <select
                                    name="account-status"
                                    class="form-select"
                                    required>


                                    <option
                                        value="active"
                                        ${customerDetail.user.accountStatus == 'active'
                                          ? 'selected'
                                          : ''} data-i18n="screen.92340695899bd2d8">

                                        ${requestScope.i18n['screen.92340695899bd2d8']}

                                    </option>


                                    <option
                                        value="locked"
                                        ${customerDetail.user.accountStatus == 'locked'
                                          ? 'selected'
                                          : ''} data-i18n="screen.a424e33d90931d1e">

                                        ${requestScope.i18n['screen.a424e33d90931d1e']}

                                    </option>


                                    <option
                                        value="inactive"
                                        ${customerDetail.user.accountStatus == 'inactive'
                                          ? 'selected'
                                          : ''} data-i18n="screen.ac7c949f1211b781">

                                        ${requestScope.i18n['screen.ac7c949f1211b781']}

                                    </option>


                                </select>


                            </div>


                            <div class="col-md-4">


                                <button
                                    type="submit"
                                    class="btn btn-primary w-100"
                                    onclick="return confirm('Change this customer account status?');">

                                    <ui:text key="screen.3030ddce9921f3f9"/>

                                </button>


                            </div>


                        </form></c:if>


                        <hr class="my-4">


                        <!-- ================================= -->
                        <!-- HISTORY                           -->
                        <!-- ================================= -->

                        <h4 class="mb-3">

                            <ui:text key="screen.9e90b33666a3d8f3"/>

                        </h4>


                        <div
                            class="d-flex
                            flex-wrap
                            gap-3">


                            <a
                                href="${pageContext.request.contextPath}/history?id=${customerDetail.customerID}&view=bookings"
                                class="btn btn-outline-primary">

                                <ui:text key="screen.1db635e08edc4126"/>

                            </a>


                            <a
                                href="${pageContext.request.contextPath}/history?id=${customerDetail.customerID}&view=payments"
                                class="btn btn-outline-success">

                                <ui:text key="screen.8cf3d5aa6b487a0a"/>

                            </a>


                        </div>


                    </div>


                </div>


            </main>


            <%@include file="/WEB-INF/include/footer.jsp" %>


        </div>


        <script
            src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js">
        </script>


    </body>

</html>
