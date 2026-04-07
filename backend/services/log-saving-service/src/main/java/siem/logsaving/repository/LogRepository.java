package siem.logsaving.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import siem.logsaving.model.LogDocument;

public interface LogRepository extends ElasticsearchRepository<LogDocument, String> {
}
