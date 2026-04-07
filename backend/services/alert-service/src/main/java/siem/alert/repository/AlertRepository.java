package siem.alert.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import siem.alert.entity.Alert;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    Page<Alert> findBySchoolId(String schoolId, Pageable pageable);
    Page<Alert> findBySchoolIdAndResolved(String schoolId, boolean resolved, Pageable pageable);

    long countBySchoolIdAndSeverityIgnoreCaseAndResolvedFalse(String schoolId, String severity);
    long countBySchoolIdAndResolvedFalse(String schoolId);
}
