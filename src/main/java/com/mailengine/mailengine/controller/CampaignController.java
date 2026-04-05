package com.mailengine.mailengine.controller;

import com.mailengine.mailengine.dto.campaign.CampaignRequest;
import com.mailengine.mailengine.dto.campaign.CampaignResponse;
import com.mailengine.mailengine.dto.campaign.CampaignScheduleRequest;
import com.mailengine.mailengine.entity.enums.CampaignStatus;
import com.mailengine.mailengine.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for campaign management.
 * Handles all campaign CRUD operations and scheduling.
 *
 * Base path: /api/campaigns
 * Auth: Required for all endpoints (Editor and above)
 *
 * Note: The actual send pipeline is triggered via CampaignSendingService
 * and will be wired up in a separate controller method once that service
 * is implemented.
 */
@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    /**
     * Returns a paginated list of campaigns for the authenticated account.
     * Optionally filtered by status.
     *
     * GET /api/campaigns?status=sent&page=0&size=20
     *
     * @param status   optional filter — draft, scheduled, sending, sent, failed, cancelled
     * @param page     page number (0-indexed, default 0)
     * @param size     number of items per page (default 20)
     */
    @GetMapping
    public ResponseEntity<Page<CampaignResponse>> getCampaigns(
            @RequestParam(required = false) CampaignStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Hardcoded accountId as 1L for now — will be replaced with the
        // authenticated user's accountId once security is fully wired up
        Long accountId = 1L;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(campaignService.getCampaigns(accountId, status, pageable));
    }

    /**
     * Returns the full details of a single campaign including all stats.
     *
     * GET /api/campaigns/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaignById(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignById(id));
    }

    /**
     * Creates a new campaign with status DRAFT.
     * The campaign is not sent at this point.
     *
     * POST /api/campaigns
     */
    @PostMapping
    public ResponseEntity<CampaignResponse> createCampaign(
            @Valid @RequestBody CampaignRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaignService.createCampaign(request));
    }

    /**
     * Updates an existing draft campaign.
     * Returns 400 if the campaign is not in DRAFT status.
     *
     * PUT /api/campaigns/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponse> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignRequest request
    ) {
        return ResponseEntity.ok(campaignService.updateCampaign(id, request));
    }

    /**
     * Schedules a draft campaign to be sent at a future date and time.
     * Returns 400 if the campaign is not in DRAFT status.
     *
     * POST /api/campaigns/{id}/schedule
     */
    @PostMapping("/{id}/schedule")
    public ResponseEntity<CampaignResponse> scheduleCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignScheduleRequest request
    ) {
        return ResponseEntity.ok(campaignService.scheduleCampaign(id, request));
    }

    /**
     * Permanently deletes a draft campaign.
     * Returns 400 if the campaign is not in DRAFT status.
     * Returns 204 No Content on success — no response body needed.
     *
     * DELETE /api/campaigns/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }
}