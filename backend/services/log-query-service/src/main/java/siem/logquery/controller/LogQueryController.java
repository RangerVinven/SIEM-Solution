package siem.logquery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import siem.logquery.model.LogDocument;
import siem.logquery.service.LogQueryService;
import siem.models.UserPrincipal;

@RestController
@RequiredArgsConstructor
public class LogQueryController {

    private final LogQueryService logQueryService;

    @GetMapping("/logs")
    public Page<LogDocument> getLogs(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String hostname,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            Pageable pageable) {
        
        return logQueryService.findLogs(principal.schoolId(), message, hostname, category, level, pageable);
    }
}
