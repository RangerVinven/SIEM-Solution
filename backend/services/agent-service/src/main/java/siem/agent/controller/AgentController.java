package siem.agent.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import siem.agent.entity.Agent;
import siem.agent.repository.AgentRepository;
import siem.models.UserPrincipal;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentRepository repository;

    @GetMapping
    public List<Agent> getAgents(@AuthenticationPrincipal UserPrincipal principal) {
        return repository.findBySchoolId(principal.schoolId());
    }

    @GetMapping("/unassigned")
    public List<Agent> getUnassignedAgents(@AuthenticationPrincipal UserPrincipal principal) {
        return repository.findBySchoolId(principal.schoolId()).stream()
                .filter(a -> "Unassigned School".equals(a.getSchoolName()))
                .toList();
    }

    @PutMapping("/{id}/assign")
    public Agent assignAgent(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestParam String schoolId,
            @RequestParam String schoolName,
            @RequestParam String location) {
        
        Agent agent = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        if (!agent.getSchoolId().equals(principal.schoolId())) {
            throw new AccessDeniedException("Forbidden");
        }

        agent.setSchoolId(schoolId);
        agent.setSchoolName(schoolName);
        agent.setLocation(location);
        
        return repository.save(agent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgent(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {

        Agent agent = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        if (!agent.getSchoolId().equals(principal.schoolId())) {
            throw new AccessDeniedException("Forbidden");
        }

        repository.delete(agent);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/internal/heartbeat")
    public void heartbeat(@RequestParam String hostname, @RequestParam(required = false) String status) {
        Agent agent = repository.findByHostname(hostname)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        agent.setLastSeen(Instant.now());
        
        if ("SHUTDOWN".equals(status)) {
            agent.setStatus("OFFLINE");
        } else {
            agent.setStatus("ONLINE");
        }
        
        repository.save(agent);
    }

    @GetMapping("/internal/search")
    public Optional<Agent> getAgentByHostname(@RequestParam String hostname) {
        return repository.findByHostname(hostname);
    }

    @GetMapping("/internal/location")
    public String getLocationByHostname(@RequestParam String hostname) {
        return repository.findByHostname(hostname)
                .map(Agent::getLocation)
                .orElse("Unknown Location");
    }
}
