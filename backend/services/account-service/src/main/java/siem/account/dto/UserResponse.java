package siem.account.dto;

public record UserResponse(
    String id,
    String firstName,
    String lastName,
    String email,
    String role,
    String schoolId
) {}
