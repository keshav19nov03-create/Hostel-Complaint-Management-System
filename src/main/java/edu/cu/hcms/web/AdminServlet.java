package edu.cu.hcms.web;

import edu.cu.hcms.dao.ComplaintDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/** Warden dashboard: all complaints, filters and statistics (access is checked by SecurityFilter). */
@WebServlet("/admin")
public class AdminServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> flt = params(req, "q", "status", "category", "priority");
        try {
            ComplaintDao dao = new ComplaintDao();
            req.setAttribute("flt", flt);
            req.setAttribute("rows", dao.search(null, flt.get("status"), flt.get("category"),
                    flt.get("priority"), flt.get("q")));
            req.setAttribute("stats", dao.stats(null));
            render(req, resp, "admin", "Warden Panel");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
