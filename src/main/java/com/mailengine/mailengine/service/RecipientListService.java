package com.mailengine.mailengine.service;

import com.mailengine.mailengine.dto.request.CreateRecipientListRequest;
import com.mailengine.mailengine.dto.response.RecipientListResponse;
import com.mailengine.mailengine.entity.RecipientList;
import com.mailengine.mailengine.entity.User;
import com.mailengine.mailengine.repository.RecipientListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipientListService {

    private final RecipientListRepository recipientListRepository;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    @Transactional(readOnly = true)
    public List<RecipientListResponse> getLists() {
        User currentUser = getCurrentUser();
        return recipientListRepository.findByAccountId(currentUser.getAccount().getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecipientListResponse getListById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public RecipientListResponse createList(CreateRecipientListRequest request) {
        User currentUser = getCurrentUser();
        RecipientList list = new RecipientList();
        list.setName(request.getName());
        list.setDescription(request.getDescription());
        list.setAccount(currentUser.getAccount());
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

    private RecipientList findOrThrow(Long id) {
        return recipientListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipient list not found"));
    }

    private RecipientListResponse toResponse(RecipientList list) {
        return new RecipientListResponse(
                list.getId(),
                list.getName(),
                list.getDescription(),
                list.getRecipientCount(),
                list.getCreatedAt()
        );
    }
}
