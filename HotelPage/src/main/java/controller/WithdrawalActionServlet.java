package controller;

import dao.WithdrawalDAO;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.security.SecureRandom;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;

@WebServlet("/withdrawal-action")
public class WithdrawalActionServlet
        extends HttpServlet {

    private static final SecureRandom RANDOM = new SecureRandom();
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

        if (!"manager".equalsIgnoreCase(role)) {
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

                case "approve": {
                    // Original approval remains available; only the demo code is local.
                    success = withdrawalDAO.approve(
                            withdrawalId,
                            user.getId()
                    );

                    if (success && isLocalDemo(request)) {
                        issueDemoCode(session, withdrawalId, user.getId());
                    }

                    break;
                }

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

                // Re-issue a local test code for an already approved request.
                case "requestPayoutDemoCode": {
                    if (!isLocalDemo(request)) {
                        response.sendError(403, "Demo mode is localhost only");
                        return;
                    }
                    model.WithdrawalRequest item = withdrawalDAO.getRequestById(
                            withdrawalId, user.getId(), "manager");
                    if (item == null || !"approved".equalsIgnoreCase(item.getStatus())) {
                        response.sendRedirect(request.getContextPath()
                                + "/withdrawal-management?result=not-processed");
                        return;
                    }
                    issueDemoCode(session, withdrawalId, user.getId());
                    response.sendRedirect(request.getContextPath()
                            + "/withdrawal-management?result=code-created");
                    return;
                }

                case "verifyPayoutDemo": {
                    // This updates the *local simulation ledger*, not a bank account.
                    if (!isLocalDemo(request)) {
                        response.sendError(403, "Demo mode is localhost only");
                        return;
                    }
                    if (!"yes".equals(request.getParameter("payoutConfirmed"))) {
                        response.sendError(400, "Demo confirmation required");
                        return;
                    }
                    String enteredCode = request.getParameter("payoutCode");
                    synchronized (session) {
                        String savedCode = (String) session.getAttribute("managerPayoutDemoCode");
                        Integer savedId = (Integer) session.getAttribute("managerPayoutWithdrawalId");
                        Integer savedManager = (Integer) session.getAttribute("managerPayoutManagerId");
                        Long expires = (Long) session.getAttribute("managerPayoutExpires");
                        Integer attempts = (Integer) session.getAttribute("managerPayoutAttempts");
                        boolean valid = savedCode != null && savedId != null
                                && savedManager != null && expires != null && attempts != null
                                && savedId.intValue() == withdrawalId
                                && savedManager.intValue() == user.getId()
                                && System.currentTimeMillis() < expires
                                && attempts < 5 && enteredCode != null
                                && enteredCode.matches("[0-9]{6}")
                                && MessageDigest.isEqual(
                                        savedCode.getBytes(StandardCharsets.UTF_8),
                                        enteredCode.getBytes(StandardCharsets.UTF_8));
                        if (!valid) {
                            if (attempts != null) {
                                session.setAttribute("managerPayoutAttempts", attempts + 1);
                            }
                            response.sendRedirect(request.getContextPath()
                                    + "/withdrawal-management?error=invalid-payout-code");
                            return;
                        }
                        // Explicitly tagged as a simulated entry, never a bank reference.
                        String demoReference = "DEMO-WD-" + withdrawalId + "-"
                                + String.format("%06d", RANDOM.nextInt(1000000));
                        success = withdrawalDAO.markPaid(withdrawalId, user.getId(), demoReference);
                        if (success) {
                            clearDemoCode(session);
                        }
                    }
                    break;
                }

                case "markPaid": {

                    if (!"yes".equals(
                            request.getParameter("payoutConfirmed"))) {

                        response.sendError(
                                400, "Confirm the payout before recording Paid");
                        return;
                    }

                    String paymentReference
                            = request.getParameter("paymentReference");

                    if (paymentReference == null
                            || paymentReference.trim().isEmpty()
                            || paymentReference.trim().length() > 100) {

                        response.sendError(
                                400, "Payment reference required");
                        return;
                    }

                    String enteredCode
                            = request.getParameter("payoutCode");

                    String savedCode
                            = (String) session.getAttribute("managerPayoutDemoCode");

                    Integer savedWithdrawalId
                            = (Integer) session.getAttribute("managerPayoutWithdrawalId");

                    Long expires
                            = (Long) session.getAttribute("managerPayoutExpires");

                    if (savedCode == null
                            || savedWithdrawalId == null
                            || savedWithdrawalId.intValue() != withdrawalId
                            || expires == null
                            || System.currentTimeMillis() >= expires
                            || enteredCode == null
                            || !enteredCode.matches("[0-9]{6}")
                            || !MessageDigest.isEqual(
                                    savedCode.getBytes(StandardCharsets.UTF_8),
                                    enteredCode.getBytes(StandardCharsets.UTF_8))) {

                        response.sendRedirect(
                                request.getContextPath()
                                + "/withdrawal-management?error=invalid-payout-code");
                        return;
                    }

                    // Consume the demo code before processing.
                    session.removeAttribute("managerPayoutDemoCode");
                    session.removeAttribute("managerPayoutWithdrawalId");
                    session.removeAttribute("managerPayoutExpires");

                    success = withdrawalDAO.markPaid(
                            withdrawalId,
                            user.getId(),
                            paymentReference.trim()
                    );

                    break;
                }

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

    private void issueDemoCode(HttpSession session, int withdrawalId, int managerId) {
        synchronized (session) {
            session.setAttribute("managerPayoutDemoCode",
                    String.format("%06d", RANDOM.nextInt(1000000)));
            session.setAttribute("managerPayoutWithdrawalId", withdrawalId);
            session.setAttribute("managerPayoutManagerId", managerId);
            session.setAttribute("managerPayoutExpires", System.currentTimeMillis() + 300000L);
            session.setAttribute("managerPayoutAttempts", 0);
        }
    }

    private void clearDemoCode(HttpSession session) {
        session.removeAttribute("managerPayoutDemoCode");
        session.removeAttribute("managerPayoutWithdrawalId");
        session.removeAttribute("managerPayoutManagerId");
        session.removeAttribute("managerPayoutExpires");
        session.removeAttribute("managerPayoutAttempts");
    }

    private boolean isLocalDemo(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return "127.0.0.1".equals(ip) || "::1".equals(ip)
                || "0:0:0:0:0:0:0:1".equals(ip);
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
