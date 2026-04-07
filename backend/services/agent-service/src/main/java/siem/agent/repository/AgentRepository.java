package siem.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import siem.agent.entity.Agent;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, UUID> {
    Optional<Agent> findByHostname(String hostname);
    List<Agent> findBySchoolId(String schoolId);
}
