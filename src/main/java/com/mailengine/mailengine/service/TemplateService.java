package com.mailengine.mailengine.service;

import com.mailengine.mailengine.dto.request.CreateTemplateRequest;
import com.mailengine.mailengine.dto.response.TemplateResponse;
import com.mailengine.mailengine.entity.Template;
import com.mailengine.mailengine.entity.User;
import com.mailengine.mailengine.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateService {

    private final TemplateRepository templateRepository;

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return the {@code User} object representing the currently authenticated user.
     */
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    /**
     * Retrieves a paginated list of templates associated with the currently authenticated user's account.
     *
     * @param pageable the {@code Pageable} object specifying pagination and sorting information
     * @return a {@code Page} of {@code TemplateResponse} objects containing details of the templates
     */
    @Transactional(readOnly = true)
    public Page<TemplateResponse> getTemplates(Pageable pageable) {
        User currentUser = getCurrentUser();
        return templateRepository.findByAccountId(currentUser.getAccount().getId(), pageable)
                .map(this::toResponse);
    }

    /**
     * Retrieves a template by its unique identifier.
     *
     * @param id the unique identifier of the template to be retrieved
     * @return a {@code TemplateResponse} object containing the details of the specified template
     * @throws RuntimeException if no template is found with the given identifier
     */
    @Transactional(readOnly = true)
    public TemplateResponse getTemplateById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Creates a new template based on the provided request data.
     *
     * @param request the {@code CreateTemplateRequest} object containing the details of the template to be created,
     *                including its name, category, and HTML content
     * @return a {@code TemplateResponse} object representing the details of the newly created template
     */
    public TemplateResponse createTemplate(CreateTemplateRequest request) {
        User currentUser = getCurrentUser();
        Template template = new Template();
        template.setName(request.getName());
        template.setCategory(request.getCategory());
        template.setHtmlContent(request.getHtmlContent());
        template.setAccount(currentUser.getAccount());
        template.setCreatedBy(currentUser);
        return toResponse(templateRepository.save(template));
    }

    /**
     * Updates an existing template in the system with the provided details.
     *
     * @param id the unique identifier of the template to be updated
     * @param request the {@code CreateTemplateRequest} object containing the updated details of the template,
     *                including its name, category, and HTML content
     * @return a {@code TemplateResponse} object representing the updated template's details
     * @throws RuntimeException if no template is found with the given identifier
     */
    public TemplateResponse updateTemplate(Long id, CreateTemplateRequest request) {
        Template template = findOrThrow(id);
        template.setName(request.getName());
        template.setCategory(request.getCategory());
        template.setHtmlContent(request.getHtmlContent());
        return toResponse(templateRepository.save(template));
    }

    /**
     * Deletes a template with the specified unique identifier.
     *
     * @param id the unique identifier of the template to be deleted
     * @throws RuntimeException if no template is found with the given identifier
     */
    public void deleteTemplate(Long id) {
        templateRepository.delete(findOrThrow(id));
    }

    /**
     * Retrieves a {@code Template} entity by its unique identifier or throws an exception if it is not found.
     *
     * @param id the unique identifier of the template to be retrieved
     * @return the {@code Template} entity associated with the given identifier
     * @throws RuntimeException if no template is found with the specified identifier
     */
    private Template findOrThrow(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
    }

    /**
     * Converts a {@code Template} entity into a {@code TemplateResponse} DTO.
     *
     * @param template the {@code Template} entity to be converted
     * @return a {@code TemplateResponse} object containing the details of the provided template
     */
    private TemplateResponse toResponse(Template template) {
        return new TemplateResponse(
                template.getId(),
                template.getName(),
                template.getCategory(),
                template.getHtmlContent(),
                template.getThumbnailUrl(),
                template.getCreatedBy() != null ? template.getCreatedBy().getName() : null,
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}