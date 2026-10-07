package controller;

import dao.CustomerDAO;
import dao.UserDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import model.Customer;
import model.User;

@WebServlet(
        name = "LoginServlet",
        urlPatterns = {"/login"}
)
public class LoginServlet extends HttpServlet {

    /*
     * =========================================================
     * GET LOGIN PAGE
     * =========================================================
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * If already logged in, there is no reason
         * to display login page again.
         */
        HttpSession currentSession =
                request.getSession(false);

        if (currentSession != null
                && currentSession.getAttribute(
                        "loggedUser"
                ) != null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/"
            );

            return;
        }

        /*
         * Password reset success message.
         */
        String success =
                request.getParameter(
                        "success"
                );

        if ("reset".equals(success)) {

            request.setAttribute(
                    "message",
                    "Password reset successful. "
                    + "Please sign in."
            );
        }

        /*
         * Remember Username only.
         *
         * NEVER restore/store password in cookies.
         */
        Cookie[] cookies =
                request.getCookies();

        if (cookies != null) {

            for (Cookie cookie : cookies) {

                if ("username".equals(
                        cookie.getName())) {

                    request.setAttribute(
                            "enteredUsername",
                            cookie.getValue()
                    );
                }
            }
        }

        request.getRequestDispatcher(
                "/WEB-INF/login/login.jsp"
        ).forward(request, response);
    }

    /*
     * =========================================================
     * LOGIN
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

        /*
         * =====================================================
         * 1. READ INPUT
         * =====================================================
         */
        String username =
                request.getParameter(
                        "username"
                );

        String password =
                request.getParameter(
                        "password"
                );

        /*
         * =====================================================
         * 2. VALIDATE USERNAME
         * =====================================================
         */
        if (username == null
                || username.trim().isEmpty()) {

            request.setAttribute(
                    "error",
                    "Username is required."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/login/login.jsp"
            ).forward(request, response);

            return;
        }

        username =
                username.trim();

        /*
         * =====================================================
         * 3. VALIDATE PASSWORD
         * =====================================================
         */
        if (password == null
                || password.isEmpty()) {

            request.setAttribute(
                    "error",
                    "Password is required."
            );

            request.setAttribute(
                    "enteredUsername",
                    username
            );

            request.getRequestDispatcher(
                    "/WEB-INF/login/login.jsp"
            ).forward(request, response);

            return;
        }

        /*
         * =====================================================
         * 4. AUTHENTICATE
         * =====================================================
         */
        UserDAO userDAO =
                new UserDAO();

        UserDAO.AuthenticationResult result =
                userDAO.authenticate(
                        username,
                        password
                );

        /*
         * =====================================================
         * 5. INVALID CREDENTIALS
         * =====================================================
         */
        if (UserDAO.LOGIN_INVALID.equals(
                result.getStatus())) {

            request.setAttribute(
                    "error",
                    "Invalid username or password."
            );

            request.setAttribute(
                    "enteredUsername",
                    username
            );

            request.getRequestDispatcher(
                    "/WEB-INF/login/login.jsp"
            ).forward(request, response);

            return;
        }

        /*
         * =====================================================
         * 6. LOCKED ACCOUNT
         * =====================================================
         */
        if (UserDAO.LOGIN_LOCKED.equals(
                result.getStatus())) {

            request.setAttribute(
                    "error",
                    "Your account is locked. "
                    + "Please contact hotel staff "
                    + "or an administrator."
            );

            request.setAttribute(
                    "enteredUsername",
                    username
            );

            request.getRequestDispatcher(
                    "/WEB-INF/login/login.jsp"
            ).forward(request, response);

            return;
        }

        /*
         * =====================================================
         * 7. INACTIVE ACCOUNT
         * =====================================================
         */
        if (UserDAO.LOGIN_INACTIVE.equals(
                result.getStatus())) {

            request.setAttribute(
                    "error",
                    "Your account is inactive."
            );

            request.setAttribute(
                    "enteredUsername",
                    username
            );

            request.getRequestDispatcher(
                    "/WEB-INF/login/login.jsp"
            ).forward(request, response);

            return;
        }

        /*
         * =====================================================
         * 8. LOGIN SUCCESS
         * =====================================================
         */
        User loggedUser =
                result.getUser();

        if (loggedUser == null) {

            request.setAttribute(
                    "error",
                    "Login could not be completed."
            );

            request.setAttribute(
                    "enteredUsername",
                    username
            );

            request.getRequestDispatcher(
                    "/WEB-INF/login/login.jsp"
            ).forward(request, response);

            return;
        }

        /*
         * Prevent session fixation:
         * invalidate old session before creating a new one.
         */
        HttpSession oldSession =
                request.getSession(false);

        if (oldSession != null) {

            oldSession.invalidate();
        }

        HttpSession session =
                request.getSession(true);

        /*
         * 30 minute inactivity timeout.
         */
        session.setMaxInactiveInterval(
                30 * 60
        );

        session.setAttribute(
                "loggedUser",
                loggedUser
        );

        /*
         * =====================================================
         * 9. LOAD CUSTOMER PROFILE
         * =====================================================
         */
        if ("customer".equalsIgnoreCase(
                loggedUser.getRole())) {

            CustomerDAO customerDAO =
                    new CustomerDAO();

            Customer customer =
                    customerDAO
                            .getCustomerByUserID(
                                    loggedUser.getId()
                            );

            /*
             * A customer account should have
             * matching Customer profile.
             */
            if (customer == null) {

                session.invalidate();

                request.setAttribute(
                        "error",
                        "Customer profile could not "
                        + "be loaded."
                );

                request.setAttribute(
                        "enteredUsername",
                        username
                );

                request.getRequestDispatcher(
                        "/WEB-INF/login/login.jsp"
                ).forward(request, response);

                return;
            }

            session.setAttribute(
                    "customer",
                    customer
            );
        }

        /*
         * =====================================================
         * 10. REMEMBER USERNAME
         *
         * Only username is stored.
         * Password is NEVER stored.
         * =====================================================
         */
        String remember =
                request.getParameter(
                        "remember"
                );

        if ("on".equals(remember)) {

            Cookie usernameCookie =
                    new Cookie(
                            "username",
                            username
                    );

            usernameCookie.setMaxAge(
                    7 * 24 * 60 * 60
            );

            usernameCookie.setHttpOnly(
                    true
            );

            usernameCookie.setPath(
                    request.getContextPath()
                            .isEmpty()
                            ? "/"
                            : request.getContextPath()
            );

            /*
             * If production uses HTTPS,
             * enable this:
             *
             * usernameCookie.setSecure(true);
             */

            response.addCookie(
                    usernameCookie
            );

        } else {

            Cookie usernameCookie =
                    new Cookie(
                            "username",
                            ""
                    );

            usernameCookie.setMaxAge(0);

            usernameCookie.setHttpOnly(
                    true
            );

            usernameCookie.setPath(
                    request.getContextPath()
                            .isEmpty()
                            ? "/"
                            : request.getContextPath()
            );

            response.addCookie(
                    usernameCookie
            );
        }

        /*
         * =====================================================
         * 11. REDIRECT BY ROLE
         * =====================================================
         */
        redirectAfterLogin(
                request,
                response,
                loggedUser
        );
    }

    /*
     * =========================================================
     * ROLE BASED REDIRECT
     * =========================================================
     */
    private void redirectAfterLogin(
            HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws IOException {

        String role =
                user.getRole();

        /*
         * Keep customer on public/customer home.
         */
        if ("customer".equalsIgnoreCase(
                role)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/"
            );

            return;
        }

        /*
         * Staff mainly works with
         * customer / booking management.
         */
        if ("staff".equalsIgnoreCase(
                role)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/customers"
            );

            return;
        }

        /*
         * Administrator manages rooms,
         * promotions, reviews, etc.
         */
        if ("admin".equalsIgnoreCase(
                role)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/room"
            );

            return;
        }

        /*
         * Manager goes to reports/dashboard.
         */
        if ("manager".equalsIgnoreCase(
                role)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/reports"
            );

            return;
        }

        /*
         * IT Support currently does not have
         * a dedicated module in source.
         */
        response.sendRedirect(
                request.getContextPath()
                + "/"
        );
    }

    @Override
    public String getServletInfo() {

        return "Authenticates users and creates "
                + "role-aware sessions.";
    }
}