package siem.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import siem.models.AlertEvent;

import siem.notification.client.AccountServiceClient;
import siem.notification.client.AgentServiceClient;
import org.springframework.web.client.RestClient;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AlertNotificationService {

    private final AccountServiceClient accountClient;
    private final AgentServiceClient agentClient;

    @Value("${spring.mail.password}")
    private String brevoApiKey;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${brevo.template.alert.id}")
    private int templateId;

    @KafkaListener(topics = "alerts", groupId = "notification-service")
    public void handleAlert(AlertEvent alert) {
        String hostname = alert.triggeringEvent().host().hostname();
        
        Map<String, Object> agentData = agentClient.getAgentDetails(hostname);
        List<Map<String, Object>> users = accountClient.getSchoolUsers(alert.schoolId());

        Map<String, Object> params = new HashMap<>();
        params.put("hostname", hostname);
        params.put("schoolName", agentData != null ? agentData.get("schoolName") : "Your School");
        params.put("location", agentData != null ? agentData.get("location") : "Unknown Location");
        params.put("severity", alert.severity());
        params.put("ruleName", alert.ruleName());
        params.put("description", alert.description());
        
        // Replaces the template's placeholders
        List<String> processedSteps = alert.remediationSteps().stream()
            .map(step -> replacePlaceholders(step, params))
            .toList();
        params.put("remediationSteps", processedSteps);

        for (Map<String, Object> user : users) {
            sendViaBrevo((String) user.get("email"), params);
        }
    }

    private void sendViaBrevo(String to, Map<String, Object> params) {
        RestClient client = RestClient.builder().baseUrl("https://api.brevo.com/v3").build();

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("email", fromEmail, "name", "School SIEM"));
        body.put("to", List.of(Map.of("email", to)));
        body.put("templateId", templateId);
        body.put("params", params);

        client.post()
            .uri("/smtp/email")
            .header("api-key", brevoApiKey)
            .body(body)
            .retrieve()
            .toBodilessEntity();
    }

    private String replacePlaceholders(String text, Map<String, Object> context) {
        String result = text;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return result;
    }
}
