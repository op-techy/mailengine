package com.mailengine.mailengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class RecipientListMemberId implements Serializable {
    private static final long serialVersionUID = 2029472206360061450L;
    @NotNull
    @Column(name = "recipient_list_id", nullable = false)
    private Long recipientListId;

    @NotNull
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;


}