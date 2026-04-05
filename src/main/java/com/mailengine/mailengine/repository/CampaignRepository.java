package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.Campaign;
import com.mailengine.mailengine.entity.enums.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    // Fetch all campaigns for an account (used on the campaigns list page)
    Page<Campaign> findByAccountId(Long accountId, Pageable pageable);

    // Fetch campaigns filtered by status (e.g. ?status=sent)
    Page<Campaign> findByAccountIdAndStatus(Long accountId, CampaignStatus status, Pageable pageable);
}