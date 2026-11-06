<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html>
<html lang="${requestScope.uiLanguage}"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
        <title data-i18n="screen.97e8a79942bca1da">${requestScope.i18n['screen.97e8a79942bca1da']}</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/general.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/luxe-modern.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/luxe-ui.css">
        <style>
            .wallet-page{
                background:#faf7f2;
                color:#32291f;
                min-height:100vh
            }
            .wallet-wrap{
                max-width:1160px;
                margin:auto;
                padding:56px 22px 85px
            }
            .wallet-eyebrow{
                font-size:12px;
                letter-spacing:.30em;
                text-transform:uppercase;
                color:#987d5b;
                font-weight:700
            }
            .wallet-heading{
                font-family:Georgia,serif;
                font-size:clamp(38px,5vw,64px);
                margin:12px 0 28px
            }
            .wallet-balance{
                background:linear-gradient(115deg,#31271f,#64503d);
                border-radius:22px;
                padding:38px;
                color:#fff;
                box-shadow:0 18px 38px #2e241f26
            }
            .wallet-balance .amount{
                font-family:Georgia,serif;
                font-size:clamp(37px,5vw,58px);
                font-weight:600
            }
            .wallet-balance p{
                color:#e8ded2
            }
            .wallet-panel{
                background:white;
                border:1px solid #e7ded1;
                border-radius:20px;
                margin-top:30px;
                overflow:hidden
            }
            .wallet-panel-head{
                padding:27px 28px 13px
            }
            .wallet-panel h2{
                font-family:Georgia,serif;
                font-size:27px
            }
            .wallet-table{
                margin-bottom:0
            }
            .wallet-table th{
                background:#f6f0e8;
                color:#79644f;
                font-size:12px;
                letter-spacing:.09em;
                text-transform:uppercase;
                padding:15px 24px
            }
            .wallet-table td{
                padding:20px 24px;
                color:#493d30;
                vertical-align:middle
            }
            .wallet-pill{
                background:#e7f4eb;
                color:#24764b;
                border-radius:99px;
                padding:5px 12px;
                font-size:12px;
                font-weight:700
            }
            .wallet-empty{
                text-align:center;
                padding:45px 20px;
                color:#8b7d6f
            }
            .wallet-links a{
                color:#7c6040;
                text-decoration:none
            }
            .wallet-links a:hover{
                text-decoration:underline
            }
            @media(max-width:650px){
                .wallet-wrap{
                    padding:38px 14px
                }
                .wallet-balance{
                    padding:26px
                }
                .wallet-table th,.wallet-table td{
                    padding:12px 9px;
                    font-size:12px
                }
            }
        </style>
    </head>
    <body class="wallet-page">
        <%@ include file="/WEB-INF/include/header.jsp" %>
        <main class="wallet-wrap">
            <div class="wallet-eyebrow"><ui:text key="screen.281a98aef303211d"/></div>
            <h1 class="wallet-heading"><ui:text key="screen.46d8ad210d354832"/></h1>
            <section class="wallet-balance">
                <div class="small text-uppercase" style="letter-spacing:.14em;color:#e8d8c7"><ui:text key="screen.3ab7dd8428d13314"/></div>
                <div class="amount mt-2"><fmt:formatNumber value="${balance}" type="number" maxFractionDigits="2"/> 
                    <span style="font-size:22px">VND</span>
                </div>
                <p class="mb-0 mt-2"><ui:text key="screen.63d4ea1d3f0d6393"/></p>
            </section>
            <div class="wallet-links mt-4">
                <a href="${pageContext.request.contextPath}/history?view=bookings"><ui:text key="screen.49464e4e28596c65"/></a>
            </div>
            <section class="wallet-panel">
                <div class="wallet-panel-head">
                    <h2><ui:text key="screen.de7c340f640fdc51"/></h2>
                    <p class="text-muted mb-0"><ui:text key="screen.979374c44fb4d98e"/></p>
                </div>
                <div class="table-responsive">
                    <table class="table wallet-table">
                        <thead><tr><th><ui:text key="screen.99c40ab405926cb5"/></th>
                                <th><ui:text key="screen.948e03e7054b919e"/></th>
                                <th><ui:text key="screen.baaddf70fb5d432b"/></th>
                                <th class="text-end"><ui:text key="screen.00b324ad3f410fba"/></th>
                            </tr></thead><tbody>
                            <c:forEach var="t" items="${transactions}">
                                <tr>
                                    <td><c:out value="${fn:substring(t.createdAt, 8, 10)}/${fn:substring(t.createdAt, 5, 7)}/${fn:substring(t.createdAt, 0, 4)}"/>
                                    </td>
                                    <td>
                                        #<c:out value="${t.bookingId}"/>
                                    </td>
                                    <td>
                                        <span class="wallet-pill">
                                            <c:out value="${t.type}"/>
                                        </span>
                                    </td>
                                    <td class="text-end fw-bold text-success">+ <fmt:formatNumber value="${t.amount}" type="number"/>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty transactions}"><tr>
                                    <td colspan="4">
                                        <div class="wallet-empty"><ui:text key="screen.8d82da8a8069d3fb"/>
                                            <div class="mt-2 small"><ui:text key="screen.4037f14c6eb72ecb"/>
                                            </div>

                                        </div>
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </section>
        </main>
        <script 
            src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js">
        </script>

    </body>
</html>
