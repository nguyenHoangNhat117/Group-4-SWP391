<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- 
    Document   : counter-payment
    Created on : Nov 5, 2026, 9:48:52 PM
    Author     : Nguyen Hoang Nhat
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}" class="uc09-page">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1">

        <title data-i18n="screen.d3467e46e9a97bd4">${requestScope.i18n['screen.d3467e46e9a97bd4']}</title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/general.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">
        <style>
            html {
                scroll-snap-type: none !important;
                scroll-behavior: auto !important;
            }

            main section {
                scroll-snap-align: none !important;
                scroll-margin-top: 0 !important;
            }
        </style>
    </head>

    <body>
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp"%>

            <main class="container py-5" style="max-width:850px">
                <h1 class="h2 mb-4">
                    <ui:text key="screen.241ad153b1e25a2e"/>${summary.bookingId}
                </h1>

                <c:if test="${param.created == '1'}">
                    <div class="alert alert-success">
                        <ui:text key="screen.b9b79b3b0a788fbf"/>
                    </div>
                </c:if>

                <c:if test="${param.paid == '1'}">
                    <div class="alert alert-success">
                        <ui:text key="screen.559a41740d6b430c"/>
                    </div>
                </c:if>

                <c:if test="${not empty param.error}">
                    <div class="alert alert-danger">
                        <ui:text key="screen.deeae93b18fd2ab5"/>
                    </div>
                </c:if>

                <section class="card shadow-sm">
                    <div class="card-body p-4">
                        <dl class="row">
                            <dt class="col-sm-5"><ui:text key="screen.bf3763383aaf4306"/></dt>
                            <dd class="col-sm-7">
                                <c:out value="${summary.customerName}"/>
                            </dd>

                            <dt class="col-sm-5"><ui:text key="screen.7bd668ac984ebb95"/></dt>
                            <dd class="col-sm-7">
                                <c:out value="${summary.creator}"/>
                            </dd>

                            <dt class="col-sm-5"><ui:text key="screen.920e413c7d411b61"/></dt>
                            <dd class="col-sm-7">
                                <ui:message text="${summary.status}"/>
                            </dd>

                            <dt class="col-sm-5">
                                <ui:text key="screen.cbd058252b370686"/>
                            </dt>
                            <dd class="col-sm-7">
                                <c:out value="${summary.holdExpiresAt}"/>
                            </dd>

                            <dt class="col-sm-5"><ui:text key="screen.aa23d755f5aa07c7"/></dt>
                            <dd class="col-sm-7">
                                <fmt:formatNumber value="${summary.total}"/>
                                VND
                            </dd>

                            <dt class="col-sm-5"><ui:text key="screen.7732ed675821a21e"/></dt>
                            <dd class="col-sm-7">
                                <c:out value="${empty summary.promotionCode
                                                ? 'Không áp dụng'
                                                : summary.promotionCode}"/>
                            </dd>

                            <dt class="col-sm-5"><ui:text key="screen.dfa94d35a251a95c"/></dt>
                            <dd class="col-sm-7">
                                <fmt:formatNumber value="${summary.payable}"/>
                                VND
                            </dd>

                            <dt class="col-sm-5"><ui:text key="screen.1533d367e933e4c2"/></dt>
                            <dd class="col-sm-7">
                                <fmt:formatNumber value="${summary.paid}"/>
                                VND
                            </dd>

                            <dt class="col-sm-5"><ui:text key="screen.e805adf0ab02e3cb"/></dt>
                            <dd class="col-sm-7 fw-bold">
                                <fmt:formatNumber value="${summary.remaining}"/>
                                VND
                            </dd>
                        </dl>

                        <c:if test="${not empty summary.promotionError}">
                            <div class="alert alert-warning">
                                <c:out value="${summary.promotionError}"/>
                                <ui:text key="screen.77f33d7c53495490"/>
                            </div>
                        </c:if>

                        <c:choose>
                            <c:when test="${summary.canPay}">
                                <form action="${pageContext.request.contextPath}/counter-payment"
                                      method="post">

                                    <input type="hidden"
                                           name="csrfToken"
                                           value="${sessionScope.csrfToken}">

                                    <input type="hidden"
                                           name="paymentKey"
                                           value="${paymentKey}">

                                    <div class="row g-3">
                                        <div class="col-md-6">
                                            <label for="method"
                                                   class="form-label">
                                                <ui:text key="screen.4da75359eda269d9"/>
                                            </label>

                                            <select class="form-select"
                                                    name="method"
                                                    id="method">
                                                <option value="cash" data-i18n="screen.e2da108f63008e44">
                                                    ${requestScope.i18n['screen.e2da108f63008e44']}
                                                </option>
                                                <option value="bank_transfer" data-i18n="screen.06be7dfc4d661339">
                                                    ${requestScope.i18n['screen.06be7dfc4d661339']}
                                                </option>
                                            </select>
                                        </div>

                                        <div class="col-md-6">
                                            <label for="percent"
                                                   class="form-label">
                                                <ui:text key="screen.11cffcafb9f952f8"/>
                                            </label>

                                            <select class="form-select"
                                                    name="percent"
                                                    id="percent">

                                                <c:if test="${summary.firstPayment
                                                              and summary.depositPercent < 100}">
                                                      <option value="${summary.depositPercent}" data-i18n="screen.0da00b600a2df498">
                                                          ${requestScope.i18n['screen.0da00b600a2df498']}
                                                          ${summary.depositPercent}${requestScope.i18n['screen.9c779a155ed57864']}
                                                      </option>
                                                </c:if>

                                                <option value="100" selected data-i18n="screen.eab6371cd10e3590">
                                                    ${requestScope.i18n['screen.eab6371cd10e3590']}
                                                </option>
                                            </select>
                                        </div>
                                    </div>

                                    <p class="text-muted small mt-3">
                                        <ui:text key="screen.bff9d6128aa12d8e"/>
                                    </p>

                                    <button class="btn btn-primary"
                                            type="submit">
                                        <ui:text key="screen.3e09dddeb9226b5f"/>
                                    </button>
                                </form>
                            </c:when>

                            <c:otherwise>
                                <p class="alert alert-info">
                                    <ui:text key="screen.7ab8d3a0c636246f"/>
                                </p>
                            </c:otherwise>
                        </c:choose>

                        <c:if test="${summary.canChange}">
                            <hr>

                            <form action="${pageContext.request.contextPath}/counter-payment"
                                  method="post"
                                  class="mb-3">

                                <input type="hidden"
                                       name="csrfToken"
                                       value="${sessionScope.csrfToken}">

                                <input type="hidden"
                                       name="paymentKey"
                                       value="${paymentKey}">

                                <label for="promotionCode" class="form-label">
                                    <ui:text key="screen.437c912b3cd38038"/>
                                </label>

                                <div class="input-group">
                                    <input class="form-control"
                                           name="promotionCode"
                                           id="promotionCode"
                                           maxlength="50"
                                           value="<c:out value='${summary.promotionCode}'/>">

                                    <button class="btn btn-outline-secondary"
                                            name="action"
                                            value="promotion">
                                        <ui:text key="screen.071b975857775907"/>
                                    </button>
                                </div>
                            </form>

                            <form action="${pageContext.request.contextPath}/counter-payment"
                                  method="post">

                                <input type="hidden"
                                       name="csrfToken"
                                       value="${sessionScope.csrfToken}">

                                <input type="hidden"
                                       name="paymentKey"
                                       value="${paymentKey}">

                                <button class="btn btn-outline-danger"
                                        name="action"
                                        value="cancel"
                                        onclick="return confirm('Hủy đặt phòng chưa thanh toán này?')">
                                    <ui:text key="screen.dad77baab3bc168a"/>
                                </button>
                            </form>

                            <p class="small text-muted mt-2">
                                <ui:text key="screen.026cacc1ee0aac84"/>
                            </p>
                        </c:if>
                    </div>
                </section>

                <div class="d-flex gap-2 mt-4">
                    <a class="btn btn-outline-secondary"
                       href="${pageContext.request.contextPath}/booking-management?view=detail&amp;id=${summary.bookingId}">
                        ${summary.canChange
                          ? 'Để thanh toán sau / xem chi tiết'
                          : requestScope.i18n['screen.a55a969205588064']}
                    </a>

                    <a class="btn btn-outline-primary"
                       href="${pageContext.request.contextPath}/offline-booking">
                        <ui:text key="screen.0187afebeb67c5a6"/>
                    </a>
                </div>
            </main>

            <%@include file="/WEB-INF/include/footer.jsp"%>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>