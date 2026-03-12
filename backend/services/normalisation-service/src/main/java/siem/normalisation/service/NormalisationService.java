package siem.normalisation.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import siem.models.RawSiemEvent;

@Service
@RequiredArgsConstructor
public class NormalisationService {

    private final KafkaTemplate<String, RawSiemEvent> kafkaTemplate;

    @KafkaListener(topics = "raw-logs", groupId = "normalisation-service")
    public void normaliseLogs(RawSiemEvent event) {

    }
}
