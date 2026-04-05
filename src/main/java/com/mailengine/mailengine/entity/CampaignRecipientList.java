package com.mailengine.mailengine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "campaign_recipient_lists")
public class CampaignRecipientList {
    @EmbeddedId
    private CampaignRecipientListId id;

    @MapsId("campaignId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @MapsId("recipientListId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_list_id", nullable = false)
    private RecipientList recipientList;


}