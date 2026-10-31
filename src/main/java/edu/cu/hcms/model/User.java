package edu.cu.hcms.model;

public record User(long id, String name, String uid, String email, String role,
                   String block, String room, String createdAt) {
    public boolean isAdmin() {
        return "admin".equals(role);
    }
}
