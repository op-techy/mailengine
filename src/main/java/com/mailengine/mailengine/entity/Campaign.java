package com.mailengine.mailengine.entity;

import com.mailengine.mailengine.entity.enums.CampaignStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "campaigns")
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 500)
    @NotNull
    @Column(name = "subject_line", nullable = false, length = 500)
    private String subjectLine;

    @Size(max = 255)
    @NotNull
    @Column(name = "from_name", nullable = false)
    private String fromName;

    @Size(max = 255)
    @NotNull
    @Column(name = "from_email", nullable = false)
    private String fromEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CampaignStatus status = CampaignStatus.draft;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Size(max = 50)
    @Column(name = "timezone", length = 50)
    private String timezone;

    @Column(name = "sent_at")
    private Instant sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ColumnDefault("0")
    @Column(name = "total_recipients")
    private Integer totalRecipients;

    @ColumnDefault("0")
    @Column(name = "total_sent")
    private Integer totalSent;

    @ColumnDefault("0")
    @Column(name = "total_delivered")
    private Integer totalDelivered;

    @ColumnDefault("0")
    @Column(name = "total_bounced")
    private Integer totalBounced;

    @ColumnDefault("0")
    @Column(name = "total_opened")
    private Integer totalOpened;

    @ColumnDefault("0")
    @Column(name = "total_clicked")
    private Integer totalClicked;

    @ColumnDefault("0")
    @Column(name = "total_complained")
    private Integer totalComplained;

    @ColumnDefault("0")
    @Column(name = "total_unsubscribed")
    private Integer totalUnsubscribed;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;


}