package siem.loganalysis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import siem.models.RawSiemEvent;
import siem.loganalysis.entity.Rule;
import siem.loganalysis.repository.RuleRepository;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
    private final RestClient accountClient;
    private final RestClient agentClient;
    private final Map<String, String> schoolNameCache = new ConcurrentHashMap<>();
    private final Map<String, String> locationCache = new ConcurrentHashMap<>();

    public RuleEngineService(RuleRepository ruleRepository, StringRedisTemplate redisTemplate, KafkaTemplate<String, AlertEvent> kafkaTemplate, @Value("${account.service.url}") String accountServiceUrl, @Value("${agent.service.url}") String agentServiceUrl) {
        this.ruleRepository = ruleRepository;
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.accountClient = RestClient.builder().baseUrl(accountServiceUrl).build();
        this.agentClient = RestClient.builder().baseUrl(agentServiceUrl).build();
    }

    @KafkaListener(topics = "normalised-logs", groupId = "log-analysis-service")
    public void analyze(RawSiemEvent event) {
        if (event.schoolId() == null) {
            return;
        }
        List<Rule> activeRules = ruleRepository.findBySchoolIdAndEnabledTrue(event.schoolId());
        Map<String, Object> eventMap = objectMapper.convertValue(event, Map.class);

        for (Rule rule : activeRules) {
            if (evaluate(rule, eventMap, event)) {
                publishAlert(rule, event);
            }
        }
    }

    private boolean evaluate(Rule rule, Map<String, Object> eventMap, RawSiemEvent originalEvent) {
        try {
            String path = "$." + rule.getFieldToWatch();
            Object value = JsonPath.read(eventMap, path);
            

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
            rule.getSchoolId(),
            rule.getId(),
            event.host().hostname());

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(rule.getWindowMinutes()));
        }

        if (count != null && count >= rule.getThreshold()) {
            redisTemplate.delete(key);
            return true;
        }

        return false;
    }

    private String getSchoolName(String schoolId) {
        return schoolNameCache.computeIfAbsent(schoolId, id -> {
            try {
                return accountClient.get()
                        .uri("/api/account/internal/schools/{id}/name", id)
                        .retrieve()
                        .body(String.class);
            } catch (Exception e) {
                return "Unknown School";
            }
        });
    }

    private String getLocation(String hostname) {
        return locationCache.computeIfAbsent(hostname, h -> {
            try {
                return agentClient.get()
                        .uri("/agents/internal/location?hostname={h}", h)
                        .retrieve()
                        .body(String.class);
            } catch (Exception e) {
                return "Unknown Location";
            }
        });
    }

    private void publishAlert(Rule rule, RawSiemEvent event) {
        String schoolName = getSchoolName(event.schoolId());
        String location = getLocation(event.host().hostname());

        AlertEvent alert = new AlertEvent(
            event.schoolId(),
            schoolName,
            location,
            rule.getName(),
            rule.getSeverity(),
            rule.getDescription(),
            rule.getRemediationSteps(),
            event,
            Instant.now().getEpochSecond()
        );

        kafkaTemplate.send("alerts", alert);
    }
}
