
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

        <title>Wallet Management - Luxe Escape</title>

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

            .wallet-container {
                max-width: 1200px;
                margin: 50px auto;
                padding: 20px;
            }

            .wallet-title {
                font-family: Georgia, serif;
                font-size: 38px;
                margin-bottom: 25px;
            }

            .wallet-card {
                background: white;
                border: 1px solid #e7ded1;
                border-radius: 16px;
                padding: 25px;
                margin-bottom: 25px;
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
                padding: 8px 15px;
                border-radius: 8px;
                text-decoration: none;
                display: inline-block;
            }

            .wallet-btn:hover {
                background: #967c5b;
                color: white;
            }
        </style>
    </head>

    <body>

        <jsp:include page="/WEB-INF/include/header.jsp"/>

        <main class="wallet-container">

            <h1 class="wallet-title">Wallet Management</h1>

            <p class="text-muted">
                View customer balances and refund transactions.
            </p>

            <section class="wallet-card">

                <h3>Customer Wallets</h3>

                <div class="table-responsive">
                    <table class="table wallet-table">

                        <thead>
                            <tr>
                                <th>Customer ID</th>
                                <th>Customer</th>
                                <th>Email</th>
                                <th>Balance</th>
                                <th>Action</th>
                            </tr>
                        </thead>

                        <tbody>

                            <c:forEach var="w" items="${wallets}">
                                <tr>
                                    <td>
                                        <c:out value="${w.customerId}"/>
                                    </td>

                                    <td>
                                        <c:out value="${w.name}"/>
                                    </td>

                                    <td>
                                        <c:out value="${w.email}"/>
                                    </td>

                                    <td>
                                        <fmt:formatNumber
                                            value="${w.balance}"
                                            type="number"
                                            maxFractionDigits="2"/>
                                        VND
                                    </td>

                                    <td>


                                        <a class="wallet-btn"
                                           href="${pageContext.request.contextPath}/wallet-management?customerId=${w.customerId}#transactions">
                                            View Transactions
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>

                            <c:if test="${empty wallets}">
                                <tr>
                                    <td colspan="5"
                                        class="text-center">
                                        No customers found.
                                    </td>
                                </tr>
                            </c:if>

                        </tbody>
                    </table>
                </div>

            </section>

            <c:if test="${not empty selectedCustomerId}">

                <section class="wallet-card" id="transactions">

                    <h3>
                        Transactions - Customer
                        #<c:out value="${selectedCustomerId}"/>
                    </h3>

                    <div class="table-responsive">
                        <table class="table wallet-table">

                            <thead>
                                <tr>
                                    <th>Transaction ID</th>
                                    <th>Booking ID</th>
                                    <th>Type</th>
                                    <th>Amount</th>
                                    <th>Date</th>
                                </tr>
                            </thead>

                            <tbody>
                                <c:forEach var="t"
                                           items="${transactions}">

                                    <tr>
                                        <td>
                                            <c:out value="${t.transactionId}"/>
                                        </td>

                                        <td>
                                            <c:out value="${t.bookingId}"/>
                                        </td>

                                        <td>
                                            <c:out value="${t.type}"/>
                                        </td>

                                        <td>
                                            <fmt:formatNumber
                                                value="${t.amount}"
                                                type="number"
                                                maxFractionDigits="2"/>
                                            VND
                                        </td>

                                        <td>
                                            <c:out value="${t.createdAt}"/>
                                        </td>
                                    </tr>

                                </c:forEach>

                                <c:if test="${empty transactions}">
                                    <tr>
                                        <td colspan="5"
                                            class="text-center">
                                            No transactions found.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>

                        </table>
                    </div>

                </section>

            </c:if>

        </main>

        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>

    </body>
</html>
