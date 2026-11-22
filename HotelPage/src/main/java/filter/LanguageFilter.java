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

import jakarta.servlet.jsp.jstl.core.Config;

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
    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi-VN");

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

        req.setAttribute("i18n", LanguageSupport.messages());
        Config.set(req, Config.FMT_LOCALE, VIETNAMESE);
        res.setHeader("Content-Language", "vi");

        HttpServletRequestWrapper wrapped
                = new HttpServletRequestWrapper(req) {

            @Override
            public Locale getLocale() {
                return VIETNAMESE;
            }

            @Override
            public Enumeration<Locale> getLocales() {
                return Collections.enumeration(
                        Collections.singletonList(VIETNAMESE)
                );
            }
        };

        chain.doFilter(wrapped, response);
    }
}
