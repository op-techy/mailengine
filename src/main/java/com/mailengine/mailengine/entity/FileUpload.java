package com.mailengine.mailengine.entity;

import com.mailengine.mailengine.entity.enums.UploadStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "file_uploads")
public class FileUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_list_id")
    private RecipientList recipientList;

    @Size(max = 255)
    @Column(name = "file_name")
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private UploadStatus status = UploadStatus.processing;

    @Column(name = "total_rows")
    private Integer totalRows;

    @Column(name = "imported_rows")
    private Integer importedRows;

    @Column(name = "skipped_rows")
    private Integer skippedRows;

    @Column(name = "duplicate_rows")
    private Integer duplicateRows;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "column_preview")
    private Map<String, Object> columnPreview;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "column_mapping")
    private Map<String, Object> columnMapping;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;


}