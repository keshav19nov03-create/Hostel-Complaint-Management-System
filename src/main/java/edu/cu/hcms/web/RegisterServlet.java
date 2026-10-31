package edu.cu.hcms.web;

import edu.cu.hcms.dao.UserDao;
import edu.cu.hcms.util.Config;
import edu.cu.hcms.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/register")
public class RegisterServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("f", params(req));
        render(req, resp, "register", "Register");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> f = params(req, "name", "uid", "email", "block", "room");
        f.put("uid", f.get("uid").toUpperCase());
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        List<String> errors = new ArrayList<>();
        if (f.get("name").length() < 3) {
            errors.add("Name must have at least 3 characters.");
        }
        if (!Validator.isUid(f.get("uid"))) {
            errors.add("UID must look like 26MCC20092.");
        }
        if (!Validator.isEmail(f.get("email"))) {
            errors.add("Enter a valid e-mail address.");
        }
        if (!Config.BLOCKS.contains(f.get("block")) || !Validator.isRoom(f.get("room"))) {
            errors.add("Select a block and enter a numeric room number.");
        }
        if (password.length() < 6) {
            errors.add("Password must have at least 6 characters.");
        }
        try {
            UserDao dao = new UserDao();
            if (errors.isEmpty() && dao.exists(f.get("email"), f.get("uid"))) {
                errors.add("An account with this e-mail or UID already exists.");
            }
            if (!errors.isEmpty()) {
                for (String e : errors) {
                    Flash.add(req.getSession(), "error", e);
                }
                req.setAttribute("f", f);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                render(req, resp, "register", "Register");
                return;
            }
            dao.create(f.get("name"), f.get("uid"), f.get("email"), "student", f.get("block"), f.get("room"),
                    password);
            Flash.add(req.getSession(), "ok", "Registration successful. Please log in.");
            redirect(req, resp, "/login");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
