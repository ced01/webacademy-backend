package fr.webskills.academy.dto;

import fr.webskills.academy.domain.enums.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public final class LearningDtos {
    private LearningDtos() {}

    public record LearningDomainRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Pattern(regexp = "[a-z0-9-]+") String slug,
            @Size(max = 300) String shortDescription,
            String description,
            String icon,
            String coverImage,
            int displayOrder,
            PublicationStatus status) {}

    public record LearningDomainResponse(
            UUID id,
            String name,
            String slug,
            String shortDescription,
            String description,
            String icon,
            String coverImage,
            int displayOrder,
            PublicationStatus status,
            Instant createdAt,
            Instant updatedAt,
            long sectionCount) {}

    public record LearningSectionRequest(
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Pattern(regexp = "[a-z0-9-]+") String slug,
            String description,
            int displayOrder,
            PublicationStatus status) {}

    public record LearningSectionResponse(
            UUID id,
            UUID domainId,
            String title,
            String slug,
            String description,
            int displayOrder,
            PublicationStatus status,
            long lessonCount) {}

    public record LessonRequest(
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Pattern(regexp = "[a-z0-9-]+") String slug,
            @Size(max = 500) String summary,
            String content,
            Level level,
            Integer estimatedDurationMinutes,
            int displayOrder,
            PublicationStatus status,
            List<LessonResourceRequest> resources) {}

    public record LessonSummaryResponse(
            UUID id,
            UUID sectionId,
            String title,
            String slug,
            String summary,
            Level level,
            Integer estimatedDurationMinutes,
            int displayOrder,
            PublicationStatus status) {}

    public record LessonResponse(
            UUID id,
            UUID sectionId,
            String title,
            String slug,
            String summary,
            String content,
            Level level,
            Integer estimatedDurationMinutes,
            int displayOrder,
            PublicationStatus status,
            List<LessonResourceResponse> resources) {}

    public record LessonResourceRequest(
            @NotBlank String title,
            ResourceType type,
            @NotBlank @Size(max = 500) String url,
            String description,
            int displayOrder) {}

    public record LessonResourceResponse(
            UUID id,
            String title,
            ResourceType type,
            String url,
            String description,
            int displayOrder) {}

    public record ProgressRequest(@Min(0) @Max(100) int progressPercentage, boolean completed) {}

    public record ProgressResponse(
            UUID id,
            UUID lessonId,
            boolean completed,
            int progressPercentage,
            Instant startedAt,
            Instant completedAt,
            Instant lastViewedAt) {}

    public record AccessCodeRequest(
            @NotBlank @Size(max = 120) String label,
            @NotBlank @Size(min = 6, max = 120) String code,
            boolean active,
            Instant expiresAt,
            Integer maxUses) {}

    public record AccessCodeUpdateRequest(
            @NotBlank @Size(max = 120) String label,
            Boolean active,
            Instant expiresAt,
            Integer maxUses) {}

    public record AccessCodeAdminResponse(
            UUID id,
            String label,
            boolean active,
            Instant expiresAt,
            Integer maxUses,
            int usageCount,
            Instant lastUsedAt,
            Instant createdAt,
            Instant updatedAt) {}

    public record StatusRequest(boolean active) {}

    public record ReorderItem(UUID id, int displayOrder) {}
}
