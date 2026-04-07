package siem.alert.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.time.Instant;

@Entity
@Data
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String schoolId;

    private String schoolName;

    private String location;

    @Column(nullable = false)
    private String ruleName;

    @Column(nullable = false)
    private String severity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alert_remediation_steps", joinColumns = @JoinColumn(name = "alert_id"))
    @Column(name = "step")
    private List<String> remediationSteps;

    @Column(nullable = false)
    private String hostName;

    @Column(nullable = false)
    private Instant timestamp;

    private boolean resolved = false;

    private Instant resolvedAt;
}
