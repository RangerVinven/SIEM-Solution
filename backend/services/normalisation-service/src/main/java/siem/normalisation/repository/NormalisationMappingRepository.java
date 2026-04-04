package siem.normalisation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import siem.normalisation.entity.NormalisationMapping;
import java.util.Optional;

@Repository
public interface NormalisationMappingRepository extends JpaRepository<NormalisationMapping, Long> {
    Optional<NormalisationMapping> findBySourceDatasetAndSourceId(String dataset, String sourceId);
}
