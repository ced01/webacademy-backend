package fr.webskills.academy.dto;

import fr.webskills.academy.domain.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public final class LearningDtos {
    private LearningDtos() {}

    public record LearningDomainRequest(
            @NotBlank @Size(max = 120) String name,
            @Pattern(regexp = "^$|[a-z0-9-]+") String slug,
            @Size(max = 300) String shortDescription,
            String description,
            String icon,
            @Size(max = 500) @Pattern(regexp = "^$|https?://.+") String coverImage,
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
            long sectionCount,
            List<String> levels) {}

    public record LearningSectionRequest(
            @NotBlank @Size(max = 180) String title,
            @Pattern(regexp = "^$|[a-z0-9-]+") String slug,
            @Size(max = 500) String summary,
            String content,
            String description,
            @NotNull Level level,
            int displayOrder,
            @Size(max = 500) @Pattern(regexp = "^$|https?://.+") String videoUrl,
            @Size(max = 500) @Pattern(regexp = "^$|https?://.+") String sourceUrl,
            @Size(max = 180) String sourceName,
            Instant sourceVerifiedAt,
            PublicationStatus status) {}

    public record LearningSectionResponse(
            UUID id,
            UUID domainId,
            String domainSlug,
            String title,
            String slug,
            String summary,
            String content,
            String description,
            Level level,
            String levelLabel,
            int displayOrder,
            String videoUrl,
            String sourceUrl,
            String sourceName,
            Instant sourceVerifiedAt,
            PublicationStatus status,
            long lessonCount,
            String previousSlug,
            String nextSlug) {}

    public record LessonRequest(
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Pattern(regexp = "[a-z0-9-]+") String slug,
            @Size(max = 500) String summary,
            String content,
            Level level,
            Integer estimatedDurationMinutes,
            int displayOrder,
            PublicationStatus status,
            List<@Valid LessonResourceRequest> resources) {}

    public record UpdateLessonRequest(
            @NotBlank(message = "Le titre est obligatoire") @Size(max = 180) String title,
            @Size(max = 500) String summary,
            @NotBlank(message = "Le contenu est obligatoire") String content,
            @NotNull(message = "Le niveau est obligatoire") Level level,
            @NotNull(message = "La section associée est obligatoire") UUID sectionId,
            Integer estimatedDurationMinutes,
            @NotNull(message = "L’ordre d’affichage est obligatoire") @Min(0) Integer displayOrder,
            @NotNull(message = "Le statut de publication est obligatoire") PublicationStatus status,
            List<@Valid LessonResourceRequest> resources) {}

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
            @NotBlank @Size(max = 500) @Pattern(regexp = "https?://.+") String url,
            String description,
            @Min(0) int displayOrder) {}

    public record LessonResourceResponse(
            UUID id,
            String title,
            ResourceType type,
            String url,
            String description,
            int displayOrder) {}

    public record AccessCodeRequest(@Size(max = 120) String label) {}

    public record AccessCodeAdminResponse(
            UUID id,
            String code,
            String label,
            boolean active,
            Instant revokedAt,
            UUID createdById,
            String createdByEmail,
            Instant createdAt,
            Instant updatedAt) {}

    public record StatusRequest(@NotNull PublicationStatus status) {}

    public record ReorderItem(UUID id, int displayOrder) {}
}
