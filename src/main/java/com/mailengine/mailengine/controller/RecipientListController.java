package com.mailengine.mailengine.controller;

import com.mailengine.mailengine.dto.request.CreateRecipientListRequest;
import com.mailengine.mailengine.dto.response.RecipientListResponse;
import com.mailengine.mailengine.service.RecipientListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipient-lists")
@RequiredArgsConstructor
public class RecipientListController {

    private final RecipientListService recipientListService;

    @GetMapping
    public ResponseEntity<List<RecipientListResponse>> getLists() {
        return ResponseEntity.ok(recipientListService.getLists());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipientListResponse> getListById(@PathVariable Long id) {
        return ResponseEntity.ok(recipientListService.getListById(id));
    }

    @PostMapping
    public ResponseEntity<RecipientListResponse> createList(
            @Valid @RequestBody CreateRecipientListRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipientListService.createList(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipientListResponse> updateList(
            @PathVariable Long id,
            @Valid @RequestBody CreateRecipientListRequest request) {
        return ResponseEntity.ok(recipientListService.updateList(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteList(@PathVariable Long id) {
        recipientListService.deleteList(id);
        return ResponseEntity.noContent().build();
    }
}
