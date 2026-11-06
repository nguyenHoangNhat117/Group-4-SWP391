<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="text" required="true" type="java.lang.Object" %>
<%@ tag import="util.MessageTranslations" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    jspContext.setAttribute("translatedMessageKey", MessageTranslations.key(text));
    jspContext.setAttribute("translatedMessageText", MessageTranslations.text(text, jspContext.getAttribute("uiLanguage", jakarta.servlet.jsp.PageContext.REQUEST_SCOPE)));
%>
<c:choose>
    <c:when test="${not empty translatedMessageKey}">
        <span data-i18n="${translatedMessageKey}"><c:out value="${translatedMessageText}"/></span>
    </c:when>
    <c:otherwise><c:out value="${translatedMessageText}"/></c:otherwise>
</c:choose>
