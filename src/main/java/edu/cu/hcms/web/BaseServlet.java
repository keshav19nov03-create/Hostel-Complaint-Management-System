package edu.cu.hcms.web;

import edu.cu.hcms.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Common helper methods for all servlets. */
public abstract class BaseServlet extends HttpServlet {
    protected User user(HttpServletRequest req) {
        return (User) req.getAttribute("user");
    }

    /** Forwards to /WEB-INF/jsp/&lt;page&gt;.jsp */
    protected void render(HttpServletRequest req, HttpServletResponse resp, String page, String title)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", title);
        req.getRequestDispatcher("/WEB-INF/jsp/" + page + ".jsp").forward(req, resp);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    /** Trimmed request parameter, never null. */
    protected String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    protected Map<String, String> params(HttpServletRequest req, String... names) {
        Map<String, String> m = new LinkedHashMap<>();
        for (String n : names) {
            m.put(n, param(req, n));
        }
        return m;
    }
}
