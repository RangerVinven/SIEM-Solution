package siem.loganalysis.dto;

import lombok.Data;
import java.util.List;

@Data
public class RuleRequest {
    private String name;
    private String description;
    private String severity;
    private String fieldToWatch;
    private String expectedValue;
    private String secondFieldToWatch;
    private String secondExpectedValue;
    private int threshold;
    private int windowMinutes;
    private List<String> remediationSteps;
    private boolean enabled = true;
}
