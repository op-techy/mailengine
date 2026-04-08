package com.mailengine.mailengine.service;

import com.mailengine.mailengine.dto.request.CreateRecipientRequest;
import com.mailengine.mailengine.dto.response.RecipientResponse;
import com.mailengine.mailengine.entity.*;
import com.mailengine.mailengine.entity.enums.RecipientStatus;
import com.mailengine.mailengine.exception.BadRequestException;
import com.mailengine.mailengine.exception.ResourceNotFoundException;
import com.mailengine.mailengine.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final RecipientListRepository recipientListRepository;
    private final RecipientListMemberRepository recipientListMemberRepository;
    private final SuppressionListRepository suppressionListRepository;

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns recipients scoped to the given list.
     * Supports optional free-text search over email, firstName, lastName.
     */
    @Transactional(readOnly = true)
    public Page<RecipientResponse> getRecipients(Long listId, String search, Pageable pageable) {
        // Verify the list exists and belongs to the caller's account
        findListInAccount(listId);

        if (StringUtils.hasText(search)) {
            return recipientRepository
                    .searchByRecipientListId(listId, search, pageable)
                    .map(this::toResponse);
        }
        return recipientRepository.findByRecipientListId(listId, pageable).map(this::toResponse);
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    public RecipientResponse addRecipient(Long listId, CreateRecipientRequest request) {
        User current = getCurrentUser();
        Long accountId = current.getAccount().getId();

        if (suppressionListRepository.existsByAccountIdAndEmail(accountId, request.getEmail())) {
            throw new BadRequestException("This email address is suppressed and cannot be added");
        }

        // Find or create the recipient record
        Recipient recipient = recipientRepository
                .findByAccountIdAndEmail(accountId, request.getEmail())
                .orElseGet(() -> {
                    Recipient r = new Recipient();
                    r.setEmail(request.getEmail());
                    r.setAccount(current.getAccount());
                    r.setStatus(RecipientStatus.active);
                    return r;
                });

        if (recipient.getStatus() == RecipientStatus.unsubscribed ||
                recipient.getStatus() == RecipientStatus.bounced) {
            throw new BadRequestException("Recipient has unsubscribed or bounced and cannot be added");
        }

        recipient.setFirstName(request.getFirstName());
        recipient.setLastName(request.getLastName());
        recipient.setCompany(request.getCompany());
        recipientRepository.save(recipient);

        RecipientList list = findListInAccount(listId);

        if (!recipientListMemberRepository.existsByRecipientListIdAndRecipientId(
                listId, recipient.getId())) {
            RecipientListMemberId memberId = new RecipientListMemberId();
            memberId.setRecipientListId(listId);
            memberId.setRecipientId(recipient.getId());

            RecipientListMember member = new RecipientListMember();
            member.setId(memberId);
            member.setRecipientList(list);
            member.setRecipient(recipient);
            recipientListMemberRepository.save(member);

            list.setRecipientCount(list.getRecipientCount() + 1);
            recipientListRepository.save(list);
        }

        return toResponse(recipient);
    }

    /**
     * Marks a recipient as unsubscribed and adds them to the suppression list
     * so they can never be re-imported. PRD "CRITICAL RULES".
     */
    public void unsubscribe(String email) {
        User current = getCurrentUser();
        Long accountId = current.getAccount().getId();

        recipientRepository.findByAccountIdAndEmail(accountId, email).ifPresent(r -> {
            r.setStatus(RecipientStatus.unsubscribed);
            recipientRepository.save(r);
        });

        // Add to suppression list if not already there
        if (!suppressionListRepository.existsByAccountIdAndEmail(accountId, email)) {
            SuppressionList entry = new SuppressionList();
            entry.setAccount(current.getAccount());
            entry.setEmail(email.toLowerCase());
            entry.setReason("unsubscribed");
            suppressionListRepository.save(entry);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private RecipientList findListInAccount(Long listId) {
        User current = getCurrentUser();
        return recipientListRepository
                .findByIdAndAccountId(listId, current.getAccount().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient list not found"));
    }

    private RecipientResponse toResponse(Recipient r) {
        return new RecipientResponse(
                r.getId(), r.getEmail(), r.getFirstName(),
                r.getLastName(), r.getCompany(), r.getStatus(), r.getCreatedAt());
    }
}
