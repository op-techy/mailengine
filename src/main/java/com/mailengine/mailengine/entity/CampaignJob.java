package com.mailengine.mailengine.entity;

import com.mailengine.mailengine.entity.enums.JobStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "campaign_jobs")
public class CampaignJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private JobStatus status = JobStatus.queued;

    @ColumnDefault("0")
    @Column(name = "total_emails")
    private Integer totalEmails;

    @ColumnDefault("0")
    @Column(name = "queued_count")
    private Integer queuedCount;

    @ColumnDefault("0")
    @Column(name = "sent_count")
    private Integer sentCount;

    @ColumnDefault("0")
    @Column(name = "failed_count")
    private Integer failedCount;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "error_message", length = Integer.MAX_VALUE)
    private String errorMessage;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;


}