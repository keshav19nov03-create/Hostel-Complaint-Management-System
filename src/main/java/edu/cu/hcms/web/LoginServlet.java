package edu.cu.hcms.web;

import edu.cu.hcms.dao.UserDao;
import edu.cu.hcms.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (user(req) != null) {
            redirect(req, resp, "/dashboard");
            return;
        }
        render(req, resp, "login", "Login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User u = new UserDao().authenticate(param(req, "email"), req.getParameter("password") == null
                    ? "" : req.getParameter("password"));
            if (u == null) {
                Flash.add(req.getSession(), "error", "Invalid e-mail or password.");
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                render(req, resp, "login", "Login");
                return;
            }
            req.getSession().invalidate();            // new session id after login
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", u.id());
            Flash.add(session, "ok", "Welcome back, " + u.name() + "!");
            redirect(req, resp, "/dashboard");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
