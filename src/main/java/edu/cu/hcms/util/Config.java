package edu.cu.hcms.util;

import java.util.List;

/** Fixed lists used by forms, validation and statistics. */
public final class Config {
    private Config() { }

    public static final List<String> CATEGORIES = List.of("Electrical", "Plumbing", "Mess / Food", "Cleaning",
            "Wi-Fi / Internet", "Furniture", "Security", "Other");
    public static final List<String> PRIORITIES = List.of("Low", "Medium", "High");
    public static final List<String> STATUSES = List.of("Pending", "In Progress", "Resolved", "Rejected");
    public static final List<String> BLOCKS = List.of("A", "B", "C", "D");
}
