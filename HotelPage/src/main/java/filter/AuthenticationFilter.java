package filter;

import dao.UserDAO;
import model.User;
import util.AuthSupport;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Objects;

// Updated by Nguyen Hoang Nhat
@WebFilter(
        filterName = "AuthenticationFilter",
        urlPatterns = {
            "/booking",
            "/payment",
            "/review",
            "/profile",
            "/avatar",
            "/history",
            "/history/*",
            "/customers",
            "/booking-management",
            "/room",
            "/room-type",
            "/discounts",
            "/reports",
            "/dashboard",
            "/admin/*",
            "/toggle-animation",
            "/offline-booking",
            "/counter-payment",
            "/change-password"
        }
)
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();

        String fullPath = servletPath
                + (pathInfo == null ? "" : pathInfo);

        // Preserve public browsing and room search.
        if (isPublicBookingSearch(req, servletPath)
                || isPublicRoomRequest(req, servletPath)) {

            chain.doFilter(request, response);
            return;
        }

        AuthSupport.noStore(res);

        HttpSession session = req.getSession(false);
        User user;

        try {
            Object value = session == null
                    ? null
                    : session.getAttribute("loggedUser");

            user = value instanceof User ? (User) value : null;

        } catch (IllegalStateException e) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (user == null) {
            preservePendingBooking(req, servletPath);

            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User current;

        try {
            current = new UserDAO().getUserById(user.getId());

        } catch (IllegalStateException e) {
            res.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Authentication is temporarily unavailable."
            );
            return;
        }

        // Old sessions lose access after password or account-status changes.
        if (current == null
                || !"active".equalsIgnoreCase(
                        current.getAccountStatus()
                )
                || !Objects.equals(current.getRole(), user.getRole())
                || !Objects.equals(
                        current.getPassword(),
                        user.getPassword()
                )) {

            invalidateSession(session);

            res.sendRedirect(
                    req.getContextPath()
                    + "/login?error=account-status"
            );
            return;
        }

        try {
            // Refresh the role before checking permissions.
            session.setAttribute("loggedUser", current);

        } catch (IllegalStateException e) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = current.getRole();

        // Change Password is available to all four supported roles.
        if ("/change-password".equals(servletPath)) {
            if (!isAnyRole(
                    role,
                    "customer",
                    "staff",
                    "admin",
                    "manager"
            )) {
                deny(res);
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        if (isCustomerPath(fullPath)) {
            if (!isRole(role, "customer")) {
                deny(res);
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        if (fullPath.startsWith("/history/cancel-booking")) {
            if (!isRole(role, "customer")) {
                deny(res);
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        if ("/history".equals(servletPath)) {
            if (!isAnyRole(role, "customer", "staff", "admin")) {
                deny(res);
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        if ("/customers".equals(servletPath)
                || "/booking-management".equals(servletPath)
                || "/offline-booking".equals(servletPath)
                || "/counter-payment".equals(servletPath)) {

            if (!isAnyRole(role, "staff", "admin")) {
                deny(res);
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        if ("/reports".equals(servletPath)
                || "/dashboard".equals(servletPath)) {

            if (!isRole(role, "manager")) {
                deny(res);
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        if ("/room-type".equals(servletPath)
                || "/discounts".equals(servletPath)
                || fullPath.startsWith("/admin/")) {

            if (!isRole(role, "admin")) {
                deny(res);
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        if ("/room".equals(servletPath)) {
            String action = req.getParameter("action");
            String view = req.getParameter("view");

            if (isRoomManagementAction(action)
                    || isRoomManagementView(view)) {

                if (!isRole(role, "admin")) {
                    deny(res);
                    return;
                }
            }

            chain.doFilter(request, response);
            return;
        }

        chain.doFilter(request, response);
    }

    private void preservePendingBooking(
            HttpServletRequest req,
            String servletPath
    ) {
        if (!"/booking".equals(servletPath)
                || !"GET".equalsIgnoreCase(req.getMethod())) {
            return;
        }

        String room = req.getParameter("roomNumber");

        if (room != null && room.matches("[0-9]+")) {
            req.getSession().setAttribute(
                    "pendingBookingUrl",
                    "/booking?roomNumber=" + room
            );
        }
    }

    private boolean isPublicBookingSearch(
            HttpServletRequest req,
            String servletPath
    ) {
        if (!"/booking".equals(servletPath)
                || !"GET".equalsIgnoreCase(req.getMethod())) {
            return false;
        }

        String roomNumber = req.getParameter("roomNumber");

        return roomNumber == null || roomNumber.trim().isEmpty();
    }

    private boolean isPublicRoomRequest(
            HttpServletRequest req,
            String servletPath
    ) {
        if (!"/room".equals(servletPath)) {
            return false;
        }

        String view = req.getParameter("view");
        String action = req.getParameter("action");

        if ("GET".equalsIgnoreCase(req.getMethod())) {
            return view == null || view.trim().isEmpty();
        }

        return "POST".equalsIgnoreCase(req.getMethod())
                && "search".equalsIgnoreCase(action);
    }

    private boolean isCustomerPath(String path) {
        return "/booking".equals(path)
                || "/payment".equals(path)
                || "/review".equals(path)
                || "/profile".equals(path)
                || "/avatar".equals(path);
    }

    private boolean isRoomManagementAction(String action) {
        return "create".equalsIgnoreCase(action)
                || "update".equalsIgnoreCase(action)
                || "delete".equalsIgnoreCase(action);
    }

    private boolean isRoomManagementView(String view) {
        return "create".equalsIgnoreCase(view)
                || "update".equalsIgnoreCase(view)
                || "delete".equalsIgnoreCase(view);
    }

    private boolean isRole(String currentRole, String expectedRole) {
        return expectedRole.equalsIgnoreCase(currentRole);
    }

    private boolean isAnyRole(
            String currentRole,
            String... allowedRoles
    ) {
        for (String allowedRole : allowedRoles) {
            if (allowedRole.equalsIgnoreCase(currentRole)) {
                return true;
            }
        }

        return false;
    }

    private void invalidateSession(HttpSession session) {
        if (session == null) {
            return;
        }

        try {
            session.invalidate();

        } catch (IllegalStateException ignored) {
            // Another request already ended the session.
        }
    }

    private void deny(HttpServletResponse res) throws IOException {
        res.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "You do not have permission to access this resource."
        );
    }
}
