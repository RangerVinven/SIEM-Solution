package siem.aggregator.controller;

import lombok.RequiredArgsConstructor;
import siem.models.RawSiemEvent;
import siem.aggregator.service.EventPublisherService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RestController
public class AggregatorController {

    private final EventPublisherService service;
    private final RestClient restClient;

    public AggregatorController(EventPublisherService service, @Value("${agent.service.url}") String agentServiceUrl) {
        this.service = service;
        this.restClient = RestClient.builder().baseUrl(agentServiceUrl).build();
    }

    @PostMapping("/aggregate")
    public void aggregate(
            @RequestBody List<RawSiemEvent> eventPayloadList,
            @RequestAttribute("organisationId") String organisationId) {
        service.publishEvent(eventPayloadList, organisationId);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> proxyHeartbeat(@RequestParam String hostname, @RequestParam(required = false) String status) {
        return restClient.post()
                .uri("/agents/internal/heartbeat?hostname={h}&status={s}", hostname, status)
                .retrieve()
                .toBodilessEntity();
    }
}
