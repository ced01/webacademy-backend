package fr.webskills.academy.mapper;

import fr.webskills.academy.domain.*;
import fr.webskills.academy.domain.enums.PublicationStatus;
import fr.webskills.academy.dto.LearningDtos.*;
import fr.webskills.academy.repository.LearningSectionRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AcademyMapper {
    private final LearningSectionRepository sections;

    public AcademyMapper(LearningSectionRepository sections) {
        this.sections = sections;
    }

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
                sections.countByDomainId(d.getId()),
                sections
                        .findByDomainIdAndStatusOrderByDisplayOrderAsc(
                                d.getId(), PublicationStatus.PUBLISHED)
                        .stream()
                        .map(s -> s.getLevel().getLabel())
                        .distinct()
                        .toList());
    }

    public LearningSectionResponse toSectionResponse(LearningSection s) {
        return new LearningSectionResponse(
                s.getId(),
                s.getDomain().getId(),
                s.getDomain().getSlug(),
                s.getTitle(),
                s.getSlug(),
                s.getSummary(),
                s.getContent(),
                s.getDescription(),
                s.getLevel(),
                s.getLevel().getLabel(),
                s.getDisplayOrder(),
                s.getVideoUrl(),
                s.getSourceUrl(),
                s.getSourceName(),
                s.getSourceVerifiedAt(),
                s.getStatus(),
                s.getLessons().size(),
                null,
                null);
    }

    public LearningSectionResponse toSectionResponse(
            LearningSection s, String previousSlug, String nextSlug) {
        LearningSectionResponse base = toSectionResponse(s);
        return new LearningSectionResponse(
                base.id(),
                base.domainId(),
                base.domainSlug(),
                base.title(),
                base.slug(),
                base.summary(),
                base.content(),
                base.description(),
                base.level(),
                base.levelLabel(),
                base.displayOrder(),
                base.videoUrl(),
                base.sourceUrl(),
                base.sourceName(),
                base.sourceVerifiedAt(),
                base.status(),
                base.lessonCount(),
                previousSlug,
                nextSlug);
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

    public AccessCodeAdminResponse toAccessCodeResponse(AccessCode c) {
        User creator = c.getCreatedBy();
        return new AccessCodeAdminResponse(
                c.getId(),
                c.getCode(),
                c.getLabel(),
                c.isActive(),
                c.getRevokedAt(),
                creator == null ? null : creator.getId(),
                creator == null ? null : creator.getEmail(),
                c.getCreatedAt(),
                c.getUpdatedAt());
    }
}
