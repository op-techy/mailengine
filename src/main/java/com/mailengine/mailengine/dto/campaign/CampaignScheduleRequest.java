package com.mailengine.mailengine.dto.campaign;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Request body for scheduling a campaign to be sent at a future date and time.
 * Sent by the frontend on:
 *   POST /api/campaigns/{id}/schedule
 *
 * Once scheduled, the campaign status changes to "scheduled" and a background
 * scheduler picks it up and triggers sending at the specified time.
 */
@Getter
@Setter
public class CampaignScheduleRequest {

    // The exact date and time to send the campaign.
    // @Future ensures the user cannot schedule a campaign in the past.
    @NotNull
    @Future
    private Instant scheduledAt;

    // IANA timezone identifier e.g. "Africa/Lagos", "Europe/London", "America/New_York"
    // Stored alongside scheduledAt so the scheduler can convert to the correct UTC time.
    @NotBlank
    private String timezone;
}