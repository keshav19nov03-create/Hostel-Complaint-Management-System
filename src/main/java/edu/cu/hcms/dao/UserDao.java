package edu.cu.hcms.dao;

import edu.cu.hcms.model.User;
import edu.cu.hcms.util.Db;
import edu.cu.hcms.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/** JDBC access to the users table. */
public class UserDao {
    private static final String COLS = "id, name, uid, email, role, block, room, created_at";

    private static User map(ResultSet rs) throws SQLException {
        return new User(rs.getLong("id"), rs.getString("name"), rs.getString("uid"), rs.getString("email"),
                rs.getString("role"), rs.getString("block"), rs.getString("room"), rs.getString("created_at"));
    }

    public int count() throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** True if the e-mail or the UID is already registered. */
    public boolean exists(String email, String uid) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM users WHERE email = ? OR uid = ?")) {
            ps.setString(1, email.toLowerCase());
            if (uid == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, uid);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public User create(String name, String uid, String email, String role, String block, String room,
                       String password) throws SQLException {
        return create(name, uid, email, role, block, room, password, Db.now());
    }

    public User create(String name, String uid, String email, String role, String block, String room,
                       String password, String createdAt) throws SQLException {
        long id;
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO users "
                     + "(name, uid, email, role, block, room, password_hash, created_at) VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, name);
            if (uid == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, uid);
            }
            ps.setString(3, email.toLowerCase());
            ps.setString(4, role);
            if (block == null) {
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
            } else {
                ps.setString(5, block);
                ps.setString(6, room);
            }
            ps.setString(7, PasswordUtil.hash(password));
            ps.setString(8, createdAt);
            ps.executeUpdate();
            id = Db.lastId(c);
        }
        return findById(id);
    }

    public User findById(long id) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM users WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Returns the user if e-mail and password are correct, otherwise null. */
    public User authenticate(String email, String password) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT id, password_hash FROM users WHERE email = ?")) {
            ps.setString(1, email.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.verify(password, rs.getString("password_hash"))) {
                    return findById(rs.getLong("id"));
                }
                return null;
            }
        }
    }
}
