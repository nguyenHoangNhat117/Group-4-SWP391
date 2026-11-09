<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="amount" required="true" type="java.lang.Number" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" scope="page" />
<fmt:formatNumber value="${amount}" type="currency" currencyCode="VND"
                  minFractionDigits="0" maxFractionDigits="0" />