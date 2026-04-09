package com.mailengine.mailengine.controller;


import com.mailengine.mailengine.dto.response.RecipientResponse;
import com.mailengine.mailengine.entity.FileUpload;
import com.mailengine.mailengine.entity.RecipientList;
import com.mailengine.mailengine.entity.User;
import com.mailengine.mailengine.entity.enums.UploadStatus;
import com.mailengine.mailengine.exception.BadRequestException;
import com.mailengine.mailengine.exception.ResourceNotFoundException;
import com.mailengine.mailengine.repository.FileUploadRepository;
import com.mailengine.mailengine.repository.RecipientListRepository;
import com.mailengine.mailengine.service.FileUploadService;
import com.mailengine.mailengine.service.RecipientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.Map;

@RestController
@RequestMapping("/api/recipient-lists")
@RequiredArgsConstructor
public class RecipientController {

    private final RecipientService recipientService;
    private final FileUploadService fileUploadService;
    private final FileUploadRepository fileUploadRepository;
    private final RecipientListRepository recipientListRepository;

    /**
     * Retrieves a paginated list of recipients associated with a specific recipient list.
     *
     * @param listId the ID of the recipient list for which the recipients are to be retrieved
     * @param search an optional search term to filter the recipients by their attributes
     * @param pageable pagination information for the recipient list
     * @return a ResponseEntity containing a paginated list of RecipientResponse objects
     */
    @GetMapping("/{listId}/recipients")
    public ResponseEntity<Page<RecipientResponse>> getRecipients(
            @PathVariable Long listId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(recipientService.getRecipients(listId, search, pageable));
    }

    /**
     * Handles the upload of a file for importing recipients into the specified recipient list.
     *
     * @param listId the ID of the recipient list to which the uploaded file corresponds
     * @param file the file to be uploaded, which contains recipient data
     * @return a ResponseEntity containing a FileUploadResponse object with upload details;
     *         returns HTTP 413 (PAYLOAD_TOO_LARGE) if the file exceeds 10 MB
     */
    @PostMapping("/{listId}/upload")
    public ResponseEntity<Map<String,Object>> uploadFile(
            @PathVariable Long listId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }
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

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "uploadId", upload.getId(),
                "message",  "Processing..."
                ));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
