
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<c:set var="withdrawalDateFormat" value='<%= DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm") %>'/>

<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1">

        <title data-i18n="management.withdrawals">${requestScope.i18n['management.withdrawals']}</title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/general.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-modern.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/management.css">
        <%@ include file="/WEB-INF/include/date-picker.jsp" %>

        <style>
            .management-card {
                background: white;
                border: 1px solid #e3e9e4;
                border-radius: 18px;
                box-shadow: 0 10px 24px #18392f0d;
            }

            .management-table th {
                background: #edf3ef;
                color: #345749;
                white-space: nowrap;
            }

            .management-table td,
            .management-table th {
                padding: 14px;
                vertical-align: middle;
            }

            .withdrawal-date {
                white-space: nowrap;
                font-variant-numeric: tabular-nums;
            }

            .withdrawal-actions {
                min-width: 15rem;
            }
        </style>
    </head>

    <body class="management-page bg-light">

        <jsp:include page="/WEB-INF/include/header.jsp"/>

        <main class="container management-container py-4 py-lg-5">

            <div class="management-hero">
                <div>
                    <h1><ui:text key="management.withdrawals"/></h1>
                    <p><ui:text key="management.withdrawalsDescription"/></p>
                </div>
                <a class="btn btn-light" href="${pageContext.request.contextPath}/hotel-wallet">
                    <ui:text key="management.wallet"/>
                </a>
            </div>

            <c:if test="${param.result == 'success'}">
                <div class="alert alert-success">
                    Withdrawal request updated successfully.
                </div>
            </c:if>

            <c:if test="${param.result == 'not-processed'}">
                <div class="alert alert-warning">
                    Request could not be processed.
                    Check its status and your permissions.
                </div>
            </c:if>

                <form action="${pageContext.request.contextPath}/withdrawal-management"
                      method="get"
                      class="management-filters">

                    <div>
                        <label for="withdrawal-status" class="form-label">
                            Filter by Status
                        </label>

                        <select id="withdrawal-status" name="status"
                                class="form-select">

                            <option value=""
                                    ${empty selectedStatus ? 'selected' : ''}>
                                All Requests
                            </option>

                            <option value="pending"
                                    ${selectedStatus == 'pending' ? 'selected' : ''}>
                                Pending
                            </option>

                            <option value="approved"
                                    ${selectedStatus == 'approved' ? 'selected' : ''}>
                                Approved
                            </option>

                            <option value="rejected"
                                    ${selectedStatus == 'rejected' ? 'selected' : ''}>
                                Rejected
                            </option>

                            <option value="paid"
                                    ${selectedStatus == 'paid' ? 'selected' : ''}>
                                Paid
                            </option>
                        </select>
                    </div>

                    <div>
                        <label for="withdrawal-date"><ui:text key="management.withdrawalDate"/></label>
                        <input id="withdrawal-date" class="form-control" name="date" type="date" value="${selectedDate}">
                    </div>
                    <div>
                        <label for="withdrawal-keyword"><ui:text key="management.withdrawalAccount"/></label>
                        <input id="withdrawal-keyword" class="form-control" name="q" type="search" maxlength="150"
                               value="<c:out value='${keyword}'/>">
                    </div>
                    <div>
                        <label for="withdrawal-bank"><ui:text key="management.withdrawalBank"/></label>
                        <input id="withdrawal-bank" class="form-control" name="bank" type="search" maxlength="100"
                               value="<c:out value='${bank}'/>">
                    </div>

                        <button type="submit" class="btn btn-success">
                            Apply Filter
                        </button>
                        <a href="${pageContext.request.contextPath}/withdrawal-management"
                           class="btn btn-outline-secondary">
                            Clear
                        </a>
                </form>

            <section class="management-card overflow-hidden">
                <div class="table-responsive">

                    <table class="table table-hover management-table mb-0">

                        <thead>
                            <tr>
                                <th>Request ID</th>
                                <th>Customer ID</th>
                                <th>Amount</th>
                                <th>Bank</th>
                                <th>Submitted</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>

                        <tbody>

                            <c:forEach var="w" items="${requests}">
                                <tr>

                                    <td>
                                        #<c:out value="${w.withdrawalId}"/>
                                    </td>

                                    <td>
                                        #<c:out value="${w.customerId}"/>
                                    </td>

                                    <td class="fw-bold">
                                        <fmt:formatNumber
                                            value="${w.amount}"
                                            type="number"
                                            maxFractionDigits="0"/>
                                        VND
                                    </td>

                                    <td>
                                        <strong class="d-block"><c:out value="${w.bankName}"/></strong>
                                        <span class="small text-muted"><c:out value="${w.accountHolder}"/></span>
                                        <c:if test="${fn:length(w.accountNumber) gt 4}">
                                            <span class="small text-muted text-nowrap">· ••••<c:out value="${fn:substring(w.accountNumber, fn:length(w.accountNumber) - 4, fn:length(w.accountNumber))}"/></span>
                                        </c:if>
                                    </td>

                                    <td class="withdrawal-date">
                                        <c:choose>
                                            <c:when test="${not empty w.createdAt}">
                                                <c:set var="submitted" value="${w.createdAt.atOffset(utcOffset).atZoneSameInstant(hotelZone)}"/>
                                                <time datetime="${submitted.toOffsetDateTime()}">${submitted.format(withdrawalDateFormat)}</time>
                                            </c:when>
                                            <c:otherwise>—</c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>
                                        <c:choose>

                                            <c:when test="${w.status == 'pending'}">
                                                <span class="badge bg-warning text-dark">
                                                    Pending
                                                </span>
                                            </c:when>

                                            <c:when test="${w.status == 'approved'}">
                                                <span class="badge bg-info text-dark">
                                                    Approved
                                                </span>
                                            </c:when>

                                            <c:when test="${w.status == 'rejected'}">
                                                <span class="badge bg-danger">
                                                    Rejected
                                                </span>
                                            </c:when>

                                            <c:when test="${w.status == 'paid'}">
                                                <span class="badge bg-success">
                                                    Paid
                                                </span>
                                            </c:when>

                                            <c:otherwise>
                                                <c:out value="${w.status}"/>
                                            </c:otherwise>

                                        </c:choose>
                                    </td>

                                    <td class="withdrawal-actions">
                                        <c:if test="${w.status == 'pending'}">

                                            <div class="d-flex flex-column gap-2">

                                                <form action="${pageContext.request.contextPath}/withdrawal-action"
                                                      method="post">

                                                    <input type="hidden"
                                                           name="csrfToken"
                                                           value="${sessionScope.withdrawalActionCsrfToken}">

                                                    <input type="hidden"
                                                           name="withdrawalId"
                                                           value="${w.withdrawalId}">


                                                    <input type="hidden"
                                                           name="action"
                                                           value="approve">

                                                    <button type="submit"
                                                            class="btn btn-success btn-sm">
                                                        Approve
                                                    </button>

                                                </form>

                                                <form action="${pageContext.request.contextPath}/withdrawal-action"
                                                      method="post">

                                                    <input type="hidden"
                                                           name="csrfToken"
                                                           value="${sessionScope.withdrawalActionCsrfToken}">

                                                    <input type="hidden"
                                                           name="withdrawalId"
                                                           value="${w.withdrawalId}">

                                                    <input type="hidden"
                                                           name="action"
                                                           value="reject">

                                                    <input type="text"
                                                           name="rejectReason"
                                                           class="form-control form-control-sm mb-2"
                                                           placeholder="Reason for rejection"
                                                           aria-label="Reason for rejection"
                                                           maxlength="500"
                                                           required>

                                                    <button type="submit"
                                                            class="btn btn-outline-danger btn-sm">
                                                        Reject
                                                    </button>
                                                </form>

                                            </div>
                                        </c:if>

                                        <c:if test="${w.status == 'approved'}">

                                              <form action="${pageContext.request.contextPath}/withdrawal-action"
                                                    method="post">

                                                  <input type="hidden"
                                                         name="csrfToken"
                                                         value="${sessionScope.withdrawalActionCsrfToken}">

                                                  <input type="hidden"
                                                         name="withdrawalId"
                                                         value="${w.withdrawalId}">

                                                  <input type="hidden"
                                                         name="action"
                                                         value="markPaid">

                                                  <label for="payout-reference-${w.withdrawalId}" class="form-label small">
                                                      Verified Payout Reference
                                                  </label>

                                                  <input type="text"
                                                         id="payout-reference-${w.withdrawalId}" name="paymentReference"
                                                         class="form-control form-control-sm mb-2"
                                                         maxlength="100"
                                                         required>

                                                  <label class="form-check mb-2">
                                                      <input type="checkbox"
                                                             class="form-check-input"
                                                             name="payoutConfirmed"
                                                             value="yes"
                                                             required>

                                                      <span class="form-check-label">
                                                          I confirm that the payout has been verified.
                                                      </span>
                                                  </label>


                                                  <button type="submit"
                                                          class="btn btn-success btn-sm">
                                                      Record as Paid
                                                  </button>
                                              </form>
                                        </c:if>

                                        <c:if test="${w.status == 'rejected'}">
                                            <span class="small text-danger">
                                                <c:out value="${w.rejectReason}"/>
                                            </span>
                                        </c:if>

                                        <c:if test="${w.status == 'paid'}">
                                            <span class="small text-success">
                                                Payment recorded
                                            </span>
                                        </c:if>
                                    </td>

                                </tr>
                            </c:forEach>

                            <c:if test="${empty requests}">
                                <tr>
                                    <td colspan="7"
                                        class="text-center text-muted py-5">
                                        No withdrawal requests found.
                                    </td>
                                </tr>
                            </c:if>

                        </tbody>
                    </table>
                </div>

                <c:if test="${totalPages > 1}">
                    <nav class="d-flex justify-content-center
                         align-items-center flex-wrap gap-3 p-3 border-top"
                         aria-label="Withdrawal pages">

                        <c:choose>
                            <c:when test="${currentPage > 1}">
                                <c:url var="previousUrl"
                                       value="/withdrawal-management">
                                    <c:param name="status"
                                             value="${selectedStatus}"/>
                                    <c:param name="date" value="${selectedDate}"/>
                                    <c:param name="q" value="${keyword}"/>
                                    <c:param name="bank" value="${bank}"/>
                                    <c:param name="page"
                                             value="${currentPage - 1}"/>
                                </c:url>

                                <a href="${previousUrl}"
                                   class="btn btn-outline-secondary">
                                    Previous
                                </a>
                            </c:when>

                            <c:otherwise>
                                <button type="button"
                                        class="btn btn-outline-secondary"
                                        disabled>
                                    Previous
                                </button>
                            </c:otherwise>
                        </c:choose>

                        <span class="small text-muted">
                            Page ${currentPage} of ${totalPages}
                            (${totalRequests} requests)
                        </span>

                        <c:choose>
                            <c:when test="${currentPage < totalPages}">
                                <c:url var="nextUrl"
                                       value="/withdrawal-management">
                                    <c:param name="status"
                                             value="${selectedStatus}"/>
                                    <c:param name="date" value="${selectedDate}"/>
                                    <c:param name="q" value="${keyword}"/>
                                    <c:param name="bank" value="${bank}"/>
                                    <c:param name="page"
                                             value="${currentPage + 1}"/>
                                </c:url>

                                <a href="${nextUrl}"
                                   class="btn btn-outline-secondary">
                                    Next
                                </a>
                            </c:when>

                            <c:otherwise>
                                <button type="button"
                                        class="btn btn-outline-secondary"
                                        disabled>
                                    Next
                                </button>
                            </c:otherwise>
                        </c:choose>

                    </nav>
                </c:if>

            </section>
        </main>

        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
