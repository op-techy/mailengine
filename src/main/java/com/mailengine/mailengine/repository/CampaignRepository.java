package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.Campaign;
import com.mailengine.mailengine.entity.enums.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByAccountId(Long accountId);

    List<Campaign> findByAccountIdAndStatus(Long accountId, CampaignStatus status);
}
