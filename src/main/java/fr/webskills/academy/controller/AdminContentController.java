package fr.webskills.academy.controller;

import fr.webskills.academy.dto.LearningDtos.*;
import fr.webskills.academy.service.ContentService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminContentController {
    private final ContentService content;

    public AdminContentController(ContentService content) {
        this.content = content;
    }

    @GetMapping({"/domains", "/learning-domains"})
    List<LearningDomainResponse> domains() {
        return content.adminDomains();
    }

    @GetMapping({"/domains/{id}", "/learning-domains/{id}"})
    LearningDomainResponse domain(@PathVariable UUID id) {
        return content.getAdminDomain(id);
    }

    @PostMapping({"/domains", "/learning-domains"})
    @ResponseStatus(HttpStatus.CREATED)
    LearningDomainResponse createDomain(@RequestBody @Valid LearningDomainRequest r) {
        return content.createDomain(r);
    }

    @PutMapping({"/domains/{id}", "/learning-domains/{id}"})
    LearningDomainResponse updateDomain(
            @PathVariable UUID id, @RequestBody @Valid LearningDomainRequest r) {
        return content.updateDomain(id, r);
    }

    @PatchMapping({"/domains/{id}/status", "/learning-domains/{id}/status"})
    LearningDomainResponse updateDomainStatus(
            @PathVariable UUID id, @RequestBody @Valid StatusRequest r) {
        return content.updateDomainStatus(id, r.status());
    }

    @DeleteMapping({"/domains/{id}", "/learning-domains/{id}"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteDomain(@PathVariable UUID id) {
        content.archiveDomain(id);
    }

    @GetMapping({"/domains/{domainId}/sections", "/learning-domains/{domainId}/sections"})
    List<LearningSectionResponse> sections(@PathVariable UUID domainId) {
        return content.sectionsForDomain(domainId, true);
    }

    @PostMapping({"/domains/{domainId}/sections", "/learning-domains/{domainId}/sections"})
    @ResponseStatus(HttpStatus.CREATED)
    LearningSectionResponse createSection(
            @PathVariable UUID domainId, @RequestBody @Valid LearningSectionRequest r) {
        return content.createSection(domainId, r);
    }

    @PutMapping("/sections/{id}")
    LearningSectionResponse updateSection(
            @PathVariable UUID id, @RequestBody @Valid LearningSectionRequest r) {
        return content.updateSection(id, r);
    }

    @PatchMapping("/sections/{id}/status")
    LearningSectionResponse updateSectionStatus(
            @PathVariable UUID id, @RequestBody @Valid StatusRequest r) {
        return content.updateSectionStatus(id, r.status());
    }

    @DeleteMapping("/sections/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteSection(@PathVariable UUID id) {
        content.archiveSection(id);
    }

    @PutMapping("/sections/reorder")
    void reorderSections(@RequestBody List<ReorderItem> items) {
        content.reorderSections(items);
    }

    @PostMapping("/sections/{sectionId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    LessonResponse createLesson(@PathVariable UUID sectionId, @RequestBody @Valid LessonRequest r) {
        return content.createLesson(sectionId, r);
    }

    @GetMapping("/lessons")
    List<LessonSummaryResponse> lessons() {
        return content.adminLessons();
    }

    @GetMapping("/lessons/{id}")
    LessonResponse lesson(@PathVariable UUID id) {
        return content.getAdminLesson(id);
    }

    @PutMapping("/lessons/{id}")
    LessonResponse updateLesson(@PathVariable UUID id, @RequestBody @Valid UpdateLessonRequest r) {
        return content.updateLesson(id, r);
    }

    @DeleteMapping("/lessons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteLesson(@PathVariable UUID id) {
        content.archiveLesson(id);
    }

    @PutMapping("/lessons/reorder")
    void reorderLessons(@RequestBody List<ReorderItem> items) {
        content.reorderLessons(items);
    }
}
