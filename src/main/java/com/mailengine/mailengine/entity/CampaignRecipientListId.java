package com.mailengine.mailengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class CampaignRecipientListId implements Serializable {
    private static final long serialVersionUID = -1044164454424599768L;
    @NotNull
    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @NotNull
    @Column(name = "recipient_list_id", nullable = false)
    private Long recipientListId;


}