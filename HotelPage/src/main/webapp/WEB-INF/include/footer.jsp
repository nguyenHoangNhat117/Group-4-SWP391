<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<footer class="luxe-shared-footer">
    <div class="luxe-footer-inner">
        <div class="luxe-footer-brand">
            <div class="luxe-footer-logo">Luxe Escape</div>
            <p><ui:text key="screen.2ee0ec882ac51430"/></p>
        </div>
        <div class="luxe-footer-group">
            <h2><ui:text key="screen.3b73900b8d29f963"/></h2>
            <a href="${pageContext.request.contextPath}/#rooms"><ui:text key="screen.c58e4170a7c896bc"/></a>
            <a href="${pageContext.request.contextPath}/#restaurant"><ui:text key="screen.1212ea9e20949ffc"/></a>
            <a href="${pageContext.request.contextPath}/promotions"><ui:text key="screen.3e743275825133f9"/></a>
        </div>
        <div class="luxe-footer-group">
            <h2><ui:text key="screen.bbb6177f118e29cc"/></h2>
            <%-- Replace placeholders below with VERIFIED hotel contact information. --%>
            <div class="luxe-footer-info">
                <ion-icon name="location-outline" aria-hidden="true"></ion-icon>
                <span><ui:text key="screen.77f1000fbb703341"/></span>
            </div>
            <div class="luxe-footer-info">
                <ion-icon name="call-outline" aria-hidden="true"></ion-icon>
                <span><ui:text key="screen.b8e62b87eca368f3"/></span>
            </div>
            <div class="luxe-footer-info">
                <ion-icon name="mail-outline" aria-hidden="true"></ion-icon>
                <span><ui:text key="screen.f65d90ec48b31cde"/></span>
            </div>
        </div>
    </div>
    <div class="luxe-footer-bottom"><ui:text key="screen.e054863fe5b390e5"/></div>
</footer>
<script src="${pageContext.request.contextPath}/assets/js/luxe-ui.js" defer></script>

