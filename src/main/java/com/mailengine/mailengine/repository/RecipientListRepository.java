package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.RecipientList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipientListRepository extends JpaRepository<RecipientList, Long> {
    List<RecipientList> findByAccountId(Long accountId);

    @Query("SELECT rl FROM RecipientList rl JOIN FETCH rl.account WHERE rl.id = :id")
    Optional<RecipientList> findByIdWithAccount(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(rl) > 0 THEN true ELSE false END " +
            "FROM RecipientList rl WHERE rl.id = :listId AND rl.account.id = :accountId")
    boolean existsByIdAndAccountId(@Param("listId") Long listId, @Param("accountId") Long accountId);

    Optional<RecipientList> findByIdAndAccountId(Long id, Long accountId);

}
