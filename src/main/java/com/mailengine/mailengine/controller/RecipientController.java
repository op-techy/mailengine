package com.mailengine.mailengine.controller;

import com.mailengine.mailengine.dto.request.CreateRecipientRequest;
import com.mailengine.mailengine.dto.response.RecipientResponse;
import com.mailengine.mailengine.service.RecipientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipient-lists/{listId}/recipients")
@RequiredArgsConstructor
public class RecipientController {

    private final RecipientService recipientService;

    @GetMapping
    public ResponseEntity<Page<RecipientResponse>> getRecipients(
            @PathVariable Long listId,
            Pageable pageable) {
        return ResponseEntity.ok(recipientService.getRecipients(listId, pageable));
    }

    @PostMapping
    public ResponseEntity<RecipientResponse> addRecipient(
            @PathVariable Long listId,
            @Valid @RequestBody CreateRecipientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipientService.addRecipient(listId, request));
    }

    @PatchMapping("/{email}/unsubscribe")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable Long listId,
            @PathVariable String email) {
        recipientService.unsubscribe(email);
        return ResponseEntity.noContent().build();
    }
}
