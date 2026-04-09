package com.mailengine.mailengine.controller;

import com.mailengine.mailengine.dto.request.CreateRecipientListRequest;
import com.mailengine.mailengine.dto.response.RecipientListResponse;
import com.mailengine.mailengine.service.RecipientListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipient-lists")
@RequiredArgsConstructor
public class RecipientListController {

    private final RecipientListService recipientListService;

    @GetMapping
    public ResponseEntity<List<RecipientListResponse>> getLists() {
        return ResponseEntity.ok(recipientListService.getLists());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createList(
            @Valid @RequestBody CreateRecipientListRequest request) {
        RecipientListResponse list = recipientListService.createList(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id",   list.getId(),
                "name", list.getName()
        ));
    }

    // DELETE /api/recipient-lists/{id}
    // Response: 204
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteList(@PathVariable Long id) {
        recipientListService.deleteList(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Exports the recipient list as a CSV file and returns it as a byte array
     * in the response. The resulting file contains details of each recipient
     * in the specified list.
     *
     * @param id the ID of the recipient list to be exported
     * @return a {@code ResponseEntity} containing the CSV file as a byte array,
     *         with appropriate headers indicating the file name and content type
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportCsv(@PathVariable Long id) {
        byte[] csv = recipientListService.exportAsCsv(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"recipients-" + id + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
