package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.CampaignRecipientList;
import com.mailengine.mailengine.entity.CampaignRecipientListId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRecipientListRepository extends JpaRepository<CampaignRecipientList, CampaignRecipientListId> {
    List<CampaignRecipientList> findByCampaignId(Long campaignId);
}
