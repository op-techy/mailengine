package com.mailengine.mailengine.dto.campaign;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Incoming request body for creating or updating a campaign.
 * Sent by the frontend on:
 *   POST /api/campaigns        (create)
 *   PUT  /api/campaigns/{id}   (update)
 *
 * All fields are validated before the service layer processes them.
 * The backend never trusts the frontend to send clean data.
 */
@Getter
@Setter
public class CampaignRequest {

    // The internal name of the campaign — visible only to the team, not recipients
    @NotBlank
    @Size(max = 255)
    private String name;

    // The subject line recipients will see in their inbox
    @NotBlank
    @Size(max = 500)
    private String subjectLine;

    // The display name recipients see as the sender e.g. "MailEngine Team"
    @NotBlank
    @Size(max = 255)
    private String fromName;

    // The email address the campaign is sent from — must be a valid email format
    @NotBlank
    @Email
    private String fromEmail;

    // References the template whose HTML will be personalised and sent to each recipient
    private Long templateId;

    // One or more recipient lists to send to.
    // The sending pipeline will deduplicate recipients across lists automatically.
    @NotEmpty
    private List<Long> recipientListIds;
}