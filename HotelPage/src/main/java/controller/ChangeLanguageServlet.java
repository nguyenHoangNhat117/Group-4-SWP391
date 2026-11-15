package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import util.LanguageSupport;

@WebServlet(name = "ChangeLanguageServlet", urlPatterns = "/change-language")
public class ChangeLanguageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        send(req, res, 200, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        req.setCharacterEncoding("UTF-8");
        if (!LanguageSupport.validToken(req)) {
            send(req, res, 403, "language.expired");
            return;
        }
        String[] values = req.getParameterValues("lang");
        if (values == null || values.length != 1 || !LanguageSupport.supported(values[0])) {
            send(req, res, 400, "language.invalid");
            return;
        }
        try {
            LanguageSupport.save(req, res, values[0]);
        } catch (IllegalStateException ex) {
            log("Cannot save language preference", ex);
            res.sendError(503);
            return;
        }
        send(req, res, 200, null);
    }

    private void send(HttpServletRequest req, HttpServletResponse res, int status, String error) throws IOException {
        String lang = LanguageSupport.resolve(req);
        Map<String, String> messages = LanguageSupport.messages(lang);
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control", "no-store");
        res.setHeader("X-Content-Type-Options", "nosniff");
        StringBuilder out = new StringBuilder("{\"lang\":").append(LanguageSupport.json(lang));
        out.append(",\"csrfToken\":").append(LanguageSupport.json(LanguageSupport.token(req.getSession())));
        out.append(",\"error\":").append(LanguageSupport.json(error == null ? null : messages.get(error)));
        out.append(",\"messages\":");
        appendMap(out, messages);
        if ("GET".equals(req.getMethod()) && "1".equals(req.getParameter("catalog"))) {
            out.append(",\"lookup\":");
            appendMap(out, LanguageSupport.lookup());
        }
        out.append('}');
        res.getWriter().write(out.toString());
    }

    private void appendMap(StringBuilder out, Map<String, String> values) {
        out.append('{');
        boolean first = true;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (!first) {
                out.append(',');
            }
            first = false;
            out.append(LanguageSupport.json(entry.getKey())).append(':').append(LanguageSupport.json(entry.getValue()));
        }
        out.append('}');
    }
}
