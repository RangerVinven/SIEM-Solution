package siem.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import siem.agent.entity.Agent;
import siem.agent.repository.AgentRepository;
import siem.models.AlertEvent;
import siem.models.RawSiemEvent;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentHealthService {

    private final AgentRepository repository;

    @Scheduled(fixedRate = 60000) // Runs every minute
    public void checkHealth() {
        List<Agent> agents = repository.findAll();
        Instant threshold = Instant.now().minusSeconds(120);

        for (Agent agent : agents) {
            if ("ONLINE".equals(agent.getStatus()) && agent.getLastSeen().isBefore(threshold)) {
                agent.setStatus("OFFLINE");
                repository.save(agent);
            }
        }
    }
}
