package fr.webskills.academy.repository;

import fr.webskills.academy.domain.LearningSection;
import fr.webskills.academy.domain.enums.PublicationStatus;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningSectionRepository extends JpaRepository<LearningSection, UUID> {
    Optional<LearningSection> findBySlug(String slug);

    List<LearningSection> findByDomainIdAndStatusOrderByDisplayOrderAsc(
            UUID domainId, PublicationStatus status);

    List<LearningSection> findByDomainIdOrderByDisplayOrderAsc(UUID domainId);
}
