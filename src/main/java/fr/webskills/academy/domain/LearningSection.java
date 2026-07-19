package fr.webskills.academy.domain;

import fr.webskills.academy.domain.enums.Level;
import fr.webskills.academy.domain.enums.PublicationStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "learning_sections",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_section_domain_slug",
                        columnNames = {"domain_id", "slug"}))
public class LearningSection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "domain_id")
    private LearningDomain domain;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(length = 500)
    private String summary;

    @Column(columnDefinition = "text")
    private String content;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Level level = Level.BEGINNER;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "source_name", length = 180)
    private String sourceName;

    @Column(name = "source_verified_at")
    private Instant sourceVerifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PublicationStatus status = PublicationStatus.DRAFT;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "section")
    @OrderBy("displayOrder ASC")
    private List<Lesson> lessons = new ArrayList<>();

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
