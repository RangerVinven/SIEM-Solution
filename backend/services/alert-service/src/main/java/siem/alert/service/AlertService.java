package siem.alert.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import siem.alert.entity.Alert;
import siem.alert.repository.AlertRepository;
import siem.models.AlertEvent;
import java.time.Instant;

import siem.alert.client.AgentServiceClient;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository repository;
    private final AgentServiceClient agentServiceClient;

    @KafkaListener(topics = "alerts", groupId = "alert-service")
    public void consumeAlert(AlertEvent event) {
        Alert alert = new Alert();
        alert.setSchoolId(event.schoolId());
        alert.setRuleName(event.ruleName());
        alert.setSeverity(event.severity());
        alert.setDescription(event.description());
        String hostname = event.triggeringEvent().host().hostname();
        alert.setHostName(hostname);

        String[] location = {""};
        agentServiceClient.getAgentDetails(hostname).ifPresent(details -> {
            alert.setSchoolName(details.get("schoolName"));
            alert.setLocation(details.get("location"));
            location[0] = details.getOrDefault("location", "");
        });

        List<String> steps = event.remediationSteps().stream()
                .map(step -> step.replace("{{host}}", hostname).replace("{{room}}", location[0]))
                .toList();
        alert.setRemediationSteps(steps);

        alert.setTimestamp(Instant.ofEpochSecond(event.timestamp()));
        repository.save(alert);
    }
}
