package com.mailengine.mailengine.entity;

import com.mailengine.mailengine.entity.enums.RecipientStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
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
@Table(name = "recipients")
public class Recipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 255)
    @Column(name = "first_name")
    private String firstName;

    @Size(max = 255)
    @Column(name = "last_name")
    private String lastName;

    @Size(max = 255)
    @Column(name = "company")
    private String company;

    @ColumnDefault("'{}'")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_fields")
    private Map<String, Object> customFields;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private RecipientStatus status = RecipientStatus.active;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;


}