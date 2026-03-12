package siem.models;

import java.time.Instant;

public record RawSiemEvent(
    String agentId,
    String osType,
    Instant timestamp,

    String eventSource,
    String rawLog
) {}
