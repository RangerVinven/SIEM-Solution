package siem.loganalysis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import siem.models.RawSiemEvent;
import siem.loganalysis.entity.Rule;
import siem.loganalysis.repository.RuleRepository;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import siem.models.AlertEvent;
import java.time.Duration;
import java.time.Instant;

import com.jayway.jsonpath.JsonPath;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Slf4j
@Service
public class RuleEngineService {

    private final RuleRepository ruleRepository;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, AlertEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public RuleEngineService(
            RuleRepository ruleRepository,
            StringRedisTemplate redisTemplate,
            KafkaTemplate<String, AlertEvent> kafkaTemplate) {
        this.ruleRepository = ruleRepository;
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "normalised-logs", groupId = "log-analysis-service")
    public void analyze(RawSiemEvent event) {
        log.info("Received event for analysis: {} from org: {}", event.event().id(), event.organisationId());
        
        if (event.organisationId() == null) {
            log.warn("Event {} has no organisationId, skipping.", event.event().id());
            return;
        }

        List<Rule> activeRules = ruleRepository.findByOrganisationIdAndEnabledTrue(event.organisationId());
        log.info("Found {} active rules for org: {}", activeRules.size(), event.organisationId());
        
        // Convert event to Map for JsonPath lookup
        Map<String, Object> eventMap = objectMapper.convertValue(event, Map.class);

        for (Rule rule : activeRules) {
            if (evaluate(rule, eventMap, event)) {
                log.info("Rule '{}' matched for event: {}", rule.getName(), event.event().id());
                publishAlert(rule, event);
            }
        }
    }

    private boolean evaluate(Rule rule, Map<String, Object> eventMap, RawSiemEvent originalEvent) {
        try {
            // Use JsonPath to find the field (e.g., "$.event.outcome")
            String path = "$." + rule.getFieldToWatch();
            Object value = JsonPath.read(eventMap, path);
            
            log.debug("Evaluating rule '{}': field '{}', expected '{}', actual '{}'", 
                rule.getName(), rule.getFieldToWatch(), rule.getExpectedValue(), value);

            if (value == null || !String.valueOf(value).equals(rule.getExpectedValue())) {
                return false;
            }

            if (rule.getThreshold() <= 1) {
                return true;
            }

            return checkThreshold(rule, originalEvent);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkThreshold(Rule rule, RawSiemEvent event) {
        String key = String.format("counter:%s:%s:%s", 
            rule.getOrganisationId(), 
            rule.getId(), 
            event.host().hostname());

        Long count = redisTemplate.opsForValue().increment(key);
        log.info("Threshold count for rule '{}' on host '{}': {}", rule.getName(), event.host().hostname(), count);
        
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(rule.getWindowMinutes()));
        }

        return count != null && count >= rule.getThreshold();
    }

    private void publishAlert(Rule rule, RawSiemEvent event) {
        String schoolName = "Unknown School";
        String location = "Unknown Location";

        AlertEvent alert = new AlertEvent(
            event.organisationId(),
            schoolName,
            location,
            rule.getName(),
            rule.getSeverity(),
            rule.getDescription(),
            rule.getRemediationSteps(),
            event,
            Instant.now().getEpochSecond()
        );

        log.info("Publishing alert: {} for org: {}", rule.getName(), event.organisationId());
        kafkaTemplate.send("alerts", alert);
    }
}
