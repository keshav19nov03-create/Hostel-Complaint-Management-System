package edu.cu.hcms.model;

public record Complaint(long id, String ticketNo, long userId, String title, String category, String priority,
                        String description, String block, String room, String status, String createdAt,
                        String updatedAt, String resolvedAt, String student, String studentUid) { }
