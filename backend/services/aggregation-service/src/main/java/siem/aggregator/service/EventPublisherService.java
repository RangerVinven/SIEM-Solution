package siem.aggregator.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import siem.aggregator.dto.SiemEvent;

@Service
@RequiredArgsConstructor
public class EventPublisherService {
    
    private final KafkaTemplate<String, SiemEvent> kafkaTemplate;

    public void publishEvent(SiemEvent event) {
        kafkaTemplate.send("events", event);
    }
}
