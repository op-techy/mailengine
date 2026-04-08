package com.mailengine.mailengine.controller;

import com.mailengine.mailengine.dto.request.CreateTemplateRequest;
import com.mailengine.mailengine.dto.response.TemplateResponse;
import com.mailengine.mailengine.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<Page<TemplateResponse>> getTemplates(
            @RequestParam(required = false) String category,
            Pageable pageable) {
        return ResponseEntity.ok(templateService.getTemplates(category, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(
            @Valid @RequestBody CreateTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.createTemplate(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody CreateTemplateRequest request) {
        return ResponseEntity.ok(templateService.updateTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    /** PRD Part 9 §3 — duplicate a template into a new draft. */
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<TemplateResponse> duplicateTemplate(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.duplicateTemplate(id));
    }

    /**
     * PRD Flow 2 — upload an .html file, receive parsed HTML back for preview.
     * Frontend then calls POST /api/templates to save.
     */
    @PostMapping("/upload-html")
    public ResponseEntity<Map<String, Object>> uploadHtml(
            @RequestParam("file") MultipartFile file) {
        String html = templateService.parseUploadedHtml(file);
        return ResponseEntity.ok(Map.of("htmlContent", html, "preview", true));
    }
}

// ─── Separate controller for image uploads (different path prefix) ───────────

// In a new file: UploadController.java (see output)
