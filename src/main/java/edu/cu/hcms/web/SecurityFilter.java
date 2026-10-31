package edu.cu.hcms.web;

import edu.cu.hcms.dao.UserDao;
import edu.cu.hcms.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;

/** Loads the logged-in user, enforces login, warden-only areas and CSRF tokens for every POST. */
@WebFilter("/*")
public class SecurityFilter implements Filter {
    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse resp = (HttpServletResponse) rs;
        req.setCharacterEncoding("UTF-8");

        String path = req.getServletPath();
        HttpSession session = req.getSession(false);
        User user = null;
        if (session != null && session.getAttribute("userId") != null) {
            try {
                user = new UserDao().findById((Long) session.getAttribute("userId"));
            } catch (SQLException e) {
                throw new ServletException(e);
            }
        }
        req.setAttribute("user", user);

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String token = req.getHeader("X-CSRF-Token");
            if (token == null) {
                token = req.getParameter("_csrf");
            }
            String expected = session == null ? null : (String) session.getAttribute("csrf");
            if (token == null || expected == null || !MessageDigest.isEqual(
                    token.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8))) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing CSRF token");
                return;
            }
        }

        boolean open = path.equals("/login") || path.equals("/register") || path.equals("/index.jsp")
                || path.startsWith("/static/");
        if (!open && user == null) {
            Flash.add(req.getSession(), "warn", "Please log in to continue.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        boolean wardenOnly = path.equals("/admin") || path.startsWith("/api");
        if (wardenOnly && (user == null || !user.isAdmin())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(req, resp);
    }
}
