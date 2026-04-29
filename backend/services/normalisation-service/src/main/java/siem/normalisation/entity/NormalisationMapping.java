package siem.normalisation.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "normalisation_mappings")
public class NormalisationMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceDataset;

    @Column(nullable = false)
    private String sourceId;

    private String targetCategory;
    private String targetAction;
    private String targetOutcome;
    private String targetLevel;
}
