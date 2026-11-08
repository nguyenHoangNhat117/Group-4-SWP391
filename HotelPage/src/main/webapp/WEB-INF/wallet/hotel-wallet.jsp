
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

    <title>Hotel Wallet - Luxe Escape</title>

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

        .hotel-wallet-container {
            max-width: 1250px;
            margin: 50px auto;
            padding: 20px;
        }

        .hotel-wallet-title {
            font-family: Georgia, serif;
            font-size: 38px;
            margin-bottom: 10px;
        }

        .wallet-summary {
            display: grid;
            grid-template-columns:
                repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
            margin: 30px 0;
        }

        .wallet-stat {
            background: #fff;
            border: 1px solid #e8dfd4;
            border-radius: 16px;
            padding: 25px;
            box-shadow: 0 4px 15px rgba(0,0,0,.03);
        }

        .wallet-stat h4 {
            font-size: 14px;
            color: #8b755e;
            margin-bottom: 15px;
        }

        .wallet-stat .amount {
            font-size: 25px;
            font-weight: bold;
            color: #32291f;
        }

        .wallet-panel {
            background: white;
            padding: 25px;
            border: 1px solid #e8dfd4;
            border-radius: 16px;
            margin-top: 25px;
        }

        .wallet-table th {
            background: #f4eee6;
            color: #745e47;
            padding: 15px;
        }

        .wallet-table td {
            padding: 15px;
            vertical-align: middle;
        }

        .wallet-link {
            background: #b49a78;
            color: white;
            padding: 10px 18px;
            border-radius: 8px;
            text-decoration: none;
            display: inline-block;
        }

        .wallet-link:hover {
            background: #947955;
            color: white;
        }
    </style>
</head>

<body>

<jsp:include page="/WEB-INF/include/header.jsp"/>

<main class="hotel-wallet-container">

    <h1 class="hotel-wallet-title">Hotel Wallet</h1>

    <p class="text-muted">
        Financial overview and customer payment records.
    </p>

    <div class="wallet-summary">

        <div class="wallet-stat">
            <h4>Total Recorded Payments</h4>
            <div class="amount">
                <fmt:formatNumber
                    value="${totalPayments}"
                    maxFractionDigits="2"/>
                VND
            </div>
        </div>

        <div class="wallet-stat">
            <h4>Total Refunds</h4>
            <div class="amount">
                <fmt:formatNumber
                    value="${totalRefunds}"
                    maxFractionDigits="2"/>
                VND
            </div>
        </div>

        <div class="wallet-stat">
            <h4>Active Booking Payments</h4>
            <div class="amount">
                <fmt:formatNumber
                    value="${activePayments}"
                    maxFractionDigits="2"/>
                VND
            </div>
        </div>

        <div class="wallet-stat">
            <h4>Customer Wallet Balances</h4>
            <div class="amount">
                <fmt:formatNumber
                    value="${customerBalances}"
                    maxFractionDigits="2"/>
                VND
            </div>
        </div>

    </div>

    <div class="wallet-panel">

        <h3 class="mb-4">Recent Payment Transactions</h3>

        <div class="table-responsive">

            <table class="table wallet-table">

                <thead>
                    <tr>
                        <th>Payment ID</th>
                        <th>Booking ID</th>
                        <th>Amount</th>
                        <th>Method</th>
                        <th>Payment Status</th>
                        <th>Booking Status</th>
                        <th>Payment Date</th>
                    </tr>
                </thead>

                <tbody>

                    <c:forEach var="p" items="${payments}">

                        <tr>
                            <td>
                                <c:out value="${p.paymentId}"/>
                            </td>

                            <td>
                                #<c:out value="${p.bookingId}"/>
                            </td>

                            <td>
                                <fmt:formatNumber
                                    value="${p.amount}"
                                    maxFractionDigits="2"/>
                                VND
                            </td>

                            <td>
                                <c:out value="${p.method}"/>
                            </td>

                            <td>
                                <c:out value="${p.status}"/>
                            </td>

                            <td>
                                <c:out value="${p.bookingStatus}"/>
                            </td>

                            <td>
                                <c:out value="${p.date}"/>
                            </td>
                        </tr>

                    </c:forEach>

                    <c:if test="${empty payments}">
                        <tr>
                            <td colspan="7"
                                class="text-center">
                                No payment records found.
                            </td>
                        </tr>
                    </c:if>

                </tbody>
            </table>
        </div>
    </div>

    <div class="wallet-panel">

        <h3>Customer Wallet Management</h3>

        <p class="text-muted">
            View customer wallet balances and
            refund transaction history.
        </p>

        <a class="wallet-link"
           href="${pageContext.request.contextPath}/wallet-management">
            View Customer Wallets
        </a>

    </div>

</main>

</body>
</html>
