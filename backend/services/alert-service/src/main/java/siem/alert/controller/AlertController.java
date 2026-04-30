package siem.alert.controller;

import lombok.RequiredArgsConstructor;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import siem.alert.dto.AlertStatsResponse;
import siem.alert.entity.Alert;
import siem.alert.repository.AlertRepository;
import siem.models.UserPrincipal;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRepository repository;

    @GetMapping
    public Page<Alert> getAlerts(@AuthenticationPrincipal UserPrincipal principal, @RequestParam(required = false) Boolean resolved, Pageable pageable) {
        if (resolved != null) {
            return repository.findBySchoolIdAndResolvedOrderByTimestampDesc(principal.schoolId(), resolved, pageable);
        }

        return repository.findBySchoolIdOrderByTimestampDesc(principal.schoolId(), pageable);
    }

    @GetMapping("/stats")
    public AlertStatsResponse getStats(@AuthenticationPrincipal UserPrincipal principal) {
        String schoolId = principal.schoolId();

        long high = repository.countBySchoolIdAndSeverityIgnoreCaseAndResolvedFalse(schoolId, "HIGH");
        long medium = repository.countBySchoolIdAndSeverityIgnoreCaseAndResolvedFalse(schoolId, "MEDIUM");
        long low = repository.countBySchoolIdAndSeverityIgnoreCaseAndResolvedFalse(schoolId, "LOW");
        long totalUnresolved = repository.countBySchoolIdAndResolvedFalse(schoolId);

        return new AlertStatsResponse(high, medium, low, totalUnresolved);
    }

    @GetMapping("/{id}")
    public Alert getAlert(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        Alert alert = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (!alert.getSchoolId().equals(principal.schoolId())) {
            throw new AccessDeniedException("Forbidden");
        }

        return alert;
    }

    @PutMapping("/{id}/resolve")
    public Alert resolveAlert(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        Alert alert = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (!alert.getSchoolId().equals(principal.schoolId())) {
            throw new AccessDeniedException("Forbidden");
        }

        alert.setResolved(true);
        alert.setResolvedAt(Instant.now());
        return repository.save(alert);
    }

    @PutMapping("/{id}/unresolve")
    public Alert unresolveAlert(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        Alert alert = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (!alert.getSchoolId().equals(principal.schoolId())) {
            throw new AccessDeniedException("Forbidden");
        }

        alert.setResolved(false);
        alert.setResolvedAt(null);

        return repository.save(alert);
    }
}
