package fr.webskills.academy.controller;

import fr.webskills.academy.dto.LearningDtos.*;
import fr.webskills.academy.service.ContentService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminContentController {
    private final ContentService content;

    public AdminContentController(ContentService content) {
        this.content = content;
    }

    @PostMapping("/domains")
    @ResponseStatus(HttpStatus.CREATED)
    LearningDomainResponse createDomain(@RequestBody @Valid LearningDomainRequest r) {
        return content.createDomain(r);
    }

    @PutMapping("/domains/{id}")
    LearningDomainResponse updateDomain(
            @PathVariable UUID id, @RequestBody @Valid LearningDomainRequest r) {
        return content.updateDomain(id, r);
    }

    @DeleteMapping("/domains/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteDomain(@PathVariable UUID id) {
        content.archiveDomain(id);
    }

    @PostMapping("/domains/{domainId}/sections")
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

    @PutMapping("/lessons/{id}")
    LessonResponse updateLesson(@PathVariable UUID id, @RequestBody @Valid LessonRequest r) {
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
