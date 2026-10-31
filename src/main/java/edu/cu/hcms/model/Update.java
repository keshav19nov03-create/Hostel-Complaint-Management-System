package edu.cu.hcms.model;

/** One entry of the progress timeline of a complaint. */
public record Update(long id, long complaintId, long userId, String message, String statusTo,
                     String createdAt, String userName, String userRole) {
    public boolean byAdmin() {
        return "admin".equals(userRole);
    }
}
