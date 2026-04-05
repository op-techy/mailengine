package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    List<TrackingEvent> findByCampaignId(Long campaignId);
    long countByCampaignIdAndEventType(Long campaignId, String eventType);
}
