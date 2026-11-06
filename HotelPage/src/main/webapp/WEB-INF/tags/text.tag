<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="key"
              required="true"
              type="java.lang.String" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<span data-i18n="${key}"><c:out value="${requestScope.i18n[key]}" default="${key}"/></span>
