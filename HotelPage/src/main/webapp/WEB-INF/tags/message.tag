<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="text" required="true" type="java.lang.Object" %>
<%@ tag import="util.MessageTranslations" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    jspContext.setAttribute("translatedMessageText", MessageTranslations.text(text));
%>
<c:out value="${translatedMessageText}"/>
