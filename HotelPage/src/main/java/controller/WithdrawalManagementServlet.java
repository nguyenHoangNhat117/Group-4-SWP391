package controller;

import dao.WithdrawalDAO;
import model.User;
import model.WithdrawalRequest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@WebServlet("/withdrawal-management")
public class WithdrawalManagementServlet
        extends HttpServlet {

    private static final int PAGE_SIZE = 20;

    private static final SecureRandom RANDOM
            = new SecureRandom();

    private final WithdrawalDAO withdrawalDAO
            = new WithdrawalDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session
                = request.getSession(false);

        User user = session == null
                ? null
                : (User) session.getAttribute(
                        "loggedUser");

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login");
            return;
        }

        String role = user.getRole();

        boolean authorized
                = "admin".equalsIgnoreCase(role)
                || "manager".equalsIgnoreCase(role);

        if (!authorized) {
            response.sendError(
                    403, "Access denied");
            return;
        }

        String status
                = request.getParameter("status");

        if (status == null) {
            status = "";
        }

        status = status.trim()
                .toLowerCase(java.util.Locale.ROOT);

        Set<String> validStatuses = Set.of(
                "",
                "pending",
                "approved",
                "rejected",
                "paid"
        );

        if (!validStatuses.contains(status)) {
            response.sendError(
                    400, "Invalid status");
            return;
        }

        int page = 1;
        String pageParam
                = request.getParameter("page");

        if (pageParam != null
                && !pageParam.isBlank()) {

            try {
                page = Integer.parseInt(pageParam);

                if (page <= 0) {
                    throw new NumberFormatException();
                }

            } catch (NumberFormatException e) {
                response.sendError(
                        400, "Invalid page");
                return;
            }
        }

        try {

            int totalRequests
                    = withdrawalDAO.countManageableRequests(
                            user.getId(),
                            role,
                            status
                    );

            int totalPages = Math.max(
                    1,
                    (int) (((long) totalRequests
                    + PAGE_SIZE - 1) / PAGE_SIZE)
            );

            if (page > totalPages) {
                page = totalPages;
            }

            List<WithdrawalRequest> requests
                    = withdrawalDAO.listManageableRequests(
                            user.getId(),
                            role,
                            status,
                            page,
                            PAGE_SIZE
                    );

            HttpSession activeSession
                    = request.getSession();

            if (activeSession.getAttribute(
                    "withdrawalActionCsrfToken") == null) {

                activeSession.setAttribute(
                        "withdrawalActionCsrfToken",
                        generateToken());
            }

            request.setAttribute(
                    "requests", requests);

            request.setAttribute(
                    "selectedStatus", status);

            request.setAttribute(
                    "currentPage", page);

            request.setAttribute(
                    "totalPages", totalPages);

            request.setAttribute(
                    "totalRequests", totalRequests);

            request.getRequestDispatcher(
                    "/WEB-INF/wallet/"
                    + "withdrawal-management.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            getServletContext().log(
                    "Withdrawal management query failed",
                    e);

            response.sendError(
                    503, "Withdrawal service unavailable");
        }
    }

    private String generateToken() {

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
