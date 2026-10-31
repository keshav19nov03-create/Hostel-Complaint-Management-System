package edu.cu.hcms.web;

import edu.cu.hcms.dao.ComplaintDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/** "My Complaints": the logged-in student's complaints with search and filters. */
@WebServlet("/complaints")
public class ComplaintListServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> flt = params(req, "q", "status", "category", "priority");
        try {
            req.setAttribute("flt", flt);
            req.setAttribute("rows", new ComplaintDao().search(user(req).id(), flt.get("status"),
                    flt.get("category"), flt.get("priority"), flt.get("q")));
            render(req, resp, "complaints", "My Complaints");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
