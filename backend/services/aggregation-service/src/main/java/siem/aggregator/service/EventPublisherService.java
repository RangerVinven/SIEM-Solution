package siem.aggregator.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import siem.models.RawSiemEvent;

@Service
@RequiredArgsConstructor
public class EventPublisherService {
    
    private final KafkaTemplate<String, RawSiemEvent> kafkaTemplate;

    public void publishEvent(RawSiemEvent event) {
        kafkaTemplate.send("raw-logs", event)
        .whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.println("Failed to publish event: " + ex.getMessage());
            }
        });
    }
}
