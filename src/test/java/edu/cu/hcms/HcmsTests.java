package edu.cu.hcms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.cu.hcms.dao.ComplaintDao;
import edu.cu.hcms.dao.UserDao;
import edu.cu.hcms.model.Complaint;
import edu.cu.hcms.model.Stats;
import edu.cu.hcms.model.User;
import edu.cu.hcms.util.Db;
import edu.cu.hcms.util.PasswordUtil;
import edu.cu.hcms.util.Validator;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** 14 automated test cases: security helpers, validation, users, complaints, timeline and statistics. */
class HcmsTests {
    private final UserDao users = new UserDao();
    private final ComplaintDao complaints = new ComplaintDao();

    @BeforeEach
    void freshDatabase(@TempDir Path dir) throws SQLException {
        Db.use(dir.resolve("test.db").toString());   // every test gets its own empty database file
        Db.init();
    }

    private User student(String email, String uid) throws SQLException {
        return users.create("Keshav Kumar", uid, email, "student", "B", "214", "student123");
    }

    private User warden() throws SQLException {
        return users.create("Warden", null, "warden@hostel.edu", "admin", null, null, "admin123");
    }

    private long fan(User u) throws SQLException {
        return complaints.create(u, "Fan not working", "Electrical", "High",
                "The ceiling fan in my room has stopped working completely.", "B", "214");
    }

    @Test
    void t01_passwordIsHashedAndVerified() {
        String hash = PasswordUtil.hash("student123");
        assertNotEquals("student123", hash);
        assertTrue(PasswordUtil.verify("student123", hash));
    }

    @Test
    void t02_wrongPasswordIsRejected() {
        assertFalse(PasswordUtil.verify("wrong", PasswordUtil.hash("student123")));
        assertFalse(PasswordUtil.verify("student123", "not-a-valid-hash"));
    }

    @Test
    void t03_samePasswordGivesDifferentHashes() {
        assertNotEquals(PasswordUtil.hash("abc123"), PasswordUtil.hash("abc123"));
    }

    @Test
    void t04_validUidAndEmailAccepted() {
        assertTrue(Validator.isUid("26MCC20092"));
        assertTrue(Validator.isEmail("keshav@student.edu"));
        assertTrue(Validator.isRoom("214"));
    }

    @Test
    void t05_invalidUidEmailAndRoomRejected() {
        assertFalse(Validator.isUid("BADUID"));
        assertFalse(Validator.isUid("26mcc20092"));
        assertFalse(Validator.isEmail("keshav@"));
        assertFalse(Validator.isRoom("21A"));
    }

    @Test
    void t06_studentCanRegisterAndLogin() throws SQLException {
        student("keshav@student.edu", "26MCC20092");
        User u = users.authenticate("keshav@student.edu", "student123");
        assertNotNull(u);
        assertEquals("26MCC20092", u.uid());
        assertFalse(u.isAdmin());
    }

    @Test
    void t07_wrongPasswordOrUnknownEmailGivesNoUser() throws SQLException {
        student("keshav@student.edu", "26MCC20092");
        assertNull(users.authenticate("keshav@student.edu", "wrong"));
        assertNull(users.authenticate("nobody@student.edu", "student123"));
    }

    @Test
    void t08_duplicateEmailOrUidIsDetected() throws SQLException {
        student("keshav@student.edu", "26MCC20092");
        assertTrue(users.exists("KESHAV@student.edu", "26MCC99999"));   // same e-mail (case-insensitive)
        assertTrue(users.exists("other@student.edu", "26MCC20092"));    // same UID
        assertFalse(users.exists("other@student.edu", "26MCC99999"));
    }

    @Test
    void t09_complaintGetsTicketNumberAndPendingStatus() throws SQLException {
        User u = student("keshav@student.edu", "26MCC20092");
        Complaint c = complaints.find(fan(u));
        assertTrue(c.ticketNo().matches("HCMS-\\d{4}-0001"));
        assertEquals("Pending", c.status());
        assertNull(c.resolvedAt());
    }

    @Test
    void t10_resolvingStoresResolvedAtAndUpdatesStats() throws SQLException {
        User u = student("keshav@student.edu", "26MCC20092");
        User w = warden();
        long id = fan(u);
        complaints.changeStatus(id, w.id(), "Resolved", "Fan replaced.");
        assertNotNull(complaints.find(id).resolvedAt());
        Stats s = complaints.stats(null);
        assertEquals(1, s.total());
        assertEquals(1, s.count("Resolved"));
        assertEquals(0, s.count("Pending"));
        assertNotNull(s.avgResolutionHours());
    }

    @Test
    void t11_timelineRecordsEveryChange() throws SQLException {
        User u = student("keshav@student.edu", "26MCC20092");
        User w = warden();
        long id = fan(u);
        complaints.changeStatus(id, w.id(), "In Progress", "Electrician assigned.");
        complaints.addComment(id, u.id(), "Please hurry, it is very hot.");
        assertEquals(3, complaints.timeline(id).size());
        assertTrue(complaints.timeline(id).get(1).byAdmin());
    }

    @Test
    void t12_searchAndFilterWork() throws SQLException {
        User u = student("keshav@student.edu", "26MCC20092");
        fan(u);
        complaints.create(u, "Water leakage", "Plumbing", "Medium",
                "Water is leaking from the wash basin pipe in the washroom.", "B", "214");
        assertEquals(1, complaints.search(null, null, "Plumbing", null, null).size());
        assertEquals(1, complaints.search(null, null, null, null, "leakage").size());
        assertEquals(2, complaints.search(null, "Pending", null, null, null).size());
        assertEquals(0, complaints.search(null, "Resolved", null, null, null).size());
    }

    @Test
    void t13_studentSeesOnlyOwnComplaints() throws SQLException {
        User a = student("keshav@student.edu", "26MCC20092");
        User b = student("rahul@student.edu", "26MCC20105");
        fan(a);
        List<Complaint> forB = complaints.search(b.id(), null, null, null, null);
        assertTrue(forB.isEmpty());
        assertEquals(1, complaints.search(a.id(), null, null, null, null).size());
    }

    @Test
    void t14_statsJsonContainsAllGroups() throws SQLException {
        User u = student("keshav@student.edu", "26MCC20092");
        fan(u);
        String json = complaints.stats(u.id()).toJson();
        assertTrue(json.contains("\"total\":1"));
        assertTrue(json.contains("\"by_status\":{\"Pending\":1,\"In Progress\":0,\"Resolved\":0,\"Rejected\":0}"));
        assertTrue(json.contains("\"Electrical\":1"));
    }
}
