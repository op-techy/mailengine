package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.RecipientListMember;
import com.mailengine.mailengine.entity.RecipientListMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipientListMemberRepository extends JpaRepository<RecipientListMember, RecipientListMemberId> {

    List<RecipientListMember> findByRecipientListId(Long recipientListId);

    boolean existsByRecipientListIdAndRecipientId(Long recipientListId, Long recipientId);
}
