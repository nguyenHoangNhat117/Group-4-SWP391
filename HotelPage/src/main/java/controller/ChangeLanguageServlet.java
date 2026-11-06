package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

import util.LanguageSupport;

@WebServlet(
        name = "ChangeLanguageServlet",
        urlPatterns = "/change-language"
)
public class ChangeLanguageServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        sendPayload(
                request,
                response,
                200,
                null
        );
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");

        if (!LanguageSupport.validToken(request)) {
            sendPayload(
                    request,
                    response,
                    403,
                    "language.expired"
            );

            return;
        }

        String[] languages
                = request.getParameterValues("lang");

        if (languages == null
                || languages.length != 1
                || !LanguageSupport.supported(languages[0])) {

            sendPayload(
                    request,
                    response,
                    400,
                    "language.invalid"
            );

            return;
        }

        try {
            LanguageSupport.save(
                    request,
                    response,
                    languages[0]
            );

        } catch (IllegalStateException ex) {
            log(
                    "Cannot save UC24 preference",
                    ex
            );

            response.sendError(503);
            return;
        }

        sendPayload(
                request,
                response,
                200,
                null
        );
    }

    private void sendPayload(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String errorKey
    ) throws IOException {

        String language
                = LanguageSupport.resolve(request);

        Map<String, String> messages
                = LanguageSupport.messages(language);

        response.setStatus(status);

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        response.setHeader(
                "Cache-Control",
                "no-store"
        );

        response.setHeader(
                "X-Content-Type-Options",
                "nosniff"
        );

        StringBuilder json
                = new StringBuilder("{\"lang\":");

        json.append(
                LanguageSupport.json(language)
        );

        json.append(",\"csrfToken\":");

        json.append(
                LanguageSupport.json(
                        LanguageSupport.token(
                                request.getSession()
                        )
                )
        );

        json.append(",\"error\":");

        json.append(
                errorKey == null
                        ? "null"
                        : LanguageSupport.json(
                                messages.get(errorKey)
                        )
        );

        json.append(",\"messages\":{");

        boolean first = true;

        for (Map.Entry<String, String> entry
                : messages.entrySet()) {

            if (!first) {
                json.append(',');
            }

            first = false;

            json.append(
                    LanguageSupport.json(
                            entry.getKey()
                    )
            );

            json.append(':');

            json.append(
                    LanguageSupport.json(
                            entry.getValue()
                    )
            );
        }

        json.append("}}");

        response.getWriter().write(
                json.toString()
        );
    }
}
