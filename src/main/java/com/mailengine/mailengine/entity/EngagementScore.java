package com.mailengine.mailengine.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "engagement_scores")
public class EngagementScore {
    @Id
    @Column(name = "recipient_id", nullable = false)
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ColumnDefault("0")
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Size(max = 20)
    @Column(name = "category", length = 20)
    private String category;

    @Column(name = "last_open_at")
    private Instant lastOpenAt;

    @Column(name = "last_click_at")
    private Instant lastClickAt;

    @ColumnDefault("0")
    @Column(name = "campaigns_sent")
    private Integer campaignsSent;

    @ColumnDefault("0")
    @Column(name = "campaigns_opened")
    private Integer campaignsOpened;

    @ColumnDefault("0")
    @Column(name = "campaigns_clicked")
    private Integer campaignsClicked;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;


}