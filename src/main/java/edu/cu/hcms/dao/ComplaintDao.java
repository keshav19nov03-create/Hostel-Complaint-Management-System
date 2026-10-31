package edu.cu.hcms.dao;

import edu.cu.hcms.model.Complaint;
import edu.cu.hcms.model.Stats;
import edu.cu.hcms.model.Update;
import edu.cu.hcms.model.User;
import edu.cu.hcms.util.Config;
import edu.cu.hcms.util.Db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** JDBC access to the complaints and updates (timeline) tables. */
public class ComplaintDao {
    private static final String BASE = "SELECT c.*, u.name AS student, u.uid AS student_uid "
            + "FROM complaints c JOIN users u ON u.id = c.user_id";

    private static Complaint map(ResultSet rs) throws SQLException {
        return new Complaint(rs.getLong("id"), rs.getString("ticket_no"), rs.getLong("user_id"),
                rs.getString("title"), rs.getString("category"), rs.getString("priority"),
                rs.getString("description"), rs.getString("block"), rs.getString("room"),
                rs.getString("status"), rs.getString("created_at"), rs.getString("updated_at"),
                rs.getString("resolved_at"), rs.getString("student"), rs.getString("student_uid"));
    }

    /** Registers a new complaint (status Pending) and writes the first timeline entry. */
    public long create(User u, String title, String category, String priority, String description,
                       String block, String room) throws SQLException {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                String ts = Db.now();
                String ticket = nextTicket(c, ts);
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO complaints (ticket_no, user_id, title, "
                        + "category, priority, description, block, room, status, created_at, updated_at) "
                        + "VALUES (?,?,?,?,?,?,?,?,'Pending',?,?)")) {
                    ps.setString(1, ticket);
                    ps.setLong(2, u.id());
                    ps.setString(3, title);
                    ps.setString(4, category);
                    ps.setString(5, priority);
                    ps.setString(6, description);
                    ps.setString(7, block);
                    ps.setString(8, room);
                    ps.setString(9, ts);
                    ps.setString(10, ts);
                    ps.executeUpdate();
                }
                long id = Db.lastId(c);
                addUpdate(c, id, u.id(), "Complaint registered.", "Pending", ts);
                c.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private static String nextTicket(Connection c, String ts) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM complaints");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.format("HCMS-%s-%04d", ts.substring(0, 4), rs.getInt(1) + 1);
        }
    }

    private static void addUpdate(Connection c, long complaintId, long userId, String message, String statusTo,
                                  String ts) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO updates "
                + "(complaint_id, user_id, message, status_to, created_at) VALUES (?,?,?,?,?)")) {
            ps.setLong(1, complaintId);
            ps.setLong(2, userId);
            ps.setString(3, message);
            ps.setString(4, statusTo);
            ps.setString(5, ts);
            ps.executeUpdate();
        }
    }

    /** Returns the complaint or null if it does not exist. */
    public Complaint find(long id) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(BASE + " WHERE c.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Search and filter. userId = null means all students (warden view). */
    public List<Complaint> search(Long userId, String status, String category, String priority, String q)
            throws SQLException {
        StringBuilder sql = new StringBuilder(BASE + " WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (userId != null) {
            sql.append(" AND c.user_id = ?");
            args.add(userId);
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND c.status = ?");
            args.add(status);
        }
        if (category != null && !category.isEmpty()) {
            sql.append(" AND c.category = ?");
            args.add(category);
        }
        if (priority != null && !priority.isEmpty()) {
            sql.append(" AND c.priority = ?");
            args.add(priority);
        }
        if (q != null && !q.trim().isEmpty()) {
            sql.append(" AND (c.title LIKE ? OR c.ticket_no LIKE ? OR u.name LIKE ?)");
            String like = "%" + q.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY c.created_at DESC, c.id DESC");
        List<Complaint> out = new ArrayList<>();
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        }
        return out;
    }

    /** Changes the status, stores resolved_at for Resolved and adds a timeline entry. */
    public void changeStatus(long id, long byUserId, String status, String remarks) throws SQLException {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                String ts = Db.now();
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE complaints SET status = ?, updated_at = ?, resolved_at = ? WHERE id = ?")) {
                    ps.setString(1, status);
                    ps.setString(2, ts);
                    ps.setString(3, "Resolved".equals(status) ? ts : null);
                    ps.setLong(4, id);
                    ps.executeUpdate();
                }
                String msg = remarks == null || remarks.trim().isEmpty() ? "Status changed to " + status + "."
                        : remarks.trim();
                addUpdate(c, id, byUserId, msg, status, ts);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void addComment(long id, long userId, String message) throws SQLException {
        try (Connection c = Db.get()) {
            addUpdate(c, id, userId, message, null, Db.now());
        }
    }

    public List<Update> timeline(long id) throws SQLException {
        List<Update> out = new ArrayList<>();
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT up.*, u.name AS uname, u.role AS urole "
                     + "FROM updates up JOIN users u ON u.id = up.user_id WHERE up.complaint_id = ? ORDER BY up.id")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Update(rs.getLong("id"), rs.getLong("complaint_id"), rs.getLong("user_id"),
                            rs.getString("message"), rs.getString("status_to"), rs.getString("created_at"),
                            rs.getString("uname"), rs.getString("urole")));
                }
            }
        }
        return out;
    }

    /** Statistics for one student (userId) or for everybody (null). */
    public Stats stats(Long userId) throws SQLException {
        String where = userId == null ? "" : " WHERE user_id = ?";
        try (Connection c = Db.get()) {
            Map<String, Integer> byStatus = new LinkedHashMap<>();
            for (String s : Config.STATUSES) {
                byStatus.put(s, 0);
            }
            byStatus.putAll(group(c, "status", where, userId));
            int total = 0;
            for (int n : byStatus.values()) {
                total += n;
            }
            Map<String, Integer> byCategory = group(c, "category", where, userId);
            Map<String, Integer> byPriority = group(c, "priority", where, userId);

            String avgSql = "SELECT AVG((julianday(resolved_at) - julianday(created_at)) * 24) FROM complaints"
                    + (userId == null ? " WHERE resolved_at IS NOT NULL"
                                      : " WHERE user_id = ? AND resolved_at IS NOT NULL");
            Double avg = null;
            try (PreparedStatement ps = c.prepareStatement(avgSql)) {
                if (userId != null) {
                    ps.setLong(1, userId);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double v = rs.getDouble(1);
                        if (!rs.wasNull()) {
                            avg = Math.round(v * 10.0) / 10.0;
                        }
                    }
                }
            }
            return new Stats(total, byStatus, byCategory, byPriority, avg);
        }
    }

    private static Map<String, Integer> group(Connection c, String col, String where, Long userId)
            throws SQLException {
        Map<String, Integer> m = new LinkedHashMap<>();
        String sql = "SELECT " + col + " AS k, COUNT(*) AS n FROM complaints" + where
                + " GROUP BY " + col + " ORDER BY " + col;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (userId != null) {
                ps.setLong(1, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    m.put(rs.getString("k"), rs.getInt("n"));
                }
            }
        }
        return m;
    }
}
