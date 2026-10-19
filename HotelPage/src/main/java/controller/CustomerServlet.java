package controller;

import dao.CustomerDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.List;

import model.Customer;
import model.User;

@WebServlet(
        name = "CustomerServlet",
        urlPatterns = {"/customers"}
)
public class CustomerServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int PAGE_SIZE = 10;

    /*
     * =========================================================
     * GET
     * =========================================================
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * Filter already protects /customers,
         * but servlet checks role again.
         */
        User user =
                getAuthorizedUser(
                        request
                );

        if (user == null) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return;
        }

        String view =
                request.getParameter(
                        "view"
                );

        /*
         * Default:
         * customer list.
         */
        if (view == null
                || view.trim().isEmpty()
                || "list".equals(view)) {

            showCustomerList(
                    request,
                    response
            );

            return;
        }

        /*
         * Customer details.
         */
        if ("detail".equals(view)) {

            showCustomerDetail(
                    request,
                    response
            );

            return;
        }

        response.sendError(
                HttpServletResponse.SC_NOT_FOUND
        );
    }

    /*
     * =========================================================
     * CUSTOMER LIST + SEARCH
     * =========================================================
     */
    private void showCustomerList(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        CustomerDAO customerDAO =
                new CustomerDAO();

        String keyword =
                request.getParameter(
                        "keyword"
                );

        if (keyword != null) {

            keyword =
                    keyword.trim();
        }

        String status =
                request.getParameter(
                        "status"
                );

        if (!isValidAccountStatus(
                status)) {

            status = null;
        }

        int currentPage =
                parsePositiveInt(
                        request.getParameter(
                                "page-index"
                        ),
                        1
                );

        int totalCustomers =
                customerDAO.countCustomers(
                        keyword,
                        status
                );

        int totalPages =
                totalCustomers == 0
                        ? 0
                        : (int) Math.ceil(
                                totalCustomers
                                / (double) PAGE_SIZE
                        );

        if (totalPages > 0
                && currentPage > totalPages) {

            currentPage =
                    totalPages;
        }

        List<Customer> customers =
                customerDAO.searchCustomers(
                        keyword,
                        status,
                        currentPage,
                        PAGE_SIZE
                );

        request.setAttribute(
                "customers",
                customers
        );

        request.setAttribute(
                "keyword",
                keyword
        );

        request.setAttribute(
                "selectedStatus",
                status
        );

        request.setAttribute(
                "currentPage",
                currentPage
        );

        request.setAttribute(
                "totalPages",
                totalPages
        );

        request.setAttribute(
                "totalCustomers",
                totalCustomers
        );

        request.getRequestDispatcher(
                "/WEB-INF/customer/customer.jsp"
        ).forward(request, response);
    }

    /*
     * =========================================================
     * CUSTOMER DETAIL
     * =========================================================
     */
    private void showCustomerDetail(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Integer customerId =
                parseId(
                        request.getParameter(
                                "id"
                        )
                );

        if (customerId == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            return;
        }

        CustomerDAO customerDAO =
                new CustomerDAO();

        Customer customer =
                customerDAO.getCustomerById(
                        customerId
                );

        if (customer == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        request.setAttribute(
                "customerDetail",
                customer
        );

        request.getRequestDispatcher(
                "/WEB-INF/customer/detail.jsp"
        ).forward(request, response);
    }

    /*
     * =========================================================
     * POST
     *
     * Account status management.
     * =========================================================
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(
                "UTF-8"
        );

        User user =
                getAuthorizedUser(
                        request
                );

        if (user == null) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return;
        }

        String action =
                request.getParameter(
                        "action"
                );

        if (!"update-status".equals(action)
                && !"update-info".equals(action)) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            return;
        }

        Integer customerId =
                parseId(
                        request.getParameter(
                                "customer-id"
                        )
                );

        String accountStatus =
                request.getParameter(
                        "account-status"
                );

        if (customerId == null
                || ("update-status".equals(action)
                    && !isValidAccountStatus(accountStatus))) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/customers"
                    + "?message=invalid-data"
            );

            return;
        }

        CustomerDAO customerDAO =
                new CustomerDAO();

        /*
         * Ensure selected customer exists.
         */
        Customer customer =
                customerDAO.getCustomerById(
                        customerId
                );

        if (customer == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/customers"
                    + "?message=customer-not-found"
            );

            return;
        }

        if ("update-info".equals(action)) {

            String firstName = trimToNull(request.getParameter("first-name"));
            String lastName = trimToNull(request.getParameter("last-name"));
            String email = trimToNull(request.getParameter("email"));
            String phone = trimToNull(request.getParameter("phone"));
            String country = trimToNull(request.getParameter("country"));
            String city = trimToNull(request.getParameter("city"));
            String street = trimToNull(request.getParameter("street"));

            if (email == null
                    || email.length() > 100
                    || !email.contains("@")
                    || tooLong(firstName, 50)
                    || tooLong(lastName, 50)
                    || tooLong(phone, 20)
                    || tooLong(country, 50)
                    || tooLong(city, 50)
                    || tooLong(street, 100)) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/customers?view=detail&id="
                        + customerId
                        + "&message=invalid-data"
                );
                return;
            }

            boolean infoUpdated = customerDAO.updateCustomerInformation(
                    customerId, firstName, lastName, email, phone,
                    country, city, street
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/customers?view=detail&id="
                    + customerId
                    + (infoUpdated
                        ? "&message=info-updated"
                        : "&message=info-update-failed")
            );
            return;
        }

        boolean updated =
                customerDAO.updateAccountStatus(
                        customerId,
                        accountStatus
                );

        if (!updated) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/customers"
                    + "?view=detail"
                    + "&id="
                    + customerId
                    + "&message=update-failed"
            );

            return;
        }

        response.sendRedirect(
                request.getContextPath()
                + "/customers"
                + "?view=detail"
                + "&id="
                + customerId
                + "&message=status-updated"
        );
    }

    /*
     * =========================================================
     * AUTHORIZATION
     *
     * UC17:
     * Staff + Admin.
     * =========================================================
     */
    private User getAuthorizedUser(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {

            return null;
        }

        User user =
                (User) session.getAttribute(
                        "loggedUser"
                );

        if (user == null) {

            return null;
        }

        if (!"staff".equalsIgnoreCase(
                    user.getRole())
                && !"admin".equalsIgnoreCase(
                    user.getRole())) {

            return null;
        }

        return user;
    }

    /*
     * =========================================================
     * HELPERS
     * =========================================================
     */
    private Integer parseId(
            String raw) {

        if (raw == null
                || raw.trim().isEmpty()) {

            return null;
        }

        try {

            int id =
                    Integer.parseInt(
                            raw.trim()
                    );

            return id > 0
                    ? id
                    : null;

        } catch (NumberFormatException e) {

            return null;
        }
    }

    private int parsePositiveInt(
            String raw,
            int defaultValue) {

        if (raw == null
                || raw.trim().isEmpty()) {

            return defaultValue;
        }

        try {

            int value =
                    Integer.parseInt(
                            raw.trim()
                    );

            return value > 0
                    ? value
                    : defaultValue;

        } catch (NumberFormatException e) {

            return defaultValue;
        }
    }

    private boolean isValidAccountStatus(
            String status) {

        return "active".equals(status)
                || "locked".equals(status)
                || "inactive".equals(status);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean tooLong(String value, int maxLength) {
        return value != null && value.length() > maxLength;
    }

    @Override
    public String getServletInfo() {

        return "Staff/Admin Customer Management.";
    }
}