<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Delete Room Type</title>

        <link href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css"
              rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/general.css"
              rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
</head>

    <body class="bg-light">
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp" %>

            <main class="min-vh-50 d-flex justify-content-center align-items-center">
                <div class="bg-white shadow rounded-4 p-5 w-100"
                     style="max-width: 600px;">

                    <form action="${pageContext.request.contextPath}/room-type?action=delete"
                          method="post"
                          class="text-center">

                        <h2 class="mb-4 text-dark">
                            Room Type Deletion
                        </h2>

                        <p class="fs-5 fw-semibold mb-4">
                            Do you really want to delete
                            <strong>
                                <c:out value="${roomType.name}"/>
                            </strong>
                            with the ID of
                            <strong>${roomType.id}</strong>?
                        </p>

                        <input type="hidden"
                               name="id"
                               value="${roomType.id}">

                        <div class="d-flex justify-content-center gap-3">
                            <button type="submit"
                                    class="btn btn-danger btn-lg px-5 py-2 rounded-3">
                                Yes
                            </button>

                            <a href="${pageContext.request.contextPath}/room-type"
                               class="btn btn-secondary btn-lg px-5 py-2 rounded-3">
                                No
                            </a>
                        </div>
                    </form>
                </div>
            </main>

            <%@include file="/WEB-INF/include/footer.jsp" %>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
    </body>
</html>