package siem.account.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import siem.account.entity.Organisation;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {
    Optional<Organisation> findById(UUID id);
    Optional<Organisation> findByApiKey(UUID apiKey);
    boolean existsByApiKey(UUID apiKey);
}
