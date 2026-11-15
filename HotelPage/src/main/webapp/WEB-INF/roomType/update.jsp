<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="${requestScope.uiLanguage}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title data-i18n="screen.dc3f08be28714b12">${requestScope.i18n['screen.dc3f08be28714b12']}</title>
        <link href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/general.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-admin-suite.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-shared-shell.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/luxe-ui.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/admin-room-types.css" rel="stylesheet">
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
                    <c:param name="action" value="update"/>
                </c:url>
                <c:url var="pictureUrl" value="/assets/img/${empty roomType.picture ? 'avatar.jpg' : roomType.picture}"/>

                <div class="room-types-edit-heading">
                    <a class="room-types-back" href="<c:out value='${listUrl}'/>">
                        <span aria-hidden="true">&larr;</span>
                        <ui:text key="room.type.back"/>
                    </a>
                    <h1><ui:text key="screen.dc3f08be28714b12"/></h1>
                    <p class="room-types-edit-subtitle">
                        <span class="room-types-id">#${roomType.id}</span>
                        <span><c:out value="${roomType.name}"/></span>
                    </p>
                </div>

                <c:choose>
                    <c:when test="${param.error == 'save-failed'}">
                        <c:set var="editErrorKey" value="room.type.save.failed"/>
                    </c:when>
                    <c:when test="${param.error == 'missing-params'}">
                        <c:set var="editErrorKey" value="screen.9eca9b88d5fc8dff"/>
                    </c:when>
                    <c:when test="${param.error == 'name-exist'}">
                        <c:set var="editErrorKey" value="screen.9dda0b654299ce61"/>
                    </c:when>
                    <c:when test="${param.error == 'id-not-exist'}">
                        <c:set var="editErrorKey" value="screen.3aa1ebdc8bb042dd"/>
                    </c:when>
                    <c:when test="${param.error == 'id-different'}">
                        <c:set var="editErrorKey" value="screen.b9ac473e86fe9c3e"/>
                    </c:when>
                    <c:when test="${param.error == 'number-format'}">
                        <c:set var="editErrorKey" value="screen.1469fc1a6acdc7d0"/>
                    </c:when>
                </c:choose>
                <c:if test="${not empty editErrorKey}">
                    <p class="alert alert-danger" role="alert"><ui:text key="${editErrorKey}"/></p>
                </c:if>

                <form action="<c:out value='${saveUrl}'/>" method="post" enctype="multipart/form-data">
                    <input type="hidden" name="id" value="${roomType.id}">

                    <div class="room-types-edit-grid">
                        <div class="room-types-edit-fields">
                            <section class="room-types-panel room-types-form-card" aria-labelledby="room-details-title">
                                <h2 id="room-details-title"><ui:text key="screen.6816c8a3445d0f73"/></h2>
                                <div class="room-types-field-grid">
                                    <div>
                                        <label for="name" class="form-label"><ui:text key="screen.741b94aab7248c7b"/></label>
                                        <input type="text" class="form-control" id="name" name="name"
                                               value="<c:out value='${roomType.name}'/>" required>
                                    </div>
                                    <div>
                                        <label for="description" class="form-label"><ui:text key="screen.526e0087cc3f254d"/></label>
                                        <textarea class="form-control" id="description" name="description"
                                                  rows="4" required><c:out value="${roomType.description}"/></textarea>
                                    </div>
                                    <div class="room-types-numbers">
                                        <div>
                                            <label for="pricePerNight" class="form-label"><ui:text key="screen.bf193eab4a1aafb0"/> (VND)</label>
                                            <input type="number" class="form-control" id="pricePerNight" name="pricePerNight"
                                                   value="${roomType.pricePerNight}" inputmode="numeric" min="0" step="1" required>
                                        </div>
                                        <div>
                                            <label for="beds" class="form-label"><ui:text key="screen.2e2a3cd1a00809d3"/></label>
                                            <input type="number" class="form-control" id="beds" name="beds"
                                                   value="${roomType.beds}" inputmode="numeric" min="1" required>
                                        </div>
                                        <div>
                                            <label for="capacity" class="form-label"><ui:text key="screen.ae65d096550ff4eb"/></label>
                                            <input type="number" class="form-control" id="capacity" name="capacity"
                                                   value="${roomType.capacity}" inputmode="numeric" min="1" required>
                                        </div>
                                    </div>
                                </div>
                            </section>

                            <fieldset class="room-types-panel room-types-form-card">
                                <legend class="room-types-card-title"><ui:text key="screen.fa7a5f35ea119b26"/></legend>
                                <c:choose>
                                    <c:when test="${empty allAmenities}">
                                        <p class="room-types-help mb-0"><ui:text key="room.type.amenities.empty"/></p>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="room-types-amenities">
                                            <c:forEach items="${allAmenities}" var="amenity">
                                                <c:set var="amenityId" value="${amenity.getAmenityID()}"/>
                                                <c:set var="isChecked" value="false"/>
                                                <c:forEach items="${roomAmenities}" var="selectedAmenity">
                                                    <c:if test="${selectedAmenity.getAmenityID() == amenityId}">
                                                        <c:set var="isChecked" value="true"/>
                                                    </c:if>
                                                </c:forEach>
                                                <label class="room-types-amenity">
                                                    <input class="form-check-input" type="checkbox" name="amenities"
                                                           value="${amenityId}" ${isChecked ? 'checked' : ''}>
                                                    <span><c:out value="${amenity.name}"/></span>
                                                </label>
                                            </c:forEach>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </fieldset>
                        </div>

                        <section class="room-types-panel room-types-form-card room-types-photo" aria-labelledby="room-picture-title">
                            <h2 id="room-picture-title"><ui:text key="screen.1f26160dc5f15704"/></h2>
                            <img id="room-picture" class="room-types-preview" src="<c:out value='${pictureUrl}'/>"
                                 width="400" height="280" alt="<c:out value="${requestScope.i18n['room.type.image.preview']}"/>"
                                 data-i18n-alt="room.type.image.preview">
                            <label for="picture" class="form-label"><ui:text key="room.type.image.replace"/></label>
                            <input type="file" name="img" accept=".png,.jpg,.jpeg" class="form-control room-types-file"
                                   id="picture" aria-describedby="picture-help picture-error">
                            <p id="picture-help" class="room-types-help"><ui:text key="room.type.image.help"/></p>
                            <p id="picture-error" class="room-types-file-error" aria-live="polite"
                               data-type-error="<c:out value="${requestScope.i18n['room.type.image.type']}"/>"
                               data-size-error="<c:out value="${requestScope.i18n['room.type.image.size']}"/>"
                               data-invalid-error="<c:out value="${requestScope.i18n['room.type.image.invalid']}"/>"></p>
                            <button type="button" class="btn room-types-secondary room-types-reset" id="picture-reset" hidden>
                                <ui:text key="room.type.image.reset"/>
                            </button>
                        </section>
                    </div>

                    <div class="room-types-panel room-types-form-actions">
                        <a class="btn room-types-secondary" href="<c:out value='${listUrl}'/>"><ui:text key="screen.19766ed6ccb2f4a3"/></a>
                        <button type="submit" class="btn room-types-primary"><ui:text key="screen.dd0ae7a5cbcf2339"/></button>
                    </div>
                </form>
            </main>

            <%@include file="/WEB-INF/include/footer.jsp" %>
        </div>
        <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
        <script src="${pageContext.request.contextPath}/assets/js/admin-room-type-image.js"></script>
    </body>
</html>
