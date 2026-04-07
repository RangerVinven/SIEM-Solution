package siem.aggregator.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import siem.models.RawSiemEvent;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {
    
    private final KafkaTemplate<String, RawSiemEvent> kafkaTemplate;

    public void publishEvent(List<RawSiemEvent> events, String schoolId) {
        for (RawSiemEvent event : events) {
            RawSiemEvent eventWithOrg = new RawSiemEvent(
                event.timestamp(),
                schoolId,
                event.event(),
                event.host(),
                event.message(),
                event.log(),
                event.tags(),
                event.labels()
            );
            kafkaTemplate.send("raw-logs", eventWithOrg)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event: {}", ex.getMessage());
                    }
                });
        }
    }
}
