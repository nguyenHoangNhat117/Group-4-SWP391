package controller;

import dao.DiscountDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "PromotionServlet", urlPatterns = {"/promotions"})
public class PromotionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DiscountDAO discountDAO = new DiscountDAO();
        request.setAttribute("promotions", discountDAO.getActivePromotions());
        request.getRequestDispatcher("/WEB-INF/promotion/promotions.jsp")
                .forward(request, response);
    }
}
