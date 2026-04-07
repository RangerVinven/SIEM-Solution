package siem.notification.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Component
public class AgentServiceClient {

    private final RestClient restClient;

    public AgentServiceClient(@Value("${agent.service.url}") String url) {
        this.restClient = RestClient.builder().baseUrl(url).build();
    }

    public Map<String, Object> getAgentDetails(String hostname) {
        return restClient.get()
                .uri("/internal/agents/search?hostname={h}", hostname)
                .retrieve()
                .body(Map.class);
    }
}
