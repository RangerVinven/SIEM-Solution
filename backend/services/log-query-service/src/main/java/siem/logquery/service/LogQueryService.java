package siem.logquery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import siem.logquery.model.LogDocument;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogQueryService {

    private final ElasticsearchOperations elasticsearchOperations;

    public Page<LogDocument> findLogs(String schoolId, String message, String hostname, String category, String level, Pageable pageable) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        
        boolQueryBuilder.must(q -> q.term(t -> t.field("schoolId").value(schoolId)));

        if (message != null && !message.isBlank()) {
            boolQueryBuilder.must(q -> q.match(m -> m.field("message").query(message)));
        }

        if (hostname != null && !hostname.isBlank()) {
            boolQueryBuilder.must(q -> q.term(t -> t.field("host.hostname").value(hostname)));
        }

        if (category != null && !category.isBlank()) {
            boolQueryBuilder.must(q -> q.term(t -> t.field("event.category").value(category)));
        }

        if (level != null && !level.isBlank()) {
            boolQueryBuilder.must(q -> q.term(t -> t.field("log.level").value(level)));
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(new Query(boolQueryBuilder.build()))
                .withPageable(pageable)
                .build();

        SearchHits<LogDocument> searchHits = elasticsearchOperations.search(query, LogDocument.class);
        SearchPage<LogDocument> searchPage = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
        
        return (Page<LogDocument>) SearchHitSupport.unwrapSearchHits(searchPage);
    }
}
