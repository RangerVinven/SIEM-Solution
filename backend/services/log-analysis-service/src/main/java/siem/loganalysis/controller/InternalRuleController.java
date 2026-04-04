package siem.loganalysis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import siem.loganalysis.service.RuleBootstrapService;

import siem.loganalysis.entity.GlobalRuleTemplate;
import siem.loganalysis.repository.GlobalRuleTemplateRepository;
import java.util.List;

@RestController
@RequestMapping("/internal/rules")
@RequiredArgsConstructor
public class InternalRuleController {

    private final RuleBootstrapService bootstrapService;
    private final GlobalRuleTemplateRepository templateRepository;

    @PostMapping("/bootstrap")
    public void bootstrap(@RequestParam String organisationId) {
        bootstrapService.bootstrap(organisationId);
    }

    @GetMapping("/templates")
    public List<GlobalRuleTemplate> getTemplates() {
        return templateRepository.findAll();
    }
}
