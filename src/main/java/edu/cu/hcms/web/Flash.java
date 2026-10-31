package edu.cu.hcms.web;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/** One-time messages shown at the top of the next rendered page (category, text). */
public final class Flash {
    private Flash() { }

    @SuppressWarnings("unchecked")
    public static void add(HttpSession session, String category, String message) {
        List<String[]> list = (List<String[]>) session.getAttribute("flash");
        if (list == null) {
            list = new ArrayList<>();
            session.setAttribute("flash", list);
        }
        list.add(new String[] {category, message});
    }

    @SuppressWarnings("unchecked")
    public static List<String[]> take(HttpSession session) {
        List<String[]> list = (List<String[]>) session.getAttribute("flash");
        session.removeAttribute("flash");
        return list == null ? new ArrayList<>() : list;
    }
}
