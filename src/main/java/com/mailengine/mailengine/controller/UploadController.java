package com.mailengine.mailengine.controller;

import com.mailengine.mailengine.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Handles file uploads that are not tied to a specific resource.
 * POST /api/upload/image — used by the Unlayer drag-and-drop editor.
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final TemplateService templateService;

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        String url = templateService.uploadImage(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
