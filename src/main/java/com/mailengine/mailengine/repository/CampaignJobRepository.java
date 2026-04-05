package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.CampaignJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CampaignJobRepository extends JpaRepository<CampaignJob, Long> {
    Optional<CampaignJob> findByCampaignId(Long campaignId);
}
