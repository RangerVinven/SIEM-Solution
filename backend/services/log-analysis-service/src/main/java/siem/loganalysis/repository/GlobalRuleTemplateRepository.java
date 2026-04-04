package siem.loganalysis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import siem.loganalysis.entity.GlobalRuleTemplate;
import java.util.UUID;

@Repository
public interface GlobalRuleTemplateRepository extends JpaRepository<GlobalRuleTemplate, Long> {
}
