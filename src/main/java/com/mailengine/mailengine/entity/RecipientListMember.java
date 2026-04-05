package com.mailengine.mailengine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "recipient_list_members")
public class RecipientListMember {
    @EmbeddedId
    private RecipientListMemberId id;

    @MapsId("recipientListId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "recipient_list_id", nullable = false)
    private RecipientList recipientList;

    @MapsId("recipientId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;


}