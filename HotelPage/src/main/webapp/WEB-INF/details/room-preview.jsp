<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<c:set var="inRoomPreview" value="${true}"/>
<div class="room-preview-layout">
    <%@ include file="room-detail-info.jspf" %>
</div>
<c:if test="${not empty reviews}">
    <div class="mt-4">
        <%@ include file="room-detail-reviews.jspf" %>
    </div>
</c:if>
