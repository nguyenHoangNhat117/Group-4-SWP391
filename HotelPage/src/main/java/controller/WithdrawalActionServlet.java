package controller;

import dao.WithdrawalDAO;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;

@WebServlet("/withdrawal-action")
public class WithdrawalActionServlet
        extends HttpServlet {

    private final WithdrawalDAO withdrawalDAO
            = new WithdrawalDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendError(
                405, "Use POST for withdrawal actions");
    }

    @Override
    protected void doPost(
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

        boolean isAdmin
                = "admin".equalsIgnoreCase(role);

        boolean isManager
                = "manager".equalsIgnoreCase(role);

        if (!isAdmin && !isManager) {
            response.sendError(
                    403, "Access denied");
            return;
        }

        if (!validCsrf(request, session)) {
            response.sendError(
                    403, "Invalid CSRF token");
            return;
        }

        String action
                = request.getParameter("action");

        String withdrawalIdParam
                = request.getParameter("withdrawalId");

        int withdrawalId;

        try {
            withdrawalId
                    = Integer.parseInt(
                            withdrawalIdParam);

            if (withdrawalId <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {
            response.sendError(
                    400, "Invalid withdrawal ID");
            return;
        }

        if (action == null) {
            response.sendError(
                    400, "Missing action");
            return;
        }

        boolean success;

        try {

            switch (action) {

                case "approve":

                    success = withdrawalDAO.approve(
                            withdrawalId,
                            user.getId()
                    );

                    break;

                case "reject":

                    String reason
                            = request.getParameter(
                                    "rejectReason");

                    if (reason == null
                            || reason.trim().isEmpty()
                            || reason.trim().length() > 500) {

                        response.sendError(
                                400, "Invalid reject reason");
                        return;
                    }

                    success = withdrawalDAO.reject(
                            withdrawalId,
                            user.getId(),
                            reason.trim()
                    );

                    break;

                case "markPaid":

                    if (!isAdmin && !isManager) {
                        response.sendError(
                                403,
                                "Payout confirmation access denied");
                        return;
                    }

                    if (!"yes".equals(
                            request.getParameter("payoutConfirmed"))) {

                        response.sendError(
                                400,
                                "Confirm the payout before recording Paid"
                        );
                        return;
                    }

                    String paymentReference
                            = request.getParameter(
                                    "paymentReference");

                    if (paymentReference == null
                            || paymentReference.trim().isEmpty()
                            || paymentReference.trim().length() > 100) {

                        response.sendError(
                                400,
                                "Payment reference required");
                        return;
                    }

                    success = withdrawalDAO.markPaid(
                            withdrawalId,
                            user.getId(),
                            paymentReference.trim()
                    );

                    break;

                default:
                    response.sendError(
                            400, "Invalid action");
                    return;
            }

            if (!success) {
                response.sendRedirect(
                        request.getContextPath()
                        + "/withdrawal-management"
                        + "?result=not-processed");
                return;
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/withdrawal-management"
                    + "?result=success");

        } catch (SQLException e) {
            getServletContext().log(
                    "Withdrawal action failed", e);

            response.sendError(
                    503, "Withdrawal service unavailable");
        }
    }

    private boolean validCsrf(
            HttpServletRequest request,
            HttpSession session) {

        if (session == null) {
            return false;
        }

        Object expected = session.getAttribute(
                "withdrawalActionCsrfToken");

        String submitted
                = request.getParameter("csrfToken");

        if (!(expected instanceof String)
                || submitted == null) {
            return false;
        }

        return MessageDigest.isEqual(
                ((String) expected).getBytes(
                        StandardCharsets.UTF_8),
                submitted.getBytes(
                        StandardCharsets.UTF_8)
        );
    }
}
