package com.mailengine.mailengine.entity;

import com.mailengine.mailengine.entity.enums.QueueStatus;
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
@Table(name = "email_queue")
public class EmailQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private Recipient recipient;

    @Size(max = 255)
    @NotNull
    @Column(name = "to_email", nullable = false)
    private String toEmail;

    @NotNull
    @Column(name = "html_content", nullable = false, length = Integer.MAX_VALUE)
    private String htmlContent;

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

   @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private QueueStatus status = QueueStatus.pending;

    @ColumnDefault("0")
    @Column(name = "attempts")
    private Integer attempts;

    @Column(name = "error_message", length = Integer.MAX_VALUE)
    private String errorMessage;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;


}