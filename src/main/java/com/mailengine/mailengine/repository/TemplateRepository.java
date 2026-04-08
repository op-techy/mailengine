package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    Page<Template> findByAccountId(Long accountId, Pageable pageable);

    Page<Template> findByAccountIdAndCategory(Long accountId, String category, Pageable pageable);

    List<Template> findByAccountId(Long accountId);
}
