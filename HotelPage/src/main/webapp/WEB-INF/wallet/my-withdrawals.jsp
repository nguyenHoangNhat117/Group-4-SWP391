
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

    <title>My Withdrawal Requests - Luxe Escape</title>

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

        .page-container {
            max-width: 1200px;
            margin: 50px auto;
            padding: 20px;
        }

        .page-title {
            font-family: Georgia, serif;
            font-size: 40px;
            margin-bottom: 20px;
        }

        .wallet-card {
            background: white;
            border: 1px solid #e7ded1;
            border-radius: 18px;
            padding: 25px;
        }

        .wallet-table th {
            background: #f6f0e8;
            color: #79644f;
        }

        .wallet-table td,
        .wallet-table th {
            padding: 14px;
            vertical-align: middle;
        }

        .wallet-btn {
            background: #b49a78;
            border: none;
            color: white;
            padding: 10px 17px;
            border-radius: 8px;
            text-decoration: none;
        }

        .wallet-btn:hover {
            background: #967c5b;
            color: white;
        }
    </style>
</head>

<body>

    <jsp:include page="/WEB-INF/include/header.jsp"/>

    <main class="page-container">

        <h1 class="page-title">
            My Withdrawal Requests
        </h1>

        <p class="text-muted">
            Track your withdrawal requests
            and their current status.
        </p>

        <div class="mb-4">
            <a href="${pageContext.request.contextPath}/withdrawal"
               class="wallet-btn">
                New Withdrawal Request
            </a>

            <a href="${pageContext.request.contextPath}/wallet"
               class="btn btn-outline-secondary ms-2">
                Back to Wallet
            </a>
        </div>

        <section class="wallet-card">

            <h3 class="mb-4">
                Withdrawal History
            </h3>

            <div class="table-responsive">
                <table class="table wallet-table">

                    <thead>
                        <tr>
                            <th>Request ID</th>
                            <th>Amount</th>
                            <th>Bank</th>
                            <th>Submitted</th>
                            <th>Status</th>
                            <th>Details</th>
                        </tr>
                    </thead>

                    <tbody>

                        <c:forEach var="w" items="${withdrawals}">
                            <tr>
                                <td>
                                    #<c:out value="${w.withdrawalId}"/>
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

                                        <c:when test="${w.status == 'paid'}">
                                            <span class="badge bg-success">
                                                Paid
                                            </span>
                                        </c:when>

                                        <c:when test="${w.status == 'rejected'}">
                                            <span class="badge bg-danger">
                                                Rejected
                                            </span>
                                        </c:when>

                                        <c:otherwise>
                                            <c:out value="${w.status}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    <c:if test="${w.status == 'rejected'
                                                and not empty w.rejectReason}">
                                        <div class="small text-danger">
                                            Reason:
                                            <c:out value="${w.rejectReason}"/>
                                        </div>
                                    </c:if>

                                    <c:if test="${w.status == 'approved'}">
                                        <span class="small text-muted">
                                            Awaiting payout confirmation
                                        </span>
                                    </c:if>

                                    <c:if test="${w.status == 'paid'}">
                                        <span class="small text-success">
                                            Payout recorded
                                        </span>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty withdrawals}">
                            <tr>
                                <td colspan="6"
                                    class="text-center text-muted py-5">
                                    No withdrawal requests found.
                                </td>
                            </tr>
                        </c:if>

                    </tbody>
                </table>
            </div>
        </section>

    </main>

    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
