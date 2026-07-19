package fr.webskills.academy.repository;

import fr.webskills.academy.domain.LessonResource;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonResourceRepository extends JpaRepository<LessonResource, UUID> {
    List<LessonResource> findByLessonIdOrderByDisplayOrderAsc(UUID lessonId);
}
