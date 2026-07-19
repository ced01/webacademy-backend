package fr.webskills.academy.repository;

import fr.webskills.academy.domain.LearningDomain;
import fr.webskills.academy.domain.enums.PublicationStatus;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningDomainRepository extends JpaRepository<LearningDomain, UUID> {
    Optional<LearningDomain> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<LearningDomain> findByStatusOrderByDisplayOrderAsc(PublicationStatus status);

    List<LearningDomain> findAllByOrderByDisplayOrderAscNameAsc();
}
