package siem.loganalysis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import siem.loganalysis.entity.Rule;
import siem.loganalysis.repository.RuleRepository;
import java.util.List;
import java.util.ArrayList;

import siem.loganalysis.entity.GlobalRuleTemplate;
import siem.loganalysis.repository.GlobalRuleTemplateRepository;

import org.springframework.kafka.annotation.KafkaListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleBootstrapService {

    private final RuleRepository repository;
    private final GlobalRuleTemplateRepository templateRepository;

    @KafkaListener(
        topics = "school-created", 
        groupId = "log-analysis-service-bootstrap",
        properties = {"value.deserializer=org.apache.kafka.common.serialization.StringDeserializer"}
    )
    public void bootstrap(String schoolId) {
        List<GlobalRuleTemplate> templates = templateRepository.findAll();
        List<Rule> rulesToCreate = new ArrayList<>();

        for (GlobalRuleTemplate template : templates) {
            Rule rule = new Rule();
            rule.setSchoolId(schoolId);
            rule.setName(template.getName());
            rule.setDescription(template.getDescription());
            rule.setSeverity(template.getSeverity());
            rule.setFieldToWatch(template.getFieldToWatch());
            rule.setExpectedValue(template.getExpectedValue());
            rule.setSecondFieldToWatch(template.getSecondFieldToWatch());
            rule.setSecondExpectedValue(template.getSecondExpectedValue());
            rule.setThreshold(template.getThreshold());
            rule.setWindowMinutes(template.getWindowMinutes());
            rule.setRemediationSteps(new ArrayList<>(template.getRemediationSteps()));
            rule.setEnabled(true);

            rulesToCreate.add(rule);
        }

        repository.saveAll(rulesToCreate);
    }
}
