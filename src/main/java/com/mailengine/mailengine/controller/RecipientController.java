package com.mailengine.mailengine.controller;

import com.mailengine.mailengine.dto.request.CreateRecipientRequest;
import com.mailengine.mailengine.dto.response.FileUploadResponse;
import com.mailengine.mailengine.dto.response.RecipientResponse;
import com.mailengine.mailengine.entity.FileUpload;
import com.mailengine.mailengine.entity.RecipientList;
import com.mailengine.mailengine.entity.User;
import com.mailengine.mailengine.entity.enums.UploadStatus;
import com.mailengine.mailengine.exception.ResourceNotFoundException;
import com.mailengine.mailengine.repository.FileUploadRepository;
import com.mailengine.mailengine.repository.RecipientListRepository;
import com.mailengine.mailengine.service.FileUploadService;
import com.mailengine.mailengine.service.RecipientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class RecipientController {

    private final RecipientService recipientService;
    private final FileUploadService fileUploadService;
    private final FileUploadRepository fileUploadRepository;
    private final RecipientListRepository recipientListRepository;

    // ── Recipients in a list ──────────────────────────────────────────────────

    @GetMapping("/api/recipient-lists/{listId}/recipients")
    public ResponseEntity<Page<RecipientResponse>> getRecipients(
            @PathVariable Long listId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(recipientService.getRecipients(listId, search, pageable));
    }

    @PostMapping("/api/recipient-lists/{listId}/recipients")
    public ResponseEntity<RecipientResponse> addRecipient(
            @PathVariable Long listId,
            @Valid @RequestBody CreateRecipientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipientService.addRecipient(listId, request));
    }

    @PatchMapping("/api/recipient-lists/{listId}/recipients/{email}/unsubscribe")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable Long listId,
            @PathVariable String email) {
        recipientService.unsubscribe(email);
        return ResponseEntity.noContent().build();
    }

    // ── CSV / Excel import ────────────────────────────────────────────────────

    /**
     * Kick off an async import. Returns 202 with an uploadId to poll.
     * PRD: POST /api/recipient-lists/{id}/upload
     */
    @PostMapping("/api/recipient-lists/{listId}/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @PathVariable Long listId,
            @RequestParam("file") MultipartFile file) {

        if (file.getSize() > 10 * 1024 * 1024) {  // 10 MB limit per PRD Part 9 §7
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        }

        User current = currentUser();
        RecipientList list = recipientListRepository
                .findByIdAndAccountId(listId, current.getAccount().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient list not found"));

        FileUpload upload = new FileUpload();
        upload.setFileName(file.getOriginalFilename());
        upload.setStatus(UploadStatus.processing);
        upload.setAccount(current.getAccount());
        upload.setRecipientList(list);
        fileUploadRepository.save(upload);

        fileUploadService.processImport(upload.getId(), file, listId, current.getAccount().getId());

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new FileUploadResponse(upload.getId(), upload.getStatus()));
    }

    /**
     * Poll upload progress.
     * PRD: GET /api/uploads/{uploadId}/status
     */
    @GetMapping("/api/uploads/{uploadId}/status")
    public ResponseEntity<FileUploadResponse> getUploadStatus(@PathVariable Long uploadId) {
        FileUpload upload = fileUploadRepository.findById(uploadId)
                .orElseThrow(() -> new ResourceNotFoundException("Upload not found"));
        return ResponseEntity.ok(new FileUploadResponse(
                upload.getId(),
                upload.getStatus(),
                upload.getTotalRows(),
                upload.getImportedRows(),
                upload.getSkippedRows(),
                upload.getDuplicateRows()
        ));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
