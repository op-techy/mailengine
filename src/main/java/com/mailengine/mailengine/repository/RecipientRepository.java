package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {
    List<Recipient> findByAccountId(Long accountId);
    Optional<Recipient> findByAccountIdAndEmail(Long accountId, String email);
    boolean existsByAccountIdAndEmail(Long accountId, String email);
}
