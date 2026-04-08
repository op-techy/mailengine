package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.Recipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    Page<Recipient> findByAccountId(Long accountId, Pageable pageable);

    Optional<Recipient> findByAccountIdAndEmail(Long accountId, String email);

    boolean existsByAccountIdAndEmail(Long accountId, String email);

    /** Returns recipients that belong to the given list (via the join table). */
    @Query("SELECT r FROM Recipient r " +
           "JOIN RecipientListMember m ON m.recipient.id = r.id " +
           "WHERE m.recipientList.id = :listId")
    Page<Recipient> findByRecipientListId(@Param("listId") Long listId, Pageable pageable);

    /** Search within a list by email or name. */
    @Query("SELECT r FROM Recipient r " +
           "JOIN RecipientListMember m ON m.recipient.id = r.id " +
           "WHERE m.recipientList.id = :listId " +
           "AND (LOWER(r.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "  OR LOWER(r.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "  OR LOWER(r.lastName)  LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Recipient> searchByRecipientListId(
            @Param("listId") Long listId,
            @Param("search") String search,
            Pageable pageable);
}
