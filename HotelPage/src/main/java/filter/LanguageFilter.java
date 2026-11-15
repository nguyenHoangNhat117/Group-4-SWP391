package filter;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;

import util.LanguageSupport;

@WebFilter(
        filterName = "LanguageFilter",
        urlPatterns = "/*",
        dispatcherTypes = {
            DispatcherType.REQUEST,
            DispatcherType.FORWARD
        }
)
public class LanguageFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest req
                = (HttpServletRequest) request;

        HttpServletResponse res
                = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(
                req.getContextPath().length()
        );

        if (path.startsWith("/assets/")
                || path.startsWith("/script/")
                || path.equals("/favicon.ico")) {

            chain.doFilter(request, response);
            return;
        }

        if (req.getCharacterEncoding() == null) {
            req.setCharacterEncoding("UTF-8");
        }

        res.setCharacterEncoding("UTF-8");
        res.setHeader("Cache-Control", "no-store");

        String language
                = LanguageSupport.resolve(req);

        HttpSession session = req.getSession();

        // Đọc lựa chọn cho request này; không ghi lại ngôn ngữ cũ vào session.
        // Chỉ ChangeLanguageServlet được lưu lựa chọn mới.

        req.setAttribute(
                "uiLanguage",
                language
        );

        req.setAttribute(
                "i18n",
                LanguageSupport.messages(language)
        );

        req.setAttribute(
                "uc24Token",
                LanguageSupport.token(session)
        );

        Locale locale
                = Locale.forLanguageTag(language);

        HttpServletRequestWrapper wrapped
                = new HttpServletRequestWrapper(req) {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public Enumeration<Locale> getLocales() {
                return Collections.enumeration(
                        Collections.singletonList(locale)
                );
            }
        };

        chain.doFilter(wrapped, response);
    }
}
