package com.mailengine.mailengine.repository;

import com.mailengine.mailengine.entity.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {
    List<FileUpload> findByAccountId(Long accountId);
    List<FileUpload> findByRecipientListId(Long recipientListId);
}
