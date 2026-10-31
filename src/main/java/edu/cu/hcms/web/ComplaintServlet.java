package edu.cu.hcms.web;

import edu.cu.hcms.dao.ComplaintDao;
import edu.cu.hcms.model.Complaint;
import edu.cu.hcms.model.User;
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

/**
 * GET  /complaint/new          new complaint form          POST /complaint/new          save complaint
 * GET  /complaint/{id}         details and timeline        POST /complaint/{id}/update  warden changes status
 *                                                          POST /complaint/{id}/comment add comment
 */
@WebServlet("/complaint/*")
public class ComplaintServlet extends BaseServlet {
    private final ComplaintDao dao = new ComplaintDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo() == null ? "" : req.getPathInfo();
        User u = user(req);
        try {
            if (path.equals("/new")) {
                if (u.isAdmin()) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                req.setAttribute("f", Map.of("block", u.block(), "room", u.room()));
                render(req, resp, "new_complaint", "New Complaint");
                return;
            }
            Complaint c = load(req, resp, path.substring(path.isEmpty() ? 0 : 1));
            if (c == null) {
                return;
            }
            req.setAttribute("c", c);
            req.setAttribute("timeline", dao.timeline(c.id()));
            render(req, resp, "detail", c.ticketNo());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo() == null ? "" : req.getPathInfo();
        User u = user(req);
        try {
            if (path.equals("/new")) {
                createComplaint(req, resp, u);
                return;
            }
            String[] parts = path.split("/");              // "", id, action
            if (parts.length != 3) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Complaint c = load(req, resp, parts[1]);
            if (c == null) {
                return;
            }
            if (parts[2].equals("update") && u.isAdmin()) {
                String status = param(req, "status");
                if (!Config.STATUSES.contains(status)) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid status");
                    return;
                }
                dao.changeStatus(c.id(), u.id(), status, param(req, "remarks"));
                Flash.add(req.getSession(), "ok", "Complaint updated.");
            } else if (parts[2].equals("comment")) {
                String msg = param(req, "message");
                if (msg.length() < 3) {
                    Flash.add(req.getSession(), "error", "Comment is too short.");
                } else {
                    dao.addComment(c.id(), u.id(), msg);
                    Flash.add(req.getSession(), "ok", "Comment added.");
                }
            } else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            redirect(req, resp, "/complaint/" + c.id());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void createComplaint(HttpServletRequest req, HttpServletResponse resp, User u)
            throws SQLException, ServletException, IOException {
        if (u.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Map<String, String> f = params(req, "title", "category", "priority", "description", "block", "room");
        List<String> errors = new ArrayList<>();
        if (f.get("title").length() < 5 || f.get("title").length() > 80) {
            errors.add("Title must be 5 to 80 characters long.");
        }
        if (!Config.CATEGORIES.contains(f.get("category")) || !Config.PRIORITIES.contains(f.get("priority"))) {
            errors.add("Choose a valid category and priority.");
        }
        if (f.get("description").length() < 20) {
            errors.add("Please describe the problem in at least 20 characters.");
        }
        if (!Config.BLOCKS.contains(f.get("block")) || !Validator.isRoom(f.get("room"))) {
            errors.add("Block or room number is invalid.");
        }
        if (!errors.isEmpty()) {
            for (String e : errors) {
                Flash.add(req.getSession(), "error", e);
            }
            req.setAttribute("f", f);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            render(req, resp, "new_complaint", "New Complaint");
            return;
        }
        long id = dao.create(u, f.get("title"), f.get("category"), f.get("priority"), f.get("description"),
                f.get("block"), f.get("room"));
        Flash.add(req.getSession(), "ok", "Complaint submitted successfully.");
        redirect(req, resp, "/complaint/" + id);
    }

    /** Loads a complaint; sends 404 / 403 and returns null if it is missing or belongs to another student. */
    private Complaint load(HttpServletRequest req, HttpServletResponse resp, String idText)
            throws SQLException, IOException {
        long id;
        try {
            id = Long.parseLong(idText);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        Complaint c = dao.find(id);
        if (c == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        User u = user(req);
        if (!u.isAdmin() && c.userId() != u.id()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return c;
    }
}
