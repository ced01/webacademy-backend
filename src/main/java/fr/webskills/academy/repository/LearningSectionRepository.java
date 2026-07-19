package fr.webskills.academy.repository;

import fr.webskills.academy.domain.LearningSection;
import fr.webskills.academy.domain.enums.PublicationStatus;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningSectionRepository extends JpaRepository<LearningSection, UUID> {
    Optional<LearningSection> findBySlug(String slug);

    Optional<LearningSection> findByDomainSlugAndSlug(String domainSlug, String slug);

    boolean existsByDomainIdAndSlug(UUID domainId, String slug);

    boolean existsByDomainIdAndSlugAndIdNot(UUID domainId, String slug, UUID id);

    long countByDomainId(UUID domainId);

    List<LearningSection> findByDomainIdAndStatusOrderByDisplayOrderAsc(
            UUID domainId, PublicationStatus status);

    List<LearningSection> findByDomainIdOrderByDisplayOrderAsc(UUID domainId);
}
