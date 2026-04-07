package siem.logsaving.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import siem.models.RawSiemEvent;
import siem.logsaving.repository.LogRepository;
import siem.logsaving.model.LogDocument;

@Service
@RequiredArgsConstructor
public class LogSavingService {

    private final LogRepository logRepository;

    @KafkaListener(topics = "normalised-logs", groupId = "log-saving-service")
    public void saveLog(RawSiemEvent event) {
        LogDocument document = LogDocument.from(event);
        logRepository.save(document);
    }
}
