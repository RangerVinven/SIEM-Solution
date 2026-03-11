package siem.models;

public record UserPrincipal(
    String userId, 
    String organisationId, 
    String role, 
    String firstName,
    String lastName
) {}
