package siem.alert.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import siem.alert.entity.Alert;
import java.util.Map;
import java.util.Optional;

@Component
public class AgentServiceClient {

    private final RestClient restClient;

    public AgentServiceClient(@Value("${agent.service.url}") String url) {
        this.restClient = RestClient.builder().baseUrl(url).build();
    }

    public Optional<Map<String, String>> getAgentDetails(String hostname) {
        try {
            Map<String, String> details = restClient.get()
                    .uri("/agents/internal/search?hostname={h}", hostname)
                    .retrieve()
                    .body(Map.class);
            return Optional.ofNullable(details);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
