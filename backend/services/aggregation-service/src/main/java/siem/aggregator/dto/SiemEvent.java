package siem.aggregator.dto;

import java.time.Instant;

public record SiemEvent(
    String agentId,
    String osType,
    Instant timestamp,

    String eventSource,
    String rawLog
) {}
