package edu.cu.hcms.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** SQLite connection helper. Database file: &lt;user home&gt;/HostelComplaintDB/hostel.db */
public final class Db {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static String url;

    private static final String[] SCHEMA = {
        "CREATE TABLE IF NOT EXISTS users ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, uid TEXT UNIQUE, email TEXT NOT NULL UNIQUE,"
            + "role TEXT NOT NULL DEFAULT 'student' CHECK (role IN ('student','admin')), block TEXT, room TEXT,"
            + "password_hash TEXT NOT NULL, created_at TEXT NOT NULL)",
        "CREATE TABLE IF NOT EXISTS complaints ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, ticket_no TEXT NOT NULL UNIQUE,"
            + "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, title TEXT NOT NULL,"
            + "category TEXT NOT NULL, priority TEXT NOT NULL DEFAULT 'Medium', description TEXT NOT NULL,"
            + "block TEXT NOT NULL, room TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'Pending',"
            + "created_at TEXT NOT NULL, updated_at TEXT NOT NULL, resolved_at TEXT)",
        "CREATE TABLE IF NOT EXISTS updates ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "complaint_id INTEGER NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,"
            + "user_id INTEGER NOT NULL REFERENCES users(id), message TEXT NOT NULL, status_to TEXT,"
            + "created_at TEXT NOT NULL)",
        "CREATE INDEX IF NOT EXISTS idx_complaints_user ON complaints(user_id)",
        "CREATE INDEX IF NOT EXISTS idx_complaints_status ON complaints(status)"
    };

    private Db() { }

    /** Use a different database file (used by the unit tests). */
    public static synchronized void use(String path) {
        url = "jdbc:sqlite:" + path;
    }

    private static synchronized String url() {
        if (url == null) {
            File dir = new File(System.getProperty("user.home"), "HostelComplaintDB");
            dir.mkdirs();
            url = "jdbc:sqlite:" + new File(dir, "hostel.db").getAbsolutePath();
        }
        return url;
    }

    public static Connection get() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
        Connection c = DriverManager.getConnection(url());
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("PRAGMA busy_timeout = 5000");
        }
        return c;
    }

    /** Creates the tables if they do not exist. */
    public static void init() throws SQLException {
        try (Connection c = get(); Statement st = c.createStatement()) {
            for (String sql : SCHEMA) {
                st.execute(sql);
            }
        }
    }

    public static long lastId(Connection c) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public static String now() {
        return LocalDateTime.now().format(FMT);
    }
}
