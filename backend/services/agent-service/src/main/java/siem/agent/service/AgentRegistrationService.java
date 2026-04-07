package siem.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import siem.agent.entity.Agent;
import siem.agent.repository.AgentRepository;
import siem.models.RawSiemEvent;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentRegistrationService {

    private final AgentRepository agentRepository;

    @KafkaListener(topics = "raw-logs", groupId = "agent-service")
    public void enrich(RawSiemEvent event) {
        String hostname = event.host().hostname();
        Optional<Agent> agentOpt = agentRepository.findByHostname(hostname);

        if (agentOpt.isPresent()) {
            Agent agent = agentOpt.get();
            agent.setLastSeen(Instant.now());
            agentRepository.save(agent);
            return;
        }

        // Registers hte agent if it hasn't been seen before
        Agent newAgent = new Agent();
        newAgent.setHostname(hostname);
        newAgent.setSchoolId(event.schoolId());
        newAgent.setSchoolName("Unassigned School");
        newAgent.setLocation("Unassigned Location");
        newAgent.setLastSeen(Instant.now());
        agentRepository.save(newAgent);
    }
}
