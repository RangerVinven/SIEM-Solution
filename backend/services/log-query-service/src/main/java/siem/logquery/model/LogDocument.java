package siem.logquery.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;

@Data
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
}
