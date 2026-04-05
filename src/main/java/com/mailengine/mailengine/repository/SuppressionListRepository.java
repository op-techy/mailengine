package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.SuppressionList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuppressionListRepository extends JpaRepository<SuppressionList, Long> {
    boolean existsByAccountIdAndEmail(Long accountId, String email);
    Optional<SuppressionList> findByAccountIdAndEmail(Long accountId, String email);
}
