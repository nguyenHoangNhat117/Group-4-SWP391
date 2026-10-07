package controller;

import dao.DiscountDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import model.Discount;

@WebServlet(name = "DiscountServlet", urlPatterns = {"/discounts"})
public class DiscountServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DiscountDAO dao = new DiscountDAO();
        String view = request.getParameter("view");

        if (view == null || view.trim().isEmpty()) {
            request.setAttribute("discounts", dao.getAll());
            request.getRequestDispatcher("/WEB-INF/discount/discount.jsp")
                    .forward(request, response);
            return;
        }

        try {
            switch (view.trim()) {
                case "create":
                    request.getRequestDispatcher("/WEB-INF/discount/create.jsp")
                            .forward(request, response);
                    return;

                case "update": {
                    Integer id = parsePositiveInt(request.getParameter("id"));
                    if (id == null) {
                        response.sendRedirect(request.getContextPath() + "/discounts");
                        return;
                    }
                    Discount discount = dao.getDiscountById(id);
                    if (discount == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }
                    request.setAttribute("discount", discount);
                    request.getRequestDispatcher("/WEB-INF/discount/update.jsp")
                            .forward(request, response);
                    return;
                }

                case "delete": {
                    Integer id = parsePositiveInt(request.getParameter("id"));
                    if (id == null || dao.getDiscountById(id) == null) {
                        response.sendRedirect(request.getContextPath() + "/discounts");
                        return;
                    }
                    request.setAttribute("discount", dao.getDiscountById(id));
                    request.getRequestDispatcher("/WEB-INF/discount/delete.jsp")
                            .forward(request, response);
                    return;
                }

                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/discounts");
            return;
        }

        DiscountDAO dao = new DiscountDAO();

        try {
            switch (action.trim()) {
                case "create":
                    handleCreate(request, response, dao);
                    return;

                case "update":
                    handleUpdate(request, response, dao);
                    return;

                case "delete": {
                    Integer id = parsePositiveInt(request.getParameter("id"));
                    if (id == null) {
                        response.sendRedirect(request.getContextPath() + "/discounts");
                        return;
                    }
                    dao.deactivateDiscount(id);
                    response.sendRedirect(request.getContextPath() + "/discounts");
                    return;
                }

                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
            }
        } catch (NumberFormatException | DateTimeParseException ex) {
            response.sendRedirect(request.getContextPath()
                    + "/discounts?error=invalid-format");
        }
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response,
                              DiscountDAO dao) throws IOException {

        DiscountInput input = readAndValidateInput(request);
        if (input.error != null) {
            response.sendRedirect(request.getContextPath()
                    + "/discounts?view=create&error=" + input.error);
            return;
        }

        boolean created = dao.createDiscount(
                input.code,
                input.quantity,
                input.saleOff,
                input.startDate,
                input.endDate,
                input.minimumAmount,
                input.maximumDiscount,
                input.active
        );

        if (!created) {
            response.sendRedirect(request.getContextPath()
                    + "/discounts?view=create&error=create-failed");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/discounts");
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response,
                              DiscountDAO dao) throws IOException {

        Integer id = parsePositiveInt(request.getParameter("id"));
        if (id == null || dao.getDiscountById(id) == null) {
            response.sendRedirect(request.getContextPath() + "/discounts");
            return;
        }

        DiscountInput input = readAndValidateInput(request);
        if (input.error != null) {
            response.sendRedirect(request.getContextPath()
                    + "/discounts?view=update&id=" + id + "&error=" + input.error);
            return;
        }

        boolean updated = dao.updateDiscount(
                id,
                input.code,
                input.quantity,
                input.saleOff,
                input.startDate,
                input.endDate,
                input.minimumAmount,
                input.maximumDiscount,
                input.active
        );

        if (!updated) {
            response.sendRedirect(request.getContextPath()
                    + "/discounts?view=update&id=" + id + "&error=update-failed");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/discounts");
    }

    private DiscountInput readAndValidateInput(HttpServletRequest request) {
        DiscountInput input = new DiscountInput();

        input.code = trimToNull(request.getParameter("code"));
        if (input.code == null) {
            input.error = "missing-params";
            return input;
        }

        try {
            input.quantity = Integer.parseInt(request.getParameter("quantity"));
            input.saleOff = new BigDecimal(request.getParameter("sale-off"));
            input.startDate = parseNullableDate(request.getParameter("start-date"));
            input.endDate = parseNullableDate(request.getParameter("end-date"));
            input.minimumAmount = parseNullableBigDecimal(request.getParameter("minimum-amount"));
            input.maximumDiscount = parseNullableBigDecimal(request.getParameter("maximum-discount"));
            input.active = request.getParameter("active") != null;
        } catch (RuntimeException ex) {
            input.error = "invalid-format";
            return input;
        }

        if (input.quantity < 0
                || input.saleOff.compareTo(BigDecimal.ZERO) < 0
                || input.saleOff.compareTo(new BigDecimal("100")) > 0
                || (input.minimumAmount != null
                    && input.minimumAmount.compareTo(BigDecimal.ZERO) < 0)
                || (input.maximumDiscount != null
                    && input.maximumDiscount.compareTo(BigDecimal.ZERO) < 0)) {
            input.error = "invalid-number";
            return input;
        }

        if (input.startDate != null && input.endDate != null
                && input.endDate.isBefore(input.startDate)) {
            input.error = "invalid-date-range";
        }

        return input;
    }

    private Integer parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException | NullPointerException ex) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LocalDate parseNullableDate(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : LocalDate.parse(trimmed);
    }

    private BigDecimal parseNullableBigDecimal(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : new BigDecimal(trimmed);
    }

    private static class DiscountInput {
        String code;
        int quantity;
        BigDecimal saleOff;
        LocalDate startDate;
        LocalDate endDate;
        BigDecimal minimumAmount;
        BigDecimal maximumDiscount;
        boolean active;
        String error;
    }
}
