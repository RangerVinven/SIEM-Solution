package siem.loganalysis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import siem.loganalysis.dto.RuleRequest;
import siem.loganalysis.entity.Rule;
import siem.loganalysis.repository.RuleRepository;
import siem.models.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleRepository repository;

    @GetMapping
    public List<Rule> getRules(@AuthenticationPrincipal UserPrincipal principal) {
        return repository.findByOrganisationIdAndEnabledTrue(principal.organisationId());
    }

    @PostMapping
    public Rule createRule(@AuthenticationPrincipal UserPrincipal principal, @RequestBody RuleRequest request) {
        Rule rule = new Rule();
        rule.setOrganisationId(principal.organisationId());
        updateRuleFromRequest(rule, request);
        return repository.save(rule);
    }

    @PutMapping("/{id}")
    public Rule updateRule(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody RuleRequest request) {
        
        Rule rule = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found"));

        if (!rule.getOrganisationId().equals(principal.organisationId())) {
            throw new AccessDeniedException("Forbidden");
        }

        updateRuleFromRequest(rule, request);
        return repository.save(rule);
    }

    @DeleteMapping("/{id}")
    public void deleteRule(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        Rule rule = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found"));

        if (!rule.getOrganisationId().equals(principal.organisationId())) {
            throw new AccessDeniedException("Forbidden");
        }

        repository.delete(rule);
    }

    private void updateRuleFromRequest(Rule rule, RuleRequest request) {
        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setSeverity(request.getSeverity());
        rule.setFieldToWatch(request.getFieldToWatch());
        rule.setExpectedValue(request.getExpectedValue());
        rule.setThreshold(request.getThreshold());
        rule.setWindowMinutes(request.getWindowMinutes());
        rule.setRemediationSteps(request.getRemediationSteps());
        rule.setEnabled(request.isEnabled());
    }
}
