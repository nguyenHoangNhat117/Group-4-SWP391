<%-- 
    Document   : revenue_chart
    Created on : Jul 11, 2025, 1:34:58 PM
    Author     : Trần Nguyễn
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
    <head>
        <title>Revenue Report</title>
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/chartjs-plugin-zoom@2.0.0"></script>

        <link href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/general.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/revenue_chart.css" rel="stylesheet">
    </head>

    <body>
        <%@include file="/WEB-INF/include/header.jsp" %>

        <div class="btn-switch text-center mb-3">
            <a class="btn btn-secondary"
               href="${pageContext.request.contextPath}/reports?view=payments&page-index=1">
                Back
            </a>
            
            <c:choose>
                <c:when test="${view == 'year'}">
                    <a class="btn btn-custom"
                       href="${pageContext.request.contextPath}/reports?view=month&year=${selectedYear}">
                        📅 Revenue by Month (${selectedYear})
                    </a>
                </c:when>

                <c:when test="${view == 'month'}">
                    <a class="btn btn-custom me-2"
                       href="${pageContext.request.contextPath}/reports?view=year">
                        📊 Revenue by Year
                    </a>

                    <c:if test="${not empty availableYears}">
                        <form method="get" action="${pageContext.request.contextPath}/reports" class="d-inline-block ms-2">
                            <input type="hidden" name="view" value="month" />
                            <select name="year" onchange="this.form.submit()" class="form-select d-inline-block w-auto">
                                <c:forEach var="y" items="${availableYears}">
                                    <option value="${y}" <c:if test="${y == selectedYear}">selected</c:if>>${y}</option>
                                </c:forEach>
                            </select>
                        </form>
                    </c:if>
                </c:when>
            </c:choose>
        </div>

        <div class="chart-container">
            <h3 class="text-center mb-4 text-black">
                Revenue by
                <c:choose>
                    <c:when test="${view == 'month'}">Month <small class="text-muted">(Year: ${selectedYear})</small></c:when>
                    <c:otherwise>Year</c:otherwise>
                </c:choose>
            </h3>
            <canvas id="revenueChart"></canvas>
        </div>



        <script>
            const labels = [
            <c:forEach var="item" items="${reportData}" varStatus="loop">
            "${item.timeUnit}"<c:if test="${!loop.last}">,</c:if>
            </c:forEach>
            ];

            const data = [
            <c:forEach var="item" items="${reportData}" varStatus="loop">
                ${item.totalRevenue}<c:if test="${!loop.last}">,</c:if>
            </c:forEach>
            ];

            const ctx = document.getElementById('revenueChart').getContext('2d');

            const revenueChart = new Chart(ctx, {
                type: "bar",
                data: {
                    labels: labels,
                    datasets: [{
                            label: 'Revenue ($)',
                            data: data,
                            backgroundColor: 'rgba(75, 192, 192, 0.2)',
                            borderColor: 'rgba(75, 192, 192, 1)',
                            borderWidth: 2,
                            fill: ${view == "month" ? "true" : "false"},
                            tension: 0.4,
                            pointRadius: 4,
                            pointHoverRadius: 6
                        }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    interaction: {
                        mode: 'index',
                        intersect: false
                    },
                    plugins: {
                        tooltip: {enabled: true},
                        legend: {
                            display: true,
                            position: 'top'
                        },
                        zoom: {
                            zoom: {
                                wheel: {enabled: true},
                                pinch: {enabled: true},
                                mode: 'x'
                            },
                            pan: {
                                enabled: true,
                                mode: 'x'
                            }
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: 'Amount ($)'
                            }
                        },
                        x: {
                            title: {
                                display: true,
                                text: '${view == "month" ? "Month" : "Year"}'
                            }
                        }
                    }
                }
            });
        </script>

        <%@include file="/WEB-INF/include/footer.jsp" %>
        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>