package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.RecipientList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipientListRepository extends JpaRepository<RecipientList, Long> {
    List<RecipientList> findByAccountId(Long accountId);
}
