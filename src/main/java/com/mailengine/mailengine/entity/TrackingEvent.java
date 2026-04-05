package com.mailengine.mailengine.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Records a single engagement event for a campaign recipient.
 * Event types: "open", "click", "unsubscribe", "bounce", "complaint", "delivery"
 *
 * Open events are triggered when the recipient's email client loads the tracking pixel.
 * Click events are triggered when a tracked link is followed through our redirect endpoint.
 * Bounce/complaint/delivery events come in via the AWS SNS webhook.
 */
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

    // "open", "click", "unsubscribe", "bounce", "complaint", "delivery"
    @Size(max = 30)
    @NotNull
    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    // Only populated for click events — the original URL before rewriting
    @Column(name = "link_url", length = Integer.MAX_VALUE)
    private String linkUrl;

    // Approximate position of the clicked link in the email (e.g. "header", "body", "footer")
    @Size(max = 50)
    @Column(name = "link_position", length = 50)
    private String linkPosition;

    // Stored as a String to handle PostgreSQL's INET type via the JDBC driver
    @Column(name = "ip_address")
    private String ipAddress;

    // Resolved from IP via MaxMind GeoLite2
    @Size(max = 5)
    @Column(name = "geo_country", length = 5)
    private String geoCountry;

    @Size(max = 100)
    @Column(name = "geo_city", length = 100)
    private String geoCity;

    // Parsed from User-Agent via Yauaa (e.g. "mobile", "desktop", "tablet")
    @Size(max = 20)
    @Column(name = "device_type", length = 20)
    private String deviceType;

    // e.g. "Gmail", "Apple Mail", "Outlook"
    @Size(max = 50)
    @Column(name = "email_client", length = 50)
    private String emailClient;

    @Size(max = 50)
    @Column(name = "os", length = 50)
    private String os;

    // Raw User-Agent string from the request header
    @Column(name = "user_agent", length = Integer.MAX_VALUE)
    private String userAgent;

    // True if the open came from Apple Mail's image pre-fetcher, not a real human open.
    // Used to calculate the adjusted open rate in analytics.
    @ColumnDefault("false")
    @Column(name = "is_apple_proxy")
    private Boolean isAppleProxy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}