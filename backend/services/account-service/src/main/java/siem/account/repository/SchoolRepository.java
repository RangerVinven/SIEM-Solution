package siem.account.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import siem.account.entity.School;

@Repository
public interface SchoolRepository extends JpaRepository<School, UUID> {
    Optional<School> findByApiKey(UUID apiKey);
    boolean existsByApiKey(UUID apiKey);
}
