package com.mailengine.mailengine.service;

import com.mailengine.mailengine.dto.request.CreateRecipientListRequest;
import com.mailengine.mailengine.dto.response.RecipientListResponse;
import com.mailengine.mailengine.entity.Recipient;
import com.mailengine.mailengine.entity.RecipientList;
import com.mailengine.mailengine.entity.User;
import com.mailengine.mailengine.exception.ResourceNotFoundException;
import com.mailengine.mailengine.repository.RecipientListRepository;
import com.mailengine.mailengine.repository.RecipientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipientListService {

    private final RecipientListRepository recipientListRepository;
    private final RecipientRepository recipientRepository;

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RecipientListResponse> getLists() {
        return recipientListRepository.findByAccountId(getCurrentUser().getAccount().getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RecipientListResponse getListById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    public RecipientListResponse createList(CreateRecipientListRequest request) {
        RecipientList list = new RecipientList();
        list.setName(request.getName());
        list.setDescription(request.getDescription());
        list.setAccount(getCurrentUser().getAccount());
        list.setRecipientCount(0);
        return toResponse(recipientListRepository.save(list));
    }

    public RecipientListResponse updateList(Long id, CreateRecipientListRequest request) {
        RecipientList list = findOrThrow(id);
        list.setName(request.getName());
        list.setDescription(request.getDescription());
        return toResponse(recipientListRepository.save(list));
    }

    public void deleteList(Long id) {
        recipientListRepository.delete(findOrThrow(id));
    }

    /**
     * Exports all recipients in the list as a UTF-8 CSV byte array.
     * PRD Part 9 §5.
     */
    @Transactional(readOnly = true)
    public byte[] exportAsCsv(Long id) {
        findOrThrow(id);   // access check

        List<Recipient> recipients = recipientRepository
                .findByRecipientListId(id, Pageable.unpaged())
                .getContent();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(bos, StandardCharsets.UTF_8))) {

            pw.println("email,first_name,last_name,company,status");
            for (Recipient r : recipients) {
                pw.printf("%s,%s,%s,%s,%s%n",
                        csvEscape(r.getEmail()),
                        csvEscape(r.getFirstName()),
                        csvEscape(r.getLastName()),
                        csvEscape(r.getCompany()),
                        r.getStatus().name());
            }
        }
        return bos.toByteArray();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private RecipientList findOrThrow(Long id) {
        User current = getCurrentUser();
        return recipientListRepository
                .findByIdAndAccountId(id, current.getAccount().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient list not found"));
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private RecipientListResponse toResponse(RecipientList list) {
        return new RecipientListResponse(
                list.getId(), list.getName(), list.getDescription(),
                list.getRecipientCount(), list.getCreatedAt());
    }

    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
