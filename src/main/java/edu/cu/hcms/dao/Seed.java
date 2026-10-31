package edu.cu.hcms.dao;

import edu.cu.hcms.model.User;
import edu.cu.hcms.util.Db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Inserts demo users and complaints when the database is empty. */
public final class Seed {
    private Seed() { }

    // {user index, title, category, priority, description, status, created, resolved, warden remark}
    private static final String[][] COMPLAINTS = {
        {"1", "Ceiling fan not working", "Electrical", "High", "The ceiling fan in room 214 has stopped working since last night. The regulator is fine, so the motor seems to be faulty.", "Resolved", "2026-10-05 09:15", "2026-10-06 16:40", "Fan motor replaced by the electrician."},
        {"1", "Wi-Fi very slow in Block B", "Wi-Fi / Internet", "Medium", "Internet speed on the second floor of Block B drops below 1 Mbps after 8 PM, which affects online classes and lab submissions.", "In Progress", "2026-10-21 20:30", "", "Network team has been informed and will check the router on Monday."},
        {"1", "Water leakage in washroom", "Plumbing", "High", "Water is leaking continuously from the wash basin pipe and the washroom floor stays wet all day. It is a slipping hazard.", "Pending", "2026-10-29 07:50", "", ""},
        {"2", "Mess food quality", "Mess / Food", "Medium", "Dal and vegetables served at dinner are often undercooked and cold. Please inspect the kitchen hygiene and cooking process.", "In Progress", "2026-10-18 21:10", "", "Mess committee meeting scheduled; menu will be reviewed."},
        {"2", "Broken study table", "Furniture", "Low", "The study table in room 108 has a broken leg and is unstable. Request replacement.", "Resolved", "2026-10-02 11:00", "2026-10-04 12:30", "New table delivered from the store."},
        {"2", "Corridor lights not working", "Electrical", "Medium", "Three tube lights in the Block A first floor corridor are fused, making it very dark at night.", "Resolved", "2026-10-09 19:45", "2026-10-10 11:15", "Tube lights replaced."},
        {"3", "Room not cleaned for a week", "Cleaning", "Medium", "The housekeeping staff has not cleaned room 305 and the common washroom for more than a week.", "Resolved", "2026-10-12 10:20", "2026-10-12 17:05", "Housekeeping supervisor informed; cleaning done."},
        {"3", "Geyser not heating", "Electrical", "High", "The geyser in the Block C washroom is not heating water. Students are facing problems in the morning.", "In Progress", "2026-10-25 06:40", "", "Technician has ordered a new heating element."},
        {"3", "Main gate CCTV not working", "Security", "High", "The CCTV camera near the Block C entrance has been offline for three days.", "Pending", "2026-10-28 22:05", "", ""},
        {"4", "Window glass broken", "Furniture", "Medium", "The window glass in room 112 is cracked and cold wind enters at night.", "Pending", "2026-10-27 14:25", "", ""},
        {"4", "Water cooler out of order", "Plumbing", "Medium", "The water cooler on the ground floor of Block D is not cooling and gives a bad smell.", "Resolved", "2026-10-14 13:00", "2026-10-15 15:20", "Filter cleaned and cooler repaired."},
        {"4", "Mosquito problem near hostel", "Cleaning", "Low", "Stagnant water behind the hostel building has caused mosquito breeding. Fogging is required.", "Rejected", "2026-10-16 18:00", "", "Fogging is already scheduled by the university; request merged with the existing plan."},
        {"2", "Wi-Fi not connecting in room 108", "Wi-Fi / Internet", "High", "The Wi-Fi access point in room 108 shows no internet even though other rooms work properly.", "Resolved", "2026-10-19 15:30", "2026-10-19 19:10", "Access point restarted and LAN cable replaced."},
        {"3", "Mess timing issue", "Mess / Food", "Low", "Breakfast ends at 8:30 AM which is early for students having 9 AM classes. Request extension by 30 minutes.", "Pending", "2026-10-30 08:10", "", ""}
    };

    public static void run() throws SQLException {
        UserDao users = new UserDao();
        String created = "2026-09-20 10:00";
        User[] u = {
            users.create("Mr. Rakesh Mehta", null, "warden@hostel.edu", "admin", null, null, "admin123", created),
            users.create("Keshav Kumar", "26MCC20092", "keshav@student.edu", "student", "B", "214", "student123", created),
            users.create("Rahul Sharma", "26MCC20105", "rahul@student.edu", "student", "A", "108", "student123", created),
            users.create("Aman Verma", "26MCC20118", "aman@student.edu", "student", "C", "305", "student123", created),
            users.create("Priya Singh", "26MCA10047", "priya@student.edu", "student", "D", "112", "student123", created)
        };
        List<String[]> rows = new ArrayList<>(List.of(COMPLAINTS));
        rows.sort((a, b) -> a[6].compareTo(b[6]));
        try (Connection c = Db.get()) {
            int n = 1;
            for (String[] r : rows) {
                User owner = u[Integer.parseInt(r[0])];
                String resolved = r[7].isEmpty() ? null : r[7];
                String updated = resolved != null ? resolved : r[6];
                long id;
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO complaints (ticket_no, user_id, title, "
                        + "category, priority, description, block, room, status, created_at, updated_at, resolved_at) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)")) {
                    ps.setString(1, String.format("HCMS-%s-%04d", r[6].substring(0, 4), n++));
                    ps.setLong(2, owner.id());
                    ps.setString(3, r[1]);
                    ps.setString(4, r[2]);
                    ps.setString(5, r[3]);
                    ps.setString(6, r[4]);
                    ps.setString(7, owner.block());
                    ps.setString(8, owner.room());
                    ps.setString(9, r[5]);
                    ps.setString(10, r[6]);
                    ps.setString(11, updated);
                    ps.setString(12, resolved);
                    ps.executeUpdate();
                    id = Db.lastId(c);
                }
                insertUpdate(c, id, owner.id(), "Complaint registered.", "Pending", r[6]);
                if (!r[8].isEmpty()) {
                    insertUpdate(c, id, u[0].id(), r[8], r[5], updated);
                }
            }
        }
    }

    private static void insertUpdate(Connection c, long cid, long uid, String msg, String st, String ts)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO updates "
                + "(complaint_id, user_id, message, status_to, created_at) VALUES (?,?,?,?,?)")) {
            ps.setLong(1, cid);
            ps.setLong(2, uid);
            ps.setString(3, msg);
            ps.setString(4, st);
            ps.setString(5, ts);
            ps.executeUpdate();
        }
    }
}
