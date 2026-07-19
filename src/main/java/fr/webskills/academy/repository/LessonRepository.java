package fr.webskills.academy.repository;

import fr.webskills.academy.domain.Lesson;
import fr.webskills.academy.domain.enums.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    Optional<Lesson> findBySlug(String slug);

    List<Lesson> findBySectionIdAndStatusOrderByDisplayOrderAsc(
            UUID sectionId, PublicationStatus status);

    List<Lesson> findBySectionIdOrderByDisplayOrderAsc(UUID sectionId);

    List<Lesson> findByStatusAndTitleContainingIgnoreCaseOrStatusAndSummaryContainingIgnoreCase(
            PublicationStatus s1, String q1, PublicationStatus s2, String q2);
}
