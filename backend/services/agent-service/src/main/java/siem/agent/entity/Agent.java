package siem.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import java.time.Instant;

@Entity
@Data
@Table(name = "agents")
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String hostname;

    @Column(nullable = false)
    private String schoolId;

    private String schoolName;

    private String location;

    private Instant lastSeen;

    private String status = "ONLINE";
}
