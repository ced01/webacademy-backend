package fr.webskills.academy.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "learner_progress",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_progress_user_lesson",
                        columnNames = {"user_id", "lesson_id"}))
public class LearnerProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "progress_percentage", nullable = false)
    private int progressPercentage;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "last_viewed_at")
    private Instant lastViewedAt;
}
