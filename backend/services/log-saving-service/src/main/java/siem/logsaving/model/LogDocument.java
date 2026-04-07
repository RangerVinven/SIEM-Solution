package siem.logsaving.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import siem.models.RawSiemEvent;

import java.util.Map;
import java.util.HashMap;

@Data
@Builder
@Document(indexName = "logs")
public class LogDocument {
    @Id
    private String id;

    @Field(name = "@timestamp", type = FieldType.Date)
    private String timestamp;

    private String schoolId;

    private String message;

    @Field(type = FieldType.Object)
    private Map<String, Object> event;

    @Field(type = FieldType.Object)
    private Map<String, Object> host;

    @Field(type = FieldType.Object)
    private Map<String, Object> log;

    public static LogDocument from(RawSiemEvent rawEvent) {
        Map<String, Object> eventMap = new HashMap<>();
        if (rawEvent.event() != null) {
            eventMap.put("id", rawEvent.event().id());
            eventMap.put("kind", rawEvent.event().kind());
            eventMap.put("category", rawEvent.event().category());
            eventMap.put("type", rawEvent.event().type());
            eventMap.put("dataset", rawEvent.event().dataset());
            eventMap.put("provider", rawEvent.event().provider());
            eventMap.put("action", rawEvent.event().action());
            eventMap.put("outcome", rawEvent.event().outcome());
        }

        Map<String, Object> hostMap = new HashMap<>();
        if (rawEvent.host() != null) {
            hostMap.put("hostname", rawEvent.host().hostname());
            hostMap.put("id", rawEvent.host().id());
            hostMap.put("architecture", rawEvent.host().architecture());
            if (rawEvent.host().os() != null) {
                Map<String, String> osMap = new HashMap<>();
                osMap.put("name", rawEvent.host().os().name());
                osMap.put("platform", rawEvent.host().os().platform());
                osMap.put("version", rawEvent.host().os().version());
                osMap.put("family", rawEvent.host().os().family());
                hostMap.put("os", osMap);
            }
        }

        Map<String, Object> logMap = new HashMap<>();
        if (rawEvent.log() != null) {
            logMap.put("file_path", rawEvent.log().filePath());
            logMap.put("level", rawEvent.log().level());
        }

        return LogDocument.builder()
                .timestamp(rawEvent.timestamp())
                .schoolId(rawEvent.schoolId())
                .message(rawEvent.message())
                .event(eventMap)
                .host(hostMap)
                .log(logMap)
                .build();
    }
}
