
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1">

        <title>Withdrawal Management - Luxe Escape</title>

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

        <style>
            body {
                background: #faf7f2;
                color: #32291f;
            }

            .management-container {
                max-width: 1300px;
                margin: 50px auto;
                padding: 20px;
            }

            .management-title {
                font-family: Georgia, serif;
                font-size: 40px;
                margin-bottom: 20px;
            }

            .management-card {
                background: white;
                border: 1px solid #e7ded1;
                border-radius: 18px;
                padding: 25px;
                margin-bottom: 25px;
            }

            .management-table th {
                background: #f6f0e8;
                color: #79644f;
                white-space: nowrap;
            }

            .management-table td,
            .management-table th {
                padding: 14px;
                vertical-align: middle;
            }

            .approve-btn {
                background: #b49a78;
                border: none;
                color: white;
                border-radius: 8px;
                padding: 7px 14px;
            }

            .approve-btn:hover {
                background: #967c5b;
                color: white;
            }
        </style>
    </head>

    <body>

        <jsp:include page="/WEB-INF/include/header.jsp"/>

        <main class="management-container">

            <h1 class="management-title">
                Withdrawal Management
            </h1>

            <p class="text-muted">
                Review and manage customer withdrawal requests.
            </p>

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

            <section class="management-card">

                <form action="${pageContext.request.contextPath}/withdrawal-management"
                      method="get"
                      class="row g-3 align-items-end mb-4">

                    <div class="col-md-4">
                        <label class="form-label">
                            Filter by Status
                        </label>

                        <select name="status"
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

                    <div class="col-md-auto">
                        <button type="submit"
                                class="approve-btn">
                            Apply Filter
                        </button>
                    </div>

                    <div class="col-md-auto">
                        <a href="${pageContext.request.contextPath}/withdrawal-management"
                           class="btn btn-outline-secondary">
                            Clear
                        </a>
                    </div>
                </form>

                <div class="table-responsive">

                    <table class="table management-table">

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
                                        <c:out value="${w.bankName}"/>
                                    </td>

                                    <td>
                                        <c:out value="${w.createdAt}"/>
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

                                    <td>
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
                                                            class="approve-btn">
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
                                                           maxlength="500"
                                                           required>

                                                    <button type="submit"
                                                            class="btn btn-outline-danger btn-sm">
                                                        Reject
                                                    </button>
                                                </form>

                                            </div>
                                        </c:if>

                                        <c:if test="${w.status == 'approved'
                                                      and (sessionScope.loggedUser.role == 'admin'
                                                      or sessionScope.loggedUser.role == 'manager')}">

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

                                                  <label class="form-label small">
                                                      Verified Payout Reference
                                                  </label>

                                                  <input type="text"
                                                         name="paymentReference"
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
                         align-items-center gap-3 mt-4"
                         aria-label="Withdrawal pages">

                        <c:choose>
                            <c:when test="${currentPage > 1}">
                                <c:url var="previousUrl"
                                       value="/withdrawal-management">
                                    <c:param name="status"
                                             value="${selectedStatus}"/>
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
