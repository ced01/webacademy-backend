package fr.webskills.academy.repository;

import fr.webskills.academy.domain.LearningSection;
import fr.webskills.academy.domain.enums.Level;
import fr.webskills.academy.domain.enums.PublicationStatus;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(
            """
            select s from LearningSection s
            join fetch s.domain d
            where s.status = fr.webskills.academy.domain.enums.PublicationStatus.PUBLISHED
              and d.status = fr.webskills.academy.domain.enums.PublicationStatus.PUBLISHED
              and (:domainSlug is null or :domainSlug = '' or d.slug = :domainSlug)
              and (:level is null or s.level = :level)
              and (
                :query is null or :query = ''
                or lower(s.title) like lower(concat('%', :query, '%'))
                or lower(coalesce(s.summary, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(s.description, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(s.content, '')) like lower(concat('%', :query, '%'))
              )
            order by d.displayOrder asc, s.displayOrder asc, s.title asc
            """)
    List<LearningSection> searchPublished(
            @Param("query") String query,
            @Param("level") Level level,
            @Param("domainSlug") String domainSlug);
}
