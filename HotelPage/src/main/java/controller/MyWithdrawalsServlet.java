
package controller;

import dao.WithdrawalDAO;
import db.DBContext;
import model.User;
import model.WithdrawalRequest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.List;

@WebServlet("/my-withdrawals")
public class MyWithdrawalsServlet extends HttpServlet {

    private final WithdrawalDAO withdrawalDAO =
            new WithdrawalDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        User user = session == null
                ? null
                : (User) session.getAttribute("loggedUser");

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login");
            return;
        }

        if (!"customer".equalsIgnoreCase(user.getRole())) {
            response.sendError(403, "Access denied");
            return;
        }

        try (Connection conn =
                new DBContext().getConnection()) {

            String sql =
                    "SELECT CustomerID FROM Customer "
                    + "WHERE UserID = ?";

            int customerId = -1;

            try (PreparedStatement ps =
                    conn.prepareStatement(sql)) {

                ps.setInt(1, user.getId());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        customerId =
                                rs.getInt("CustomerID");
                    }
                }
            }

            if (customerId <= 0) {
                response.sendError(
                        403, "Customer profile not found");
                return;
            }

            List<WithdrawalRequest> withdrawals =
                    withdrawalDAO.listCustomerRequests(
                            customerId);

            request.setAttribute(
                    "withdrawals", withdrawals);

            request.getRequestDispatcher(
                    "/WEB-INF/wallet/my-withdrawals.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            getServletContext().log(
                    "Cannot load withdrawal history", e);

            response.sendError(
                    503, "Withdrawal service unavailable");
        }
    }
}
