package edu.cu.hcms.util;

import java.util.List;
import java.util.Map;

/** Small helpers used by the JSP pages. */
public final class Html {
    private Html() { }

    /** Escapes text so that it is safe to print inside HTML. */
    public static String esc(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String status(String s) {
        return "<span class=\"badge s-" + s.toLowerCase().replace(' ', '-') + "\">" + esc(s) + "</span>";
    }

    public static String prio(String p) {
        return "<span class=\"badge p-" + p.toLowerCase() + "\">" + esc(p) + "</span>";
    }

    /** Value of a form field from a map (escaped, empty if missing). */
    public static String val(Map<String, String> m, String key) {
        String v = m == null ? null : m.get(key);
        return v == null ? "" : esc(v);
    }

    /** &lt;option&gt; tags for a drop-down; the current value is selected. */
    public static String options(List<String> items, String current, boolean withAll) {
        StringBuilder sb = new StringBuilder();
        if (withAll) {
            sb.append("<option value=\"\">All</option>");
        }
        for (String it : items) {
            sb.append("<option").append(it.equals(current) ? " selected" : "").append(">")
              .append(esc(it)).append("</option>");
        }
        return sb.toString();
    }
}
