package edu.cu.hcms.model;

import edu.cu.hcms.util.Json;
import java.util.Map;

public record Stats(int total, Map<String, Integer> byStatus, Map<String, Integer> byCategory,
                    Map<String, Integer> byPriority, Double avgResolutionHours) {
    public int count(String status) {
        return byStatus.getOrDefault(status, 0);
    }

    public String toJson() {
        return "{\"total\":" + total
                + ",\"by_status\":" + Json.map(byStatus)
                + ",\"by_category\":" + Json.map(byCategory)
                + ",\"by_priority\":" + Json.map(byPriority)
                + ",\"avg_resolution_hours\":" + (avgResolutionHours == null ? "null" : avgResolutionHours) + "}";
    }
}
