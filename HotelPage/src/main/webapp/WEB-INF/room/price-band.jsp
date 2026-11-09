<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<label for="price-band" class="${param.searchStyle == 'booking' ? '' : 'form-label small fw-bold text-uppercase text-muted mb-1'}">
    <ui:text key="room.price.band"/>
</label>
<div class="price-band-field">
<i class="bi bi-tag" aria-hidden="true"></i>
<select id="price-band" name="price-band" class="${param.searchStyle == 'booking' ? 'form-control' : 'form-select bg-light border-0'}">
    <c:forEach var="band" items="ALL,UNDER_1M,FROM_1M,FROM_5M,ABOVE_15M">
        <option value="${band}" ${sessionScope.searchPriceBand == band ? 'selected' : ''}
                data-i18n="room.price.${band}"><c:out value="${requestScope.i18n['room.price.'.concat(band)]}"/></option>
    </c:forEach>
</select>
</div>
