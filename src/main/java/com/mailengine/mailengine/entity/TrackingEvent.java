package com.mailengine.mailengine.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.net.InetAddress;
import java.time.Instant;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tracking_events")
public class TrackingEvent {
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

    @Size(max = 30)
    @NotNull
    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "link_url", length = Integer.MAX_VALUE)
    private String linkUrl;

    @Size(max = 50)
    @Column(name = "link_position", length = 50)
    private String linkPosition;

    @Column(name = "ip_address")
    private String ipAddress;

    @Size(max = 5)
    @Column(name = "geo_country", length = 5)
    private String geoCountry;

    @Size(max = 100)
    @Column(name = "geo_city", length = 100)
    private String geoCity;

    @Size(max = 20)
    @Column(name = "device_type", length = 20)
    private String deviceType;

    @Size(max = 50)
    @Column(name = "email_client", length = 50)
    private String emailClient;

    @Size(max = 50)
    @Column(name = "os", length = 50)
    private String os;

    @Column(name = "user_agent", length = Integer.MAX_VALUE)
    private String userAgent;

    @ColumnDefault("false")
    @Column(name = "is_apple_proxy")
    private Boolean isAppleProxy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;


}