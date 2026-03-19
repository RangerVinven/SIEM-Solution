package siem.aggregator.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import siem.models.RawSiemEvent;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventPublisherService {
    
    private final KafkaTemplate<String, RawSiemEvent> kafkaTemplate;

    public void publishEvent(List<RawSiemEvent> events) {
        for (RawSiemEvent event : events) {
            kafkaTemplate.send("raw-logs", event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        System.err.println("Failed to publish event: " + ex.getMessage());
                    }
                });
        }
    }
}
