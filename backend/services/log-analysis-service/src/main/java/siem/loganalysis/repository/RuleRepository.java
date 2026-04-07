package siem.loganalysis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import siem.loganalysis.entity.Rule;
import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {
    List<Rule> findBySchoolIdAndEnabledTrue(String schoolId);
}
