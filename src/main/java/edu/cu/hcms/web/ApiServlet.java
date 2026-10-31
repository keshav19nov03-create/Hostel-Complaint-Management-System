package edu.cu.hcms.web;

import edu.cu.hcms.dao.ComplaintDao;
import edu.cu.hcms.model.Complaint;
import edu.cu.hcms.util.Config;
import edu.cu.hcms.util.Json;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * JSON API (warden only).
 * GET  /api/stats                      statistics
 * POST /api/complaint/{id}/status      body {"status":"Resolved"}
 */
@WebServlet("/api/*")
public class ApiServlet extends BaseServlet {
    private static final Pattern STATUS = Pattern.compile("\"status\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern STATUS_PATH = Pattern.compile("/complaint/(\\d+)/status");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"/stats".equals(req.getPathInfo())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            json(resp, 200, new ComplaintDao().stats(null).toJson());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Matcher pm = STATUS_PATH.matcher(req.getPathInfo() == null ? "" : req.getPathInfo());
        if (!pm.matches()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String body = req.getReader().lines().collect(Collectors.joining());
        Matcher sm = STATUS.matcher(body);
        String status = sm.find() ? sm.group(1) : "";
        if (!Config.STATUSES.contains(status)) {
            json(resp, 400, "{\"ok\":false,\"error\":\"Invalid status\"}");
            return;
        }
        try {
            ComplaintDao dao = new ComplaintDao();
            Complaint c = dao.find(Long.parseLong(pm.group(1)));
            if (c == null) {
                json(resp, 404, "{\"ok\":false,\"error\":\"Complaint not found\"}");
                return;
            }
            dao.changeStatus(c.id(), user(req).id(), status, "");
            json(resp, 200, "{\"ok\":true,\"id\":" + c.id() + ",\"status\":" + Json.quote(status)
                    + ",\"stats\":" + dao.stats(null).toJson() + "}");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void json(HttpServletResponse resp, int code, String body) throws IOException {
        resp.setStatus(code);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(body);
    }
}
