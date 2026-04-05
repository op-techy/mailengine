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

/**
 * Tracks the live progress of a campaign's sending operation.
 * One job is created per campaign when sending begins.
 * The sending worker updates sentCount and failedCount after each batch.
 * The frontend polls GET /api/campaigns/{id}/job-status to display a live progress bar.
 */
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

    // One campaign has exactly one job at a time
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    // Current state of the job: queued → processing → completed/failed/paused/cancelled
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private JobStatus status = JobStatus.queued;

    // Total emails loaded into the queue for this campaign
    @ColumnDefault("0")
    @Column(name = "total_emails")
    private Integer totalEmails;

    // How many are still waiting to be picked up by the worker
    @ColumnDefault("0")
    @Column(name = "queued_count")
    private Integer queuedCount;

    // Incremented each time the worker successfully sends an email via SES
    @ColumnDefault("0")
    @Column(name = "sent_count")
    private Integer sentCount;

    // Incremented when an email exhausts its retry attempts and is marked failed
    @ColumnDefault("0")
    @Column(name = "failed_count")
    private Integer failedCount;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    // Stores the last error message if the job itself fails (not individual emails)
    @Column(name = "error_message", length = Integer.MAX_VALUE)
    private String errorMessage;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}