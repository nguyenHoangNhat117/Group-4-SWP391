<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="group" required="true" type="java.lang.String" %>
<%@ attribute name="value" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="translationKey" value="${group}.${value}"/>
<span data-i18n="${not empty requestScope.i18n[translationKey] ? translationKey : ''}"><c:out value="${requestScope.i18n[translationKey]}" default="${value}"/></span>
