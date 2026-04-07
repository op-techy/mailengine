package com.mailengine.mailengine.controller;

import com.mailengine.mailengine.dto.request.CreateRecipientRequest;
import com.mailengine.mailengine.dto.response.FileUploadResponse;
import com.mailengine.mailengine.dto.response.RecipientResponse;
import com.mailengine.mailengine.entity.FileUpload;
import com.mailengine.mailengine.entity.RecipientList;
import com.mailengine.mailengine.entity.User;
import com.mailengine.mailengine.entity.enums.UploadStatus;
import com.mailengine.mailengine.repository.FileUploadRepository;
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
@RequestMapping("/api/recipient-lists/{listId}/recipients")
@RequiredArgsConstructor
public class RecipientController {

    private final RecipientService recipientService;
    private final FileUploadService fileUploadService;
    private final FileUploadRepository fileUploadRepository;

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

    @PostMapping("/import")
    public ResponseEntity<FileUploadResponse> importFile(
            @PathVariable Long listId,
            @RequestParam("file") MultipartFile file) {

        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        FileUpload upload = new FileUpload();
        upload.setFileName(file.getOriginalFilename());
        upload.setStatus(UploadStatus.processing);
        upload.setAccount(currentUser.getAccount());
        RecipientList list = new RecipientList();
        list.setId(listId);
        upload.setRecipientList(list);
        fileUploadRepository.save(upload);

        fileUploadService.processImport(upload.getId(), file, listId,
                currentUser.getAccount().getId());

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new FileUploadResponse(upload.getId(), upload.getStatus()));
    }

    @GetMapping("/import/{uploadId}")
    public ResponseEntity<FileUploadResponse> getImportStatus(
            @PathVariable Long listId,
            @PathVariable Long uploadId) {
        FileUpload upload = fileUploadRepository.findById(uploadId)
                .orElseThrow(() -> new RuntimeException("Upload not found"));
        return ResponseEntity.ok(new FileUploadResponse(
                upload.getId(),
                upload.getStatus(),
                upload.getTotalRows(),
                upload.getImportedRows(),
                upload.getSkippedRows(),
                upload.getDuplicateRows()
        ));
    }
}
