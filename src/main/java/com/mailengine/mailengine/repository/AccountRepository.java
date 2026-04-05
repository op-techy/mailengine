package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
