package siem.models;

import java.util.List;

public record AlertEvent(
    String schoolId,
    String schoolName,
    String location,
    String ruleName,
    String severity,
    String description,
    List<String> remediationSteps,
    RawSiemEvent triggeringEvent,
    long timestamp
) {}
