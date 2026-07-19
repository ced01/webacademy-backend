package fr.webskills.academy.mapper;

import fr.webskills.academy.domain.*;
import fr.webskills.academy.dto.LearningDtos.*;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AcademyMapper {
    public LearningDomainResponse toDomainResponse(LearningDomain d) {
        return new LearningDomainResponse(
                d.getId(),
                d.getName(),
                d.getSlug(),
                d.getShortDescription(),
                d.getDescription(),
                d.getIcon(),
                d.getCoverImage(),
                d.getDisplayOrder(),
                d.getStatus(),
                d.getCreatedAt(),
                d.getUpdatedAt(),
                0);
    }

    public LearningSectionResponse toSectionResponse(LearningSection s) {
        return new LearningSectionResponse(
                s.getId(),
                s.getDomain().getId(),
                s.getTitle(),
                s.getSlug(),
                s.getDescription(),
                s.getDisplayOrder(),
                s.getStatus(),
                0);
    }

    public LessonSummaryResponse toLessonSummary(Lesson l) {
        return new LessonSummaryResponse(
                l.getId(),
                l.getSection().getId(),
                l.getTitle(),
                l.getSlug(),
                l.getSummary(),
                l.getLevel(),
                l.getEstimatedDurationMinutes(),
                l.getDisplayOrder(),
                l.getStatus());
    }

    public LessonResponse toLessonResponse(Lesson l, List<LessonResource> resources) {
        return new LessonResponse(
                l.getId(),
                l.getSection().getId(),
                l.getTitle(),
                l.getSlug(),
                l.getSummary(),
                l.getContent(),
                l.getLevel(),
                l.getEstimatedDurationMinutes(),
                l.getDisplayOrder(),
                l.getStatus(),
                resources.stream().map(this::toResourceResponse).toList());
    }

    public LessonResourceResponse toResourceResponse(LessonResource r) {
        return new LessonResourceResponse(
                r.getId(),
                r.getTitle(),
                r.getType(),
                r.getUrl(),
                r.getDescription(),
                r.getDisplayOrder());
    }

    public ProgressResponse toProgressResponse(LearnerProgress p) {
        return new ProgressResponse(
                p.getId(),
                p.getLesson().getId(),
                p.isCompleted(),
                p.getProgressPercentage(),
                p.getStartedAt(),
                p.getCompletedAt(),
                p.getLastViewedAt());
    }

    public AccessCodeAdminResponse toAccessCodeResponse(AccessCode c) {
        return new AccessCodeAdminResponse(
                c.getId(),
                c.getLabel(),
                c.isActive(),
                c.getExpiresAt(),
                c.getMaxUses(),
                c.getUsageCount(),
                c.getLastUsedAt(),
                c.getCreatedAt(),
                c.getUpdatedAt());
    }
}
