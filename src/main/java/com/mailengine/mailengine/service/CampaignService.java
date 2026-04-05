package com.mailengine.mailengine.service;

import com.mailengine.mailengine.dto.campaign.CampaignRequest;
import com.mailengine.mailengine.dto.campaign.CampaignResponse;
import com.mailengine.mailengine.dto.campaign.CampaignScheduleRequest;
import com.mailengine.mailengine.entity.Campaign;
import com.mailengine.mailengine.entity.enums.CampaignStatus;
import com.mailengine.mailengine.repository.CampaignRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles all business logic for campaign management.
 * This service covers the full campaign lifecycle:
 * create → update → schedule → send → complete.
 *
 * It does NOT handle the actual sending pipeline — that lives in
 * CampaignSendingService, which is responsible for populating the
 * email queue and triggering the sending worker.
 */
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;

    /**
     * Returns a paginated list of campaigns for a given account.
     * If a status filter is provided, only campaigns with that status are returned.
     * This powers GET /api/campaigns?status=sent&page=0&size=20
     */
    public Page<CampaignResponse> getCampaigns(Long accountId, CampaignStatus status, Pageable pageable) {
        Page<Campaign> campaigns;

        if (status != null) {
            campaigns = campaignRepository.findByAccountIdAndStatus(accountId, status, pageable);
        } else {
            campaigns = campaignRepository.findByAccountId(accountId, pageable);
        }

        // Convert each Campaign entity to a CampaignResponse DTO before returning
        return campaigns.map(this::toResponse);
    }

    /**
     * Returns the full details of a single campaign by its ID.
     * Throws EntityNotFoundException if the campaign does not exist.
     * This powers GET /api/campaigns/{id}
     */
    public CampaignResponse getCampaignById(Long id) {
        Campaign campaign = findOrThrow(id);
        return toResponse(campaign);
    }

    /**
     * Creates a new campaign with status DRAFT.
     * The campaign is not sent at this point — it must go through
     * the send or schedule flow to actually deliver emails.
     * This powers POST /api/campaigns
     */
    @Transactional
    public CampaignResponse createCampaign(CampaignRequest request) {
        Campaign campaign = new Campaign();
        applyRequest(campaign, request);
        campaign.setStatus(CampaignStatus.draft);
        return toResponse(campaignRepository.save(campaign));
    }

    /**
     * Updates an existing campaign's details.
     * Only campaigns in DRAFT status can be edited — you cannot
     * modify a campaign that is already sending or has been sent.
     * This powers PUT /api/campaigns/{id}
     */
    @Transactional
    public CampaignResponse updateCampaign(Long id, CampaignRequest request) {
        Campaign campaign = findOrThrow(id);

        // Guard: prevent editing a campaign that is no longer a draft
        if (campaign.getStatus() != CampaignStatus.draft) {
            throw new IllegalStateException(
                    "Campaign cannot be edited because its current status is: " + campaign.getStatus()
            );
        }

        applyRequest(campaign, request);
        return toResponse(campaignRepository.save(campaign));
    }

    /**
     * Schedules a campaign to be sent at a future date and time.
     * Sets status to SCHEDULED and stores the scheduledAt timestamp and timezone.
     * The scheduler (in CampaignSendingService) will pick it up at the right time.
     * This powers POST /api/campaigns/{id}/schedule
     */
    @Transactional
    public CampaignResponse scheduleCampaign(Long id, CampaignScheduleRequest request) {
        Campaign campaign = findOrThrow(id);

        // Guard: only draft campaigns can be scheduled
        if (campaign.getStatus() != CampaignStatus.draft) {
            throw new IllegalStateException(
                    "Only draft campaigns can be scheduled. Current status: " + campaign.getStatus()
            );
        }

        campaign.setScheduledAt(request.getScheduledAt());
        campaign.setTimezone(request.getTimezone());
        campaign.setStatus(CampaignStatus.scheduled);
        return toResponse(campaignRepository.save(campaign));
    }

    /**
     * Deletes a campaign permanently.
     * Only draft campaigns can be deleted — sent campaigns are kept for reporting.
     * This powers DELETE /api/campaigns/{id}
     */
    @Transactional
    public void deleteCampaign(Long id) {
        Campaign campaign = findOrThrow(id);

        if (campaign.getStatus() != CampaignStatus.draft) {
            throw new IllegalStateException(
                    "Only draft campaigns can be deleted. Current status: " + campaign.getStatus()
            );
        }

        campaignRepository.delete(campaign);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    /**
     * Looks up a campaign by ID and throws a clear exception if not found.
     * Centralizing this avoids repeating the same findById + orElseThrow pattern
     * in every method.
     */
    private Campaign findOrThrow(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found with id: " + id));
    }

    /**
     * Applies fields from the request DTO onto the campaign entity.
     * Used by both createCampaign and updateCampaign to avoid duplicating
     * the field-mapping logic.
     */
    private void applyRequest(Campaign campaign, CampaignRequest request) {
        campaign.setName(request.getName());
        campaign.setSubjectLine(request.getSubjectLine());
        campaign.setFromName(request.getFromName());
        campaign.setFromEmail(request.getFromEmail());
    }

    /**
     * Converts a Campaign entity into a CampaignResponse DTO.
     * We never expose raw entities to the controller layer — DTOs
     * give us control over what the frontend receives.
     */
    private CampaignResponse toResponse(Campaign campaign) {
        return CampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .subjectLine(campaign.getSubjectLine())
                .fromName(campaign.getFromName())
                .fromEmail(campaign.getFromEmail())
                .status(campaign.getStatus())
                .scheduledAt(campaign.getScheduledAt())
                .timezone(campaign.getTimezone())
                .sentAt(campaign.getSentAt())
                .createdAt(campaign.getCreatedAt())
                .totalRecipients(campaign.getTotalRecipients())
                .totalSent(campaign.getTotalSent())
                .totalDelivered(campaign.getTotalDelivered())
                .totalBounced(campaign.getTotalBounced())
                .totalOpened(campaign.getTotalOpened())
                .totalClicked(campaign.getTotalClicked())
                .totalComplained(campaign.getTotalComplained())
                .totalUnsubscribed(campaign.getTotalUnsubscribed())
                .build();
    }
}