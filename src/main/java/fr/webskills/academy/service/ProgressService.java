package fr.webskills.academy.service;

import fr.webskills.academy.domain.*;
import fr.webskills.academy.dto.LearningDtos.*;
import fr.webskills.academy.exception.ResourceNotFoundException;
import fr.webskills.academy.mapper.AcademyMapper;
import fr.webskills.academy.repository.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {
    private final LearnerProgressRepository progress;
    private final LessonRepository lessons;
    private final UserRepository users;
    private final AcademyMapper mapper;

    public ProgressService(
            LearnerProgressRepository progress,
            LessonRepository lessons,
            UserRepository users,
            AcademyMapper mapper) {
        this.progress = progress;
        this.lessons = lessons;
        this.users = users;
        this.mapper = mapper;
    }

    public List<ProgressResponse> all(UUID userId) {
        return progress.findByUserId(userId).stream().map(mapper::toProgressResponse).toList();
    }

    @Transactional
    public ProgressResponse update(UUID userId, UUID lessonId, ProgressRequest req) {
        var p =
                progress.findByUserIdAndLessonId(userId, lessonId)
                        .orElseGet(() -> create(userId, lessonId));
        p.setLastViewedAt(Instant.now());
        p.setProgressPercentage(req.progressPercentage());
        if (req.completed()) complete(p);
        return mapper.toProgressResponse(p);
    }

    @Transactional
    public ProgressResponse complete(UUID userId, UUID lessonId) {
        var p =
                progress.findByUserIdAndLessonId(userId, lessonId)
                        .orElseGet(() -> create(userId, lessonId));
        complete(p);
        return mapper.toProgressResponse(p);
    }

    public Map<String, Object> domainProgress(UUID userId, UUID domainId) {
        long done = progress.countByUserIdAndCompletedTrue(userId);
        return Map.of("domainId", domainId, "completedLessons", done);
    }

    private LearnerProgress create(UUID userId, UUID lessonId) {
        LearnerProgress p = new LearnerProgress();
        p.setUser(
                users.findById(userId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Utilisateur introuvable")));
        p.setLesson(
                lessons.findById(lessonId)
                        .orElseThrow(() -> new ResourceNotFoundException("Leçon introuvable")));
        p.setStartedAt(Instant.now());
        p.setLastViewedAt(Instant.now());
        return progress.save(p);
    }

    private void complete(LearnerProgress p) {
        p.setCompleted(true);
        p.setProgressPercentage(100);
        p.setCompletedAt(Instant.now());
        p.setLastViewedAt(Instant.now());
    }
}
