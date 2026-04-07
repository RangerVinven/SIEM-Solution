package siem.alert.dto;

public record AlertStatsResponse(
    long high,
    long medium,
    long low,
    long totalUnresolved
) {}
