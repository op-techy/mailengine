package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.EmailQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface EmailQueueRepository extends JpaRepository<EmailQueue, Long> {

    List<EmailQueue> findByCampaignId(Long campaignId);

    @Query("""
        SELECT e FROM EmailQueue e
        WHERE e.status = 'pending'
        AND (e.lockedUntil IS NULL OR e.lockedUntil < :now)
        ORDER BY e.createdAt ASC
        LIMIT :limit
    """)
    List<EmailQueue> findPendingEmails(Instant now, int limit);
}
