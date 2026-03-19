package siem.models;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record RawSiemEvent(
    @JsonProperty("@timestamp") String timestamp,
    Event event,
    Host host,
    String message,
    Log log,
    List<String> tags,
    Map<String, String> labels
) {
    public record Event(
        String id, String kind, String category, String type,
        String dataset, String provider, String action,
        String outcome, String original
    ) {}

    public record Host(
        String hostname, String id, String architecture, Os os
    ) {
        public record Os(String name, String platform, String version, String family) {}
    }

    public record Log(
        @JsonProperty("file.path") String filePath,
        String level
    ) {}
}
