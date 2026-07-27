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
        name = "access_codes",
        uniqueConstraints = @UniqueConstraint(name = "uk_access_codes_code", columnNames = "code"))
public class AccessCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 120)
    private String label;

    @Column(name = "code_hash", length = 120)
    private String codeHash;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "welcome_message", length = 1000)
    private String welcomeMessage;

    @Column(name = "recommended_path", length = 2000)
    private String recommendedPath;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "usage_count", nullable = false)
    private int usageCount;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean isUsable() {
        Instant now = Instant.now();
        return active
                && revokedAt == null
                && (startsAt == null || !startsAt.isAfter(now))
                && (expiresAt == null || expiresAt.isAfter(now))
                && (maxUses == null || usageCount < maxUses);
    }

    public void recordUsage() {
        usageCount++;
        lastUsedAt = Instant.now();
    }

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
