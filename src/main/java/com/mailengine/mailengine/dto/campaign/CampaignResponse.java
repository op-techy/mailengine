package com.mailengine.mailengine.dto.campaign;

import com.mailengine.mailengine.entity.enums.CampaignStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Outgoing response body returned to the frontend after any campaign operation.
 * Used as the response for:
 *   GET  /api/campaigns              (list — wrapped in a Page)
 *   GET  /api/campaigns/{id}         (single campaign with full stats)
 *   POST /api/campaigns              (newly created campaign)
 *   PUT  /api/campaigns/{id}         (updated campaign)
 *
 * We never return the raw entity directly to the frontend — the DTO gives us
 * control over exactly what data is exposed and in what shape.
 *
 * @Builder allows the service to construct this cleanly:
 *   CampaignResponse.builder().id(campaign.getId()).name(campaign.getName())...build()
 */
@Getter
@Setter
@Builder
public class CampaignResponse {

    private Long id;
    private String name;
    private String subjectLine;
    private String fromName;
    private String fromEmail;

    // Current lifecycle state: draft, scheduled, sending, sent, failed, cancelled
    private CampaignStatus status;

    // Only present when the campaign is scheduled — null for draft/sent campaigns
    private Instant scheduledAt;
    private String timezone;

    // Populated once the campaign has started sending
    private Instant sentAt;
    private Instant createdAt;

    // Running counters updated by the sending worker and the SES webhook handler.
    // These power the stats shown on the campaign detail and analytics pages.
    private Integer totalRecipients;
    private Integer totalSent;
    private Integer totalDelivered;
    private Integer totalBounced;
    private Integer totalOpened;
    private Integer totalClicked;
    private Integer totalComplained;
    private Integer totalUnsubscribed;
}