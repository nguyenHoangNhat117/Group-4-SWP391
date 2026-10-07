package filter;

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
import model.User;

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
            "/admin/*",
            "/toggle-animation"
        }
)
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();
        String fullPath = servletPath + (pathInfo == null ? "" : pathInfo);

        /*
         * PUBLIC ROUTES THAT SHARE PROTECTED SERVLETS
         *
         * UC07: Guest/Customer may open Search Rooms.
         * /booking without roomNumber is only the public search form.
         *
         * /room is public only for browsing available results and submitting
         * action=search. Room create/update/delete still require Admin.
         */
        if (isPublicBookingSearch(req, servletPath)
                || isPublicRoomRequest(req, servletPath)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("loggedUser");

        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (user.getAccountStatus() != null
                && !"active".equalsIgnoreCase(user.getAccountStatus())) {
            session.invalidate();
            res.sendRedirect(req.getContextPath() + "/login?error=account-status");
            return;
        }

        String role = user.getRole();

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

        if ("/customers".equals(servletPath)) {
            if (!isAnyRole(role, "staff", "admin")) {
                deny(res);
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        if ("/booking-management".equals(servletPath)) {
            if (!isAnyRole(role, "staff", "admin")) {
                deny(res);
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        if ("/reports".equals(servletPath)) {
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

            if (isRoomManagementAction(action) || isRoomManagementView(view)) {
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

    private boolean isPublicBookingSearch(HttpServletRequest req, String servletPath) {
        if (!"/booking".equals(servletPath)) {
            return false;
        }
        if (!"GET".equalsIgnoreCase(req.getMethod())) {
            return false;
        }
        String roomNumber = req.getParameter("roomNumber");
        return roomNumber == null || roomNumber.trim().isEmpty();
    }

    private boolean isPublicRoomRequest(HttpServletRequest req, String servletPath) {
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
        if (action == null) {
            return false;
        }
        return "create".equalsIgnoreCase(action)
                || "update".equalsIgnoreCase(action)
                || "delete".equalsIgnoreCase(action);
    }

    private boolean isRoomManagementView(String view) {
        if (view == null) {
            return false;
        }
        return "create".equalsIgnoreCase(view)
                || "update".equalsIgnoreCase(view)
                || "delete".equalsIgnoreCase(view);
    }

    private boolean isRole(String currentRole, String expectedRole) {
        return currentRole != null
                && expectedRole != null
                && expectedRole.equalsIgnoreCase(currentRole);
    }

    private boolean isAnyRole(String currentRole, String... allowedRoles) {
        if (currentRole == null || allowedRoles == null) {
            return false;
        }
        for (String allowedRole : allowedRoles) {
            if (allowedRole != null && allowedRole.equalsIgnoreCase(currentRole)) {
                return true;
            }
        }
        return false;
    }

    private void deny(HttpServletResponse response) throws IOException {
        response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "You do not have permission to access this resource."
        );
    }
}
