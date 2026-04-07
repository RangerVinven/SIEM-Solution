package siem.models;

public record UserPrincipal(
    String userId,
    String schoolId,
    String role,
    String firstName,
    String lastName,
    String schoolName
) {}
