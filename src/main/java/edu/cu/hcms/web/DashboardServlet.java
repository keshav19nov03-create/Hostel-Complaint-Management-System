package edu.cu.hcms.web;

import edu.cu.hcms.dao.ComplaintDao;
import edu.cu.hcms.model.Complaint;
import edu.cu.hcms.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = user(req);
        if (u.isAdmin()) {
            redirect(req, resp, "/admin");
            return;
        }
        try {
            ComplaintDao dao = new ComplaintDao();
            List<Complaint> all = dao.search(u.id(), null, null, null, null);
            req.setAttribute("recent", all.size() > 5 ? all.subList(0, 5) : all);
            req.setAttribute("stats", dao.stats(u.id()));
            render(req, resp, "dashboard", "Dashboard");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
