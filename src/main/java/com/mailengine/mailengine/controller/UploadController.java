package com.mailengine.mailengine.controller;

import com.mailengine.mailengine.dto.response.UploadStatusResponse;
import com.mailengine.mailengine.entity.FileUpload;
import com.mailengine.mailengine.exception.BadRequestException;
import com.mailengine.mailengine.exception.ResourceNotFoundException;
import com.mailengine.mailengine.repository.FileUploadRepository;
import com.mailengine.mailengine.service.FileUploadService;
import com.mailengine.mailengine.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles file uploads that are not tied to a specific resource.
 * POST /api/upload/image — used by the Unlayer drag-and-drop editor.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private final TemplateService templateService;
    private final FileUploadService fileUploadService;
    private final FileUploadRepository fileUploadRepository;

    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        String url = templateService.uploadImage(file);
        return ResponseEntity.ok(Map.of("url", url));
    }


    /**
     * Retrieves the upload status and additional metadata for a given upload.
     *
     * @param uploadId The unique identifier of the file upload whose status is to be retrieved.
     * @return A {@code ResponseEntity} containing an {@code UploadStatusResponse} object,
     *         which includes the upload status, column preview, column names,
     *         as well as statistics such as total rows, imported rows, skipped rows,
     *         and duplicate rows.
     * @throws ResourceNotFoundException If no upload is found with the given {@code uploadId}.
     */
    @GetMapping("/uploads/{uploadId}/status")
    public ResponseEntity<UploadStatusResponse> getUploadStatus(@PathVariable Long uploadId) {
        FileUpload upload = fileUploadRepository.findById(uploadId)
                .orElseThrow(() -> new ResourceNotFoundException("Upload not found"));

        List<String> columns = new ArrayList<>();
        List<Map<String, String>> preview = new ArrayList<>();

        if (upload.getColumnPreview() != null) {
            upload.getColumnPreview().forEach((col, val) -> columns.add(col));
            // columnPreview stores the header→value map for the first rows;
            // we expose it as a single-row preview for the mapping UI
            Map<String, String> row = new LinkedHashMap<>();
            upload.getColumnPreview().forEach((col, val) ->
                    row.put(col, val != null ? val.toString() : ""));
            if (!row.isEmpty()) preview.add(row);
        }

        return ResponseEntity.ok(new UploadStatusResponse(
                upload.getStatus(),
                preview,
                columns,
                upload.getTotalRows(),
                upload.getImportedRows(),
                upload.getSkippedRows(),
                upload.getDuplicateRows()
        ));
    }

    /**
     * Applies column mapping to the uploaded file identified by the given upload ID.
     * The column mapping specifies how columns in the uploaded file should be interpreted.
     *
     * @param uploadId the unique identifier of the upload to which the column mapping should be applied
     * @param body a map containing the column mapping details, where the key "mapping" maps
     *             to another map describing the column mapping rules
     * @return a ResponseEntity containing a map with the outcome of the mapping application process.
     *         The response map includes the following keys:
     *         - "imported": the number of successfully imported rows
     *         - "skipped": the number of skipped rows
     *         - "duplicates": the number of duplicate rows
     * @throws BadRequestException if the "mapping" key in the request body is null or empty
     */
    @PostMapping("/uploads/{uploadId}/map")
    public ResponseEntity<Map<String, Object>> applyColumnMapping(
            @PathVariable Long uploadId,
            @RequestBody Map<String, Map<String, String>> body) {

        Map<String, String> mapping = body.get("mapping");
        if (mapping == null || mapping.isEmpty()) {
            throw new BadRequestException("mapping is required");
        }

        FileUploadService.ImportResult result = fileUploadService.applyMapping(uploadId, mapping);

        return ResponseEntity.ok(Map.of(
                "imported", result.imported() != null ? result.imported() : 0,
                "skipped",  result.skipped()  != null ? result.skipped()  : 0,
                "duplicates", result.duplicates() != null ? result.duplicates() : 0
        ));
    }

}
