<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<c:set var="editing" value="${not empty roomType}"/>
<c:set var="titleKey" value="${editing ? 'screen.dc3f08be28714b12' : 'screen.4e5d70d7ca7b5ab9'}"/>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><c:out value="${requestScope.i18n[titleKey]}"/></title>
        <link href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/general.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-admin-suite.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-ui.css" rel="stylesheet">
        <link href="<c:url value='/assets/css/admin-room-types.css?v=7'/>" rel="stylesheet">
    </head>
    <body class="luxe-admin-suite luxe-admin-types">
        <div class="body-wrapper">
            <%@include file="/WEB-INF/include/header.jsp" %>

            <main class="room-types-page">
                <c:url var="listUrl" value="/room-type">
                    <c:if test="${not empty param['page-index']}">
                        <c:param name="page-index" value="${param['page-index']}"/>
                    </c:if>
                </c:url>
                <c:url var="saveUrl" value="/room-type">
                    <c:param name="action" value="${editing ? 'update' : 'create'}"/>
                </c:url>
                <c:if test="${editing}">
                    <c:url var="pictureUrl" value="/assets/img/${empty roomType.picture ? 'avatar.jpg' : roomType.picture}"/>
                </c:if>

                <div class="room-types-edit-heading">
                    <a class="room-types-back" href="<c:out value='${listUrl}'/>">
                        <span aria-hidden="true">&larr;</span>
                        <ui:text key="room.type.back"/>
                    </a>
                    <h1><ui:text key="${titleKey}"/></h1>
                    <c:if test="${editing}">
                        <p class="room-types-edit-subtitle">
                            <span class="room-types-id">#${roomType.id}</span>
                            <span><c:out value="${roomType.name}"/></span>
                        </p>
                    </c:if>
                </div>

                <c:if test="${not empty roomTypeError}">
                    <p class="alert alert-danger" role="alert"><ui:text key="${roomTypeError}"/></p>
                </c:if>

                <form action="<c:out value='${saveUrl}'/>" method="post" enctype="multipart/form-data">
                    <c:if test="${editing}">
                        <input type="hidden" name="id" value="${roomType.id}">
                    </c:if>
                    <input type="hidden" name="page-index" value="<c:out value="${param['page-index']}"/>">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

                    <div class="room-types-edit-grid">
                        <section class="room-types-panel room-types-form-card room-types-details" aria-labelledby="room-details-title">
                            <h2 id="room-details-title"><ui:text key="screen.6816c8a3445d0f73"/></h2>
                            <div class="room-types-field-grid">
                                <div>
                                    <label for="name" class="form-label"><ui:text key="screen.741b94aab7248c7b"/></label>
                                    <input type="text" class="form-control" id="name" name="name"
                                           value="<c:out value='${formValues.name}'/>" maxlength="50" required>
                                </div>
                                <div>
                                    <label for="description" class="form-label"><ui:text key="screen.526e0087cc3f254d"/></label>
                                    <textarea class="form-control" id="description" name="description"
                                              rows="4" maxlength="500" required><c:out value="${formValues.description}"/></textarea>
                                </div>
                                <div class="room-types-numbers">
                                    <div>
                                        <label for="pricePerNight" class="form-label"><ui:text key="screen.bf193eab4a1aafb0"/> (VND)</label>
                                        <input type="number" class="form-control" id="pricePerNight" name="pricePerNight"
                                               value="<c:out value='${formValues.pricePerNight}'/>" inputmode="numeric" min="0" step="1" required>
                                    </div>
                                    <div>
                                        <label for="beds" class="form-label"><ui:text key="screen.2e2a3cd1a00809d3"/></label>
                                        <input type="number" class="form-control" id="beds" name="beds"
                                               value="<c:out value='${formValues.beds}'/>" inputmode="numeric" min="1" required>
                                    </div>
                                    <div>
                                        <label for="capacity" class="form-label"><ui:text key="room.type.capacity.adults"/></label>
                                        <input type="number" class="form-control" id="capacity" name="capacity"
                                               value="<c:out value='${formValues.capacity}'/>" inputmode="numeric" min="1" required>
                                    </div>
                                    <div>
                                        <label for="maxChildren" class="form-label"><ui:text key="room.type.capacity.children"/></label>
                                        <input type="number" class="form-control" id="maxChildren" name="maxChildren"
                                               value="<c:out value='${formValues.maxChildren}'/>" inputmode="numeric" min="0" required>
                                    </div>
                                </div>
                            </div>
                        </section>

                        <section class="room-types-panel room-types-form-card room-types-photo" aria-labelledby="room-picture-title">
                            <h2 id="room-picture-title"><ui:text key="screen.1f26160dc5f15704"/></h2>
                            <c:if test="${not editing}">
                                <div id="room-picture-placeholder" class="room-types-preview room-types-preview-empty">
                                    <ui:text key="room.type.image.empty"/>
                                </div>
                            </c:if>
                            <img id="room-picture" class="room-types-preview" width="400" height="280"
                                 alt="<c:out value="${requestScope.i18n['room.type.image.preview']}"/>"
                                 <c:choose>
                                     <c:when test="${editing}">src="<c:out value='${pictureUrl}'/>"</c:when>
                                     <c:otherwise>hidden</c:otherwise>
                                 </c:choose>>
                            <label for="picture" class="form-label">
                                <ui:text key="${editing ? 'room.type.image.replace' : 'room.type.image.choose'}"/>
                            </label>
                            <input type="file" name="img" accept=".png,.jpg,.jpeg" class="form-control room-types-file"
                                   id="picture" aria-describedby="picture-help picture-error" ${editing ? '' : 'required'}>
                            <p id="picture-help" class="room-types-help">
                                <ui:text key="${editing ? 'room.type.image.help' : 'room.type.image.createHelp'}"/>
                            </p>
                            <p id="picture-error" class="room-types-file-error" aria-live="polite"
                               data-type-error="<c:out value="${requestScope.i18n['room.type.image.type']}"/>"
                               data-size-error="<c:out value="${requestScope.i18n['room.type.image.size']}"/>"
                               data-invalid-error="<c:out value="${requestScope.i18n['room.type.image.invalid']}"/>"></p>
                            <button type="button" class="btn room-types-secondary room-types-reset" id="picture-reset" hidden>
                                <ui:text key="${editing ? 'room.type.image.reset' : 'room.type.image.clear'}"/>
                            </button>
                            <div class="room-types-form-actions">
                                <a class="btn room-types-secondary" href="<c:out value='${listUrl}'/>"><ui:text key="screen.19766ed6ccb2f4a3"/></a>
                                <button type="submit" class="btn room-types-primary">
                                    <ui:text key="${editing ? 'screen.dd0ae7a5cbcf2339' : 'screen.cf53dfbb45bc51f9'}"/>
                                </button>
                            </div>
                        </section>

                        <div class="room-types-edit-amenities">
                            <%@ include file="amenity-picker.jspf" %>
                        </div>
                    </div>
                </form>
            </main>

            <%@include file="/WEB-INF/include/footer.jsp" %>
        </div>
        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
        <script src="<c:url value='/assets/js/admin-room-type-image.js?v=2'/>"></script>
        <script src="<c:url value='/assets/js/admin-amenity-search.js'/>" defer></script>
    </body>
</html>
