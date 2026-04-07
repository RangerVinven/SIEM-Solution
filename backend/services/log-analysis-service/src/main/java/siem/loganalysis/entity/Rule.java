package siem.loganalysis.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Entity
@Data
@Table(name = "rules")
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String schoolId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private String fieldToWatch;

    @Column(nullable = false)
    private String expectedValue;

    private int threshold;

    private int windowMinutes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "remediation_steps", joinColumns = @JoinColumn(name = "rule_id"))
    @Column(name = "step")
    private List<String> remediationSteps = new ArrayList<>();

    private boolean enabled = true;
}
