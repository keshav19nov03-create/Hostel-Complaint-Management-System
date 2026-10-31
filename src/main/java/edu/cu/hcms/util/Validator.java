package edu.cu.hcms.util;

import java.util.regex.Pattern;

/** Server side input validation rules. */
public final class Validator {
    private static final Pattern UID = Pattern.compile("\\d{2}[A-Z]{3}\\d{5}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    private Validator() { }

    public static boolean isUid(String s) {
        return s != null && UID.matcher(s).matches();
    }

    public static boolean isEmail(String s) {
        return s != null && EMAIL.matcher(s).matches();
    }

    public static boolean isRoom(String s) {
        return s != null && s.matches("\\d{1,5}");
    }
}
