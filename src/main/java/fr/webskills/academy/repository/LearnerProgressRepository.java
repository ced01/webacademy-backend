package fr.webskills.academy.repository;

import fr.webskills.academy.domain.LearnerProgress;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearnerProgressRepository extends JpaRepository<LearnerProgress, UUID> {
    Optional<LearnerProgress> findByUserIdAndLessonId(UUID userId, UUID lessonId);

    List<LearnerProgress> findByUserId(UUID userId);

    long countByUserIdAndCompletedTrue(UUID userId);
}
