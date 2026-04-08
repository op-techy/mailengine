package com.mailengine.mailengine.service;

import com.mailengine.mailengine.dto.request.CreateTemplateRequest;
import com.mailengine.mailengine.dto.response.TemplateResponse;
import com.mailengine.mailengine.entity.Template;
import com.mailengine.mailengine.entity.User;
import com.mailengine.mailengine.exception.ResourceNotFoundException;
import com.mailengine.mailengine.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final S3Service s3Service;

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TemplateResponse> getTemplates(String category, Pageable pageable) {
        Long accountId = getCurrentUser().getAccount().getId();
        if (StringUtils.hasText(category)) {
            return templateRepository.findByAccountIdAndCategory(accountId, category, pageable)
                    .map(this::toResponse);
        }
        return templateRepository.findByAccountId(accountId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TemplateResponse getTemplateById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    public TemplateResponse createTemplate(CreateTemplateRequest request) {
        User current = getCurrentUser();
        Template t = new Template();
        t.setName(request.getName());
        t.setCategory(request.getCategory());
        t.setHtmlContent(request.getHtmlContent());
        t.setJsonDesign(request.getJsonDesign());
        t.setAccount(current.getAccount());
        t.setCreatedBy(current);
        return toResponse(templateRepository.save(t));
    }

    public TemplateResponse updateTemplate(Long id, CreateTemplateRequest request) {
        Template t = findOrThrow(id);
        t.setName(request.getName());
        t.setCategory(request.getCategory());
        t.setHtmlContent(request.getHtmlContent());
        t.setJsonDesign(request.getJsonDesign());
        return toResponse(templateRepository.save(t));
    }

    public void deleteTemplate(Long id) {
        templateRepository.delete(findOrThrow(id));
    }

    /**
     * Duplicates an existing template — creates a new draft with "Copy of" prefix.
     * PRD Part 9 §3.
     */
    public TemplateResponse duplicateTemplate(Long id) {
        Template source = findOrThrow(id);
        User current = getCurrentUser();

        Template copy = new Template();
        copy.setName("Copy of " + source.getName());
        copy.setCategory(source.getCategory());
        copy.setHtmlContent(source.getHtmlContent());
        copy.setJsonDesign(source.getJsonDesign());
        copy.setAccount(current.getAccount());
        copy.setCreatedBy(current);

        return toResponse(templateRepository.save(copy));
    }

    /**
     * Parses an uploaded .html file and returns its content for preview.
     * The caller then POSTs to /api/templates to save.
     * PRD Flow 2 — "Upload HTML File" mode.
     */
    @Transactional(readOnly = true)
    public String parseUploadedHtml(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".html")) {
            throw new IllegalArgumentException("Only .html files are accepted");
        }
        if (file.getSize() > 500 * 1024) {  // 500 KB limit per PRD Part 9 §7
            throw new IllegalArgumentException("HTML template must be under 500 KB");
        }
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded HTML file", e);
        }
    }

    /**
     * Uploads an image to S3 and returns its public URL.
     * Used by the drag-and-drop editor.
     */
    public String uploadImage(MultipartFile file) {
        if (file.getSize() > 2 * 1024 * 1024) {  // 2 MB limit per PRD Part 9 §7
            throw new IllegalArgumentException("Image must be under 2 MB");
        }
        return s3Service.uploadImage(file);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Template findOrThrow(Long id) {
        User current = getCurrentUser();
        Template t = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        // Ensure the template belongs to the current account
        if (!t.getAccount().getId().equals(current.getAccount().getId())) {
            throw new ResourceNotFoundException("Template not found");
        }
        return t;
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private TemplateResponse toResponse(Template t) {
        return new TemplateResponse(
                t.getId(),
                t.getName(),
                t.getCategory(),
                t.getHtmlContent(),
                t.getJsonDesign(),
                t.getThumbnailUrl(),
                t.getCreatedBy() != null ? t.getCreatedBy().getName() : null,
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
