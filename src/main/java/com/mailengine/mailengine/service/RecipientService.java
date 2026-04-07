package com.mailengine.mailengine.service;

import com.mailengine.mailengine.dto.request.CreateRecipientRequest;
import com.mailengine.mailengine.dto.response.RecipientResponse;
import com.mailengine.mailengine.entity.*;
import com.mailengine.mailengine.entity.enums.RecipientStatus;
import com.mailengine.mailengine.repository.RecipientListMemberRepository;
import com.mailengine.mailengine.repository.RecipientListRepository;
import com.mailengine.mailengine.repository.RecipientRepository;
import com.mailengine.mailengine.repository.SuppressionListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final RecipientListRepository recipientListRepository;
    private final RecipientListMemberRepository recipientListMemberRepository;
    private final SuppressionListRepository suppressionListRepository;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    @Transactional(readOnly = true)
    public Page<RecipientResponse> getRecipients(Long listId, Pageable pageable) {
        User currentUser = getCurrentUser();
        // if listId provided, get recipients for that list
        // otherwise get all recipients for the account
        return recipientRepository.findByAccountId(currentUser.getAccount().getId(),
                        pageable)
                .map(this::toResponse);
    }

    public RecipientResponse addRecipient(Long listId, CreateRecipientRequest request) {
        User currentUser = getCurrentUser();
        Long accountId = currentUser.getAccount().getId();

        // check suppression list first
        if (suppressionListRepository.existsByAccountIdAndEmail(accountId, request.getEmail())) {
            throw new RuntimeException("Email is suppressed");
        }

        // find or create recipient
        Recipient recipient = recipientRepository
                .findByAccountIdAndEmail(accountId, request.getEmail())
                .orElseGet(() -> {
                    Recipient r = new Recipient();
                    r.setEmail(request.getEmail());
                    r.setAccount(currentUser.getAccount());
                    r.setStatus(RecipientStatus.active);
                    return r;
                });

        recipient.setFirstName(request.getFirstName());
        recipient.setLastName(request.getLastName());
        recipient.setCompany(request.getCompany());
        recipientRepository.save(recipient);

        // add to list if not already a member
        RecipientList list = recipientListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));

        if (!recipientListMemberRepository.existsByRecipientListIdAndRecipientId(
                listId, recipient.getId())) {
            RecipientListMember member = new RecipientListMember();
            RecipientListMemberId memberId = new RecipientListMemberId();
            memberId.setRecipientListId(listId);
            memberId.setRecipientId(recipient.getId());
            member.setId(memberId);
            member.setRecipientList(list);
            member.setRecipient(recipient);
            recipientListMemberRepository.save(member);

            // increment count
            list.setRecipientCount(list.getRecipientCount() + 1);
            recipientListRepository.save(list);
        }

        return toResponse(recipient);
    }

    public void unsubscribe(String email) {
        User currentUser = getCurrentUser();
        recipientRepository.findByAccountIdAndEmail(currentUser.getAccount().getId(), email)
                .ifPresent(r -> {
                    r.setStatus(RecipientStatus.unsubscribed);
                    recipientRepository.save(r);
                });
    }

    private RecipientResponse toResponse(Recipient recipient) {
        return new RecipientResponse(
                recipient.getId(),
                recipient.getEmail(),
                recipient.getFirstName(),
                recipient.getLastName(),
                recipient.getCompany(),
                recipient.getStatus(),
                recipient.getCreatedAt()
        );
    }
}
