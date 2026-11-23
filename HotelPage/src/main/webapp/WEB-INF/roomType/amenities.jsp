<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><ui:text key="room.amenity.manage"/></title>
    <link rel="stylesheet" href="<c:url value='/assets/css/bootstrap.min.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/general.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/luxe-admin-suite.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/luxe-shared-shell.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/luxe-ui.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/admin-room-types.css?v=3'/>">
</head>
<body class="luxe-admin-suite luxe-admin-types">
    <div class="body-wrapper">
        <%@ include file="../include/header.jsp" %>
        <main class="room-types-page">
            <a class="room-types-back" href="<c:url value='/room-type'/>">&larr; <ui:text key="room.type.back"/></a>
            <div class="amenity-heading">
                <h1><ui:text key="room.amenity.manage"/></h1>
                <c:choose>
                    <c:when test="${not empty editingAmenity}">
                        <a class="btn room-types-primary" href="<c:url value='/amenity'><c:param name='new' value='1'/></c:url>">
                            <ui:text key="room.amenity.create"/>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <button class="btn room-types-primary" type="button" data-bs-toggle="modal" data-bs-target="#amenity-editor">
                            <ui:text key="room.amenity.create"/>
                        </button>
                    </c:otherwise>
                </c:choose>
            </div>
            <c:if test="${param.saved eq '1'}">
                <p class="alert alert-success" role="status"><ui:text key="room.amenity.saved"/></p>
            </c:if>
            <section class="room-types-panel" aria-labelledby="amenity-catalog-title" data-amenity-list>
                <div class="amenity-catalog-toolbar">
                    <div>
                        <h2 id="amenity-catalog-title" class="h5 mb-1"><ui:text key="room.amenity.catalog"/></h2>
                        <span class="small text-muted">${amenities.size()} <ui:text key="room.amenity.unit"/></span>
                    </div>
                    <c:if test="${not empty amenities}">
                        <div class="amenity-catalog-filter" data-amenity-filter hidden>
                            <label class="form-label mb-1" for="catalog-amenity-search"><ui:text key="room.amenity.search"/></label>
                            <input class="form-control" id="catalog-amenity-search" type="search" data-amenity-search>
                        </div>
                    </c:if>
                </div>
                <c:choose>
                    <c:when test="${empty amenities}">
                        <p class="text-muted p-4 mb-0"><ui:text key="room.type.amenities.empty"/></p>
                    </c:when>
                    <c:otherwise>
                        <ul class="list-unstyled mb-0" role="list">
                            <c:forEach var="amenity" items="${amenities}">
                                <c:url var="editUrl" value="/amenity">
                                    <c:param name="edit" value="${amenity.amenityID}"/>
                                </c:url>
                                <li class="amenity-catalog-item" data-amenity-item>
                                    <div>
                                        <div class="amenity-catalog-title">
                                            <h3 class="h6 fw-bold mb-0" data-amenity-name><c:out value="${amenity.name}"/></h3>
                                            <span class="catalog-status ${amenity.active ? 'is-active' : ''}">
                                                <ui:text key="${amenity.active ? 'room.amenity.active' : 'room.amenity.inactive'}"/>
                                            </span>
                                        </div>
                                    </div>
                                    <div class="amenity-catalog-links">
                                        <span><ui:text key="room.amenity.linkedTypes"/></span>
                                        <c:set var="linkedTypes" value="${amenityTypes[amenity.amenityID]}"/>
                                        <c:choose>
                                            <c:when test="${empty linkedTypes}">
                                                <span class="d-block mt-1"><ui:text key="room.amenity.noLinks"/></span>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="amenity-linked-types">
                                                    <c:forEach var="type" items="${linkedTypes}">
                                                        <c:url var="typeUrl" value="/room-type">
                                                            <c:param name="view" value="update"/>
                                                            <c:param name="id" value="${type.id}"/>
                                                        </c:url>
                                                        <a href="<c:out value='${typeUrl}'/>"><c:out value="${type.name}"/></a>
                                                    </c:forEach>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="amenity-catalog-actions">
                                        <a class="btn room-types-secondary" href="<c:out value='${editUrl}'/>">
                                            <ui:text key="room.type.action.edit"/>
                                            <span class="visually-hidden"> <c:out value="${amenity.name}"/></span>
                                        </a>
                                        <form method="post" action="<c:url value='/amenity'/>">
                                            <input type="hidden" name="action" value="status">
                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="id" value="${amenity.amenityID}">
                                            <input type="hidden" name="active" value="${not amenity.active}">
                                            <button type="submit" class="btn room-types-secondary">
                                                <ui:text key="${amenity.active ? 'room.amenity.disable' : 'room.amenity.enable'}"/>
                                                <span class="visually-hidden"> <c:out value="${amenity.name}"/></span>
                                            </button>
                                        </form>
                                    </div>
                                </li>
                            </c:forEach>
                        </ul>
                        <p class="text-muted p-4 mb-0" data-amenity-no-results hidden><ui:text key="room.amenity.noResults"/></p>
                    </c:otherwise>
                </c:choose>
            </section>
        </main>
        <%@ include file="../include/footer.jsp" %>
    </div>

    <div class="modal fade" id="amenity-editor" tabindex="-1" aria-labelledby="amenity-editor-title" aria-hidden="true"
         data-open-on-load="${not empty editingAmenity or param['new'] eq '1'}">
        <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
            <form class="modal-content" method="post" action="<c:url value='/amenity'/>">
                <input type="hidden" name="action" value="save">
                <input type="hidden" name="id" value="${empty editingAmenity ? 0 : editingAmenity.amenityID}">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <div class="modal-header">
                    <h2 class="modal-title h5" id="amenity-editor-title">
                        <ui:text key="${editingAmenity.amenityID gt 0 ? 'room.amenity.edit' : 'room.amenity.create'}"/>
                    </h2>
                    <button class="btn-close" type="button" data-bs-dismiss="modal"
                            aria-label="${requestScope.i18n['screen.7d9eb7acb13e2462']}"></button>
                </div>
                <div class="modal-body">
                    <c:if test="${not empty amenityError}">
                        <p class="alert alert-warning" role="alert"><ui:text key="${amenityError}"/></p>
                    </c:if>
                    <label class="form-label" for="amenity-name"><ui:text key="room.amenity.name"/></label>
                    <input class="form-control" id="amenity-name" name="name" maxlength="100" required
                           value="<c:out value='${editingAmenity.name}'/>">
                </div>
                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal"><ui:text key="common.cancel"/></button>
                    <button class="btn room-types-primary" type="submit"><ui:text key="common.save"/></button>
                </div>
            </form>
        </div>
    </div>
    <script src="<c:url value='/assets/js/bootstrap.bundle.min.js'/>"></script>
    <script src="<c:url value='/assets/js/admin-amenity-search.js'/>" defer></script>
    <script>
        const amenityEditor = document.getElementById('amenity-editor');
        amenityEditor.addEventListener('shown.bs.modal', () => document.getElementById('amenity-name').focus());
        if (amenityEditor.dataset.openOnLoad === 'true') {
            bootstrap.Modal.getOrCreateInstance(amenityEditor).show();
        }
    </script>
</body>
</html>
