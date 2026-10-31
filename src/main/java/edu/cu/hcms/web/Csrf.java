package edu.cu.hcms.web;

import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;

/** Per-session CSRF token. */
public final class Csrf {
    private static final SecureRandom RANDOM = new SecureRandom();

    private Csrf() { }

    public static String token(HttpSession session) {
        String t = (String) session.getAttribute("csrf");
        if (t == null) {
            byte[] b = new byte[16];
            RANDOM.nextBytes(b);
            StringBuilder sb = new StringBuilder();
            for (byte x : b) {
                sb.append(String.format("%02x", x));
            }
            t = sb.toString();
            session.setAttribute("csrf", t);
        }
        return t;
    }
}
